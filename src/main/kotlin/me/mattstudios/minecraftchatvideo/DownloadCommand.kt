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
import org.jcodec.common.io.IOUtils
import org.jcodec.common.io.NIOUtils
import org.jcodec.common.model.Picture
import org.jcodec.scale.AWTUtil
import java.awt.image.BufferedImage
import java.io.File
import java.net.URL
import javax.imageio.ImageIO


/**
 * @author Matt
 */
@Command("downloadvideo")
class DownloadCommand(private val plugin: MinecraftChatVideo) : CommandBase() {

    // Youtube downloader
    private val downloader = YoutubeDownloader()

    init {
        downloader.addCipherFunctionPattern(2, "\\b([a-zA-Z0-9$]{2})\\s*=\\s*function\\(\\s*a\\s*\\)\\s*\\{\\s*a\\s*=\\s*a\\.split\\(\\s*\"\"\\s*\\)")
        // extractor features
        downloader.setParserRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36")
    }

    @Default
    fun download(player: Player, videoUrl: URL) {

        // Gets the video ID
        val id = videoUrl.query.substring(2)
        async {
            player.sendMessage("Downloading video...")
            val video = downloader.getVideo(id)

            // Gets the video format and audio format
            val videoWithAudioFormats = video.videoWithAudioFormats()
            val videoQuality = video.findVideoWithQuality(VideoQuality.tiny)

            val outputDir = File(plugin.dataFolder, video.details().title().replace(" ", ""))

            // Gets the format to use on the download (this one has been the only one to work so far)
            val format = videoWithAudioFormats[0]

            // Downloads the video into the videos dir
            val videoFile = video.download(format, outputDir)

            player.sendMessage("Resizing and saving video...")

           // Calculates how many frames the video has
            val max = videoQuality[0].fps() * video.details().lengthSeconds()

            // Starts the frame grabber
            val grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(videoFile))
            for (i in 0..max) {
                ImageIO.write(resize(AWTUtil.toBufferedImage(grab.nativeFrame)), "jpg", File(outputDir.path + "/frame-$i.jpg"))
                if (i % 100 == 0) println("Complete - ${i * 100 / max}%")
            }


            player.sendMessage("Load complete!")
        }

    }
    private fun resize(bufferedImage: BufferedImage): BufferedImage {
        return Thumbnails.of(bufferedImage).size(128, 72).asBufferedImage()
    }


}