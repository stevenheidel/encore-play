// Retrieved from: http://lifelongprogrammer.blogspot.ca/2013/11/java-use-zip-stream-and-base64-to-compress-big-string.html

package utils;

import java.io.*;
import java.util.zip.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.codec.binary.Base64;

public class StringCompression {
  /**
  * At server side, use ZipOutputStream to zip text to byte array, then convert
  * byte array to base64 string, so it can be trasnfered via http request.
  */
  public static String compress(String srcTxt)
      throws IOException {
    ByteArrayOutputStream rstBao = new ByteArrayOutputStream();
    GZIPOutputStream zos = new GZIPOutputStream(rstBao);
    zos.write(srcTxt.getBytes());
    IOUtils.closeQuietly(zos);

    byte[] bytes = rstBao.toByteArray();
    // In my solr project, I use org.apache.solr.co mmon.util.Base64.
    // return = org.apache.solr.common.util.Base64.byteArrayToBase64(bytes, 0,
    // bytes.length);
    return Base64.encodeBase64String(bytes);
  }

  /**
   * When client receives the zipped base64 string, it first decode base64
   * String to byte array, then use ZipInputStream to revert the byte array to a
   * string.
   */
  public static String uncompress(String zippedBase64Str)
      throws IOException {
    String result = null;
     
    // In my solr project, I use org.apache.solr.common.util.Base64.
    // byte[] bytes =
    // org.apache.solr.common.util.Base64.base64ToByteArray(zippedBase64Str);
    byte[] bytes = Base64.decodeBase64(zippedBase64Str);
    GZIPInputStream zi = null;
    try {
      zi = new GZIPInputStream(new ByteArrayInputStream(bytes));
      result = IOUtils.toString(zi);
    } finally {
      IOUtils.closeQuietly(zi);
    }
    return result;
  }
}
