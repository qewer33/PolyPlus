package org.polyfrost.polyplus.client.features

import net.minecraft.client.Minecraft
import org.apache.logging.log4j.LogManager
import org.polyfrost.oneconfig.api.event.v1.eventHandler
import org.polyfrost.oneconfig.api.event.v1.events.TickEvent
import org.polyfrost.oneconfig.api.hud.v1.Font
import org.polyfrost.oneconfig.api.hud.v1.Hud
import org.polyfrost.oneconfig.api.hud.v1.HudManager
import org.polyfrost.oneconfig.internal.ui.themes.MinecraftDark
import org.polyfrost.oneconfig.internal.ui.themes.MinecraftLight
import org.polyfrost.oneconfig.internal.ui.themes.PolyGlassDark
import org.polyfrost.oneconfig.internal.ui.themes.PolyGlassLight
import org.polyfrost.oneconfig.internal.ui.themes.ThemeRegistry
import org.polyfrost.polyplus.client.PolyPlusConfig

object OnboardingFeatures {
    private val logger = LogManager.getLogger("PolyPlus/Onboarding")

    val polySprintAvailable: Boolean by lazy { classExists(POLYSPRINT_CONFIG) }
    val polyBlurAvailable: Boolean by lazy { classExists(POLYBLUR_CONFIG) }
    val evergreenAvailable: Boolean by lazy { classExists(EVERGREEN_FPS) }

    val modsPageAvailable: Boolean
        get() = polySprintAvailable || polyBlurAvailable || evergreenAvailable

    fun initialize() {
        eventHandler { _: TickEvent.End ->
            if (!PolyPlusConfig.onboardingCompleted) return@eventHandler
            var changed = false
            if (!PolyPlusConfig.onboardingFeaturesApplied) {
                applyCoreSettings()
                changed = true
            }
            if (polySprintAvailable && !PolyPlusConfig.onboardingSprintApplied) {
                applyToggleSprint(PolyPlusConfig.onboardingToggleSprint)
                PolyPlusConfig.onboardingSprintApplied = true
                changed = true
            }
            if (polyBlurAvailable && !PolyPlusConfig.onboardingPolyBlurApplied) {
                if (applyPolyBlur(PolyPlusConfig.onboardingMotionBlur)) {
                    PolyPlusConfig.onboardingPolyBlurApplied = true
                    changed = true
                }
            }
            if (evergreenAvailable && !PolyPlusConfig.onboardingEvergreenApplied) {
                if (applyEvergreenHuds()) {
                    PolyPlusConfig.onboardingEvergreenApplied = true
                    changed = true
                }
            }
            if (changed) PolyPlusConfig.save()
        }
    }

    fun applySavedSettings() {
        applyCoreSettings()
        if (polySprintAvailable) {
            applyToggleSprint(PolyPlusConfig.onboardingToggleSprint)
            PolyPlusConfig.onboardingSprintApplied = true
        }
        if (applyPolyBlur(PolyPlusConfig.onboardingMotionBlur)) {
            PolyPlusConfig.onboardingPolyBlurApplied = true
        }
        if (applyEvergreenHuds()) {
            PolyPlusConfig.onboardingEvergreenApplied = true
        }
        PolyPlusConfig.save()
    }

    private fun applyCoreSettings() {
        applyTheme(PolyPlusConfig.onboardingLightTheme, PolyPlusConfig.onboardingUiStyle)
        PolyPlusConfig.onboardingFeaturesApplied = true
    }

    private fun applyTheme(light: Boolean, style: Int) {
        val theme = when {
            style == 1 && light -> MinecraftLight
            style == 1 -> MinecraftDark
            light -> PolyGlassLight
            else -> PolyGlassDark
        }
        ThemeRegistry.activate(theme)
    }

    private fun applyToggleSprint(enabled: Boolean) {
        runCatching {
            Minecraft.getInstance().options.toggleSprint().set(enabled)
            Minecraft.getInstance().options.save()
        }.onFailure { logger.warn("Could not apply toggle sprint preference", it) }
    }

    private fun applyEvergreenHuds(): Boolean {
        val choices = listOf(
            HudChoice(PolyPlusConfig.onboardingHudFps, EVERGREEN_FPS),
            HudChoice(PolyPlusConfig.onboardingHudCps, EVERGREEN_CPS),
            HudChoice(PolyPlusConfig.onboardingHudPing, EVERGREEN_PING),
            HudChoice(PolyPlusConfig.onboardingHudTime, EVERGREEN_TIME),
        )
        var found = false
        choices.forEach { choice ->
            val type = hudClass(choice.className) ?: return@forEach
            found = applyHud(type, choice.enabled) || found
        }

        val positionType = hudClass(EVERGREEN_POSITION)
        if (positionType != null) {
            val enabled = PolyPlusConfig.onboardingHudCoords || PolyPlusConfig.onboardingHudDirection
            found = applyHud(positionType, enabled) { hud ->
                setBoolean(hud, "setShowDirection", PolyPlusConfig.onboardingHudDirection)
                setBoolean(hud, "setShowAxis", PolyPlusConfig.onboardingHudCoords)
                setBoolean(hud, "setShowX", PolyPlusConfig.onboardingHudCoords || PolyPlusConfig.onboardingHudDirection)
                setBoolean(hud, "setShowY", PolyPlusConfig.onboardingHudCoords)
                setBoolean(hud, "setShowZ", PolyPlusConfig.onboardingHudCoords || PolyPlusConfig.onboardingHudDirection)
            } || found
        }
        return found
    }

    private fun applyHud(type: Class<out Hud>, enabled: Boolean, configure: (Hud) -> Unit = {}): Boolean {
        val provider = HudManager.getProvider(type) ?: return false
        styleHud(provider)
        configure(provider)
        HudManager.toggleAllHuds(provider, !enabled)
        HudManager.getHudsOfType(type).forEach {
            styleHud(it)
            configure(it)
            it.hidden = !enabled
            runCatching { it.save() }
        }
        return true
    }

    private fun styleHud(hud: Hud) {
        val polyGlass = PolyPlusConfig.onboardingHudStyle == 0
        hud.font = if (polyGlass) Font.Poppins else Font.Minecraft
        hud.bgRadius = if (polyGlass) 4f else 0f
        hud.showBackground = true
    }

    private fun applyPolyBlur(value: Int): Boolean {
        val strength = value.coerceIn(0, 10)
        return runCatching {
            val config = Class.forName(POLYBLUR_CONFIG)
            val instance = config.getField("INSTANCE").get(null)
            config.getMethod("setEnabled", Boolean::class.javaPrimitiveType).invoke(instance, strength > 0)
            if (strength > 0) {
                config.getMethod("setStrength", Float::class.javaPrimitiveType).invoke(instance, strength.toFloat())
            }
            config.getMethod("save").invoke(instance)
            true
        }.onFailure {
            if (it !is ClassNotFoundException) logger.warn("Could not apply PolyBlur preference", it)
        }.getOrDefault(false)
    }

    private fun classExists(name: String) = runCatching { Class.forName(name, false, javaClass.classLoader) }.isSuccess

    @Suppress("UNCHECKED_CAST")
    private fun hudClass(name: String): Class<out Hud>? =
        runCatching { Class.forName(name, false, javaClass.classLoader) as Class<out Hud> }.getOrNull()

    private fun setBoolean(target: Any, setter: String, value: Boolean) {
        runCatching { target.javaClass.getMethod(setter, Boolean::class.javaPrimitiveType).invoke(target, value) }
            .onFailure { logger.warn("Could not apply EvergreenHUD option {}", setter, it) }
    }

    private data class HudChoice(
        val enabled: Boolean,
        val className: String,
    )

    private const val POLYSPRINT_CONFIG = "org.polyfrost.polysprint.client.PolySprintConfig"
    private const val POLYBLUR_CONFIG = "org.polyfrost.polyblur.client.PolyBlurConfig"
    private const val EVERGREEN_FPS = "org.polyfrost.evergreenhud.client.hud.FpsHud"
    private const val EVERGREEN_CPS = "org.polyfrost.evergreenhud.client.hud.CpsHud"
    private const val EVERGREEN_PING = "org.polyfrost.evergreenhud.client.hud.PingHud"
    private const val EVERGREEN_TIME = "org.polyfrost.evergreenhud.client.hud.clock.DigitalClockHud"
    private const val EVERGREEN_POSITION = "org.polyfrost.evergreenhud.client.hud.PositionHud"
}
