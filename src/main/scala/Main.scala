import java.io.File

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

import akka.actor.{ActorSystem, Props}





object Main extends App{
  val photo1 = ImageIO.read(new File("image.jpg"))
  val photo2 = makeSmaller(photo1)
  //val photo1 = ImageIO.read(new File("image.jpg"))
  //val photo2 = phototest(photo1)

  val system = ActorSystem("MainActorSystem")
  val MainImageActor = system.actorOf(Props[ MainActor ], "MainImage")



  ImageIO.write(photo2,"jpg", new File("smallerImage.jpg"))

  def makeSmaller(img: BufferedImage): BufferedImage = {
    val w = img.getWidth()
    val h = img.getHeight()


    //create new image that is 0.5 the size
    val outputImg = new BufferedImage(w/2,h/2,BufferedImage.TYPE_INT_RGB)
    val colorArray = new Array[Int]((w/2)*(h/2))

    var counter = 0
    for(x <- 0 until w/2){
      for(y <- 0 until h/2) {


        colorArray(counter) = (red * 65536) + (green * 256) + blue
        counter += 1
      }
    }

    counter = 0
    for(x <- 0 until w/2){
      for(y <- 0 until h/2){
        outputImg.setRGB(x,y,colorArray(counter)& 0xffffff)
        counter += 1
      }
    }

    outputImg
  }



}
