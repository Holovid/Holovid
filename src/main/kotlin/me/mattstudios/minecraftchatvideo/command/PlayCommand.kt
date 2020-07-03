package me.mattstudios.minecraftchatvideo.command

import me.mattstudios.mf.annotations.Command
import me.mattstudios.mf.annotations.Completion
import me.mattstudios.mf.annotations.SubCommand
import me.mattstudios.mf.base.CommandBase
import me.mattstudios.minecraftchatvideo.MinecraftChatVideo
import net.md_5.bungee.api.ChatColor
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity
import org.bukkit.craftbukkit.v1_16_R1.util.CraftChatMessage
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.io.File
import java.lang.StringBuilder
import javax.imageio.ImageIO

/**
 * @author Matt
 */
@Command("holovid")
class PlayCommand(private val plugin: MinecraftChatVideo) : CommandBase() {

    // All the frames
    private val frames = plugin.temporaryFrames
    private val armorStands = plugin.armorStands

    @SubCommand("play")
    @Completion("#videos")
    fun command(player: Player, folder: String) {
        //loadFrames(folder)

        object : BukkitRunnable() {

            var frameCounter = 0

            override fun run() {

                val frame = frames[frameCounter]

                // Cycles through the lines to send
                for (i in frame.indices) {
                    // Uses spread operator cuz spigot's varargs
                    val line = frame[frame.size - i - 1]

                    val armorStand = (armorStands[i] as CraftEntity).handle
                    armorStand.customName = CraftChatMessage.fromStringOrNull(line)
                }

                if (frameCounter == frames.size - 1) cancel()

                frameCounter++
            }

        }.runTaskTimerAsynchronously(plugin, 0L, 1L)

    }

    /**
     * Loads all the frames from the images folder
     */
    private fun loadFrames(folder: String) {
        // Gets all the files in the images folder
        val files = File(plugin.dataFolder, folder).listFiles()?.filter { it.extension.equals("jpg", true) }?.sorted() ?: return

        // Cycles through the files
        for (file in files) {
            // Loads the image and ignores non image files
            val image = ImageIO.read(file) ?: continue

            val frame = mutableListOf<String>()

            // Cycles through the image pixels
            for (i in 0 until image.height) {

                val builder = StringBuilder()

                for (j in 0 until image.width) {
                    val color = image.getRGB(j, i)
                    builder.append("${ChatColor.of("#" + Integer.toHexString(color).substring(2))}█")
                }

                frame.add(builder.toString())
            }

            frames.add(frame)
        }
    }

}