package io.xrystalll.playdesu.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.PivotOffsets
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.itemsIndexed
import androidx.tv.foundation.lazy.list.rememberTvLazyListState
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.ImmersiveList
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.xrystalll.playdesu.GameActivity
import io.xrystalll.playdesu.R
import io.xrystalll.playdesu.cardHeight
import io.xrystalll.playdesu.cardSpacing
import io.xrystalll.playdesu.cardWidth
import io.xrystalll.playdesu.data.GameModel
import io.xrystalll.playdesu.immersiveHeadHeight
import io.xrystalll.playdesu.pageEndPadding
import io.xrystalll.playdesu.pageStartPadding
import io.xrystalll.playdesu.ui.theme.Typography
import io.xrystalll.playdesu.ui.theme.bodyColor
import io.xrystalll.playdesu.ui.theme.buttonFocusBg
import io.xrystalll.playdesu.ui.theme.buttonFocusText
import io.xrystalll.playdesu.ui.theme.mainTitleColor


@Composable
fun GameDetails(game: GameModel) {
    val context = LocalContext.current

    Column(Modifier.verticalScroll(rememberScrollState())) {
        GameHead(game, context)

        if (game.screenshots.isNotEmpty()) {
            Screenshots(game)
        }
    }
}


@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun GameHead(game: GameModel, context: Context) {
    val focusRequester = remember { FocusRequester() }
    var btnMounted by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(btnMounted){
        if (btnMounted)
            focusRequester.requestFocus()
    }

    ImmersiveList(
        modifier = Modifier
            .fillMaxWidth()
            .height(immersiveHeadHeight),
        listAlignment = Alignment.BottomStart,
        background = { _, _ ->
            Box(Modifier.fillMaxSize()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(game.backdrop)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxSize()
                )

                Image(
                    painter = painterResource(id = R.drawable.scrim),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    alignment = Alignment.BottomStart,
                    contentScale = ContentScale.FillWidth
                )

                Details(
                    gameItem = game,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(
                            start = pageStartPadding,
                            end = pageEndPadding
                        )
                        .fillMaxWidth(0.7f)
                )

            }
        }
    ) {
        Column(
            Modifier.padding(
                start = pageStartPadding,
                end = pageEndPadding,
                top = 16.dp,
                bottom = 48.dp
            )
        ) {
            btnMounted = true

            Button(
                modifier = Modifier.focusRequester(focusRequester),
                onClick = {
                    val intent = Intent(context, GameActivity::class.java)
                    intent.putExtra("PROP_ID", game.id)
                    intent.putExtra("PROP_NAME", game.displayName)
                    intent.putExtra("PROP_SYSTEM", game.gameSystem)
                    intent.putExtra("PROP_ROM", game.file)
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.colors(
                    containerColor = game.getColor(),
                    focusedContainerColor = buttonFocusBg,
                    focusedContentColor = buttonFocusText
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_gamepad_24),
                    contentDescription = null
                )
                Text(
                    text = "Play now",
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun Details(gameItem: GameModel, modifier: Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = gameItem.displayName,
            maxLines = 2,
            style = Typography.headlineLarge.copy(color = mainTitleColor)
        )

        Text(
            text = "${gameItem.genre} • ${gameItem.studio} • ${gameItem.gameSystem} • ${gameItem.releaseYear}",
            style = Typography.labelMedium.copy(color = bodyColor),
            modifier = Modifier.padding(top = 6.dp, bottom = 8.dp)
        )

        Text(
            text = gameItem.description,
            maxLines = 4,
            style = Typography.labelMedium.copy(color = bodyColor),
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}


@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun Screenshots(game: GameModel) {
    val screenshotsRowListState = rememberTvLazyListState()

    Column {
        Text(
            modifier = Modifier.padding(start = pageStartPadding, top = 24.dp),
            text = "Screenshots",
            style = Typography.headlineSmall.copy(color = mainTitleColor)
        )

        TvLazyRow(
            pivotOffsets = PivotOffsets(0.15f, 0.15f),
            horizontalArrangement = Arrangement.spacedBy(cardSpacing * 1.2f),
            state = screenshotsRowListState,
            contentPadding = PaddingValues(
                start = pageStartPadding,
                end = pageEndPadding,
                top = 24.dp,
                bottom = 40.dp,
            )
        ) {
            itemsIndexed(game.screenshots) {_, item ->
                Card(
                    onClick = { },
                    modifier = Modifier
                        .width(cardWidth * 1.6f)
                        .height(cardHeight * 1.6f),
                    shape = CardDefaults.shape(
                        shape = RoundedCornerShape(14.dp)
                    ),
                    border = CardDefaults.border(
                        focusedBorder = Border(
                            BorderStroke(
                                2.dp,
                                Color.White
                            )
                        )
                    )
                ) {
                    Box {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(item)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.fillMaxSize(),
                            placeholder = painterResource(R.drawable.empty_card_bg),
                        )
                    }
                }
            }
        }
    }
}
