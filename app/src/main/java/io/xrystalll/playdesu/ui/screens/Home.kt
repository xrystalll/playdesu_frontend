package io.xrystalll.playdesu.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.tv.foundation.PivotOffsets
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.itemsIndexed
import androidx.tv.foundation.lazy.list.rememberTvLazyListState
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.ImmersiveList
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
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
import io.xrystalll.playdesu.ui.theme.mainBackground
import io.xrystalll.playdesu.ui.theme.mainTitleColor


@Composable
fun Home(navController: NavHostController, gamesList:  List<GameModel>) {
    Column(Modifier.verticalScroll(rememberScrollState())) {
        HomeHead(navController, gamesList)

        SecondLine(navController, gamesList)
    }
}


@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HomeHead(navController: NavHostController, list: List<GameModel>) {
    val tvFirstRowListState = rememberTvLazyListState()
    val focusRequester = remember { FocusRequester() }
    var cardIsMounted by remember { mutableStateOf(false) }

    LaunchedEffect(cardIsMounted){
        if (cardIsMounted)
            focusRequester.requestFocus()
    }

    ImmersiveList(
        modifier = Modifier
            .fillMaxWidth()
            .height(immersiveHeadHeight),
        listAlignment = Alignment.BottomStart,
        background = { index, _ ->
            Box(Modifier.fillMaxSize()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(list[index].backdrop)
                        .build(),
                    contentDescription = list[index].displayName,
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

                ContentBlock(
                    gameItem = list[index],
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(
                            start = pageStartPadding,
                            end = pageEndPadding,
                            bottom = 48.dp
                        )
                        .fillMaxWidth(0.7f)
                )
            }
        }
    ) {
        TvLazyRow(
            pivotOffsets = PivotOffsets(0.12f, 0.12f),
            horizontalArrangement = Arrangement.spacedBy(cardSpacing),
            state = tvFirstRowListState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(
                start = pageStartPadding,
                end = pageEndPadding,
                bottom = 48.dp
            )
        ) {
            itemsIndexed(list) { index, item ->
                val firstItemModifier = if (index == 0) {
                    cardIsMounted = true

                    Modifier
                        .width(cardWidth)
                        .height(cardHeight)
                        .immersiveListItem(index)
                        .focusRequester(focusRequester)
                } else {
                    Modifier
                        .width(cardWidth)
                        .height(cardHeight)
                        .immersiveListItem(index)
                }
                Card(
                    onClick = { navController.navigate("GameDetails/${index}") },
                    modifier = firstItemModifier,
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
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(item.poster)
                            .build(),
                        contentDescription = item.displayName,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.fillMaxSize(),
                        placeholder = painterResource(R.drawable.empty_card_bg),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ContentBlock(gameItem: GameModel, modifier: Modifier) {
    AnimatedContent(
        targetState = gameItem,
        transitionSpec = {
            fadeIn(animationSpec = tween(durationMillis = 400)) togetherWith
                    fadeOut(animationSpec = tween(durationMillis = 400))
        },
        label = "",
        contentAlignment = Alignment.CenterStart,
        modifier = modifier
    ) { item ->
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = item.displayName,
                maxLines = 1,
                style = Typography.headlineLarge.copy(color = mainTitleColor)
            )

            Text(
                text = "${item.genre} • ${item.studio} • ${item.gameSystem} • ${item.releaseYear}",
                style = Typography.labelMedium.copy(color = bodyColor),
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}


@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SecondLine(navController: NavHostController, list: List<GameModel>) {
    val tvSecondRowListState = rememberTvLazyListState()
    var rowBackground by remember { mutableStateOf(list[0].getColor()) }

    Column(
        Modifier.background(Brush.linearGradient(listOf(mainBackground, rowBackground)))
    ) {
        Text(
            modifier = Modifier.padding(start = pageStartPadding, top = 24.dp),
            text = "All Games",
            style = Typography.headlineSmall.copy(color = mainTitleColor)
        )

        TvLazyRow(
            pivotOffsets = PivotOffsets(0.12f, 0.12f),
            horizontalArrangement = Arrangement.spacedBy(cardSpacing),
            state = tvSecondRowListState,
            contentPadding = PaddingValues(
                start = pageStartPadding,
                end = pageEndPadding,
                top = 24.dp,
                bottom = 30.dp,
            )
        ) {
            itemsIndexed(list) { index, item ->
                var cardFocused by remember { mutableStateOf(false) }

                Card(
                    onClick = { navController.navigate("GameDetails/${index}") },
                    modifier = Modifier
                        .width(cardWidth)
                        .height(cardHeight)
                        .onFocusChanged {
                            cardFocused = it.isFocused
                            rowBackground = item.getColor()
                        },
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
                                .data(item.poster)
                                .build(),
                            contentDescription = item.displayName,
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.fillMaxSize(),
                            placeholder = painterResource(R.drawable.empty_card_bg),
                        )

                        this@Card.AnimatedVisibility(
                            visible = cardFocused,
                            enter = fadeIn(animationSpec = tween(300)),
                            exit = fadeOut(animationSpec = tween(300))
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.BottomStart
                            ) {
                                Image(
                                    modifier = Modifier.fillParentMaxSize(),
                                    painter = painterResource(id = R.drawable.card_gradient),
                                    contentDescription = null
                                )

                                Text(
                                    modifier = Modifier.padding(12.dp),
                                    text = item.displayName,
                                    maxLines = 2,
                                    style = Typography.titleLarge.copy(color = mainTitleColor)
                                )
                            }
                        }

                    }
                }
            }
        }
    }
}
