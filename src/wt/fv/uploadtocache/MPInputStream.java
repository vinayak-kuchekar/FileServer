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

import java.io.CharArrayWriter;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;



/**
 * A MP input stream is a filter that allows a business class to read consecutive
 * multipart data object bodies whilst alleviating the associated pain.  It does
 * this by parsing out object body headers and values, and reading the input
 * stream for the business class whilst checking for the encapsulation
 * boundaries.  Analagous to other input streams, MP will respond to read
 * requests as long as there is data in the present object body. Once the end
 * of the data for said object body is reached, a negative one is returned on
 * the next read.
 * <p>
 * The only required steps are to instantiate a new MPInputStream (passing in
 * the HTTPRequest's input stream and CGI_MULTIPART_BOUNDARY), and to call MP's
 * hasMoreObjectBodies method.  Provided the the instantiation succeeds and the
 * return value of the hasMoreObjectBodies method call is true, the business
 * class is free to proceed with reading of the first object body.
 * <p>
 * Upon receipt of a negative one from a read request, said object body is
 * finished.  The return value of another call to hasMoreObjectBodies will
 * indicate if another object body is available for reading.
 * <p>
 * Note that this input stream filter reads the object body data as bytes, and
 * thus does not impinge any format nor conversion upon the data therein.  Thus
 * if the object body data is, for example, base64 encoded, the resultant reads
 * of the business class will get base64 encoded data.  It is in this way, that
 * this filter can operate independant of the data enclosed in the object body.
 * It is therefore incumbant on the business class to perform any post
 * processing of the data that may be necessary.
 * <br><br><B>Supported API:</B> true
 * <br><B>Extendable:</B> false
 *
 **/

public class MPInputStream
  extends FilterInputStream
  implements MPInputStreamIfc
{
   private final static String versionID = "$Header: /Windchill/current/wt/util/MPInputStream.java 10    12/22/98 8:06p Jdg $";  // ver of this class
   private final static String RESOURCE = "wt.util.utilResource";
   private static final Logger  logger = null;
   private static final Pattern  MALICIOUS_DATA_PATTERN;
   static
   {
     final String  maliciousDataPatternString = "";//( ( wtProps != null ) ? wtProps.getProperty( "wt.util.maliciousDataPattern" ) : null );
     if ( ( maliciousDataPatternString != null ) && ( maliciousDataPatternString.length() > 0 ) )
     {
       Pattern  tmpPattern = null;
       try
       {
         tmpPattern = Pattern.compile( maliciousDataPatternString, Pattern.DOTALL );
       }
       catch( Exception e )
       {
         logger.error( "Failed to compile regexp specified by wt.util.maliciousDataPattern", e );
       }
       MALICIOUS_DATA_PATTERN = tmpPattern;
     }
     else
       MALICIOUS_DATA_PATTERN = null;
   }

   private boolean delim_found = false;  // whether we've found the a boundary
   private String Delimitor = null;  // boundary plus leading two dashes
   private byte[] BDelimitor = null; // copy of boundary value for fast search
   private int leading_dashes_count = 0; // number of leading dashes in the delimintor
   private String FinalDelimitor = null;  // delim plus trailing two dashes
   private Properties bodyHeaders = new Properties();  // holds obj body headers and values
   private Vector boundariesV;  // holds boundaries during imbeded obj work
   private int bVindex;  // how many boundaries in vector, how deep are we.
   private String temp = null;  // holds converted array for delim search
   private byte ibuf[] = null;  // internal buffer for our use.
   private int ibufd;  // index, where next byte will be read into.
   private int ibuf_count;  // how many bytes we will copy to readers buf.
   private int ibuf_first;  // first byte position for copy to readers buf.
   private int ibuf_last;  // last byte position for copy to readers buf.
   private boolean eof;  // have we encountered eof
   private IOException ioe;  // we have encountered an IOException
   private String encoding;  // character encoding of text parts

   static final byte CR = 13;
   static final byte LF = 10;

   /**
    * Creates a new MPInputStream initialized with the
    * specified input stream and object body boundary
    * <br><br><B>Supported API:</B> true
    * @param  in         the input stream
    * @param  boundary   the encapsulation boundary string
    */
   public MPInputStream( InputStream in, String boundary ) {

      super( in );
      initDelimitorData ("--" + boundary);

      // Make internal buffer big enough for cascading reads through a BufferedInputStream.
      int bufsiz = 2048 + BDelimitor.length + 4;
      ibuf = new byte[bufsiz];

      // Current implementation requires body overread to be pushed back
      this.in = new PushbackInputStream(in, bufsiz);

      delim_found = false;
      boundariesV = new Vector();
      bVindex = boundariesV.size();
      ibufd = 0;
      ibuf_last = 0; //set to end of buf to cause 1st fill_buff
      ibuf_first = 0;
      ibuf_count = 0;

   }

   /**
    * Set character encoding to be returned by <code>getEncoding</code> method.
    * Useful if the creator of a <code>MPInputStream</code> want to communicate
    * a known character encoding to a body part processing method.
    * <br><br><B>Supported API:</B> true
    * @param  encoding   the character encoding
    */
   public void setEncoding( String encoding ) {

      this.encoding = encoding;

   }

   /**
    * get the character encoding set by <code>setEncoding</code> method.
    * Returns null if not encoding has been set for this <code>MPInputStream</code>.
    *
    * @deprecated
    *
    * <br><br><B>Supported API:</B> true
    * @return  encoding   the character encoding
    */
   public String getEncoding( String encoding ) {

      return this.encoding;

   }

   /**
    * get the character encoding set by <code>setEncoding</code> method.
    * Returns null if not encoding has been set for this <code>MPInputStream</code>.
    * <br><br><B>Supported API:</B> true
    * @return  encoding   the character encoding
    */
   public String getEncoding( ) {

      return encoding;

   }

   /** Setup read of next object body if one is available.
    * Accomplished by checking that the encapsulation boundary at the
    * top of the header is what we expect, generate a hashtable of the
    * object body headers for later retrieval, and set the stream to the
    * top of the body data for subsequent reads.
    * <br><br><B>Supported API:</B> true
    * <p>
    * @returns  boolean    <code>true</code> if valid object body is ready to
    *                      be read, <code>false</code> if not.
    * @exception  IOException is thrown if error on reading object body
    *                        header(s) or encapsulation boundary doesn't
    *                        match that which was passed into the constructor,
    *                        of read from the boundary object body header.
    * <p>
    * Note: this method must be called and a value of true returned prior to
    *       getBodyHeaders() and/or getBodyHeader(String s) calls.
    **/
   public boolean hasMoreObjectBodies() throws IOException {
      if (ioe != null)
         throw ioe;

      try {
         delim_found = false;
         do {
            temp = readLn();
         } while ((temp.length() == 0) && !eof); //boundary should be on 1st non-empty line
         if (temp.equals(FinalDelimitor)){
            if (bVindex > 0) {
               initDelimitorData (boundariesV.elementAt(bVindex).toString());
               bVindex--;
               boundariesV.removeElementAt(bVindex);
            }
            return false; // we're done.
         }
         else if (!(temp.equals(Delimitor))){
            System.err.println("MPInputStream object body top boundary mismatch");
            System.err.println("\texpecting " + Delimitor);
            System.err.println("\tfound " + temp);
            return false;
         }
         genBodyHeaders();
         // if the next obj header contains a new boundary, we have a mixed,
         // alt, etc. type multipart.  we need to save the old Delimitor to use
         // when we're done with this body.
         if (bodyHeaders.containsKey("boundary") && bodyHeaders.get("boundary") != null) {
            bVindex++;
            boundariesV.addElement(Delimitor);
            initDelimitorData ("--" + bodyHeaders.get("boundary"));
         }
      }
      catch( IOException e ) {
         e.printStackTrace();
         ioe = e;
         throw e;
      }
      if (bodyHeaders.size() > 0) {
         return true;
      }
      return false;
   }

   /**
    * This is our private method to parse and load object body headers
    * into an internal private hashtable.  Access to said keys and values
    * is through the getBodyHeaders and getBodyHeader public methods.
    *
    * Note: this method handles filenames with imbeded spaces.
    *
    **/
   private void genBodyHeaders() throws IOException {

      try {
         bodyHeaders.clear();
         String line = null;
         boolean inQuote = false;

         while ( (line = readLn()).length() != 0 ) {

            String name = null;
            String value = null;
            StringBuilder buf = new StringBuilder();

            StringTokenizer stok = new StringTokenizer(line, ";\":=", true);
            while (stok.hasMoreTokens()) {
               String tok = stok.nextToken().trim();
               if (tok.equals("\"")) {
                  inQuote = !inQuote;
               } else {
                  if (tok.equals(";") || tok.equals(":") || tok.equals("=")) {
                     if (inQuote) {
                        buf.append(tok);
                     } else {
                        if (tok.equals(";")) {
                           value = convert(buf.toString().trim());
                           bodyHeaders.put( name.toLowerCase(), value );

                           // clear for next token
                           name = null;
                           value = null;
                           buf = new StringBuilder();
                        }
                     } // else ignore delimitor
                  } else {
                     if (name == null) {
                        name = tok;
                     } else {
                        buf.append(tok);
                     }
                  }
               }
            }

            value = convert(buf.toString().trim());
            bodyHeaders.put( name.toLowerCase(), value );
         }
         checkBodyHeaders();
      }
      catch( IOException e )
      {
         // improved exception reporting [JMH]
         final IOException  wrappedIoe = new IOException("MIME_READ_ERROR");
         wrappedIoe.initCause( e );  // could do this as part of the constructor in Java 6, but we do it this way to ease backporting to X-12
         // could log exception here, but higher level code should do so upon receipt of this exception and so this would just be redundant
         throw wrappedIoe;
      }
   }

   // Convert a String that was created from 8-bit int values in the readLn method
   // into real Unicode characters using the encoding set for this MPInputStream.
   private String convert (String orig) {
      byte[] buf = new byte[orig.length()];
      orig.getBytes(0, orig.length(), buf, 0);
      String s = null;
      if (encoding != null) {
         try {
            s = new String(buf, encoding);
         }
         catch (Exception e) {
            e.printStackTrace(); // Ignore
         }
      }
      if (s == null)
         s = new String(buf);

      return s;
   }

   /**
    *
    * Public accessor to Object Body Headers and values.
    * <br><br><B>Supported API:</B> true
    * @returns Enumeration of keys and their values.
    *
    **/
   public Enumeration getBodyHeaders() {
      return bodyHeaders.keys();
   }

   /**
    *
    * Public accessor to specific Object Body Header.
    * <br><br><B>Supported API:</B> true
    * @returns String of key's value
    * @param s string key for which to get value.
    *
    **/
   public String getBodyHeader(String s) {
      return bodyHeaders.getProperty(s.toLowerCase());
   }

   /**
    *
    * Public checker for specific Object Body Header.
    * <br><br><B>Supported API:</B> true
    * @returns boolean true if key is present, otherwise false
    * @param s string key for which to get value.
    *
    **/
   public boolean containsBodyHeader(String s) {
      return bodyHeaders.containsKey(s.toLowerCase());
   }


   /**
    *
    * This method parses out the next MIME object body binary data
    * by looking through an internal buffer for the encapsulation boundary.
    * Which, of course, is the boundary specified on the content-type header,
    * immediately preceeded by a "--" sequence, as part of the boundary.
    * A -1 is return on next fill if delim was found to signal EOF for this
    * body.
    *
    *
    **/
   private int fill_buf() throws IOException {
      int x = 0;
      int y = 0;
      int z = 0;
      int ret = 0;

      if (!delim_found) {  // if delim found on last pass, drop through
         if (ibufd > 0) {
            // move remnants from last read up to begining of buffer
            int remaining = ibufd - ibuf_last;
            System.arraycopy(ibuf, ibuf_last, ibuf, 0, remaining);
            ibufd = remaining;
         }
         try {
            do {
               // try to preclude short reads with partial delim
               z = in.read(ibuf, ibufd, ibuf.length - ibufd);
               if (z != -1) ibufd += z;
            } while ((ibufd < BDelimitor.length) && (z != -1));
         }
         catch (EOFException e) {
            throw new EOFException("INPUT_EOF");
         }

         x = findDelimitor(ibuf, 0, ibufd);

         switch (x) {
            case (-1):
               // Didn't find whole delimitor.
               // We only return subset of bytes because we could have a
               // partial delimitor hanging in at the end of ibuf.
               // We copy remaining bytes to beginning of ibuff for usage later
               // on next call.
               if (z < 0)
                  throw new EOFException("INPUT_EOF");
               ibuf_first = 0;
               ret = ibuf_last = ibuf_count = ibufd - (BDelimitor.length + 4);
               break;

            default:
               // found the delimitor, x tells us where
               ibuf_first = 0;

               // strip CR LF preceeding delimiter
               if ((x > 1) && (ibuf[x-2] == CR) && (ibuf[x-1] == LF))
                  ibuf_last = x-2;
               // strip CR || LF -- in case client uses PrintWriter, etc to us
               else if ((x > 0) && ((ibuf[x-1] == CR) || (ibuf[x-1] == LF)))
                  ibuf_last = x-1;
               // strip nothing -- delimitor should be on own line, but...
               else
                  ibuf_last = x;

               delim_found = true; // set so next fill returns -1 for EOF of this obj body.

               // now let's pushback the boundary and trailing data
               y = ibufd - x; //want to pushback the delimitor minus the CRLF
               if (y > 0) {
                  ((PushbackInputStream)in).unread(ibuf, x, y);
               }

               // and return with numbytes placed in buf
               ibufd = 0;
               ibuf_count = ibuf_last;
               if (ibuf_count > 0)
                  ret = ibuf_count;
               else
                  ret = -1;
               break;
         } // end switch
      }
      else {
         ret = -1; // delim was found on last run, this obj body is done.
      }
      return ret;
   }

   /**
    * Same as no-arg fill_buf but reads directly into caller's buffer.
    * Max length read is identical (ibuf.length) because pushback is limited.
    **/
   private int fill_buf (byte b[], int off) throws IOException {
      int x = 0;
      int y = 0;
      int z = 0;
      int ret = 0;

      int b_ibufd = 0;  // b_ibufd is to b as ibufd is to ibuf

      if (!delim_found) {  // if delim found on last pass, drop through
         if (ibufd > 0) {
            // move remnants from last read to this buffer
            int remaining = ibufd - ibuf_last;
            System.arraycopy(ibuf, ibuf_last, b, off, remaining);
            ibufd = 0;
            b_ibufd = remaining;
         }
         try {
            do {
               // try to preclude short reads with partial delim
               z = in.read(b, off + b_ibufd, ibuf.length - b_ibufd);
               if (z != -1) b_ibufd += z;
            } while ((b_ibufd < BDelimitor.length) && (z != -1));
         }
         catch (EOFException e) {
            throw new EOFException("INPUT_EOF");
         }

         x = findDelimitor(b, off, b_ibufd);

         switch (x) {
            case (-1):
               // Didn't find whole delimitor.
               // We only return subset of bytes because we could have a
               // partial delimitor hanging in at the end of the buffer.
               // We copy remaining bytes to beginning of ibuff for usage later
               // on next read.
               if (z < 0)
                  throw new EOFException("INPUT_EOF");
               ret = b_ibufd - (BDelimitor.length + 4);

               // Copy remaining bytes back into internal buffer
               int remaining = b_ibufd - ret;
               System.arraycopy(b, off + ret, ibuf, 0, remaining);
               ibuf_first = ibuf_last = ibuf_count = 0;
               ibufd = remaining;
               break;

            default:
               // found the delimitor, x tells us where
               // strip CR LF preceeding delimiter
               if ((x > off + 1) && (b[x-2] == CR) && (b[x-1] == LF))
                  ret = x - 2 - off;
               // strip CR || LF -- in case client uses PrintWriter, etc to us
               else if ((x > off) && ((b[x-1] == CR) || (b[x-1] == LF)))
                  ret = x - 1 - off;
               // strip nothing -- delimitor should be on own line, but...
               else
                  ret = x - off;

               delim_found = true; // set so next fill returns -1 for EOF of this obj body.

               // now let's pushback the boundary and trailing data
               y = off + b_ibufd - x; //want to pushback the delimitor minus the CRLF
               if (y > 0) {
                  ((PushbackInputStream)in).unread(b, x, y);
               }

               // and return with numbytes placed in buf
               if (ret == 0)
                  ret = -1;
               break;
         } // end switch
      }
      else {
         ret = -1; // delim was found on last run, this obj body is done.
      }
      return ret;
   }

   private void initDelimitorData (String delim) {
      Delimitor   = delim;
      BDelimitor  = Delimitor.getBytes();
      leading_dashes_count = 0;

      byte first  = BDelimitor[0];

      for (int i = 0; i < BDelimitor.length; i++) {
         if (BDelimitor[i] != first) {
            leading_dashes_count = i;
            break;
         }
      }

      FinalDelimitor = Delimitor + "--";
   }

   /**
    * Searches for the delimitor in the input buffer until it reaches
    * the end of a buffer of the delimitor is found.
    * <p>
    * This method uses the fact that the delimitor is starting
    * from a series of dashes. It attempts to sample the buffer
    * using number of leading dashes as a sampling interval. When
    * a dash is found, this method switches to use a simple byte
    * comparison algorithm. If simple algorithm fails, sampling
    * starts again.
    *
    * @return     return index to beginning of pattern or -1
    */
   private int findDelimitor(byte[] buf, int off, int len) {
      int b=off; // buffer cursor
      int p=0; // pattern cursor
      int     buf_length  = off + len;
      int     del_length  = BDelimitor.length;
      byte    first       = BDelimitor[0];
      int     next_b      = off;
      int     prev_b      = off;
      boolean jumped      = false;

Begining:
      while (b < buf_length) {
         if (buf[b] == first) {
            if (jumped) {
               b = prev_b + 1;
               p = 0;
            }
            else {
               ++b;
               p = 1;
            }
         }
         else {
            next_b = b + leading_dashes_count;

            if (next_b < buf_length) {
               prev_b = b;
               b      = next_b;
               jumped = true;
               continue Begining;
            }
            else {
               ++b;
            }
         }

BasicAlgorithm:

         // while either cursor has not reached end
         while (b < buf_length && p < del_length) {

            if (buf[b] == BDelimitor[p]) {
               // values at cursors are equal, increment both cursors and continue
               ++b;
               ++p;
            } else {
               // values at cursors differ, reset pattern cursor and inc buffer
               ++b;
               p=0;
               jumped = false;
               continue Begining;
            }
         }

         // if pattern cursor is at end of pattern then pattern has been found
         if (p == del_length)
            return b-p; // return index to beginning of pattern
         else
            return -1; // not found

      }

      return -1; // not found
   }


   /**
    * Reads the next line of text from the underlying input stream. This
    * method successively reads bytes from the underlying input stream
    * until it reaches the end of a line of text.
    * <p>
    * A line of text is terminated by a carriage return character
    * (<code>'&#92;r'</code>), a newline character (<code>'&#92;n'</code>), a
    * carriage return character immediately followed by a newline
    * character, or the end of the input stream. The line-terminating
    * character(s), if any, are not returned as part of the string that
    * is returned.
    * <p>
    * This method blocks until a newline character is read, a carriage
    * return and the byte following it are read (to see if it is a
    * newline), the end of the stream is detected, or an exception is
    * thrown.
    *
    *
    * @return     the next line of text from this input stream.
    * @exception  IOException  if an I/O error occurs.
    */
   private String readLn() throws IOException {
      InputStream In = this.in;
      char buf[];
      char lineBuffer[];

      buf = lineBuffer = new char[128];

      int room = buf.length;
      int offset = 0;
      int c = 0;
      boolean endObjHdr = false;

      try {
         do {
            while ((c = In.read()) > '\r') {
               if (--room < 0) {
                  buf = new char[offset + 128];
                  room = buf.length - offset - 1;
                  System.arraycopy(lineBuffer, 0, buf, 0, offset);
                  lineBuffer = buf;
               }
               buf[offset++] = (char) c;
            }
            if (c == '\r' || c == '\n') {
               endObjHdr = true;
               if (c == '\r' && ((c = In.read()) != '\n')){
                  if (!(In instanceof PushbackInputStream)) {
                     In = this.in = new PushbackInputStream(In);
                  }
                  ((PushbackInputStream)In).unread(c);
               }
            }
            else if (c == -1) {
               offset = 0;
               endObjHdr = true;
               eof = true;
            }
            else {
               throw new IOException ("MALFORMED_MIME");
            }
         } while (!endObjHdr);
      }
      catch (EOFException e) {
         throw new EOFException("INPUT_EOF");
      }

      if ((c <= -1) || (offset == 0)) {
         return "";
      }
      return String.copyValueOf(buf, 0, offset);
   }


   /**
    * Reads a byte of data.  The method will block if no input is available.
    * <br><br><B>Supported API:</B> true
    * @return  the byte read, or -1 if the end of the stream is reached.
    * @exception IOException If an I/O error has occurred.
    *
    * We are overriding this to return 0 bytes for now.
    * Should just call read (byte[], 0, 1)...maybe?
    *
    */
   public final int read () throws IOException {
      if (ibuf_count > 0) {
         ibuf_count--;
         return ibuf[ibuf_first++];
      }
      byte[] buf = new byte[1];
      if (read(buf, 0, 1) < 0)
         return -1;
      else
         return (int) buf[0];
   }

   /**
    * Reads up to <code>b.length</code> bytes of data from this input
    * stream into an array of bytes.
    * <p>
    * The <code>read</code> method of <code>MPInputStream</code> calls
    * the <code>read</code> method of three arguments with the arguments
    * <code>b</code>, <code>0</code>, and <code>b.length</code>.
    *
    * <br><br><B>Supported API:</B> true
    * @param      b   the buffer into which the data is read.
    * @return     the total number of bytes read into the buffer, or
    *             <code>-1</code> is there is no more data because the end of
    *             the stream has been reached.
    * @exception  IOException  if an I/O error occurs.
    * @see        java.io.InputStream#read(byte[], int, int)
    * @since      JDK1.0
    */
   public final int read(byte b[]) throws IOException {
      return read(b, 0, b.length);
   }


   /**
    * Reads up to <code>len</code> bytes of data from this MP input
    * stream into an array of bytes. This method blocks until some input
    * is available.
    * <p>
    * This <code>read</code> method of <code>MPInputStream</code>
    * reads bytes out of an internal buffer.
    *
    * <br><br><B>Supported API:</B> true
    * @param      b     the buffer into which the data is read.
    * @param      off   the start offset of the data.
    * @param      len   the maximum number of bytes read.
    * @return     the total number of bytes read into the buffer, or
    *             <code>-1</code> if there is no more data because the end
    *             of the object body has been reached.
    * @exception  IOException  if an I/O error occurs.
    **/
   public final int read (byte b[], int off, int len) throws IOException {

      if (ioe != null)
         throw ioe;

      int fbr;
      int c;
      int d = len;
      int i;

      for (i=off; i<(off+len);) {
         switch (ibuf_count) {
            case (0):
               try {
                  // Internal buffer is empty.
                  // If destination is as large as internal buffer, read through and scan
                  // for delimter from there, copying only unused to internal buffer.
                  if (d >= ibuf.length) {
                     fbr = fill_buf(b, i);
                     if (fbr >= 0) {
                        i += fbr; d -= fbr;
                        continue;
                     }
                  }
                  // else fill internal buffer
                  fbr = fill_buf();
               }
               catch (IOException e) {
                  ioe = e;
                  throw e;
               }
               if (fbr == -1){
                  if (i > off)
                     return (i - off);
                  else
                     return -1;
               }
            default:
               c = (d > ibuf_count) ? ibuf_count : d;
               System.arraycopy(ibuf, ibuf_first, b, i, c);
               i += c; ibuf_first += c; ibuf_count -= c; d -= c;
         }
      }
      return (i - off);
   }

   /**
    * Skips bytes of input.
    * <br><br><B>Supported API:</B> true
    * @param n         bytes to be skipped
    * @return  actual number of bytes skipped
    * @exception IOException If an I/O error has occurred.
    **/
   public long skip (long n) throws IOException {
      // Can't just skip n bytes from 'in' and throw them
      // away, because n bytes from 'in' doesn't necessarily
      // correspond to n bytes from 'this'.

      if (n <= 0)
         return 0;
      byte data[] = new byte[64];
      long remaining = n;
      for (int i = 0; (remaining > 0) && (i >= 0); remaining -= i)
         i = read(data, 0, (data.length > remaining) ? (int)remaining : data.length);
      return n - remaining;
   }

   /**
    * Returns the number of bytes that can be read without blocking.
    * Returns number of bytes body bytes already read into local buffer.
    *
    * <br><br><B>Supported API:</B> true
    * @return number of bytes available
    **/
   public int available () {
      return ibuf_count;
   }

   /**
    * Reads the remainder of the current body part into a string.
    *
    * <br><br><B>Supported API:</B> true
    * @return     String read using the encoding set for this stream
    * @exception  IOException  if an I/O error occurs.
    **/
   public String readString () throws IOException {
      InputStreamReader reader = null;
      if (encoding != null) {
         try {
            reader = new InputStreamReader(this, encoding);
         }
         catch (Exception e) {
            e.printStackTrace(); // Ignore
         }
      }
      if (reader == null)
         reader = new InputStreamReader(this);
      CharArrayWriter buf = new CharArrayWriter();
      java.io.BufferedReader r = new java.io.BufferedReader(reader, 1024);
      int c;
      while ((c = r.read()) >= 0)
         buf.write(c);
      final String  result = buf.toString();
      checkString( result );
      return ( result );
   }

   private static void  checkString( final String string )
     throws IOException
   {
     if ( MALICIOUS_DATA_PATTERN != null )
       if ( MALICIOUS_DATA_PATTERN.matcher( string ).matches() )
       {
         if ( logger.isDebugEnabled() )
           logger.debug( "Potentially malicious string found: " + string );
         throw new IOException( "Found potentially malicious data" );
       }
   }

   private void  checkBodyHeaders()
     throws IOException
   {
     if ( MALICIOUS_DATA_PATTERN != null )
       for ( Map.Entry<Object,Object> headerEntry : bodyHeaders.entrySet() )
       {
         checkString( (String) headerEntry.getKey() );
         checkString( (String) headerEntry.getValue() );
       }
   }
}
