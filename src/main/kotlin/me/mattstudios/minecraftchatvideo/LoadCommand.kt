package me.mattstudios.minecraftchatvideo

import com.github.kiulian.downloader.YoutubeDownloader
import com.github.kiulian.downloader.model.formats.AudioVideoFormat
import com.github.kiulian.downloader.model.quality.VideoQuality
import me.mattstudios.mf.annotations.Command
import me.mattstudios.mf.annotations.Default
import me.mattstudios.mf.base.CommandBase
import me.mattstudios.minecraftchatvideo.Tasks.async
import net.md_5.bungee.api.ChatColor
import org.apache.commons.lang.StringUtils
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FilenameUtils
import org.bukkit.entity.Player
import org.jcodec.api.FrameGrab
import org.jcodec.api.JCodecException
import org.jcodec.common.io.NIOUtils
import org.jcodec.scale.AWTUtil
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.lang.StringBuilder
import java.net.URL
import java.util.function.Consumer
import javax.imageio.ImageIO


/**
 * @author Matt
 */
@Command("loadvideo")
class LoadCommand(private val plugin: MinecraftChatVideo) : CommandBase() {

    // Youtube downloader
    private val downloader = YoutubeDownloader()

    init {
        downloader.addCipherFunctionPattern(2, "\\b([a-zA-Z0-9$]{2})\\s*=\\s*function\\(\\s*a\\s*\\)\\s*\\{\\s*a\\s*=\\s*a\\.split\\(\\s*\"\"\\s*\\)")
        // extractor features
        downloader.setParserRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36")
    }

    @Default
    fun load(player: Player, videoUrl: URL) {

        // Gets the video ID
        val id = videoUrl.query.substring(2)

        async {
            player.sendMessage("Downloading video...")
            val video = downloader.getVideo(id)

            // Gets the video format and audio format
            val videoWithAudioFormats = video.videoWithAudioFormats()
            val videoQuality = video.findVideoWithQuality(VideoQuality.tiny)

            val outputDir = File(plugin.dataFolder, "videos")

            // Gets the format to use on the download (this one has been the only one to work so far)
            val format = videoWithAudioFormats[0]

            // Downloads the video into the videos dir
            val videoFile = video.download(format, outputDir)

            player.sendMessage("Resizing and loading video...")

            val fileName = videoFile.nameWithoutExtension.replace(" ", "_")

            // Calculates how many frames the video has
            val max = videoQuality[0].fps() * video.details().lengthSeconds()
            var count = 0

            // Starts the frame grabber
            val grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(videoFile))
            grab.seekToSecondPrecise(0.0)

            // Cycles through all the frames and loads them
            while (count++ < max) {
                val frame = grab.seekToFramePrecise(count)
                saveFrame(frame)

                // Prints out every 20 frames
                if (count % 20 == 0) println("Complete - ${count * 100 / max}%")
            }

            player.sendMessage("Load complete!")
        }

    }

    @Throws(IOException::class, JCodecException::class)
    private fun saveFrame(frame: FrameGrab) {
        val bufferedImage = AWTUtil.toBufferedImage(frame.nativeFrame)

        loadFrame(resize(bufferedImage))

        /*val tempFile = File(plugin.dataFolder, "/videos/$folderName/frame-$frameNumber.png")

        if (!tempFile.parentFile.exists()) tempFile.parentFile.mkdirs()
        if (!tempFile.exists()) tempFile.createNewFile()*/


        //ImageIO.write(resize(bufferedImage), "png", tempFile)
    }

    private fun resize(bufferedImage: BufferedImage): BufferedImage {
        val image = bufferedImage.getScaledInstance(128, 72, Image.SCALE_SMOOTH)
        val bImage = BufferedImage(128, 72, BufferedImage.TYPE_INT_ARGB)

        val graphics = bImage.createGraphics()
        graphics.drawImage(image, 0, 0, null)
        graphics.dispose()

        return bImage
    }

    /**
     * Loads all the frames from the images folder
     */
    private fun loadFrame(image: BufferedImage) {

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