package me.mattstudios.minecraftchatvideo

import com.github.kiulian.downloader.YoutubeDownloader
import com.github.kiulian.downloader.model.formats.VideoFormat
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
import javax.imageio.ImageIO


/**
 * @author Matt
 */
@Command("loadvideo")
class LoadCommand(private val plugin: MinecraftChatVideo) : CommandBase() {

    //private val downloader = YoutubeDownloader()
    private val regex = Regex("watch\\?v=(\\w+)")

    @Default
    fun load(player: Player, videoUrl: URL) {

        val id = videoUrl.query.substring(2)

        async {
            player.sendMessage("Downloading video...")
            /*val video = downloader.getVideo(id)

            val videoFormats = video.findVideoWithQuality(VideoQuality.hd720)
            val outputDir = File(plugin.dataFolder, "videos")

            val videoFile = video.download(videoFormats[0], outputDir)*/

            object : YouTubeExtractor(this) {
                fun onExtractionComplete(ytFiles: SparseArray<YtFile?>?, vMeta: VideoMeta?) {
                    if (ytFiles != null) {
                        val itag = 22
                        val downloadUrl: String = ytFiles.get(itag).getUrl()
                    }
                }
            }.extract(youtubeLink, true, true)

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
        val tempFile = File("frames" + File.separator + frameNumber + "frameVideo.png")
        if (!tempFile.parentFile.exists()) tempFile.parentFile.mkdirs()
        if (!tempFile.exists()) tempFile.createNewFile()
        ImageIO.write(AWTUtil.toBufferedImage(frame), "png", tempFile)
        return tempFile
    }


}