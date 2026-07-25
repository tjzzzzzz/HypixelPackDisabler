package dev.hypixelpackdisabler

import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.chat.Component

object Catharsis {

    const val MOD_ID = "catharsis"
    const val URL = "https://modrinth.com/mod/catharsis"

    private var warned = false

    val isInstalled: Boolean
        get() = FabricLoader.getInstance().isModLoaded(MOD_ID)

    fun warnIfMissing() {
        if (isInstalled || warned) return
        warned = true
        HypixelPackDisabler.logger.warn(
            "Catharsis is not installed; the legacy pack cannot be read without it ({})",
            URL
        )
        LegacyPackInstaller.toast("Catharsis is required — see $URL")
    }

    fun statusLine(): Component =
        if (isInstalled) Component.literal("Catharsis detected")
        else Component.literal("Catharsis missing — pack will not load")
}
