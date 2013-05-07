package adept.operations

import adept.models.Module
import org.apache.ivy.plugins.latest.LatestRevisionStrategy

private[adept] object Version {
  val specialChars = "[\\._\\-\\+]"
  
  val SpecialMeanings = Map( //As DEFAULT_SPECIAL_MEANINGS in LatestVersionStrategy
      "dev" -> -1,
      "rc" -> 1,
      "final" -> 2
  )
  
  /**
   Compares versions as in LatestVersionStrategy.MridComparator in Ivy.
   This is again simliar to the compare_version in PHP and works as described:
   The function first replaces _, - and + with a dot . in the version strings and also inserts dots . before and after any non number so that for example '4.3.2RC1' becomes '4.3.2.RC.1'. 
   Then it splits the results on the dot. 
   Then it compares the parts starting from left to right. 
   If a part contains special version strings these are handled in the following order: dev < any string not found in this list < rc < final as in the val SpecialMeanings 
   */
  def stringVersionCompare(v1: String, v2: String) = {
    
    def insertDotsAsArray(v: String) = 
      v.replaceAll("([a-zA-Z])(\\d)", "$1.$2")
       .replaceAll("(\\d)([a-zA-Z])", "$1.$2").split(specialChars)
    
    def isNumber(s: String) = s.matches("\\d+")
    
    val parts1 = insertDotsAsArray(v1)
    val parts2 = insertDotsAsArray(v2)
    
    val differentParts =  parts1.zip(parts2).dropWhile{ case (v1, v2) => v1 == v2 }
    
    val maybeResult = differentParts.collectFirst{ 
      case (v1, v2) if (isNumber(v1) && !isNumber(v2))=> 1
      case (v1, v2) if (!isNumber(v1) && isNumber(v2))=> -1
      case (v1, v2) if (isNumber(v1) && isNumber(v2))=> v1.compareTo(v2)
      case (v1, v2) if (SpecialMeanings.get(v1).isDefined || SpecialMeanings.get(v2).isDefined) =>
        (SpecialMeanings.get(v1), SpecialMeanings.get(v2)) match {
          case (Some(sm1), Some(sm2)) => {
            println(sm1 + " VS " + sm2)
            sm1.compareTo(sm2)
          }
          case (Some(sm1), None) => sm1.compareTo(0)
          case (None, Some(sm2)) => 0.compareTo(sm2)
          case somethingElse => throw new Exception("while comparing version got unexpected pair of versions: " +somethingElse)
        }
      case (v1, v2) => v1.compareTo(v2)
    }
    maybeResult.getOrElse{
    	  if (parts1.lastOption.isDefined && parts1.size > parts2.size) {
    	     if (isNumber(parts1.last)) 1
    	     else -1
    	  } else if (parts2.lastOption.isDefined && parts2.size > parts1.size){
    	    if (isNumber(parts2.last)) 1
    	     else -1
    	  } else {
    	    0
    	  }
    }
  }
}

private[adept] case class Version(private val value: String) extends Ordered[Version] {
  def compare(that: Version) = {
    Version.stringVersionCompare(this.value, that.value)
  }
}

private[adept] object Prune {
  
  //TODO:Today we are comparing dynamically, I am not sure this is a good idea or if we should compare statically (i.e. have a replaces for each modules)
  def apply(modules: Seq[Module]): Seq[Module] = {
	modules.groupBy(m => m.coords.org -> m.coords.name)
		   .map{ case (_, comparableModules) =>
	  comparableModules.maxBy(m => Version(m.coords.version))
	}.toSeq
  }
}