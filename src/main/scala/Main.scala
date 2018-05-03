import java.awt.image.BufferedImage
import java.io.{ByteArrayInputStream, ByteArrayOutputStream, File}

import javax.imageio.ImageIO
import akka.actor.{ActorSystem, Props}
import akka.cluster.Cluster
import com.typesafe.config.ConfigFactory
import ImageByteConverter._


object Main extends App{

  //loading conf file and creating a ActorSystem for the node
  val config = ConfigFactory.load()
  val system = ActorSystem("ImageResizingCluster",config)

  //To run the nodes:
  //seed
  //java -DPORT=2551 -Dconfig.resource=/seed.conf -jar safir.jar
  //worker
  //java -DPORT=2553 -Dconfig.resource=/worker.conf -jar safir.jar
  //master
  //java -DPORT=2557 -Dconfig.resource=/master.conf -jar safir.jar
  // ports 2551 and 2552 are reserved for seeds. Other ports can be used freely


  //This is only run if node being run is a master node
  if(system.settings.config.getStringList("akka.cluster.roles").contains("master")){

    //this function is called when the clustersystem is operational.
    //Which means that that the required nodes are operational.
    //Nodes and their minimum amount is defined in the configuration.
    Cluster(system).registerOnMemberUp{
      //import Mainactor companion object for initialsplit message type
      import MainActor._

      //Creating a MainImageActor, which holds the router.
      val mainActor =  system.actorOf(Props(new MainActor(4)),"MainImageActor")


      //getting photo from project folder
      println("Image Sent to main actor")
      val photo1 = ImageIO.read(new File("image.jpg"))

      //Wait a little time so that the router can create the routees
      Thread.sleep(3000)


      //sending image(converted to ByteArray) to mainActor for resizing
      mainActor ! initialSplit(convertBufferToByteArray(photo1))

    }
  }
}

object ImageByteConverter{
  //converts imageBuffer to Byte array
  //needed as java serializer cant serialize a remote message that includes a BufferedImage
  def convertBufferToByteArray(img: BufferedImage): Array[Byte] = {
    // convert BufferedImage to byte array
    val baos = new ByteArrayOutputStream
    ImageIO.write(img, "jpg", baos)
    baos.flush()
    val imageInByte = baos.toByteArray
    baos.close()
    imageInByte
  }

  //converts a ByteArray to a BufferedImage(jpg)
  def convertToBufferedImage(byteArr: Array[Byte]): BufferedImage = {
    val in = new ByteArrayInputStream(byteArr)
    val bImageFromConvert = ImageIO.read(in)
    bImageFromConvert
  }

}