import java.io.File
import javax.imageio.ImageIO
import java.awt.image.BufferedImage





object Main extends App{
  val photo1 = ImageIO.read(new File("image5.jpg"))
  val photo2 = makeSmaller(photo1)
  //val photo1 = ImageIO.read(new File("image.jpg"))
  //val photo2 = phototest(photo1)


  ImageIO.write(photo2,"jpg", new File("smallerImage5.jpg"))

  def makeSmaller(img: BufferedImage): BufferedImage = {
    val w = img.getWidth()
    val h = img.getHeight()


    //create new image that is 0.5 the size
    val outputImg = new BufferedImage(w/2,h/2,BufferedImage.TYPE_INT_RGB)
    val colorArray = new Array[Int]((w/2)*(h/2))

    var counter = 0
    for(x <- 0 until w/2){
      for(y <- 0 until h/2) {

        val red = getRed(List(img.getRGB(x*2,y*2),img.getRGB(x*2+1,y*2), img.getRGB(x*2,y*2+1), img.getRGB(x*2+1,y*2+1)))
        val green = getGreen(List(img.getRGB(x*2,y*2),img.getRGB(x*2+1,y*2), img.getRGB(x*2,y*2+1), img.getRGB(x*2+1,y*2+1)))
        val blue = getBlue(List(img.getRGB(x*2,y*2),img.getRGB(x*2+1,y*2), img.getRGB(x*2,y*2+1), img.getRGB(x*2+1,y*2+1)))

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
