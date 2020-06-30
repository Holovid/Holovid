package me.mattstudios.minecraftchatvideo

import me.mattstudios.mf.annotations.Command
import me.mattstudios.mf.annotations.Default
import me.mattstudios.mf.base.CommandBase
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.command.CommandSender
import org.bukkit.scheduler.BukkitRunnable
import java.io.File
import javax.imageio.ImageIO

/**
 * @author Matt
 */
@Command("playvideo")
class PlayCommand(private val plugin: MinecraftChatVideo) : CommandBase() {

    // All the frames
    private val frames = mutableListOf<List<String>>()

    init {
        loadFrames()
    }

    @Default
    fun command(sender: CommandSender) {

        object : BukkitRunnable() {

            var frameCounter = 0

            override fun run() {

                // Cycles through the lines to send
                for (line in frames[frameCounter]) {
                    // Uses spread operator cuz spigot's varargs
                    sender.spigot().sendMessage(*TextComponent.fromLegacyText(line))
                }

                if (frameCounter == frames.size - 1) cancel()

                frameCounter++

            }

        }.runTaskTimer(plugin, 0L, 1L)

    }

    /**
     * Loads all the frames from the images folder
     */
    private fun loadFrames() {
        // Gets all the files in the images folder
        val files = File(plugin.dataFolder, "images").listFiles() ?: return

        // Cycles through the files
        for (file in files) {

            // Loads the image and ignores non image files
            val image = ImageIO.read(file) ?: continue

            val frame = mutableListOf<String>()

            // Cycles through the image pixels
            for (i in 0 until image.height) {

                var line = ""

                for (j in 0 until image.width) {
                    val color = image.getRGB(j, i)

                    // Gets the color values
                    val red = color and 0x00ff0000 shr 16
                    val green = color and 0x0000ff00 shr 8
                    val blue = color and 0x000000ff

                    // Converts from RBG to hex
                    val hex = java.lang.String.format("#%02x%02x%02x", red, green, blue)

                    line += "${ChatColor.of(hex)}█"

                }

                frame.add(line)
            }

            frames.add(frame)
        }
    }

}