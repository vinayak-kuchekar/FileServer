/* bcwti
 *
 * Copyright (c) 2010 Parametric Technology Corporation (PTC). All Rights Reserved.
 *
 * This software is the confidential and proprietary information of PTC
 * and is subject to the terms of a software license agreement. You shall
 * not disclose such confidential information and shall use it only in accordance
 * with the terms of the license agreement.
 *
 * ecwti
 */
package wt.fv.uploadtocache;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import org.apache.logging.log4j.Logger;


////////////////////////////////////////////////////////////////////////////
/**
* The class contains utility methods for converting a
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
*     The EncodingConverter is a replacement for WTURLEncoder and is instance
*     based.  The EncodingConverter should not be shared amongst threads or
*     used as a static instance unless appropriate synchronization blocks are
*     used to protect the internal buffers from overrighting.  This can be done
*     one of two ways.  Firstly by declaring the method which calls the
*     encode/decode methods as synchronized, or by placing a syncrhonized block
*     around the actually calls to encode/decode as shown below.<P><BR><BR><code>
*     <UL>static EncodingConverter staticEncoder = new EncodingConverter();<BR><BR>
*     public void sampleMethod(String s)<BR>
*     {<BR><UL>
*        synchronized( staticEncoder )<BR>
*        {<BR><UL>
*           staticEncoder.encode(s);<BR>
*        </UL>}<BR>
*     </UL>}</UL></CODE><BR>
*  </UL>
* <BR><BR><B>Supported API:</B> true
* <BR><B>Extendable:</B> false
*/
////////////////////////////////////////////////////////////////////////////
public class EncodingConverter extends ByteArrayOutputStream
{
   private static final Logger logger = null;
   ///////////////////////////////////////////////////////////////////////////
   //
   // Constants and Statics
   //
   ///////////////////////////////////////////////////////////////////////////

	// public so WTURLEncoder can still use it.
   public static final char[] toHex = {
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
   };
   /** Array of characters that don't require encoding public so
    * WTURLEncoder can use it.*/
   public static boolean[] dontNeedEncoding;  // should probably be a BitSet like in JDK!  [but used in WTURLEncoder so I'm leaving it for now...]
   /** Constnat for Maximum bytes per character */
	 public static final int MAX_BYTES_PER_CHAR = 10;	// public so WTURLEncoder can still use it.
   /** Constant for UTF8 encoding used by the Java Language */
   public static final String UTF8 = "UTF8";

   // The list of characters that are not encoded is taken from Sun's implementation
   static
   {
      dontNeedEncoding = new boolean[Character.MAX_VALUE];  // if this were a bit set we'd only need to size this at 256 as JDK does...
      int i;
      for (i = 'a'; i <= 'z'; i++)
         dontNeedEncoding[i] = true;
      for (i = 'A'; i <= 'Z'; i++)
         dontNeedEncoding[i] = true;
      for (i = '0'; i <= '9'; i++)
         dontNeedEncoding[i] = true;
      dontNeedEncoding[' '] = true; /* encoding a space to a + is done in the encode() method */
      dontNeedEncoding['-'] = true;
      dontNeedEncoding['_'] = true;
      dontNeedEncoding['.'] = true;
      dontNeedEncoding['*'] = true;
   }


   ////////////////////////////////////////////////////////////////////////////
   //
   // Instance Variables
   //
   ////////////////////////////////////////////////////////////////////////////
   /** The EncodingConverter's Encoding */
   private String ivEncoding = UTF8;
   /** The default output stream writer */
   private OutputStreamWriter defaultWriter = null;


   ////////////////////////////////////////////////////////////////////////////
   /**
    * Consturctor to create a new EncodingConverter with the encoding set to
    * UTF-8.
    * <BR><BR><B>Supported API:</B> true
    */
   ////////////////////////////////////////////////////////////////////////////
   public EncodingConverter( )
   {
      super( MAX_BYTES_PER_CHAR );
      setupBuffersAndWriters( );
   }

   ////////////////////////////////////////////////////////////////////////////
   /**
    * Constructor to create a new EncodingConverter with an encoding set to
    * UTF-8 and a specified buffer capacity.
    * <BR><BR><B>Supported API:</B> true
    * <P>
    * @param   ByteSize The buffer capacity.
    */
   ////////////////////////////////////////////////////////////////////////////
   public EncodingConverter(int ByteSize )
   {
      super( ByteSize );
      setupBuffersAndWriters( );
   }

   ////////////////////////////////////////////////////////////////////////////
   /**
    * Constructor to create a new EncodingConverter with a specified encoding.
    *
    * <BR><BR><B>Supported API:</B> true
    *
    * @param   encoding The encoding to use for encode/decoding.
    */
   ////////////////////////////////////////////////////////////////////////////
   public EncodingConverter(String encoding)
   {
      super( MAX_BYTES_PER_CHAR );
      ivEncoding = encoding;
      setupBuffersAndWriters();
   }

   ////////////////////////////////////////////////////////////////////////////
   /**
    * Internal method used to create the writers for the class
    *
    * <BR><BR><B>Supported API:</B> false
    */
   ////////////////////////////////////////////////////////////////////////////
   private void setupBuffersAndWriters( )
   {
      try
      {
         defaultWriter = new OutputStreamWriter(this, ivEncoding);
      }
      catch (Exception e)
      {
         logger.error((CharSequence)null, e );  // Ignore
         defaultWriter = new OutputStreamWriter(this);
      }
   }

   ////////////////////////////////////////////////////////////////////////////
   /**
    * Translates a string into <code>x-www-form-urlencoded</code> format.
    * Hex escaped characters are first encoded using UTF8 character encoding
    * before Hex escaping the resulting bytes.  This produces an encoded
    * string that can be decoded without loss of data.
    * <BR><BR><B>Thread Safe:</B> true (not if shared between thread)
    * <BR><BR><B>Supported API:</B> true
    * @param s <code>String</code> to be translated.
    * @return the translated <code>String</code>.
    */
   ////////////////////////////////////////////////////////////////////////////
   public String encode (String s)
   {
      return encode(s, (String)null);
   }

   ////////////////////////////////////////////////////////////////////////////
   /**
    * Translates a string into <code>x-www-form-urlencoded</code> format.
    * Hex escaped characters are first encoded using the given character encoding
    * before Hex escaping the resulting bytes.
    *
    * <BR><BR><B>Thread Safe:</B> true (not if shared between thread)
    * <BR><BR><B>Supported API:</B> true
    * @param s <code>String</code> to be translated.
    * @param encoding the character encoding name
    * @return the translated <code>String</code>, or "" if the string s was null.
    */
   ////////////////////////////////////////////////////////////////////////////
   public String encode (String s, String encoding)
   {
      if ( s != null )
      {
         StringBuffer buf = new StringBuffer(s.length() << 1);
         encode(buf, s, encoding);
         return buf.toString();
      }
      return "";
   }

   ////////////////////////////////////////////////////////////////////////////
   /**
   * Translates a string into <code>x-www-form-urlencoded</code> format.
   * Hex escaped characters are first encoded using UTF8 character encoding
   * before Hex escaping the resulting bytes.  This produces an encoded
   * string that can be decoded without loss of data.
   *
   * <BR><BR><B>Thread Safe:</B> true (not if shared between thread)
   * <BR><BR><B>Supported API:</B> true
   * @param buf <code>StringBuffer</code> to receive encoded characters.
   * @param s <code>String</code> to be translated.
   */
   ////////////////////////////////////////////////////////////////////////////
   public void encode (StringBuffer buf, String s)
   {
      encode(buf, s, (String)null);
   }

   ////////////////////////////////////////////////////////////////////////////
   /**
    * Translates a string into <code>x-www-form-urlencoded</code> format.
    * Hex escaped characters are first encoded using the given character encoding
    * before Hex escaping the resulting bytes.
    *
    * <BR><BR><B>Thread Safe:</B> true (not if shared between thread)
    * <BR><BR><B>Supported API:</B> true
    * @param buf <code>StringBuffer</code> to receive encoded characters.
    * @param s <code>String</code> to be translated.
    * @param encoding the character encoding name
    */
   ////////////////////////////////////////////////////////////////////////////
   public void encode (StringBuffer buf, String s, String encoding)
   {
	   OutputStreamWriter writer = null;
      boolean utf8 = (encoding == null) || encoding.equals("UTF8");
      if ( s == null )
      {
         return;
      }
      for (int i = 0; i < s.length(); i++)
      {
         char c = s.charAt(i);
         if (dontNeedEncoding[c])
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
                  buf.append(toHex[c >> 4 & 0xF]);
                  buf.append(toHex[c & 0xF]);
                  continue;
               }
            }
            byte[] ba;
            // synchronized( this )
            // {
               if (writer == null)
               {
                  if (encoding != null)
                  {
                    try
                    {
                        writer = new OutputStreamWriter(this, encoding);
                    }
                    catch (Exception e)
                    {
                        logger.error((CharSequence)null, e ); // Ignore
                        writer = defaultWriter;
                     }
                  }
                  // If the encoding == null then we need to use the default
                  // writer.
                  else
                  {
                     writer = defaultWriter;
                  }
               }
               this.reset();
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
                     if ( writer != null )
                     {
                        writer.flush();
                     }
                  }
                  catch (IOException e)
                  {
                     continue;
                  }
               }
            // } // end synch
            ba = this.toByteArray();
            for (int j = 0; j < ba.length; j++)
            {
               buf.append('%');
               buf.append(toHex[ba[j] >> 4 & 0xF]);
               buf.append(toHex[ba[j] & 0xF]);
            }
         }
      }
   }


   ////////////////////////////////////////////////////////////////////////////
   /**
    * Escape special characters in accordance with URL path segment encoding
    * requirements.
    * Replaces spaces with %20 and double quotes with %22.  In theory, all special
    * characters
    * should be URLEncoded, but the 4.x browsers don't interpret the file name correctly
    * using their native character encoding when that is done.
    *
    * <BR><BR><B>Supported API:</B> true
    *
    * @param str <code>String</code> to escape.
    * @return Escaped string value.
    */
   ///////////////////////////////////////////////////////////////////////////

   public static String  escape( String str )
   {
     EncodingConverter  encoder = new EncodingConverter();  // UTF-8 encoder
     return ( encoder.escape( str, encoder ) );
   }

   // ' ' must be changed to "%20"; other characters listed must be left as is
   private static final char  cannotEscapeByEncodingChars[] = { ' ', '/' };

   public static String  escape( String str, EncodingConverter encoder )
   {
     int  strLength = str.length();

     // if 'str' does not contain any of 'cannotEscapeByEncodingChars', then just encode it
     int  nextIdx = getNextIndexOf( str, strLength, 0, cannotEscapeByEncodingChars );
     if ( nextIdx == -1 )
       return ( encoder.encode( str ) );

     // if 'str does contain some 'cannotEscapeByEncodingChars', then handle these specially and encode remaining characters
     // [Note that spaces could safely left as is and then converted from +'s to %20's, but the other characters
     // cannot be left alone as these turn in %xx sequences which could also result from non-ASCII character bytes.]
     StringBuffer  buf = new StringBuffer( strLength );
     for (  int prevIdx = 0; ; )
     {
       String strToEncode = ( ( nextIdx >= 0 ) ? str.substring( prevIdx, nextIdx ) : str.substring( prevIdx ) );
       if ( strToEncode.length() > 0 )
         encoder.encode( buf, strToEncode );
       if ( nextIdx < 0 )
         break;  // we just encoded the remainder of the string
       char  trulySpecialChar = str.charAt( nextIdx );
       if ( trulySpecialChar == ' ' )
         buf.append( "%20" );  // encode it via %20, not as + as this is no good in URL path (only in URL params)
       else
         buf.append( trulySpecialChar );  // all other cases that we currently have just need to be left alone!
       prevIdx = nextIdx + 1;
       if ( prevIdx >= strLength )
         break;  // the special character was the last one in the string
       nextIdx = getNextIndexOf( str, strLength, prevIdx, cannotEscapeByEncodingChars );
     }

     return ( buf.toString() );
   }

   private static int  getNextIndexOf( String str, int strLength, int startIdx, char searchChars[] )
   {
     for ( int idx = startIdx; idx < strLength; ++idx )
     {
       char  strChar = str.charAt( idx );
       for ( int jj = 0; jj < searchChars.length; ++jj )
         if ( strChar == searchChars[jj] )
           return ( idx );
     }
     return ( -1 );
   }

// I'm leaving this here for now as the previous implementation was a multi-byte unaware escape,
// but had additional issues (e.g. it would doubly escape characters and was multi-pass without
// a need to be.  The code in this comment block is untested, but should fix these issues.  This
// should eventually be removed as it is now clear that the code must also encode and then escape
// any non-ASCII character.
//
//   // According to HTTP 1.1 RFC, up through > are unsafe in URL path segments and ';' and '?' are reserved as well.
//   // Char code 0 is special to C based languages and should not appear in this context either!
//   // ('/' is not in this list as escape() could be passed multiple path segments, e.g. foo/bar, in which case the
//   // '/' should not be escaped)
//   protected static final char[] unsafe = { ' ', '"', '#', '%', '<', '>', ';', '?', 0 };
//
//   public static String  multiByteUnawareEscape( String str )
//   {
//     StringBuffer  buf = null;
//
//     int  strLength = str.length();
//     for ( int charIdx = 0; charIdx < strLength; ++charIdx )
//     {
//       char  ch = str.charAt( charIdx );
//       int  unsafeCharIdx;
//       for ( unsafeCharIdx = 0; unsafeCharIdx < unsafe.length; ++unsafeCharIdx )
//         if ( ch == unsafe[unsafeCharIdx] )
//           break;
//       if ( unsafeCharIdx >= unsafe.length )
//       {
//         if ( buf != null )
//           buf.append( ch );
//         else
//           continue;
//       }
//       else
//       {
//         if ( buf == null )
//         {
//           buf = new StringBuffer( strLength + 2 );
//           if ( charIdx > 0 )
//             buf.append( str.substring( 0, charIdx ) );
//         }
//         buf.append( '%' );
//         buf.append( toHex[ch >> 4 & 0xF] );
//         buf.append( toHex[ch & 0xF] );
//       }
//     }
//
//     if ( buf != null )
//       return ( buf.toString() );
//     else
//       return str;
//   }

   ////////////////////////////////////////////////////////////////////////////
   /**
    * Translates a string from <code>x-www-form-urlencoded</code> format back
    * into a string.  Hex escaped bytes are converted and the resulting sequence of
    * 8-bit values is converted to characters using UTF8 encoding.  This produces a
    * decoded string from the results of <code>encode</code> without loss data.
    *
    * <BR><BR><B>Thread Safe:</B> true (not if shared between thread)
    * <BR><BR><B>Supported API:</B> true
    * @param s <code>String</code> to be translated.
    * @return the translated <code>String</code>.
    */
   ////////////////////////////////////////////////////////////////////////////
   public String decode (String s)
   {
      return decode(s, ivEncoding);
   }

   ////////////////////////////////////////////////////////////////////////////
   /**
    * Translates a string from <code>x-www-form-urlencoded</code> format back the
    * original string.  Hex escaped bytes are converted and the resulting
    * sequence of 8-bit values is converted to characters using the given character
    * encoding.
    *
    * <BR><BR><B>Thread Safe:</B> true (not if shared between thread)
    * <BR><BR><B>Supported API:</B> true
    * @param encoded <code>String</code> to be translated.
    * @param encoding the character encoding name
    * @return the translated <code>String</code> or "" if encoded is null.
    */
   ////////////////////////////////////////////////////////////////////////////
   public String decode (String encoded, String encoding)
   {
      if ( encoded == null )
         return "";
      int len = encoded.length();
      this.reset();
      try
      {
         for (int i = 0; i < len; i++)
         {
            char c = encoded.charAt(i);

            if (c == '+')
               this.write(' ');
            else
            {
               if (c == '%')
               {
                  if (encoded.charAt(i+1) == '%')  //%%
                     this.write('%');
                  else
                  {
                     //convert from '%xy'
                     i += 2;
                     c = (char)Integer.parseInt(encoded.substring(i-1, i+1), 16);
                     this.write(c);
                  }
               }
               else
                  this.write(c);
            }
         }
      }
      catch(IndexOutOfBoundsException | NumberFormatException ioobe)
      {
         logger.error((CharSequence)null, ioobe );
      }

      String decoded = null;
      if (encoding != null)
      {
         try
         {
            decoded = new String(this.buf, 0, this.count, encoding);
         }
         catch (Exception e)
         {
            logger.error((CharSequence)null, e );  // Ignore
         }
      }
      // Try and create a string with UTF-8 Encoding then
      if (decoded == null)
      {
         try
         {
            decoded = new String(this.buf, 0, this.count, UTF8);
         }
         catch (Exception e)
         {
            logger.error((CharSequence)null, e );  // Ignore
         }
      }
      // Finally try to create a simple string
      if (decoded == null)
      {
         decoded = new String(this.buf, 0, this.count);
      }
      return decoded;
   }


   ////////////////////////////////////////////////////////////////////////////
   /**
    * Convience method to convert the bytes of a String in ISO-8859_1 encoding
    * to UTF-8 encoding.  This should typically be called after a JSP
    * request.getParameter( ) call.
    *
    * <BR><BR><B>Supported API:</B> true
    * @param str  The string to re-encode for the correct bytes.
    *
    * @return  String the decoded and translated String.
    */
   ////////////////////////////////////////////////////////////////////////////
   public static final String decodeBytes(String str) throws UnsupportedEncodingException
   {
      return (new String(str.getBytes("ISO8859_1"),"UTF-8"));
   }

   ////////////////////////////////////////////////////////////////////////////
   /**
    * Convience method to convert the bytes of a String in ISO-8859_1 encoding
    * to a designated encoding.  This should typically be called after a JSP
    * request.getParameter( ) call.
    *
    * <BR><BR><B>Supported API:</B> true
    * @param str  The string to re-encode for the correct bytes.
    * @param enc  The encoding to translate to.
    *
    * @return  String the decoded and translated String.
    */
   ////////////////////////////////////////////////////////////////////////////
   public static final String decodeBytes(String str, String enc)throws UnsupportedEncodingException
   {
      return (new String(str.getBytes("ISO8859_1"),enc));
   }

   ////////////////////////////////////////////////////////////////////////////
   /**
    * Convience method to convert the bytes of a String in an input encoding encoding
    * to a designated encoding.  This should typically be called after a JSP
    * request.getParameter( ) call.
    *
    * <BR><BR><B>Supported API:</B> true
    * @param str The string to re-encode for the correct bytes.
    * @param from_enc   The encoding coming from.
    * @param to_enc  The encoding to translate to.
    *
    * @return  String the decoded and translated String.
    */
   ////////////////////////////////////////////////////////////////////////////
   public static final String decodeBytes(String str, String from_enc, String to_enc ) throws UnsupportedEncodingException
   {
      return (new String(str.getBytes(from_enc),to_enc));
   }

   ////////////////////////////////////////////////////////////////////////////
   /**
    * Convert a Unicode string into the so called 'ascii' format, where each
    * Unicode characters (except the range of 0x00 - 0x7F) are represented
    * with uxxxx escape sequences. xxxx is a four digit hexadecimal integer.
    */
   ////////////////////////////////////////////////////////////////////////////
   public static String unicodeToAscii( final String in_str )
   {
     if ( in_str == null )
       return ( "" );

     final int  in_str_len = in_str.length();

     // allocate StringBuilder with exactly the right size
     final StringBuilder  builder;
     {
       int  out_str_len = in_str_len;
       for ( int ii = 0; ii < in_str_len; ++ii )
       {
         final int  val = in_str.charAt( ii );
         // Anything above ASCII 127 AND the characters " and '; why is \ not escape as it is used in the escape sequence?!?
         if ( val > 0x7F || val == 0x22 || val == 0x27 /* || val == 0x5C */ )
           out_str_len += 5;  // total length of escape sequence is 6, but we've already accounted for 1 character in out_str_len
       }
       if ( out_str_len == in_str_len )
         return ( in_str );  // result of this transformation would be identical to the incoming string, so simply return it now
       builder = new StringBuilder( out_str_len );
     }

     for ( int ii = 0; ii < in_str_len; ++ii )
     {
       final char  ch = in_str.charAt( ii );
       final int  val = ch;
       // Anything above ASCII 127 AND the characters " and '; why is \ not escape as it is used in the escape sequence?!?
       if ( val > 0x7F || val == 0x22 || val == 0x27 /* || val == 0x5C */ )
       {
         final String  hex_str = Integer.toHexString( val );
         final String  prefix;
         switch ( hex_str.length() )
         {
           case 1:
             prefix = "\\u000";
             break;
           case 2:
             prefix = "\\u00";
             break;
           case 3:
             prefix = "\\u0";
             break;
           default:
             prefix = "\\u";
             break;
         }
         builder.append( prefix );
         builder.append( hex_str );
       }
       else
         builder.append( ch );
     }
     return ( builder.toString() );
   }
}
