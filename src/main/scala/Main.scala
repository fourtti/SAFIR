import java.io.File

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.awt.{Graphics2D, Image}

import akka.actor.{ActorSystem, Props}


object Main extends App{



  println("Starting work")
  //val photo1 = ImageIO.read(new File("image5.jpg"))
  //val photo2 = makeSmaller(photo1)

  //val photo1 = ImageIO.read(new File("image.jpg"))
  //val photo2 = phototest(photo1)
  val photo1 = ImageIO.read(new File("image.jpg"))
  //val photo2 = resize(photo1)

  val system = ActorSystem("MainActorSystem")
  //val MainImageActor = system.actorOf(Props[ MainActor ], "MainImage")


  //import Mainactor companion object for initialsplit message type
  import MainActor._

  //creating mainactor and sending the original photo to it
  val mainImageActor = system.actorOf(Props (new MainActor(4)), s"MainActor")
  mainImageActor ! (initialSplit(photo1))


 //ImageIO.write(photo2,"jpg", new File("smallerImage.jpg"))

  def resize(img: BufferedImage) = {
    val resized =  img.getScaledInstance(img.getWidth()/2, img.getHeight()/2,Image.SCALE_DEFAULT)
    val bufferedImage = new BufferedImage(img.getWidth()/2, img.getHeight()/2, BufferedImage.TYPE_INT_RGB)
    bufferedImage.getGraphics.drawImage(resized, 0, 0, null)
    bufferedImage
  }


}
