package dev.hypixelpackdisabler

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.client.Minecraft
import net.minecraft.server.packs.repository.PackSource

object ServerPackControl {

    private const val CHECK_INTERVAL = 10
    private const val COOLDOWN_TICKS = 40

    @Volatile
    private var reorderedPack: String? = null

    private var sinceCheck = 0
    private var cooldown = 0

    fun init() {
        ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
            if (!onHypixel()) {
                reorderedPack = null
                return@register
            }
            val pack = serverPackId() ?: return@register
            if (pack == reorderedPack) return@register
            reorderedPack = pack
            Minecraft.getInstance().execute { LegacyPackInstaller.ensureInstalled() }
        }
        ClientPlayConnectionEvents.DISCONNECT.register { _, client ->
            client.execute {
                if (client.currentServer == null || !onHypixel()) reorderedPack = null
            }
        }
        ClientTickEvents.END_CLIENT_TICK.register { _ ->
            if (cooldown > 0) cooldown--
            if (!Config.enabled || !onHypixel()) return@register
            if (++sinceCheck < CHECK_INTERVAL) return@register
            sinceCheck = 0
            if (cooldown > 0) return@register
            if (LegacyPackInstaller.reassertOrdering()) cooldown = COOLDOWN_TICKS
        }
    }

    fun isReordering(): Boolean = Config.enabled && onHypixel()

    private fun onHypixel(): Boolean {
        val server = Minecraft.getInstance().currentServer ?: return false
        val ip = server.ip.lowercase()
        return ip.contains("hypixel.net") || ip.contains("hypixel.io")
    }

    private fun serverPackId(): String? =
        Minecraft.getInstance().resourcePackRepository.selectedPacks
            .firstOrNull { it.packSource == PackSource.SERVER }
            ?.id
}
