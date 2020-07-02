package me.mattstudios.minecraftchatvideo

import com.github.kiulian.downloader.YoutubeDownloader
import com.github.kiulian.downloader.model.quality.VideoQuality
import me.mattstudios.mf.annotations.Command
import me.mattstudios.mf.annotations.Default
import me.mattstudios.mf.base.CommandBase
import me.mattstudios.minecraftchatvideo.Tasks.async
import net.coobird.thumbnailator.Thumbnails
import net.md_5.bungee.api.ChatColor
import org.bukkit.entity.Player
import org.jcodec.api.FrameGrab
import org.jcodec.common.io.NIOUtils
import org.jcodec.common.model.Picture
import org.jcodec.scale.AWTUtil
import java.awt.image.BufferedImage
import java.io.File
import java.net.URL


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
        plugin.temporaryFrames.clear()
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
//
//            // Calculates how many frames the video has
            val max = videoQuality[0].fps() * video.details().lengthSeconds()

            // Starts the frame grabber
            val grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(videoFile))
            for (i in 0..max) {
                loadFrame(resize(AWTUtil.toBufferedImage(grab.nativeFrame)))
                if (i % 100 == 0) println("Complete - ${i * 100 / max}%")
            }

//            // Cycles through all the frames and loads them
//            while (count++ < (max - 1)) {
//                val picture
//                val frame = grab.seekToFramePrecise(count)
//                saveFrame(frame)
//
//                // Prints out every 20 frames
//                if (count % 20 == 0) println("Complete - ${count * 100 / max}%")
//            }

            player.sendMessage("Load complete!")
        }

    }

    private fun saveFrame(frame: FrameGrab) {
//        val bufferedImage = AWTUtil.toBufferedImage(frame.nativeFrame)
        loadFrame(AWTUtil.toBufferedImage(frame.nativeFrame))
    }

    private fun resize(bufferedImage: BufferedImage): BufferedImage {
        return Thumbnails.of(bufferedImage).size(128, 72).asBufferedImage()
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