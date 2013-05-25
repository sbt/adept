package adept.cli.commands.module

import adept.cli.commands.Command
import adept.models._

object ModuleAddDependencyCommand extends Command with JsonFileSystemModulePersistance {

  val command = "add-dependency"
  val shortDescription = "add artifact to current module"

  def execute(args: List[String]): CommandResult = {
    for {
      dependency <- parseArgs(args).right
    } yield {
      updateModule(dependency)
      None
    }
  }

  def updateModule(dep: Dependency) {
    updatePersistedModule { module =>
      module.copy(
        dependencies = module.dependencies + dep
      )
    }
  }

  def parseArgs(args: List[String]) = {
    val (coordsStr :: hash :: config :: Nil) = args

    for {
      coords <- Coordinates.parse(coordsStr).right
    } yield {
      Dependency(coords, Hash(hash), config)
    }
  }
}
