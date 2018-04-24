import java.awt.image.BufferedImage
import akka.actor.{Actor, Props}

object MainActor {
  case class PartialImageResize(bufferedImage: BufferedImage)
  case class returnRGB(rgb: Int, position: Int)
}


class MainActor extends Actor {
  import MainActor._
  import ImageActor._
  override def receive: Receive = {

    case PartialImageResize(img) => {
      val h = img.getHeight()
      val w = img.getWidth()
      val actorAmount = (h*w)/4
      for(x <- 0 until w/2){
        for(y <- 0 until h/2) {

          val actor = context.actorOf(Props[ ImageActor ], s"imageActor_$i")
          actor ! getRGB(img,x,y)

        }
      }




    }
    case returnRGB(rgb,position) => {

  }



  }


}