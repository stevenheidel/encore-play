package utils

import java.io._
import java.util.zip._
import org.apache.commons.io.IOUtils
import org.apache.commons.codec.binary.Base64

object GZipHelper {

  def deflate(txt: String): String = {
    val arrOutputStream = new ByteArrayOutputStream()
    val zipOutputStream = new GZIPOutputStream(arrOutputStream)

    zipOutputStream.write(txt.getBytes)
    IOUtils.closeQuietly(zipOutputStream)
    
    Base64.encodeBase64String(arrOutputStream.toByteArray)
  }

  def inflate(deflatedTxt: String): String = {
    val bytes = Base64.decodeBase64(deflatedTxt)
    val zipInputStream = new GZIPInputStream(new ByteArrayInputStream(bytes))
    
    val result = IOUtils.toString(zipInputStream)
    IOUtils.closeQuietly(zipInputStream)

    result
  }
}
