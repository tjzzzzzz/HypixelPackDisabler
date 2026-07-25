package dev.hypixelpackdisabler

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.client.Minecraft
import net.minecraft.server.packs.repository.PackSource

object ServerPackControl {

    @Volatile
    private var reorderedPack: String? = null

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
    }

    fun isReordering(): Boolean = onHypixel()

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
