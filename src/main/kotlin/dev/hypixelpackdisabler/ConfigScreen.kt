package dev.hypixelpackdisabler

import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.StringWidget
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

        addRenderableWidget(
            Button.builder(Component.translatable("gui.done")) { onClose() }
                .bounds(centerX - 100, top + 28, 200, 20).build()
        )
    }

    override fun onClose() {
        minecraft.gui.setScreen(parent)
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
