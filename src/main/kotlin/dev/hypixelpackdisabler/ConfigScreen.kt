package dev.hypixelpackdisabler

import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.StringWidget
import net.minecraft.client.gui.screens.ConfirmLinkScreen
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style

class ConfigScreen(private val parent: Screen?) : Screen(TITLE) {

    override fun init() {
        val centerX = width / 2
        val top = height / 2 - 30

        addRenderableWidget(StringWidget(centerX - 100, top - 40, 200, 20, title, font))

        lateinit var toggle: Button
        toggle = Button.builder(label()) {
            Config.enabled = !Config.enabled
            toggle.message = label()
            LegacyPackInstaller.applyState()
        }.bounds(centerX - 100, top, 200, 20).build()

        addRenderableWidget(toggle)

        var next = top + 28

        if (!Catharsis.isInstalled) {
            addRenderableWidget(
                StringWidget(centerX - 130, next, 260, 20, Catharsis.statusLine(), font)
            )
            next += 22
            addRenderableWidget(
                Button.builder(
                    Component.literal("Get Catharsis"),
                    ConfirmLinkScreen.confirmLink(this, Catharsis.URL)
                ).bounds(centerX - 100, next, 200, 20).build()
            )
            next += 28
        }

        addRenderableWidget(
            Button.builder(Component.translatable("gui.done")) { onClose() }
                .bounds(centerX - 100, next, 200, 20).build()
        )
    }

    override fun onClose() {
        Compat.setScreen(minecraft, parent)
    }

    private fun label(): Component {
        val state = if (Config.enabled) "ON" else "OFF"
        val color = if (Config.enabled) 0x55FF55 else 0xFF5555
        return Component.literal("Legacy textures: ")
            .append(Component.literal(state).setStyle(Style.EMPTY.withColor(color)))
    }

    companion object {
        private val TITLE: Component = Component.literal(HypixelPackDisabler.MOD_NAME)
    }
}
