import java.awt.image.BufferedImage
import akka.actor.{Actor, Props}

object MainActor {

  case class returnRGB(rgb: Int, position: Int)
  case class startResizing(bufferedImage: BufferedImage, position: Int)
}


class MainActor extends Actor {
  import MainActor._
  import ImageActor._
  import Main._
  override def receive: Receive = {

    case startResizing(img,pos) => {

    }

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