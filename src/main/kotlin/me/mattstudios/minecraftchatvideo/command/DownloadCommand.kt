package me.mattstudios.minecraftchatvideo.command

import com.github.kiulian.downloader.YoutubeDownloader
import com.github.kiulian.downloader.model.quality.VideoQuality
import me.mattstudios.mf.annotations.Command
import me.mattstudios.mf.annotations.SubCommand
import me.mattstudios.mf.base.CommandBase
import me.mattstudios.minecraftchatvideo.MinecraftChatVideo
import me.mattstudios.minecraftchatvideo.func.Tasks.async
import me.mattstudios.minecraftchatvideo.func.resize
import me.mattstudios.minecraftchatvideo.func.asBufferedImage
import net.md_5.bungee.api.ChatColor
import org.bukkit.entity.Player
import org.jcodec.api.FrameGrab
import org.jcodec.common.io.NIOUtils
import java.io.File
import java.lang.StringBuilder
import java.net.URL
import javax.imageio.ImageIO
import kotlin.system.measureTimeMillis


/**
 * @author Matt
 */
@Command("holovid")
class DownloadCommand(private val plugin: MinecraftChatVideo) : CommandBase() {

    // Youtube downloader
    private val downloader = YoutubeDownloader()

    init {
        // extractor features
        downloader.setParserRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36")
    }

    @SubCommand("download")
    fun download(player: Player, videoUrl: URL) {

        // TODO more website support
        // Gets the video ID
        val id = videoUrl.query.substring(2)

        async {
            player.sendMessage("Downloading video...")
            val video = downloader.getVideo(id)

            // Gets the video format and audio format
            val videoWithAudioFormats = video.videoWithAudioFormats()
            val videoQuality = video.findVideoWithQuality(VideoQuality.tiny)

            val outputDir = File(plugin.dataFolder, "saves/" + video.details().title().replace(" ", ""))

            // Gets the format to use on the download (this one has been the only one to work so far)
            val format = videoWithAudioFormats[0]

            // Downloads the video into the videos dir
            val videoFile = video.download(format, outputDir)

            player.sendMessage("Resizing and saving video...")

            // Calculates how many frames the video has
            val max = videoQuality[0].fps() * video.details().lengthSeconds()

            // Starts the frame grabber
            val grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(videoFile))

            for (i in 0 downTo max) {
                val frame = grab.nativeFrame ?: break
                ImageIO.write(frame.asBufferedImage().resize(128, 72), "jpg", File(outputDir.path + "/frame-$i.jpg"))
                if (i % 100 == 0) println("Complete - ${i * 100 / max}%")
            }

            player.sendMessage("Load complete!")
        }


    }

    private fun loadFrames(folder: String) {
        // Gets all the files in the images folder
        val files = File(plugin.dataFolder, folder).listFiles()?.filter { it.extension.equals("jpg", true) }?.sorted()
                    ?: return

        println("Here?")

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

            plugin.temporaryFrames.add(frame)
        }
    }


}