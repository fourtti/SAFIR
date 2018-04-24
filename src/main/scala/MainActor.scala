import java.awt.image.BufferedImage
import java.io.File

import akka.actor.{Actor, Props}
import javax.imageio.ImageIO


object MainActor {
    case class initialSplit(bufferedImage: BufferedImage)
}

class MainActor(imageAmount: Int) extends Actor {

  import MainActor._
  import ResizingActor._

  var chunkCount = 0
  var counter = 0
  val returningImageArray = new Array[BufferedImage](imageAmount)

  override def receive: Receive = {

    case initialSplit(img) => {
      val images = imageToChunks(img, 2, 2)

      for (i <- 0 until images.length) {
        val PartialImageActor = context.actorOf(Props(new ResizingActor(chunkCount)), s"MainImage_$i")
        PartialImageActor ! startResizing(images(i),i)
      }
    }
    case returningImage(img, pos) => {
      counter += 1
      returningImageArray(pos) = img


      if (counter == imageAmount) {
        val buildImage = buildImageFromChunks(returningImageArray)
        ImageIO.write(buildImage,"jpg", new File("smallerImage.jpg"))
      }
    }
  }

  def imageToChunks(img: BufferedImage, rows: Int, cols: Int): Array[BufferedImage] = {


    //total amount of chunks. Determines the size of the array returned
    chunkCount = rows * cols
    //width and height of each chunk
    val chunkWidth = img.getWidth() / cols
    val chunkHeight = img.getHeight() / rows
    val chunkArray = new Array[BufferedImage](chunkCount)

    //counter for inputing chunks to the array
    var counter = 0
    for (x <- 0 until rows) {
      for (y <- 0 until cols) {

        // intitialize new imagechunks in array
        chunkArray(counter) = new BufferedImage(chunkWidth, chunkHeight, BufferedImage.TYPE_INT_RGB)


        //draw the image to the imagechunk
        val graphics = chunkArray(counter).createGraphics()
        graphics.drawImage(img, 0, 0, chunkWidth, chunkHeight, chunkWidth * y, chunkHeight * x, chunkWidth * y + chunkWidth, chunkHeight * x + chunkHeight, null)
        graphics.dispose()

        //adding to counter
        counter += 1
      }
    }
    chunkArray
  }
  def buildImageFromChunks(arr: Array[BufferedImage]): BufferedImage = {
    val h = arr(1).getHeight()
    val w = arr(1).getWidth()
    val outputImg = new BufferedImage(w*2,h*2,BufferedImage.TYPE_INT_RGB)
    var counter = 0
    for (x: Int <- 0 until Math.sqrt(arr.length)) {
      for (y: Int <- 0 until Math.sqrt(arr.length)) {
        outputImg.getGraphics.drawImage(arr(counter), x*w, y*h, null)
        counter+=1
      }
    }
    outputImg
  }
}