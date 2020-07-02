package me.mattstudios.minecraftchatvideo

import me.mattstudios.mf.annotations.Command
import me.mattstudios.mf.annotations.Default
import me.mattstudios.mf.base.CommandBase
import net.coobird.thumbnailator.Thumbnails
import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity
import org.bukkit.craftbukkit.v1_16_R1.util.CraftChatMessage
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO


/**
 * @author Matt
 */
@Command("playvideo")
class PlayCommand(private val plugin: MinecraftChatVideo) : CommandBase() {

    // All the frames
    //private val frames = plugin.temporaryFrames
    private val frames = mutableListOf<List<String>>()

    private var data = mutableListOf<String>()

    private val armorStands = plugin.armorStands

    init {
        loadFromWebCam()
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            Bukkit.broadcastMessage("Loading..")
            //loadFrames()

            Bukkit.broadcastMessage("Loaded!")
        })
    }

    @Default
    fun command(player: Player) {

        object : BukkitRunnable() {

            var frameCounter = 0

            override fun run() {

                val frame = data

                if (data.isEmpty()) return

                // Cycles through the lines to send
                for (i in frame.indices) {
                    // Uses spread operator cuz spigot's varargs
                    val line = frame[frame.size - i - 1]

                    val armorStand = (armorStands[i] as CraftEntity).handle
                    armorStand.customName = CraftChatMessage.fromStringOrNull(line)
                }

                if (frameCounter == frames.size - 1) cancel()

                loadFromWebCam()
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

                for (j in 0 until image.width) {
                    val color = image.getRGB(j, i)
                    builder.append("${ChatColor.of("#" + Integer.toHexString(color).substring(2))}█")
                }

                frame.add(builder.toString())
            }

            frames.add(frame)
        }
    }

    private fun loadFromWebCam() {
        // Gets all the files in the images folder

        // Loads the image being saved by the webcam app
        val file = File(plugin.dataFolder, "/images/image.jpg")

        // Useless attempt to fix the issue
        val isFileUnlocked = try {
            FileUtils.touch(file)
            true
        } catch (e: IOException) {
            false
        }
        // This is part of it ^
        if (!isFileUnlocked) return

        if (!file.exists()) return
        val imageRead = ImageIO.read(file) ?: return

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

        data = frame
    }

}