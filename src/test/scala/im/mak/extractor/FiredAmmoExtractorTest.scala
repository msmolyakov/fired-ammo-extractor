package im.mak.extractor

import java.io.{File, PrintWriter}
import java.nio.file.{Files, Paths}

import org.scalatest.FreeSpec
import org.scalatest.Matchers._

class FiredAmmoExtractorTest extends FreeSpec {
  import FiredAmmoExtractorTest._

  "isFileStartsFromHttpRequest" - {
    "should accept file that starts correctly" in {
      val file = tempFile(
        ammo("GET", "TEST_GET"),
        ammo("POST", "TEST_POST", "{\"key\": \"value\"}")
      )
      FiredAmmoExtractor.isFileStartsFromHttpRequest(file) shouldBe true
      file.deleteOnExit()
    }

    "should return error when file starts incorrectly" in {
      val file = tempFile(
        ammo("GET", "TEST_GET").replaceFirst("\\d+(\\W+.*)?\\R?", ""),
        ammo("POST", "TEST_POST", "{\"key\": \"value\"}")
      )
      FiredAmmoExtractor.isFileStartsFromHttpRequest(file) shouldBe false
      file.deleteOnExit()
    }
  }

  "ammoCount" - {
    "should calculate count of ammo in file" in {
      val file = tempFile(
        ammo("GET", "TEST_GET"),
        ammo("POST", "TEST_POST", "{\"key\": \"value\"}"),
        ammo("GET", "TEST_GET")
      )
      FiredAmmoExtractor.ammoCount(file) shouldBe 3
      file.deleteOnExit()
    }
  }

  "deleteAmmo" - {
    "should delete fired ammo from file to POST" in {
      val file = tempFile(
        ammo("GET", "TEST_GET"),
        ammo("POST", "TEST_POST", "{\"key\": \"value\"}"),
        ammo("GET", "TEST_GET"),
        ammo("POST", "TEST_POST", "{\"key\": \"value\"}"),
        ammo("GET", "TEST_GET")
      )
      FiredAmmoExtractor.deleteAmmo(file, 3) shouldBe 2
      Files.readAllBytes(Paths.get(file.getPath)) shouldEqual
        (ammo("POST", "TEST_POST", "{\"key\": \"value\"}")
          + ammo("GET", "TEST_GET")).getBytes
      file.deleteOnExit()
    }

    "should delete fired ammo from file to GET" in {
      val file = tempFile(
        ammo("GET", "TEST_GET"),
        ammo("POST", "TEST_POST", "{\"key\": \"value\"}"),
        ammo("GET", "TEST_GET"),
        ammo("POST", "TEST_POST", "{\"key\": \"value\"}"),
        ammo("GET", "TEST_GET")
      )
      FiredAmmoExtractor.deleteAmmo(file, 2) shouldBe 3
      Files.readAllBytes(Paths.get(file.getPath)) shouldEqual
        (ammo("GET", "TEST_GET")
          + ammo("POST", "TEST_POST", "{\"key\": \"value\"}")
          + ammo("GET", "TEST_GET")).getBytes
      file.deleteOnExit()
    }
  }

}

object FiredAmmoExtractorTest {

  def tempFile(lines: String*): File = {
    val file = File.createTempFile("extr-", ".txt")
    new PrintWriter(file) { write(lines.mkString("")); close() }
    file
  }

  def ammo(method: String, tag: String, body: String = ""): String = {
    var req: String = s"$method /api/path HTTP1.1\r\n"

    req += "Host: 127.0.0.1\r\n"
    req += "Accept: application/json\r\n"
    req += "Connection: close\r\n"
    req += "Content-Type: application/json\r\n"
    if (body.nonEmpty) {
      req += s"Content-Length: ${body.length}\r\n"
      req += "\r\n"
      req += s"$body"
    }
    req += "\r\n"

    s"${req.length} $tag\n" + req
  }

}