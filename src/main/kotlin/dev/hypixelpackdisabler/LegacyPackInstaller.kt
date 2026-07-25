package dev.hypixelpackdisabler

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.utils.io.jvm.javaio.copyTo
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.toasts.SystemToast
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import java.nio.file.Files
import java.security.MessageDigest
import kotlin.io.path.exists

object LegacyPackInstaller {

    private const val PROJECT_ID = "eiWiefXD"
    private const val VERSIONS_URL = "https://api.modrinth.com/v2/project/$PROJECT_ID/version"

    private const val LOCAL_FILE = "SkyBlock Legacy.zip"
    private const val ACCENT = 0xFFAA00

    private val lock = Mutex()

    @Volatile
    var status: Status = Status.IDLE
        private set

    enum class Status { IDLE, DOWNLOADING, DONE, FAILED }

    fun applyState() {
        if (Config.enabled) ensureInstalled() else disablePack()
    }

    fun ensureInstalled() {
        if (!Config.enabled) return
        Catharsis.warnIfMissing()

        val target = Minecraft.getInstance().resourcePackDirectory.resolve(LOCAL_FILE)

        if (target.exists()) {
            enablePack()
            return
        }
        if (status == Status.DOWNLOADING) return

        HypixelPackDisabler.scope.launch {
            lock.withLock {
                if (status == Status.DOWNLOADING) return@withLock
                status = Status.DOWNLOADING
                toast("Downloading legacy textures…")
                runCatching { download(target) }
                    .onSuccess {
                        status = Status.DONE
                        HypixelPackDisabler.logger.info("legacy pack installed at {}", target)
                        toast("Legacy textures installed")
                        enablePack()
                    }
                    .onFailure {
                        status = Status.FAILED
                        HypixelPackDisabler.logger.warn("legacy pack download failed", it)
                        toast("Legacy pack download failed")
                    }
            }
        }
    }

    fun toast(message: String) {
        val client = Minecraft.getInstance()
        client.execute {
            val title = Component.literal(HypixelPackDisabler.MOD_NAME)
                .setStyle(Style.EMPTY.withColor(ACCENT))
            SystemToast.addOrUpdate(
                Compat.toastManager(client),
                SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
                title,
                Component.literal(message)
            )
        }
    }

    private suspend fun download(target: java.nio.file.Path) {
        val client = Http.instance
        val json = client.get(VERSIONS_URL).bodyAsText()
        val versions = Http.json.decodeFromString<List<ModrinthVersion>>(json)

        val running = HypixelPackDisabler.minecraftVersion
        val chosen = versions
            .sortedByDescending { it.datePublished }
            .let { sorted ->
                sorted.firstOrNull { v -> v.gameVersions.contains(running) } ?: sorted.firstOrNull()
            } ?: error("no versions returned by modrinth")

        if (!chosen.gameVersions.contains(running)) {
            HypixelPackDisabler.logger.warn(
                "no legacy pack build for {}, falling back to {}", running, chosen.versionNumber
            )
        }

        val file = chosen.files.firstOrNull { it.primary } ?: chosen.files.firstOrNull()
        ?: error("version ${chosen.versionNumber} has no files")

        HypixelPackDisabler.logger.info("downloading legacy pack {} from {}", chosen.versionNumber, file.url)

        val tmp = target.resolveSibling("$LOCAL_FILE.part")
        Files.newOutputStream(tmp).use { out ->
            client.get(file.url).bodyAsChannel().copyTo(out)
        }

        val sha1 = file.hashes?.sha1
        if (sha1 != null && !sha1.equals(sha1Of(tmp), ignoreCase = true)) {
            Files.deleteIfExists(tmp)
            error("sha1 mismatch, refusing to install a corrupt pack")
        }

        Files.move(tmp, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING)
    }

    private fun enablePack() {
        val client = Minecraft.getInstance()
        client.execute {
            client.reloadResourcePacks().thenRun {
                client.execute { selectLegacyPack(client) }
            }
        }
    }

    private fun disablePack() {
        val client = Minecraft.getInstance()
        client.execute {
            val id = client.options.resourcePacks.firstOrNull { it.contains(LOCAL_FILE) } ?: return@execute
            client.options.resourcePacks.remove(id)
            client.resourcePackRepository.setSelected(client.options.resourcePacks)
            client.options.save()
            client.reloadResourcePacks()
            HypixelPackDisabler.logger.info("disabled legacy pack '{}'", id)
        }
    }

    private fun selectLegacyPack(client: Minecraft) {
        val repo = client.resourcePackRepository
        val id = repo.availableIds.firstOrNull { it.contains(LOCAL_FILE) }

        if (id == null) {
            HypixelPackDisabler.logger.warn(
                "legacy pack still not in repository, available ids: {}",
                repo.availableIds.joinToString()
            )
            return
        }

        val alreadyOn = client.options.resourcePacks.contains(id)
        if (!alreadyOn) client.options.resourcePacks.add(id)

        repo.setSelected(client.options.resourcePacks)
        client.options.save()

        client.reloadResourcePacks()
        HypixelPackDisabler.logger.info("enabled legacy pack '{}' (was on: {})", id, alreadyOn)
    }

    private fun sha1Of(path: java.nio.file.Path): String {
        val digest = MessageDigest.getInstance("SHA-1")
        Files.newInputStream(path).use { input ->
            val buffer = ByteArray(1 shl 16)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    @Serializable
    private data class ModrinthVersion(
        @SerialName("version_number") val versionNumber: String = "",
        @SerialName("date_published") val datePublished: String = "",
        @SerialName("game_versions") val gameVersions: List<String> = emptyList(),
        val files: List<ModrinthFile> = emptyList()
    )

    @Serializable
    private data class ModrinthFile(
        val url: String = "",
        val filename: String = "",
        val primary: Boolean = false,
        val hashes: Hashes? = null
    )

    @Serializable
    private data class Hashes(val sha1: String? = null)
}
