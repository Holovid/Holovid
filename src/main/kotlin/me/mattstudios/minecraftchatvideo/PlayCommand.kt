package me.mattstudios.minecraftchatvideo

import me.mattstudios.mf.annotations.Command
import me.mattstudios.mf.annotations.Default
import me.mattstudios.mf.base.CommandBase
import me.mattstudios.minecraftchatvideo.Tasks.async
import net.coobird.thumbnailator.Thumbnails
import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity
import org.bukkit.craftbukkit.v1_16_R1.util.CraftChatMessage
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO


/**
 * @author Matt
 */
@Command("playvideo")
class PlayCommand(private val plugin: MinecraftChatVideo) : CommandBase() {

    // All the frames
    //private val frames = plugin.temporaryFrames
    private val frames = mutableListOf<List<String>>()

    private val armorStands = plugin.armorStands

    init {
        async {
            Bukkit.broadcastMessage("Loading..")
            loadFrames()
            Bukkit.broadcastMessage("Loaded!")
        }
    }

    @Default
    fun command(player: Player) {

        object : BukkitRunnable() {

            var frameCounter = 0

            override fun run() {

                val frame = loadFromWebCam()//frames[frameCounter]

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

        //player.playSound(player.location, "lotr.audio", SoundCategory.MASTER, 1f, 1f)
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
            val image = ImageIO.read(file) ?: return

            val frame = mutableListOf<String>()

            // Cycles through the image pixels
            for (i in 0 until image.height) {

                val builder = StringBuilder()
                var firstPixel = false
                var lastAlpha = 0

                for (j in 0 until image.width) {
                    val color = Color(image.getRGB(j, i), true)

                    val alpha = color.alpha

                    if (lastAlpha != 0 && alpha == 0) {
                        if (checkIfLast(image, j, i)) break
                    }

                    if (alpha == 0) {

                        if (!firstPixel) continue

                        builder.append("  ")
                        continue
                    } else {
                        firstPixel = true
                    }

                    lastAlpha = alpha
                    val hex = java.lang.String.format("#%02x%02x%02x", color.red, color.green, color.blue)

                    builder.append("${ChatColor.of(hex)}█")
                }

                frame.add(builder.toString())
            }

            frames.add(frame)
        }
    }

    private fun checkIfLast(image: BufferedImage, j: Int, i: Int): Boolean {
        for (k in j..image.width) {
            val color = Color(image.getRGB(j, i), true)

            val alpha = color.alpha

            if (alpha != 0) return false
        }

        return true
    }

    private fun loadFromWebCam(): List<String> {
        // Gets all the files in the images folder

        // Loads the image being saved by the webcam app
        val files = File(plugin.dataFolder, "images").listFiles() ?: return emptyList()
        files.sort()

        val file = files.first() ?: return emptyList()

        val imageRead = ImageIO.read(file) ?: return emptyList()

        val image = Thumbnails.of(imageRead).size(128, 72).asBufferedImage()

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

        file.delete()

        return frame
    }

}