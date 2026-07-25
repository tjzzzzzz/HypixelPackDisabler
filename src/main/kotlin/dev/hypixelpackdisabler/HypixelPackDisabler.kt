package dev.hypixelpackdisabler

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import net.fabricmc.api.ClientModInitializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object HypixelPackDisabler : ClientModInitializer {

    const val MOD_ID = "hypixelpackdisabler"
    const val MOD_NAME = "Hypixel Pack Disabler"

    val logger: Logger = LoggerFactory.getLogger(MOD_NAME)

    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default + CoroutineName(MOD_NAME))

    override fun onInitializeClient() {
        Http.init()
        Config.load()
        Commands.init()
        ServerPackControl.init()
        logger.info("$MOD_NAME ready")
    }
}
