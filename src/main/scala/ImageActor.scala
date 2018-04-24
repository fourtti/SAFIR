import java.awt.image.BufferedImage

import akka.actor.{Actor, ActorRef}

object ImageActor {
  case class getRGB(bufferedImage: BufferedImage, x: Int, y: Int)
}
class ImageActor extends Actor {
  override def postStop(): Unit = {
    println(s"$self: I am stopped.")
    super.postStop()
  }
  import MainActor._
  import ImageActor._

  override def receive: Receive = {
    case getRGB(img,x,y) => {
      println(s"$self")
      val red = getRed(List(img.getRGB(x*2,y*2),img.getRGB(x*2+1,y*2), img.getRGB(x*2,y*2+1), img.getRGB(x*2+1,y*2+1)))
      val green = getGreen(List(img.getRGB(x*2,y*2),img.getRGB(x*2+1,y*2), img.getRGB(x*2,y*2+1), img.getRGB(x*2+1,y*2+1)))
      val blue = getBlue(List(img.getRGB(x*2,y*2),img.getRGB(x*2+1,y*2), img.getRGB(x*2,y*2+1), img.getRGB(x*2+1,y*2+1)))

      val color = (red * 65536) + (green * 256) + blue
      ActorRef ! returnRGB(color,)
    }
  }

  def getRed(list:List[Int]): Int ={
    var red = 0
    for(i <-list){
      red += (i & 0xff0000) / 65536
    }
    red/list.length
  }

  def getGreen(list:List[Int]): Int ={
    var green = 0
    for(i <-list){
      green += (i & 0xff00) / 256
    }
    green/list.length
  }

  def getBlue(list:List[Int]): Int ={
    var blue = 0
    for(i <-list){
      blue += (i & 0xff)
    }
    blue/list.length
  }
}