package wt.fv.replica;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import wt.fv.uploadtocache.EncodingConverter;

////////////////////////////////////////////////////////////////////////////
/**
* The class contains a utility method for converting a
* <code>String</code> into a MIME format called
* "<code>x-www-form-urlencoded</code>" and vice versa.
* <p>
* To convert a <code>String</code>, each character is examined in turn:
* <ul>
* <li>The ASCII characters '<code>a</code>' through '<code>z</code>',
*     '<code>A</code>' through '<code>Z</code>', and '<code>0</code>'
*     through '<code>9</code>' remain the same.
* <li>The space character '<code>&nbsp;</code>' is converted into a
*     plus sign '<code>+</code>'.
* <li>All other characters are converted into an 8-bit byte sequence using
*     a character encoding and the resulting bytes are each represented as a
*     3-character string "<code>%<i>xy</i></code>", where <i>xy</i> is the
*     two-digit hexadecimal representation of the byte.
* </ul>
* <P><BR>
* <B>Deployment Notes:</B>
* <BR><UL>
*     WTURLEncoder has been replaced by <i>wt.httpgw.EncodingConverter</i>
*     For Windchill R6.0+.  This encoder is instance based and is more scalable
*     in that it allows simultaenous encoding and decoding to be performed
*     by multiple threads in the same Java VM.  This class should not be
*     used for any new development and this class will disappear after R6.0
*     deployment. (JAD)
*  </UL>
* <BR><BR><B>Deprecated</B>
* @deprecated
* @see wt.httpgw.EncodingConverter EncodingConverter
* <BR><BR><B>Supported API:</B> false
* <BR><B>Extendable:</B> false
*/
////////////////////////////////////////////////////////////////////////////
public class WTURLEncoder extends ByteArrayOutputStream
{
	 private static final ByteArrayOutputStream  convertBuf = new ByteArrayOutputStream(EncodingConverter.MAX_BYTES_PER_CHAR);
   private static final OutputStreamWriter  defaultWriter;

   // The list of characters that are not encoded is taken from Sun's implementation
   static
   {
      OutputStreamWriter  osw;
      try
      {
         osw = new OutputStreamWriter(convertBuf, EncodingConverter.UTF8);
      }
      catch (Exception e)
      {
         e.printStackTrace(); // Ignore
         osw = new OutputStreamWriter(convertBuf);
      }
      defaultWriter = osw;
   }

   // Static instance of the decoder.
   private static final EncodingConverter  cvDecoder = new EncodingConverter( );

   // Static instance of the new encoder
   //private static EncodingConverter cvEncoder = new EncodingConverter( );

   // No instance methods
   private WTURLEncoder()
   {
      super();
   }

   ////////////////////////////////////////////////////////////////////////////
   /**
   * Translates a string into <code>x-www-form-urlencoded</code> format.
   * Hex escaped characters are first encoded using UTF8 character encoding
   * before Hex escaping the resulting bytes.  This produces an encoded
   * string that can be decoded without loss of data.
   *
   * @deprecated
   * @see wt.httpgw.EncodingConverter.encode(String)
   * @param s <code>String</code> to be translated.
   * @return the translated <code>String</code>.
   */
   ////////////////////////////////////////////////////////////////////////////
   public static String encode (String s)
   {
      // To be implemented post R6.0
      // StringBuffer buf = new StringBuffer(s.length() << 1);
      // synchronized( cvEncoder )
      // {
      //   return cvEncoder.encode(buf, s, null );
      // }
      // return buf.toString();
      return encode(s, (String)null);
   }


   ////////////////////////////////////////////////////////////////////////////
   /**
   * Translates a string into <code>x-www-form-urlencoded</code> format.
   * Hex escaped characters are first encoded using the given character encoding
   * before Hex escaping the resulting bytes.
   * @deprecated
   * @see wt.httpgw.EncodingConverter.encode(String,String)
   * @param s <code>String</code> to be translated.
   * @param encoding the character encoding name
   * @return the translated <code>String</code>.
   */
   ////////////////////////////////////////////////////////////////////////////
   public static String encode (String s, String encoding)
   {
      StringBuffer buf = new StringBuffer(s.length() << 1);
      // To be implemented post R6.0
      // synchronized( cvEncoder )
      // {
      //    cvEncoder.encode(buf,s,encoding);
      // }
      encode(buf, s, encoding);
      return buf.toString();
   }

   ////////////////////////////////////////////////////////////////////////////
   /**
   * Translates a string into <code>x-www-form-urlencoded</code> format.
   * Hex escaped characters are first encoded using UTF8 character encoding
   * before Hex escaping the resulting bytes.  This produces an encoded
   * string that can be decoded without loss of data.
   *
   * @deprecated
   * @see wt.httpgw.EncodingConverter.encode(StringBuffer,String)
   * @param buf <code>StringBuffer</code> to receive encoded characters.
   * @param s <code>String</code> to be translated.
   */
   ////////////////////////////////////////////////////////////////////////////
   public static void encode (StringBuffer buf, String s)
   {
      // To be implemented post R6.0
      // synchronized( cvEncoder )
      // {
      //    cvEncoder.encode(buf,s,null);
      // }
      encode(buf, s, (String)null);
   }

   ////////////////////////////////////////////////////////////////////////////
   /**
   * Translates a string into <code>x-www-form-urlencoded</code> format.
   * Hex escaped characters are first encoded using the given character encoding
   * before Hex escaping the resulting bytes.
   *
   * @deprecated
   * @see wt.httpgw.EncodingConverter.encode(StringBuffer,String,String)
   * @param buf <code>StringBuffer</code> to receive encoded characters.
   * @param s <code>String</code> to be translated.
   * @param encoding the character encoding name
   */
   ////////////////////////////////////////////////////////////////////////////
   public static void encode (StringBuffer buf, String s, String encoding)
   {
      // To be implemented at post R6.0
      // synchronized( cvEncoder )
      // {
      //    cvEncoder.encode( buf, s, encoding );
      // }


      //////////////////////////////////////////////////////////////////////
      //
      // This could MUST remain in WTURLEncoder for R6.0 Release
      // The code implements a synchronization block around the output
      // buffer only when there are non-ASCII characters.  For Post R6.0
      // the EncodingConverter will be instance based, so this isn't really
      // an issue, but WTURLEncoder uses static buffers.  For performance
      // at R6.0 Release this could should remain
      //
      ///////////////////////////////////////////////////////////////////////
	   OutputStreamWriter writer = null;
      boolean utf8 = (encoding == null) || encoding.equals(EncodingConverter.UTF8);

      for (int i = 0; i < s.length(); i++)
      {
         char c = s.charAt(i);
         if (EncodingConverter.dontNeedEncoding[c])
         {
            if (c == ' ')
               c = '+';
            buf.append(c);
         }
         else
         {
            // convert to external encoding before hex conversion
            if (utf8)
            {
               // Optimization
               if ((c >= 0x0001) && (c <= 0x007F))
               {
                  buf.append('%');
                  buf.append(EncodingConverter.toHex[c >> 4 & 0xF]);
                  buf.append(EncodingConverter.toHex[c & 0xF]);
                  continue;
               }
            }
            byte[] ba;
            synchronized (convertBuf)
            {
               if (writer == null)
               {
                  if (encoding != null)
                  {
                     try
                     {
                        writer = new OutputStreamWriter(convertBuf, encoding);
                     }
                     catch (Exception e)
                     {
                        e.printStackTrace(); // Ignore
                     }
                  }
                  if (writer == null)
                     writer = defaultWriter;
               }
               convertBuf.reset();
               try
               {
                  writer.write(c);
               }
               catch(IOException e)
               {
                  continue;
               }
               finally
               {
                  try
                  {
                     writer.flush();
                  }
                  catch (IOException e)
                  {
                     continue;
                  }
               }
               ba = convertBuf.toByteArray();
            }
            for (int j = 0; j < ba.length; j++)
            {
               buf.append('%');
               buf.append(EncodingConverter.toHex[ba[j] >> 4 & 0xF]);
               buf.append(EncodingConverter.toHex[ba[j] & 0xF]);
            }
         }
      }
   }

   ////////////////////////////////////////////////////////////////////////////
   /**
   * Translates a string from <code>x-www-form-urlencoded</code> format back
   * into a string.  Hex escaped bytes are converted and the resulting sequence of 8-bit
   * values is converted to characters using UTF8 encoding.  This produces a decoded
   * string from the results of <code>encode</code> without loss data.
   *
   * @see wt.httpgw.EncodingConverter.deocde(String)
   * @param s <code>String</code> to be translated.
   * @return the translated <code>String</code>.
   */
   ////////////////////////////////////////////////////////////////////////////
   public static synchronized String decode (String s)
   {
      return cvDecoder.decode(s, (String)null);
   }

   ////////////////////////////////////////////////////////////////////////////
   /**
   * Translates a string from <code>x-www-form-urlencoded</code> format back the original
   * string.  Hex escaped bytes are converted and the resulting sequence of 8-bit values
   * is converted to characters using the given character encoding.
   *
   * @see wt.httpgw.EncodingConverter.deocde(String,String)
   * @param encoded <code>String</code> to be translated.
   * @param encoding the character encoding name
   * @return the translated <code>String</code>.
   */
   ////////////////////////////////////////////////////////////////////////////
   public static synchronized String decode (String encoded, String encoding)
   {
      return cvDecoder.decode(encoded,encoding);
   }
}
