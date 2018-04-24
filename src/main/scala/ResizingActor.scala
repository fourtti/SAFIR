import java.awt.image.BufferedImage

import akka.actor.{Actor, Props}

object ResizingActor {

  case class returningImage(bufferedImage: BufferedImage, position: Int)
  case class startResizing(bufferedImage: BufferedImage, position: Int)
  case class lastResize(bufferedImage: BufferedImage, position: Int)
}


class ResizingActor(imageAmount: Int) extends Actor {
  import ResizingActor._


  var chunkCount = 0
  val returningImageArray = new Array[BufferedImage](imageAmount)
  var counter = 0
  var actorPosition = 0

  override def receive: Receive = {

    case startResizing(img,pos) =>
      //println(s"got inital image. Size: ${img.getHeight} * ${img.getWidth}")
      actorPosition = pos
      if(img.getHeight() <= 4 || img.getWidth() <= 4) {
        println(s"Sent to Last Actor")
        val PartialImageActor = context.actorOf(Props(new EndActor))
        PartialImageActor ! startResizing(img,pos)

      } else {
        val images = imageToChunks(img,2,2)

        for (i <- images.indices) {
          val PartialImageActor = context.actorOf(Props(new ResizingActor(chunkCount)), s"MainImage_$i")
          PartialImageActor ! startResizing(images(i),i)
        }
      }

    case returningImage(img,pos) =>
      counter+=1
      returningImageArray(pos) = img

      if (counter == imageAmount) {
        println(s"got $counter messages back. Creating Larger picture")
        val buildImage = buildImageFromChunks(returningImageArray)
        context.parent ! returningImage(buildImage,actorPosition)
        println(s"sent image to parent actor. Size is ${buildImage.getHeight} * ${buildImage.getWidth}")
      }
    case lastResize(img,pos) => context.parent ! returningImage(img,pos)

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
    for(x <- 0 until rows){
      for(y <- 0 until cols){

        // intitialize new imagechunks in array
        chunkArray(counter) = new BufferedImage(chunkWidth,chunkHeight,BufferedImage.TYPE_INT_RGB)


        //draw the image to the imagechunk
        val graphics = chunkArray(counter).createGraphics()
        graphics.drawImage(img,0,0,chunkWidth,chunkHeight,chunkWidth*y,chunkHeight*x,chunkWidth * y + chunkWidth, chunkHeight * x + chunkHeight,null)
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
    for (x: Int <- 0 until Math.sqrt(arr.length).toInt) {
      for (y: Int <- 0 until Math.sqrt(arr.length).toInt) {
        outputImg.getGraphics.drawImage(arr(counter), y*w, x*h, null)
        counter+=1
      }
    }
    outputImg
  }



}