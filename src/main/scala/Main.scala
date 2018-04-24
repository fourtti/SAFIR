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
  val photo1 = ImageIO.read(new File("image5.jpg"))
  val photo2 = resize(photo1)

  val system = ActorSystem("MainActorSystem")
  //val MainImageActor = system.actorOf(Props[ MainActor ], "MainImage")

  val MainImageActor = system.actorOf(Props (new MainActor(4)), s"MainActor")
  MainImageActor ! (photo1)





  ImageIO.write(photo2,"jpg", new File("smallerImage.jpg"))




  def makeSmaller(img: BufferedImage,times: Int): BufferedImage = {
    val w = img.getWidth()
    val h = img.getHeight()

    BufferedImage.TYPE_INT_RGB
    //create new image that is 0.5 the size
    val outputImg = new BufferedImage(w/2,h/2,BufferedImage.TYPE_INT_RGB)
    val colorArray = new Array[Int]((w/2)*(h/2))

    var counter = 0


    counter = 0
    for(x <- 0 until w/2){
      for(y <- 0 until h/2){
        outputImg.setRGB(x,y,colorArray(counter)& 0xffffff)
        counter += 1
      }
    }

    outputImg
  }


  def resize(img: BufferedImage) = {
    val resized =  img.getScaledInstance(img.getWidth()/2, img.getHeight()/2,Image.SCALE_DEFAULT)
    val bufferedImage = new BufferedImage(img.getWidth()/2, img.getHeight()/2, BufferedImage.TYPE_INT_RGB)
    bufferedImage.getGraphics.drawImage(resized, 0, 0, null)
    bufferedImage
  }






}
