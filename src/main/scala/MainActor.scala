import java.awt.image.BufferedImage
import java.io.{ByteArrayInputStream, File}
import ImageByteConverter._
import akka.actor.{Actor, ActorRef, Props}
import akka.cluster.routing.{ClusterRouterPool, ClusterRouterPoolSettings}
import akka.routing.RoundRobinPool
import javax.imageio.ImageIO


object MainActor {
    case class initialSplit(byteImage: Array[Byte])
}

class MainActor(imageAmount: Int) extends Actor {

  import MainActor._
  import ResizingActor._

  println("xXx(SNIPORRZZZ)Lets see if this is before the errors(SNIPORRZZZ)xXx")
  var router:ActorRef = context.system.actorOf(ClusterRouterPool(
    local = RoundRobinPool(2),  //type of router and the amount of actors created at the start
    settings = ClusterRouterPoolSettings(
      totalInstances = 15,      //maximum amount of actors created, not sure where it creates them though
      maxInstancesPerNode = 1,  //how many actors are created per node in cluster
      allowLocalRoutees = false //allow actors to be created in the node the router lies in. In this case the Master node
    )
  ).props(Props(new ResizingActor(4))), //the type of the actors, that are created
    name = "master-router")
  //var chunkCount = 0
  var counter = 0 //counts how many pictures have been returned from the router
  val returningImageArray = new Array[BufferedImage](imageAmount) // the images returned the router are stored here before they are constructed.

  override def receive: Receive = {
    case initialSplit(byteImg) =>
      //converting image from ByteArray back to Buffered Image
      val img = convertToBufferedImage(byteImg)

      //println(s"got inital image. Size: ${img.getHeight} * ${img.getWidth}")

      val images = imageToChunks(img, 2, 2)

      for (i <- images.indices) {
        router ! startResizing(convertBufferToByteArray(images(i)),i)
      }

    case returningImage(byteImg, pos) =>
      println("HEY DUDE! I GOT SOMETHING!!!!")
      val img = convertToBufferedImage(byteImg)
      counter += 1
      returningImageArray(pos) = img
      if (counter == imageAmount) { //if all images have been returned, construct image and save it to the project folder
        val buildImage = buildImageFromChunks(returningImageArray)
        ImageIO.write(buildImage,"jpg", new File("smallerImage.jpg"))
        println("The work is done")
      }

  }

  def imageToChunks(img: BufferedImage, rows: Int, cols: Int): Array[BufferedImage] = {
    //total amount of chunks. Determines the size of the array returned
    val chunkCount = rows * cols
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
    for (x: Int <- 0 until Math.sqrt(arr.length).toInt) {
      for (y: Int <- 0 until Math.sqrt(arr.length).toInt) {
        outputImg.getGraphics.drawImage(arr(counter), y*w, x*h, null)
        counter+=1
      }
    }
    outputImg
  }


}
