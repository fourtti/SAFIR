import java.io.File

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.awt.{Graphics2D, Image}





object Main extends App{
  println("Starting work")
  //val photo1 = ImageIO.read(new File("image5.jpg"))
  //val photo2 = makeSmaller(photo1)
  //val photo1 = ImageIO.read(new File("image.jpg"))
  //val photo2 = phototest(photo1)
  val photo1 = ImageIO.read(new File("image5.jpg"))
  val photo2 = resize(photo1)


  ImageIO.write(photo2,"jpg", new File("smallerImage0.jpg"))

  def makeSmaller(img: BufferedImage,times: Int): BufferedImage = {
    val w = img.getWidth()
    val h = img.getHeight()

    BufferedImage.TYPE_INT_RGB
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

  def resize(img: BufferedImage) = {
    val resized =  img.getScaledInstance(img.getWidth()/2, img.getHeight()/2,Image.SCALE_DEFAULT)
    val bufferedImage = new BufferedImage(img.getWidth()/2, img.getHeight()/2, BufferedImage.TYPE_INT_RGB)
    bufferedImage.getGraphics.drawImage(resized, 0, 0, null)
    bufferedImage
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

  def imageToChunks(img: BufferedImage,rows:Int, cols:Int): Array[BufferedImage] = {
    //total amount of chunks. Determines the size of the array returned
    val chunkCount = rows * cols
    //width and height of each chunk
    val chunkWidth = img.getWidth() / cols
    val chunkHeight = img.getHeight() / rows
    val chunkArray = new Array[BufferedImage](chunkCount)

    //counter for inputing chunks to the array
    var counter = 0
    for(x <- 0 until rows){
      for(y <- 0 until cols){

        // intitialize new imagechunks in array
        chunkArray(counter) = new BufferedImage(chunkWidth,chunkHeight,BufferedImage.TYPE_INT_RGB)

        //draw the image to the imagechunk
        val graphics = chunkArray(counter).createGraphics()
        graphics.drawImage(img,0,0,chunkWidth,chunkHeight,chunkWidth*y,chunkHeight*x,chunkWidth * y + chunkWidth, chunkHeight * x + chunkHeight,null)
        graphics.dispose()

        //adding to counter
        counter += 1
      }
    }
    chunkArray
  }

}
