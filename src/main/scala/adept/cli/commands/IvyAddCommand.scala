package adept.cli.commands

import adept.Adept
import java.io.File
import adept.models.Coordinates
import adept.ivy.IvyHelpers

object IvyAddCommand extends Command {
  override val command = "ivy-add"
  override val shortDescription = "add using ivy"
  
  override def execute(args: List[String]): Either[String, String] ={
    val dir = Defaults.dir
    val name = Defaults.name
    
    val coordsArg = args.drop(0).headOption.toRight("could not find coords argument")
    val settingsArg = args.drop(1).headOption
    
    for{
      adept <- Adept.open(dir, name).right
      coordsString <- coordsArg.right
      coords <- Coordinates.parse(coordsString).right
      ivy <- IvyHelpers.load(settingsArg).right
    } yield{
      IvyHelpers.add(coords, ivy, adept).mkString("\n")
    }
  }
  
}