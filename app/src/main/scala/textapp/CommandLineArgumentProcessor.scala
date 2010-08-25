package textapp


class CommandLineArgumentProcessor(args: Array[String]) {
  def getParam(longName: String, shortName: String): Option[String] = {
    val longFormat = "--%s".format(longName)
    val shortFormat = "-%s".format(shortName)

    val paramsWithValues = argPairs.flatMap { case (first, second) =>
      if (first == longFormat || first == shortFormat) {
        if (!isParam(second)) Some(second)
        else None
      }
      else None
    }.toList

    val paramsWithoutValues = args.flatMap { arg =>
      if (arg == longFormat || arg == shortFormat) Some("")
      else None
    }.toList
    (paramsWithValues ::: paramsWithoutValues).headOption
  }

  private def argPairs = args.zip(args.drop(1))
  private def isParam(s: String) = s.startsWith("-")
}