import java.awt.Image
import java.awt.image.BufferedImage

import akka.actor.Actor

object EndActor{
  case class LastResizeMsg(img:BufferedImage,num: Int)
  case class DoneResizeMsg(img:BufferedImage,num: Int)
}


class EndActor extends Actor{
  import EndActor._
  override def receive: Receive = {
    case LastResizeMsg(img,num) =>
      val resized = img.getScaledInstance(1, 1,Image.SCALE_DEFAULT)
      val bufferedImage = new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB)
      bufferedImage.getGraphics.drawImage(resized,0,0,null)
      sender ! DoneResizeMsg(bufferedImage,num)

  }
}
