package me.mattstudios.minecraftchatvideo.func

import net.coobird.thumbnailator.Thumbnails
import org.jcodec.common.model.Picture
import org.jcodec.scale.AWTUtil
import java.awt.image.BufferedImage

/**
 * @author Matt
 */

/**
 * Simplifies the resizing of the images
 */
fun BufferedImage.resize(width: Int, height: Int): BufferedImage = Thumbnails.of(this).size(width, height).asBufferedImage()

/**
 * Simplifies the picture to buffered images
 */
fun Picture.asBufferedImage(): BufferedImage = AWTUtil.toBufferedImage(this)