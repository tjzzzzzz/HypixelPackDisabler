package dev.hypixelpackdisabler

import com.mojang.brigadier.Command
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.ClientCommands
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents

object Commands {

    @Volatile
    private var openRequested = false

    fun init() {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(
                ClientCommands.literal(HypixelPackDisabler.MOD_ID).executes {
                    openRequested = true
                    Command.SINGLE_SUCCESS
                }
            )
        }

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (!openRequested) return@register
            openRequested = false
            if (client.gui.screen() == null) client.gui.setScreen(ConfigScreen(null))
        }
    }
}
