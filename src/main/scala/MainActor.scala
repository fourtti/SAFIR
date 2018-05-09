import java.awt.image.BufferedImage
import java.io.{ByteArrayInputStream, File}
import java.time.LocalTime

import ImageByteConverter._
import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import akka.cluster.{Cluster, MemberStatus}
import akka.cluster.ClusterEvent.{ClusterDomainEvent, MemberExited, MemberRemoved, UnreachableMember}
import akka.cluster.routing.{ClusterRouterPool, ClusterRouterPoolSettings}
import akka.routing.RoundRobinPool
import javax.imageio.ImageIO


object MainActor {
    case class initialSplit(byteImage: Array[Byte])
}

class MainActor(photoWidth: Int, photoHeight: Int) extends Actor with ActorLogging{


  Cluster(context.system).subscribe(self, classOf[ClusterDomainEvent])


  import MainActor._
  import ResizingActor._



  var router:ActorRef = context.system.actorOf(ClusterRouterPool(
    local = RoundRobinPool(2),  //type of router and the amount of actors created at the start
    settings = ClusterRouterPoolSettings(
      totalInstances = 15,      //maximum amount of actors created, not sure where it creates them though
      maxInstancesPerNode = 1,  //how many actors are created per node in cluster
      allowLocalRoutees = false //allow actors to be created in the node the router lies in. In this case the Master node
    )
  ).props(Props(new EndActor())))
  //var chunkCount = 0

  val imageAmount = photoWidth*photoHeight
  var counter = 0 //counts how many pictures have been returned from the router
  val returningImageArray = new Array[BufferedImage](imageAmount) // the images returned the router are stored here before they are constructed.
  var eventRunning = false
  var images = new Array[BufferedImage](imageAmount)

  override def postStop(): Unit = {
    Cluster(context.system).unsubscribe(self)
    super.postStop()
  }

  override def receive: Receive = {

    case initialSplit(byteImg) =>
      eventRunning = true
      //converting image from ByteArray back to Buffered Image
      val img = convertToBufferedImage(byteImg)

      //println(s"got inital image. Size: ${img.getHeight} * ${img.getWidth}")

      images = imageToChunks(img, photoWidth, photoHeight)

      for (i <- images.indices) {
        router ! startResizing(convertBufferToByteArray(images(i)),i)
      }

    case returningImage(byteImg, pos) =>
      val img = convertToBufferedImage(byteImg)
      counter += 1
      println(s"HEY DUDE! I GOT SOMETHING!!!! $counter/$imageAmount PICS ARRIVED. YEEHAA! pos of image was:" + pos)
      returningImageArray(pos) = img
      if (counter == imageAmount) { //if all images have been returned, construct image and save it to the project folder
        val buildImage = buildImageFromChunks(returningImageArray,photoHeight,photoWidth)
        ImageIO.write(buildImage,"jpg", new File("smallerImage.jpg"))
        eventRunning = false
        println("The work is done, time now: " + LocalTime.now())
      }
    case MemberExited(m) => log.info(s"$m EXITED")
    case MemberRemoved(m, previousState) =>
      if(previousState == MemberStatus.Exiting) {
        log.info(s"Member $m gracefully exited, REMOVED.")
      } else {
        if (eventRunning) {
          //event found running, killing router with poisonpill.
          router ! PoisonPill
          // Creating new router
          router = context.system.actorOf(ClusterRouterPool(
            local = RoundRobinPool(2),  //type of router and the amount of actors created at the start
            settings = ClusterRouterPoolSettings(
              totalInstances = 15,      //maximum amount of actors created, not sure where it creates them though
              maxInstancesPerNode = 1,  //how many actors are created per node in cluster
              allowLocalRoutees = false //allow actors to be created in the node the router lies in. In this case the Master node
            )
          ).props(Props(new ResizingActor(10))))

          //Have to sleep, so that router has time to get up
          Thread.sleep(1000)

          for (i <- images.indices) {
            router ! startResizing(convertBufferToByteArray(images(i)),i)
          }
        }
        log.info(s"$m downed after unreachable, REMOVED.")
      }
    case UnreachableMember(m) => log.info(s"$m is Unreachable")

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
    println("array size is: " + chunkArray.length)
    chunkArray
  }
  def buildImageFromChunks(arr: Array[BufferedImage],width:Int,height:Int): BufferedImage = {
    val h = arr(1).getHeight()
    val w = arr(1).getWidth()
    val outputImg = new BufferedImage(width/2,height/2,BufferedImage.TYPE_INT_RGB)
    var counter = 0
    for (x: Int <- 0 until height) {
      for (y: Int <- 0 until width) {
        outputImg.getGraphics.drawImage(arr(counter), y*w, x*h, null)
        counter+=1
      }
    }
    outputImg
  }


}
