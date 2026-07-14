package org.polyfrost.polyplus.client.gui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.skia.Image as SkiaImage
import org.polyfrost.oneconfig.internal.ui.components.Icon
import org.polyfrost.oneconfig.internal.ui.components.LocalUiOversample
import org.polyfrost.oneconfig.internal.ui.compose.ComposeScreen
import org.polyfrost.oneconfig.internal.ui.themes.LocalTheme
import org.polyfrost.oneconfig.internal.ui.themes.Theme
import org.polyfrost.polyplus.client.PolyPlusConfig
import org.polyfrost.polyplus.client.PolyPlusClient
import org.polyfrost.polyplus.client.features.OnboardingFeatures
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.coroutineScope
import kotlin.math.roundToInt

class PolyPlusOnboardingScreen : ComposeScreen(RenderMode.CONTINUOUS) {
    private var firstFrameDrawn = false

    override fun shouldCloseOnEsc(): Boolean = false

    //? if <26.1 {
    /*override fun render(ctx: net.minecraft.client.gui.GuiGraphics, mouseX: Int, mouseY: Int, tickDelta: Float) {
        MenuBackgroundPass.enqueue(true)
        renderPanorama(ctx, tickDelta)
        if (firstFrameDrawn) {
            val gameRenderer = net.minecraft.client.Minecraft.getInstance().gameRenderer
            //? if <1.21.4 {
            /*gameRenderer.processBlurEffect(tickDelta)
            *///?} else {
            gameRenderer.processBlurEffect()
            //?}
        }
        super.render(ctx, mouseX, mouseY, tickDelta)
        firstFrameDrawn = true
    }

    override fun renderBackground(ctx: net.minecraft.client.gui.GuiGraphics, mouseX: Int, mouseY: Int, tickDelta: Float) = Unit
    *///?} else {
    override fun extractRenderState(ctx: net.minecraft.client.gui.GuiGraphicsExtractor, mouseX: Int, mouseY: Int, tickDelta: Float) {
        MenuBackgroundPass.enqueue(true)
        net.minecraft.client.Minecraft.getInstance().gameRenderer
            //? if >= 26.2 {
            /*.panorama()
            .extractRenderState(ctx, width, height)
            *///?} else {
            .getPanorama()
            .extractRenderState(ctx, width, height, true)
            //?}
        ctx.blurBeforeThisStratum()
        super.extractRenderState(ctx, mouseX, mouseY, tickDelta)
    }

    override fun extractBackground(ctx: net.minecraft.client.gui.GuiGraphicsExtractor, mouseX: Int, mouseY: Int, tickDelta: Float) = Unit
    //?}

    @Composable
    override fun compose() {
        var page by remember { mutableIntStateOf(0) }
        var lightTheme by remember { mutableStateOf(PolyPlusConfig.onboardingLightTheme) }
        var uiStyle by remember { mutableIntStateOf(PolyPlusConfig.onboardingUiStyle) }
        var hudStyle by remember { mutableIntStateOf(PolyPlusConfig.onboardingHudStyle) }
        var toggleSprint by remember { mutableStateOf(PolyPlusConfig.onboardingToggleSprint) }
        var hudSelections by remember {
            mutableStateOf(
                HudSelections(
                    fps = PolyPlusConfig.onboardingHudFps,
                    cps = PolyPlusConfig.onboardingHudCps,
                    ping = PolyPlusConfig.onboardingHudPing,
                    time = PolyPlusConfig.onboardingHudTime,
                    coords = PolyPlusConfig.onboardingHudCoords,
                    direction = PolyPlusConfig.onboardingHudDirection,
                ),
            )
        }
        var motionBlur by remember { mutableIntStateOf(PolyPlusConfig.onboardingMotionBlur.coerceIn(0, MOTION_BLUR_MAX)) }
        val finish = {
            PolyPlusConfig.onboardingLightTheme = lightTheme
            PolyPlusConfig.onboardingUiStyle = uiStyle
            PolyPlusConfig.onboardingHudStyle = hudStyle
            PolyPlusConfig.onboardingToggleSprint = toggleSprint
            PolyPlusConfig.onboardingHudFps = hudSelections.fps
            PolyPlusConfig.onboardingHudCps = hudSelections.cps
            PolyPlusConfig.onboardingHudPing = hudSelections.ping
            PolyPlusConfig.onboardingHudTime = hudSelections.time
            PolyPlusConfig.onboardingHudCoords = hudSelections.coords
            PolyPlusConfig.onboardingHudDirection = hudSelections.direction
            PolyPlusConfig.onboardingMotionBlur = motionBlur
            PolyPlusConfig.onboardingCompleted = true
            PolyPlusConfig.save()
            OnboardingFeatures.applySavedSettings()
            val mc = net.minecraft.client.Minecraft.getInstance()
            //? if >= 26.2 {
            /*mc.gui.setScreen(PolyPlusMainMenuScreen())
            *///?} else {
            mc.setScreen(PolyPlusMainMenuScreen())
            //?}
        }

        Theme {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val scale = minOf(maxWidth.value / DESIGN_WIDTH, maxHeight.value / DESIGN_HEIGHT)
                CompositionLocalProvider(LocalUiOversample provides (LocalUiOversample.current * scale.coerceAtLeast(1f))) {
                    Box(
                        Modifier
                            .align(Alignment.Center)
                            .requiredSize(DESIGN_WIDTH.dp, DESIGN_HEIGHT.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                transformOrigin = TransformOrigin.Center
                            },
                    ) {
                        Box(
                            Modifier
                                .offset(PANEL_X.dp, PANEL_Y.dp)
                                .size(PANEL_WIDTH.dp, PANEL_HEIGHT.dp)
                                .shadow(
                                    elevation = 29.dp,
                                    shape = PANEL_SHAPE,
                                    ambientColor = ShadowColor,
                                    spotColor = ShadowColor,
                                )
                                .clip(PANEL_SHAPE)
                                .background(PanelBackground)
                                .border(1.3.dp, PanelBorder, PANEL_SHAPE),
                        ) {
                            when (page) {
                                0 -> LookAndFeelPage(lightTheme, { lightTheme = it }, uiStyle, { uiStyle = it }, hudStyle, { hudStyle = it })
                                1 -> ModsPage(
                                    toggleSprint,
                                    { toggleSprint = it },
                                    hudSelections,
                                    { hudSelections = it },
                                    motionBlur,
                                    { motionBlur = it },
                                )
                                2 -> CosmeticsPage(
                                    onClaim = { PolyPlusClient.refreshCosmetics() },
                                    onStore = {
                                        finish()
                                        PolyPlusOneConfigIntegration.openCosmetics()
                                    },
                                )
                                else -> DonePage()
                            }
                            BottomNavigation(
                                page,
                                onSkip = finish,
                                onBack = { page-- },
                                onNext = { if (page == PAGE_COUNT - 1) finish() else page++ },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LookAndFeelPage(
    lightTheme: Boolean,
    onLightTheme: (Boolean) -> Unit,
    uiStyle: Int,
    onUiStyle: (Int) -> Unit,
    hudStyle: Int,
    onHudStyle: (Int) -> Unit,
) {
    Header("Let’s configure the", "Look & Feel")
    SectionLabel("UI Colors", 140f)
    Row(Modifier.offset(232.dp, 172.dp), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        ChoiceButton("Dark", MAIN_MENU_ASSETS + "moon-star.svg", !lightTheme, 198f) { onLightTheme(false) }
        ChoiceButton("Light", ONBOARDING_ASSETS + "sun.svg", lightTheme, 198f) { onLightTheme(true) }
    }
    SectionLabel("UI Style", 228f)
    Row(Modifier.offset(232.dp, 260.dp), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        StyleCard("PolyGlass", uiStyle == 0, rounded = true) { onUiStyle(0) }
        StyleCard("Minecraft", uiStyle == 1, rounded = false) { onUiStyle(1) }
    }
    SectionLabel("HUD Style", 439f)
    Row(Modifier.offset(232.dp, 471.dp), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        HudCard("PolyGlass", hudStyle == 0, rounded = true) { onHudStyle(0) }
        HudCard("Minecraft", hudStyle == 1, rounded = false) { onHudStyle(1) }
    }
}

@Composable
private fun ModsPage(
    toggleSprint: Boolean,
    onToggleSprint: (Boolean) -> Unit,
    hud: HudSelections,
    onHudChange: (HudSelections) -> Unit,
    motionBlur: Int,
    onMotionBlur: (Int) -> Unit,
) {
    Header("Continuing with", "Mods")
    SectionLabel("Toggle Sprint", 140f)
    Row(Modifier.offset(232.dp, 172.dp), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        ChoiceButton("Enabled", ONBOARDING_ASSETS + "zap.svg", toggleSprint, 198f) { onToggleSprint(true) }
        ChoiceButton("Disabled", ONBOARDING_ASSETS + "flash-off.svg", !toggleSprint, 198f) { onToggleSprint(false) }
    }
    SectionLabel("HUD Mods", 228f)
    Column(Modifier.offset(232.dp, 260.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            ChoiceButton("FPS", ONBOARDING_ASSETS + "play-square.svg", hud.fps, 126f) { onHudChange(hud.copy(fps = !hud.fps)) }
            ChoiceButton("CPS", ONBOARDING_ASSETS + "mouse.svg", hud.cps, 126f) { onHudChange(hud.copy(cps = !hud.cps)) }
            ChoiceButton("Ping", ONBOARDING_ASSETS + "wifi.svg", hud.ping, 126f) { onHudChange(hud.copy(ping = !hud.ping)) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            ChoiceButton("Time", ONBOARDING_ASSETS + "clock.svg", hud.time, 126f) { onHudChange(hud.copy(time = !hud.time)) }
            ChoiceButton("Coords", ONBOARDING_ASSETS + "marker-pin.svg", hud.coords, 126f) { onHudChange(hud.copy(coords = !hud.coords)) }
            ChoiceButton("Direction", ONBOARDING_ASSETS + "compass.svg", hud.direction, 126f) { onHudChange(hud.copy(direction = !hud.direction)) }
        }
    }
    SectionLabel("Motion Blur", 367f)
    Row(Modifier.offset(232.dp, 399.dp), verticalAlignment = Alignment.CenterVertically) {
        val thumbSize = 13.dp
        var trackWidthPx by remember { mutableStateOf(0f) }
        val progress by animateFloatAsState(
            (motionBlur.toFloat() / MOTION_BLUR_MAX).coerceIn(0f, 1f),
            animationSpec = spring(),
        )
        Box(
            Modifier
                .width(332.dp)
                .height(13.dp)
                .onSizeChanged { trackWidthPx = it.width.toFloat() }
                .pointerInput(MOTION_BLUR_MAX) {
                    val thumbPx = thumbSize.toPx()
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        fun update(x: Float) {
                            val usableWidth = (trackWidthPx - thumbPx).coerceAtLeast(1f)
                            val progress = ((x - thumbPx / 2f) / usableWidth).coerceIn(0f, 1f)
                            onMotionBlur((progress * MOTION_BLUR_MAX).roundToInt())
                        }
                        update(down.position.x)
                        down.consume()
                        do {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull() ?: break
                            update(change.position.x)
                            change.consume()
                        } while (change.pressed)
                    }
                },
        ) {
            Box(
                Modifier
                    .align(Alignment.Center)
                    .width(332.dp)
                    .height(7.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0x80273137))
                    .border(1.dp, PanelBorder, RoundedCornerShape(4.dp)),
            ) {
                Box(Modifier.fillMaxWidth(progress).height(7.dp).background(Brand))
            }
            Box(
                Modifier
                    .align(Alignment.CenterStart)
                    .offset { androidx.compose.ui.unit.IntOffset((progress * (trackWidthPx - thumbSize.toPx())).roundToInt(), 0) }
                    .size(thumbSize)
                    .clip(RoundedCornerShape(7.dp))
                    .background(TextPrimary),
            )
        }
        Spacer(Modifier.width(18.dp))
        Box(
            Modifier.width(64.dp).height(26.dp).clip(RoundedCornerShape(6.dp)).background(Color(0x80273137))
                .border(1.dp, PanelBorder, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.CenterStart,
        ) { OnboardingText(motionBlur.toString(), 12, Modifier.padding(start = 8.dp)) }
    }
    MotionBlurPreview(motionBlur, Modifier.offset(233.5.dp, 442.dp).size(413.dp, 115.dp))
}

@Composable
private fun CosmeticsPage(onClaim: () -> Unit, onStore: () -> Unit) {
    Header("Level up your drip 🔥 with", "Cosmetics")
    OnboardingText(
        "We decided to give you some for free as a warm welcome gift.\nEnjoy them, and check out the store if you want to see more!",
        15,
        Modifier.offset(215.dp, 137.dp).width(450.dp),
        Color.White,
        FontWeight.Light,
    )
    Row(Modifier.offset(124.dp, 209.dp), horizontalArrangement = Arrangement.spacedBy(46.dp)) {
        CosmeticCard("Starter Glasses")
        CosmeticCard("Starter Cape")
        CosmeticCard("Starter Bag")
    }
    ChoiceButton("Claim Free Cosmetics", ONBOARDING_ASSETS + "diamond.svg", true, 272f, Modifier.offset(304.dp, 445.dp), onClaim)
    ChoiceButton("Check Out the Store", ONBOARDING_ASSETS + "shopping-bag.svg", false, 272f, Modifier.offset(304.dp, 493.dp), onStore)
}

@Composable
private fun DonePage() {
    OnboardingIcon(ONBOARDING_ASSETS + "check-verified.svg", Color.White, Modifier.offset(374.75.dp, 157.dp).size(130.5.dp))
    OnboardingText("All Done!", 32, Modifier.offset(0.dp, 311.dp).width(PANEL_WIDTH.dp))
    OnboardingText(
        "That’s all for now, thank you for choosing OneClient! We hope you have a nice experience using it.",
        15,
        Modifier.offset(225.dp, 382.dp).width(430.dp),
        Color.White,
        FontWeight.Light,
    )
}

@Composable
private fun Header(kicker: String, title: String) {
    OnboardingText(kicker, 15, Modifier.offset(0.dp, 35.dp).width(PANEL_WIDTH.dp), Color.White, FontWeight.Normal)
    OnboardingText(title, 32, Modifier.offset(0.dp, 66.dp).width(PANEL_WIDTH.dp), Color.White, FontWeight.Normal)
}

@Composable
private fun SectionLabel(label: String, y: Float) {
    OnboardingText(label, 15, Modifier.offset(232.dp, y.dp).width(198.dp), Color.White, FontWeight.Normal, TextAlign.Start)
}

@Composable
private fun ChoiceButton(
    label: String,
    icon: String,
    selected: Boolean,
    width: Float,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Row(
        modifier
            .width(width.dp)
            .height(32.dp)
            .clip(ButtonShape)
            .background(ChoiceBackground)
            .border(if (selected) 2.dp else 1.dp, if (selected) Brand else PanelBorder, ButtonShape)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OnboardingIcon(icon, TextPrimary, Modifier.size(17.dp))
        OnboardingText(label, 14, color = TextPrimary, weight = FontWeight.Medium)
    }
}

@Composable
private fun StyleCard(label: String, selected: Boolean, rounded: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.size(198.dp, 155.dp).clip(ButtonShape).background(ChoiceBackground)
            .border(if (selected) 2.dp else 1.dp, if (selected) Brand else PanelBorder, ButtonShape)
            .clickable(onClick = onClick),
    ) {
        UiPreview(Modifier.offset(13.dp, 12.dp), rounded)
        OnboardingText(label, 14, Modifier.align(Alignment.BottomCenter).padding(bottom = 9.dp), TextPrimary, FontWeight.Medium)
    }
}

@Composable
private fun UiPreview(modifier: Modifier, rounded: Boolean) {
    val shape = if (rounded) RoundedCornerShape(8.dp) else RoundedCornerShape(0.dp)
    Row(modifier.size(172.dp, 108.dp).clip(shape).border(1.dp, Color(0x1AFFFFFF), shape)) {
        Column(Modifier.width(44.dp).height(108.dp).background(Color(0xB3151C22)).padding(8.dp, 7.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Box(Modifier.size(29.dp, 7.dp).background(Brand))
            repeat(3) { Box(Modifier.width(if (it == 0) 20.dp else 29.dp).height(4.dp).background(if (it == 0) TextSecondary else TextPrimary)) }
        }
        Column(Modifier.width(128.dp).height(108.dp).background(Color(0xF211171C)).padding(8.dp, 7.dp)) {
            Row { Box(Modifier.width(43.dp).height(7.dp).background(TextPrimary)); Spacer(Modifier.width(61.dp)); Box(Modifier.size(7.dp).background(TextPrimary)) }
            Spacer(Modifier.height(8.dp))
            repeat(3) {
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    repeat(3) { Box(Modifier.size(34.dp, 23.dp).background(Color(0xFF1A2229)).border(0.dp, Color.Transparent).padding(top = 17.dp).background(Brand)) }
                }
                Spacer(Modifier.height(5.dp))
            }
        }
    }
}

@Composable
private fun HudCard(label: String, selected: Boolean, rounded: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.size(198.dp, 94.dp).clip(ButtonShape).background(ChoiceBackground)
            .border(if (selected) 2.dp else 1.dp, if (selected) Brand else PanelBorder, ButtonShape)
            .clickable(onClick = onClick),
    ) {
        Box(
            Modifier.offset(31.5.dp, 16.dp).size(134.dp, 38.dp)
                .clip(if (rounded) RoundedCornerShape(5.dp) else RoundedCornerShape(0.dp))
                .background(Color(0x80000000)),
            contentAlignment = Alignment.Center,
        ) { OnboardingText("FPS: 150", 16) }
        OnboardingText(label, 14, Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp), TextPrimary, FontWeight.Medium)
    }
}

@Composable
private fun CosmeticCard(label: String) {
    Box(Modifier.size(180.dp, 202.dp).clip(RoundedCornerShape(10.dp)).background(ChoiceBackground).border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(10.dp))) {
        Checkerboard(Modifier.offset(17.dp, 18.dp).size(146.dp, 146.dp).clip(RoundedCornerShape(4.dp)))
        OnboardingText(label, 14, Modifier.align(Alignment.BottomCenter).padding(bottom = 13.dp), TextPrimary, FontWeight.Medium)
    }
}

@Composable
private fun Checkerboard(modifier: Modifier) {
    Canvas(modifier) {
        val cell = 12f
        var y = 0f
        var row = 0
        while (y < size.height) {
            var x = 0f
            var col = 0
            while (x < size.width) {
                drawRect(if ((row + col) % 2 == 0) Color(0xFF666666) else Color(0xFF4A4A4A), androidx.compose.ui.geometry.Offset(x, y), androidx.compose.ui.geometry.Size(cell, cell))
                x += cell
                col++
            }
            y += cell
            row++
        }
    }
}

@Composable
private fun BottomNavigation(page: Int, onSkip: () -> Unit, onBack: () -> Unit, onNext: () -> Unit) {
    if (page == 0) {
        ChoiceButton("Skip", MAIN_MENU_ASSETS + "x-close.svg", false, 100f, Modifier.offset(26.dp, 604.dp), onSkip)
    } else {
        ChoiceButton("Back", "assets/polyplus/ico/left-arrow.svg", false, 100f, Modifier.offset(26.dp, 604.dp), onBack)
    }
    ChoiceButton(if (page == PAGE_COUNT - 1) "Finish" else "Next", MAIN_MENU_ASSETS + "chevron-right.svg", true, 100f, Modifier.offset(754.dp, 604.dp), onNext)
    Row(Modifier.offset(((PANEL_WIDTH - (PAGE_COUNT * 17f - 5f)) / 2f).dp, 614.dp), horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(PAGE_COUNT) { index ->
            Box(
                Modifier.size(if (index == page) 12.dp else 10.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (index == page) Color(0x80EBF2FF) else Color(0x73232D32))
                    .border(1.dp, if (index == page) Color(0xCCFFFFFF) else Color(0x66FFFFFF), RoundedCornerShape(8.dp)),
            )
        }
    }
}

@Composable
private fun OnboardingText(
    text: String,
    size: Int,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    weight: FontWeight = FontWeight.Normal,
    align: TextAlign = TextAlign.Center,
) {
    BasicText(text, modifier, TextStyle(color = color, fontSize = size.sp, fontWeight = weight, fontFamily = LocalTheme.current.typography.family, textAlign = align))
}

@Composable
private fun OnboardingIcon(path: String, color: Color, modifier: Modifier) = Icon(path, color, modifier)

@Composable
private fun MotionBlurPreview(strength: Int, modifier: Modifier = Modifier) {
    val bitmap = remember { loadOnboardingRaster(ONBOARDING_ASSETS + "motion-test.png") }
    var pan by remember { mutableStateOf(Offset.Zero) }
    var velocity by remember { mutableStateOf(Offset.Zero) }
    val shape = RoundedCornerShape(6.dp)

    Box(
        modifier
            .clip(shape)
            .background(Color(0xFF273137))
            .border(1.dp, Color.White, shape),
    ) {
        if (bitmap != null) {
            Canvas(
                Modifier
                    .fillMaxSize()
                    .pointerInput(strength) {
                        coroutineScope {
                            var previous: Offset? = null
                            var generation = 0
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val position = event.changes.firstOrNull()?.position ?: continue
                                    val last = previous
                                    previous = position
                                    pan = Offset(
                                        ((position.x / size.width) - 0.5f) * -12f,
                                        ((position.y / size.height) - 0.5f) * -8f,
                                    )
                                    if (last != null) {
                                        val scale = strength.coerceIn(0, MOTION_BLUR_MAX) / MOTION_BLUR_MAX.toFloat()
                                        velocity = Offset(
                                            (position.x - last.x).coerceIn(-32f, 32f) * scale,
                                            (position.y - last.y).coerceIn(-24f, 24f) * scale,
                                        )
                                        val current = ++generation
                                        launch {
                                            delay(70L)
                                            if (current == generation) velocity = Offset.Zero
                                        }
                                    }
                                }
                            }
                        }
                    },
            ) {
                val overscan = 1.12f
                val destinationWidth = size.width * overscan
                val destinationHeight = size.height * overscan
                val destinationRatio = destinationWidth / destinationHeight
                val sourceRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
                val sourceWidth: Int
                val sourceHeight: Int
                if (sourceRatio > destinationRatio) {
                    sourceHeight = bitmap.height
                    sourceWidth = (sourceHeight * destinationRatio).roundToInt()
                } else {
                    sourceWidth = bitmap.width
                    sourceHeight = (sourceWidth / destinationRatio).roundToInt()
                }
                val sourceOffset = androidx.compose.ui.unit.IntOffset(
                    (bitmap.width - sourceWidth) / 2,
                    (bitmap.height - sourceHeight) / 2,
                )
                val samples = if (strength == 0 || velocity == Offset.Zero) 1 else strength.coerceIn(2, MOTION_BLUR_MAX)
                repeat(samples) { index ->
                    val samplePosition = if (samples == 1) 0f else index.toFloat() / (samples - 1) - 0.5f
                    val sampleOffset = velocity * samplePosition
                    drawImage(
                        image = bitmap,
                        srcOffset = sourceOffset,
                        srcSize = androidx.compose.ui.unit.IntSize(sourceWidth, sourceHeight),
                        dstOffset = androidx.compose.ui.unit.IntOffset(
                            ((size.width - destinationWidth) / 2f + pan.x + sampleOffset.x).roundToInt(),
                            ((size.height - destinationHeight) / 2f + pan.y + sampleOffset.y).roundToInt(),
                        ),
                        dstSize = androidx.compose.ui.unit.IntSize(destinationWidth.roundToInt(), destinationHeight.roundToInt()),
                        alpha = 1f / samples,
                        blendMode = if (index == 0) BlendMode.SrcOver else BlendMode.Plus,
                    )
                }
            }
        }
        OnboardingText("Move your mouse here to test!", 13, Modifier.align(Alignment.Center), Color(0xBFFFFFFF), FontWeight.Light)
    }
}

private fun loadOnboardingRaster(path: String): ImageBitmap? = runCatching {
    val bytes = PolyPlusOnboardingScreen::class.java.getResourceAsStream("/$path")!!.use { it.readBytes() }
    SkiaImage.makeFromEncoded(bytes).toComposeImageBitmap()
}.getOrNull()

private data class HudSelections(
    val fps: Boolean,
    val cps: Boolean,
    val ping: Boolean,
    val time: Boolean,
    val coords: Boolean,
    val direction: Boolean,
)

private const val DESIGN_WIDTH = 1920f
private const val DESIGN_HEIGHT = 1080f
private const val PANEL_X = 520f
private const val PANEL_Y = 210f
private const val PANEL_WIDTH = 880f
private const val PANEL_HEIGHT = 660f
private const val PAGE_COUNT = 4
private const val MOTION_BLUR_MAX = 10
private const val ONBOARDING_ASSETS = "assets/polyplus/onboarding/"
private const val MAIN_MENU_ASSETS = "assets/polyplus/mainmenu/"
private val PANEL_SHAPE = RoundedCornerShape(10.345.dp)
private val ButtonShape = RoundedCornerShape(6.dp)
private val PanelBackground = Color(0x73232D32)
private val PanelBorder = Color(0x66FFFFFF)
private val ShadowColor = Color(0x26000000)
private val ChoiceBackground = Color(0x80273137)
private val Brand = Color(0xFF3F7CE4)
private val TextPrimary = Color(0xFFD5DBFF)
private val TextSecondary = Color(0xFF78818D)
