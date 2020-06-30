package me.mattstudios.minecraftchatvideo

import com.github.kiulian.downloader.YoutubeDownloader
import com.github.kiulian.downloader.model.formats.AudioVideoFormat
import com.github.kiulian.downloader.model.quality.VideoQuality
import me.mattstudios.mf.annotations.Command
import me.mattstudios.mf.annotations.Default
import me.mattstudios.mf.base.CommandBase
import me.mattstudios.minecraftchatvideo.Tasks.async
import org.bukkit.entity.Player
import org.jcodec.api.FrameGrab
import org.jcodec.api.JCodecException
import org.jcodec.scale.AWTUtil
import java.io.File
import java.io.IOException
import java.net.URL
import java.util.function.Consumer
import javax.imageio.ImageIO


/**
 * @author Matt
 */
@Command("loadvideo")
class LoadCommand(private val plugin: MinecraftChatVideo) : CommandBase() {

    private val downloader = YoutubeDownloader()
    private val regex = Regex("watch\\?v=(\\w+)")

    init {
        downloader.addCipherFunctionPattern(2, "\\b([a-zA-Z0-9$]{2})\\s*=\\s*function\\(\\s*a\\s*\\)\\s*\\{\\s*a\\s*=\\s*a\\.split\\(\\s*\"\"\\s*\\)")
        // extractor features
        downloader.setParserRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36")
    }

    @Default
    fun load(player: Player, videoUrl: URL) {

        val id = videoUrl.query.substring(2)

        async {
            player.sendMessage("Downloading video...")
            val video = downloader.getVideo(id)

            val videoWithAudioFormats = video.videoWithAudioFormats()

            val videoFormats = video.findVideoWithQuality(VideoQuality.tiny)
            val outputDir = File(plugin.dataFolder, "videos")

            val videoFile = video.download(videoWithAudioFormats[0], outputDir)

            player.sendMessage("Resizing video...")

            var count = 100
            while (count-- > 0) {
                println("Saving frame $count!")
                saveFrame(videoFile, count)
            }
        }

    }

    @Throws(IOException::class, JCodecException::class)
    fun saveFrame(file: File, frameNumber: Int): File {
        val frame = FrameGrab.getFrameFromFile(file, frameNumber)
        val tempFile = File(plugin.dataFolder, "/videos/frames" + File.separator + frameNumber + "frameVideo.png")
        if (!tempFile.parentFile.exists()) tempFile.parentFile.mkdirs()
        if (!tempFile.exists()) tempFile.createNewFile()
        ImageIO.write(AWTUtil.toBufferedImage(frame), "png", tempFile)
        return tempFile
    }


}