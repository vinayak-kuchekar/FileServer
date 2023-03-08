package wt.fv.replica;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;


import wt.fv.uploadtocache.EncodingConverter;

public class Test {

    final static String SIGNATURE_ALGORITHM = "RSA";
    public static int KEY_LENGTH = 1024;
    private static final char[] keyArray = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/' };
    private static final char padChar = 0x3D;
    private static final byte[] keyByteArray = new byte[256];

    
    public static void main1(String[] args)throws Exception {}
    
    public static void main(String[] args)throws Exception {
        System.out.println("in Test");
        String vaultName = new String("平仮名".getBytes(), StandardCharsets.UTF_8);
        System.out.println("---vaultName = "+vaultName);
        System.out.println("---vaultName = "+"平仮名");
//        PrivateKey privatek = getKey();
//        String url = "http://vkucheka2l3.ptcnet.ptc.com/Windchill/servlet/WindchillGW";
//        String signature = sign0(url, privatek);
//        System.out.println("signature="+new String(signature));
//        //DCxZEpSp66PbABclN9V2tXvGW6BkD8u%2B9fj1rbKbZU%2BDaZuFkx8wTKWYuKBGww9pLAEZwWBEktiBEMkyepOcAekYD1WjUNjB%2BiVu7Zh64xrvx2BB2LhJAm%2FtenLpQNGtRdH8OBX1CMWCP4rDphCGZbUphz2NcC4wTD%2BzEV8sJ0Q%3D
//        PublicKey publick = getPublicKey();
//        System.out.println("publick = "+publick);
        System.out.println("done");
    }
    
    public static PrivateKey getKey()throws Exception {
        FileInputStream pubKeyStream = new FileInputStream("D:\\ptc\\temp\\privatekey");
        ByteArrayOutputStream baos = new ByteArrayOutputStream(10240);
        ObjectInputStream ois = new ObjectInputStream( pubKeyStream );
        byte[] bytes = (byte[])ois.readObject();
        PKCS8EncodedKeySpec PKCS8_prvSpec = new PKCS8EncodedKeySpec(bytes);
        PrivateKey privatek = null;
        try{
           KeyFactory kf = KeyFactory.getInstance(SIGNATURE_ALGORITHM);
           privatek = kf.generatePrivate(PKCS8_prvSpec);
        }
        catch(NoSuchAlgorithmException nsae){
           throw new Exception(nsae);
        }
        catch(InvalidKeySpecException ikse){
           throw new Exception(ikse);
        }
        System.out.println("Private="+privatek);
        return privatek;
    }
    
    public static PublicKey getPublicKey()throws Exception {
        FileInputStream pubKeyStream = new FileInputStream("D:\\ptc\\temp\\publickey");
        ByteArrayOutputStream baos = new ByteArrayOutputStream(10240);
        ObjectInputStream ois = new ObjectInputStream( pubKeyStream );
        byte[] bytes = (byte[])ois.readObject();
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(bytes);
        PublicKey publick = null;
        try{
           KeyFactory kf = KeyFactory.getInstance(SIGNATURE_ALGORITHM);
           publick = kf.generatePublic(x509EncodedKeySpec);
        }
        catch(NoSuchAlgorithmException nsae){
           throw new Exception(nsae);
        }
        catch(InvalidKeySpecException ikse){
           throw new Exception(ikse);
        }
        System.out.println("publick="+publick);
        return publick;
    }

    private static String sign0(String url, PrivateKey key)
            throws NoSuchAlgorithmException, Exception {
        Signature dsa = Signature.getInstance("MD5WithRSA");
        dsa.initSign(key);
        byte[] urlBytes = url.getBytes(Charset.defaultCharset().name());
        int i;
        for (i = 0; i < urlBytes.length; i++) {
            dsa.update(urlBytes[i]);
        }
        byte[] signBytes = dsa.sign();
        signBytes = new Char3in4Encoder().encode(signBytes).getBytes();
        String sign = WTURLEncoder.encode(new String(signBytes));
        return (sign);
    }
    
    public static String encode( byte[] byteArray ) {
        int iLen = byteArray.length;
        java.lang.StringBuilder strBuf = new java.lang.StringBuilder(iLen);
        for (int i=0;i<iLen ;i+=3 ) {
           int iNrChar = iLen-i;
           if (iNrChar==1)  {
              strBuf.append(keyArray[byteArray[i] >>> 0x2 & 0x3F]);
              strBuf.append(keyArray[byteArray[i]  << 0x4 & 0x30]);
              strBuf.append(padChar);
              strBuf.append(padChar);
           }
           else if(iNrChar==2) {
              strBuf.append(keyArray[byteArray[i] >>> 0x2 & 0x3F]);
              strBuf.append(keyArray[(byteArray[i] << 0x4 & 0x30)+(byteArray[i+1] >>> 0x4 & 0xF)]);
              strBuf.append(keyArray[(byteArray[i+1] << 0x2 & 0x3C)]);
              strBuf.append(padChar);
           }
           else {
              strBuf.append(keyArray[byteArray[i] >>> 0x2 & 0x3F]);
              strBuf.append(keyArray[(byteArray[i] << 0x4 & 0x30)+(byteArray[i+1] >>> 0x4 & 0xF)]);
              strBuf.append(keyArray[(byteArray[i+1] << 0x2 & 0x3C)+(byteArray[i+2] >>> 0x6 & 0x3)]);
              strBuf.append(keyArray[(byteArray[i+2]  & 0x3F)]);
           }
        }
        return(strBuf.toString());
     }
    public static byte[] decode( byte[] byteArray )
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
