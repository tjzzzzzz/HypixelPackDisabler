package dev.hypixelpackdisabler

import kotlinx.serialization.Serializable
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

object Config {

    @Serializable
    data class Data(val enabled: Boolean = true)

    private val file: Path =
        FabricLoader.getInstance().configDir.resolve("${HypixelPackDisabler.MOD_ID}.json")

    @Volatile
    private var data: Data = Data()

    var enabled: Boolean
        get() = data.enabled
        set(value) {
            if (data.enabled == value) return
            data = data.copy(enabled = value)
            save()
        }

    fun load() {
        if (!file.exists()) {
            save()
            return
        }
        runCatching { Http.json.decodeFromString<Data>(Files.readString(file)) }
            .onSuccess { data = it }
            .onFailure { HypixelPackDisabler.logger.warn("failed to read config, using defaults", it) }
    }

    private fun save() {
        runCatching {
            Files.createDirectories(file.parent)
            Files.writeString(file, Http.json.encodeToString(data))
        }.onFailure { HypixelPackDisabler.logger.warn("failed to write config", it) }
    }
}
