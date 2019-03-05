package im.mak.extractor

import FiredAmmoExtractor._

object Main extends App {

  var (file, count) = parse(args)

  if (!isFileStartsFromHttpRequest(file)) {
    println("Incorrect start of the file!")
    System.exit(1)
  }

  if (count > 0)
    println(deleteAmmo(file, count))
  else println(ammoCount(file))

}
