import java.awt.Image
import java.awt.image.BufferedImage

import akka.actor.Actor



class EndActor extends Actor{
  import ResizingActor._
  override def receive: Receive = {
    case startResizing(img,num) =>
      val resized = img.getScaledInstance(img.getWidth()/2, img.getHeight()/2,Image.SCALE_DEFAULT)
      val bufferedImage = new BufferedImage(img.getWidth()/2, img.getHeight()/2,BufferedImage.TYPE_INT_RGB)
      bufferedImage.getGraphics.drawImage(resized,0,0,null)
      sender ! lastResize(bufferedImage,num)
      //println(s"EndActor sent image back. size ${bufferedImage.getWidth} * ${bufferedImage.getHeight}")

      
  }

}
