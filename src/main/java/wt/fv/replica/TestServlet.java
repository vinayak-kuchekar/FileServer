package wt.fv.replica;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import wt.fv.uploadtocache.EncodingConverter;

public class TestServlet extends HttpServlet {
    private static final char[] keyArray = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/' };
    private static final byte[] keyByteArray = new byte[256];
    private static final char padChar = 0x3D;

    @Override
    public void doGet(HttpServletRequest req,
            HttpServletResponse response) throws ServletException, IOException {
        doExecute(req, response);
    }

    @Override
    public void doPost(HttpServletRequest req,
            HttpServletResponse response) throws ServletException, IOException {
        doExecute(req, response);
    }
    private void doExecute(HttpServletRequest req,
            HttpServletResponse response) throws ServletException, IOException {}
    
    private static boolean verify0(String url,String sign, PublicKey key)
            throws NoSuchAlgorithmException, Exception {
        Signature dsa = Signature.getInstance("MD5WithRSA");
        dsa.initVerify(key);
        byte[] urlBytes = url.getBytes(Charset.defaultCharset().name());
        int i;
        for (i = 0; i < urlBytes.length; i++) {
            dsa.update(urlBytes[i]);
        }
        sign = WTURLEncoder.decode(sign);
        byte[] signbytes = new Char3in4Decoder().decode(sign.getBytes());
        boolean toReturn = dsa.verify(signbytes);
        return toReturn;
    }
 
    public byte[] decode( byte[] byteArray )
            throws Exception {

      int iLen = byteArray.length;
      byte[] outputBuf = new byte[iLen];    //actual length ~= 3/4*iLen-2 to 3/4*iLen
      int iOutputBufCnt = 0;          //where we are in outputBuf
      for (int i=0;i<iLen ;i+=4 ) {
         int iNrChar = iLen-i;
         int iCharToConvert = -1;
         byte[] incomingBits = new byte[4];

         //Need at least 2 char to get the 8 bits to describe the byte
         if ( (iNrChar==1) || (iNrChar>1 && byteArray[i+1]==padChar) )   {
            if(byteArray[i]!=0x00)
               throw new Exception( " Asked to decode a single 6-bit byte; ERROR");
         }
         else if ( (iNrChar==2) || (iNrChar>2 && byteArray[i+2]==padChar) )  {
            iCharToConvert = 2;
         }
         else if ( (iNrChar==3) || (iNrChar>3 && byteArray[i+3]==padChar) ) {
            iCharToConvert = 3;
         }
         else {
            iCharToConvert = 4;
         }

         switch(iCharToConvert)  {
            case 4:
               incomingBits[3]=keyByteArray[byteArray[i+3]];
            case 3:
               incomingBits[2]=keyByteArray[byteArray[i+2]];
            case 2:
               incomingBits[1]=keyByteArray[byteArray[i+1]];
               incomingBits[0]=keyByteArray[byteArray[i]];
               break;
            default:
               throw new Exception(" trying to decode "+iCharToConvert+" incoming char - MUST BE 4; ERROR");
         }

         switch(iCharToConvert)  {
            case 2:
               outputBuf[iOutputBufCnt++]=(byte)(incomingBits[0]<<0x2&0xFC | incomingBits[1]>>>0x4&0x3);
               break;
            case 3:
               outputBuf[iOutputBufCnt++]=(byte)(incomingBits[0]<<0x2&0xFC | incomingBits[1]>>>0x4&0x3);
               outputBuf[iOutputBufCnt++]=(byte)(incomingBits[1]<<0x4&0xF0 | incomingBits[2]>>>0x2&0xF);
               break;
            case 4:
               outputBuf[iOutputBufCnt++]=(byte)(incomingBits[0]<<0x2&0xFC | incomingBits[1]>>>0x4&0x3);
               outputBuf[iOutputBufCnt++]=(byte)(incomingBits[1]<<0x4&0xF0 | incomingBits[2]>>>0x2&0xF);
               outputBuf[iOutputBufCnt++]=(byte)(incomingBits[2]<<0x6&0xC0 | incomingBits[3]&0x3F);
               break;
            default:
               //Can't get here!
               throw new Exception(" trying to decode "+iCharToConvert+" incoming char - MUST BE 4; ERROR");
         }
      }
      byte[] actualRtn = new byte[iOutputBufCnt];
      for (int ij=0; ij<iOutputBufCnt; ij++ ) {
         actualRtn[ij] =  outputBuf[ij];
      }
      return actualRtn;

   }
}
