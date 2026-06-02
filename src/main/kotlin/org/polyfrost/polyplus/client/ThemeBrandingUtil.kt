package org.polyfrost.polyplus.client

import org.polyfrost.oneconfig.internal.ui.themes.UIBranding
import org.polyfrost.oneconfig.internal.ui.themes.UITheme

/** Kotlin-only helper so Java mixins can update [UITheme] branding without calling mangled `copy` methods. */
internal object ThemeBrandingUtil {
    @JvmStatic
    fun withBranding(theme: UITheme, branding: UIBranding): UITheme = theme.copy(branding = branding)
}
