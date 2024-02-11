package io.xrystalll.playdesu

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.widget.TextClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import io.xrystalll.playdesu.data.GameModel
import io.xrystalll.playdesu.ui.screens.GameDetails
import io.xrystalll.playdesu.ui.screens.Home
import io.xrystalll.playdesu.ui.theme.MainTheme
import io.xrystalll.playdesu.ui.theme.Typography
import io.xrystalll.playdesu.ui.theme.bodyColor
import io.xrystalll.playdesu.ui.theme.mainBackground
import io.xrystalll.playdesu.ui.theme.mainTitleColor
import io.xrystalll.playdesu.ui.theme.transparent
import io.xrystalll.playdesu.util.ApiHelper
import io.xrystalll.playdesu.util.SystemUIHelper

const val TAG = "MyLog"
const val allGamesUrl = "https://raw.githubusercontent.com/xrystalll/playdesu/master/store/games_db.json"

val appBarPadding = 56.dp
val pageStartPadding = 86.dp
val pageEndPadding = 86.dp
val cardSpacing = 28.dp
val cardWidth = 210.dp
val cardHeight = 118.dp
val immersiveHeadHeight = 420.dp


class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        SystemUIHelper(window).hideStatusBar()

        setContent {
            MainTheme {
                val navController = rememberNavController()
                var showTopBar by rememberSaveable { mutableStateOf(true) }
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                var canPop by remember { mutableStateOf(false) }

                DisposableEffect(navController) {
                    val listener = NavController.OnDestinationChangedListener { controller, _, _ ->
                        canPop = controller.previousBackStackEntry != null
                    }
                    navController.addOnDestinationChangedListener(listener)
                    onDispose {
                        navController.removeOnDestinationChangedListener(listener)
                    }
                }

                showTopBar = when (navBackStackEntry?.destination?.route) {
                    "Game" -> false
                    else -> true
                }

                Scaffold(
                    topBar = {
                        if (showTopBar) {
                            TopAppBar(
                                modifier = Modifier.padding(start = appBarPadding),
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = transparent
                                ),
                                title = {
                                    Text(
                                        text = stringResource(id = R.string.app_name),
                                        style = Typography.displayLarge.copy(color = mainTitleColor)
                                    )
                                },
                                navigationIcon = {
                                    if (canPop) {
                                        IconButton(
                                            modifier = Modifier.padding(top = 6.dp),
                                            onClick = { navController.navigateUp() }
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                                                contentDescription = null,
                                                tint = mainTitleColor
                                            )
                                        }
                                    }
                                },
                                actions = {
                                    AndroidView(
                                        factory = { context ->
                                            TextClock(context).apply {
                                                format12Hour?.let { this.format12Hour = "hh:mm" }
                                                timeZone?.let { this.timeZone = it }
                                                textSize.let { this.textSize = 16f }
                                                setTextColor(
                                                    ContextCompat.getColor(
                                                        context,
                                                        R.color.mainTitleColor
                                                    )
                                                )
                                            }
                                        },
                                        modifier = Modifier.padding(end = 12.dp)
                                    )

                                    var battery by remember { mutableIntStateOf(0) }

                                    SystemBroadcastReceiver(Intent.ACTION_BATTERY_CHANGED) { batteryStatus ->
                                        val batteryPct: Float? = batteryStatus?.let { intent ->
                                            val level: Int =
                                                intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                                            val scale: Int =
                                                intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                                            level * 100 / scale.toFloat()
                                        }
                                        battery = batteryPct!!.toInt()
                                    }

                                    val batteryIc = when {
                                        battery < 5 -> R.drawable.twotone_battery_alert_24
                                        battery < 20 -> R.drawable.twotone_battery_20_24
                                        battery < 30 -> R.drawable.twotone_battery_30_24
                                        battery < 50 -> R.drawable.twotone_battery_50_24
                                        battery < 60 -> R.drawable.twotone_battery_60_24
                                        battery < 80 -> R.drawable.twotone_battery_80_24
                                        battery <= 90 -> R.drawable.twotone_battery_90_24
                                        battery > 90 -> R.drawable.twotone_battery_full_24
                                        else -> R.drawable.twotone_battery_alert_24
                                    }

                                    Row(horizontalArrangement = Arrangement.Center) {
                                        Icon(
                                            modifier = Modifier.size(20.dp),
                                            painter = painterResource(id = batteryIc),
                                            contentDescription = null,
                                            tint = mainTitleColor
                                        )

                                        Text(
                                            modifier = Modifier.padding(
                                                start = 2.dp,
                                                top = 1.dp,
                                                end = appBarPadding
                                            ),
                                            text = "$battery%",
                                            style = Typography.titleMedium.copy(color = mainTitleColor)
                                        )
                                    }
                                }
                            )
                        }
                    },
                ) {
                    App(navController)
                }
            }
        }
    }
}


@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun App(navController: NavHostController) {

    Box(
        Modifier.fillMaxSize()
                .background(mainBackground)
    ) {
        val isLoading = remember { mutableStateOf(true) }
        val noData = remember { mutableStateOf(true) }
        val gamesList = remember { mutableStateOf(listOf<GameModel>()) }

        ApiHelper.getAllGames(LocalContext.current, gamesList, isLoading, noData)

        if (isLoading.value) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            if (noData.value) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        modifier = Modifier.size(64.dp),
                        painter = painterResource(id = R.drawable.outline_error_outline_24),
                        contentDescription = null,
                        tint = bodyColor
                    )
                    Spacer(Modifier.padding(top = 16.dp))
                    Text(
                        text = "No games",
                        color = mainTitleColor,
                        style = Typography.titleLarge.copy(color = mainTitleColor)
                    )
                }
            } else {
                NavHost(
                    navController = navController,
                    startDestination = "Home"
                ) {

                    composable("Home") {
                        Home(navController, gamesList.value)
                    }

                    composable(
                        route = "GameDetails/{gameId}",
                        arguments = listOf(navArgument("gameId") {
                            type = NavType.StringType
                        })
                    ) { props ->
                        val gameId = props.arguments?.getString("gameId") ?: ""
                        GameDetails(gamesList.value[gameId.toInt()])
                    }

                }
            }
        }
    }
}

@Composable
fun SystemBroadcastReceiver(
    systemAction: String,
    onSystemEvent: (intent: Intent?) -> Unit
) {
    val context = LocalContext.current

    val currentOnSystemEvent by rememberUpdatedState(onSystemEvent)

    DisposableEffect(context, systemAction) {
        val intentFilter = IntentFilter(systemAction)
        val broadcast = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                currentOnSystemEvent(intent)
            }
        }

        context.registerReceiver(broadcast, intentFilter)

        onDispose {
            context.unregisterReceiver(broadcast)
        }
    }
}
