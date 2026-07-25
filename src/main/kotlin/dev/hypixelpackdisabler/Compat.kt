package dev.hypixelpackdisabler

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.toasts.ToastManager
import net.minecraft.client.gui.screens.Screen

object Compat {

    fun currentScreen(client: Minecraft): Screen? = client.gui.screen()

    fun setScreen(client: Minecraft, screen: Screen?) = client.gui.setScreen(screen)

    fun toastManager(client: Minecraft): ToastManager = client.gui.toastManager()
}
