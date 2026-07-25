package dev.hypixelpackdisabler

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.toasts.ToastManager
import net.minecraft.client.gui.screens.Screen

object Compat {

    fun currentScreen(client: Minecraft): Screen? = client.screen

    fun setScreen(client: Minecraft, screen: Screen?) = client.setScreen(screen)

    fun toastManager(client: Minecraft): ToastManager = client.toastManager

    fun literal(name: String): LiteralArgumentBuilder<FabricClientCommandSource> =
        ClientCommandManager.literal(name)
}
