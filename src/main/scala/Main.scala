import java.io.File

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.awt.{Graphics2D, Image}

import akka.actor.{ActorSystem, Props}
import akka.cluster.Cluster
import com.typesafe.config.ConfigFactory


object Main extends App{

  //loading conf file and creating a ActorSystem for the node
  val config = ConfigFactory.load()
  val system = ActorSystem("ImageResizingCluster",config)

  //To run the nodes:
  //seed
  //java -DPORT=2551 -Dconfig.resource=/seed.conf -jar INSERT NAME HERE.jar
  //worker
  //java -DPORT=2553 -Dconfig.resource=/worker.conf -jar INSERT NAME HERE.jar
  //master
  //java -DPORT=2557 -Dconfig.resource=/master.conf -jar INSERT NAME HERE.jar
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

      //Wait a little time so that the router can create the routees
      Thread.sleep(1000)
      //getting photo from project folder
      val photo1 = ImageIO.read(new File("image.jpg"))
      //sending image to mainActor for resizing
      mainActor ! initialSplit(photo1)

    }
  }
}
