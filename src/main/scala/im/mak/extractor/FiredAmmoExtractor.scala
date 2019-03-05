package im.mak.extractor

import java.io.{File, FileWriter}
import java.nio.file.Files

import scala.io.Source
import scala.util.control.NonFatal
import scala.util.control.Breaks.{breakable, break}

object FiredAmmoExtractor {
  val sizeLinePattern = "\\d+(\\W+.*)?"
  val methodLinePattern = "(GET|POST|PUT|DELETE)\\W.*"

  def parse(args: Array[String]): (File, Int) = {
    if (args.length == 0 || args.length > 2) {
      println("java -jar delreq.jar [file_path]\t\t\tReturns the number of ammo in the file")
      println("java -jar delreq.jar [file_path] [ammo_count]\tRemoves the number of ammo from the beginning of the file. Returns the number of remaining ammo in the file")
      System.exit(0)
    }

    var file = None: Option[File]
    var count = None: Option[Int]

    try {
      file = Some(new File(args(0)))
      if (args.length > 1)
        count = if (args(1).toInt < 1) Some(0) else Some(args(1).toInt)
      else count = Some(0)
    } catch {
      case e: NumberFormatException =>
        println("Ammo count must be a positive number")
        e.printStackTrace()
        System.exit(1)
      case NonFatal(e) =>
        e.printStackTrace()
        System.exit(1)
    }

    (file.get, count.get)
  }

  def isFileStartsFromHttpRequest(file: File): Boolean = {
    val fileReader = Source.fromFile(file).bufferedReader
    val (sizeLine, methodLine) = (fileReader.readLine, fileReader.readLine)
    val isFileCorrect = sizeLine.matches(sizeLinePattern) || methodLine.matches(methodLinePattern)

    fileReader.close()
    isFileCorrect
  }

  def deleteAmmo(file: File, count: Int): Int = {
    val reader = Source.fromFile(file).bufferedReader
    val tempFile = new File(file.getAbsolutePath + ".tmp")
    val writer = new FileWriter(tempFile)
    var skipped = 0
    var remaining = 0
    var line: String = null

    breakable { while ({line = reader.readLine; line != null}) {
      if (line.matches(sizeLinePattern)) {
        reader.mark(0)
        line = reader.readLine
        if (line.matches(methodLinePattern)) {
            if (skipped < count)
              skipped += 1
            else break
        }
      }
    }}

    reader.reset()
    while ({line = reader.readLine; line != null}) {
      if (line.matches(methodLinePattern))
        remaining += 1
      writer.write(line)
    }

    reader.close()
    Files.delete(file.toPath)
    writer.close()
    tempFile.renameTo(file)

    remaining
  }

  def ammoCount(file: File): Int = {
    val reader = Source.fromFile(file).bufferedReader
    var count = 0
    var line: String = null

    while ({line = reader.readLine; line != null}) {
      if (line.matches(sizeLinePattern)) {
        line = reader.readLine
        if (line.matches(methodLinePattern)) {
          count += 1
        }
      }
    }

    reader.close()
    count
  }
}
