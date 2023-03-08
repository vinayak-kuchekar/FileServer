package wt.fv.replica;
public class Char3in4Encoder  {


   // --- Attribute Section ---


   private static final String RESOURCE = "wt.util.xml.io.ioResource";
   private static final String CLASSNAME = Char3in4Encoder.class.getName();
   private static final char[] keyArray = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/' };

   /**
    * equivalent to '='.
    **/
   private static final char padChar = 0x3D;





   // --- Operation Section ---

   /**
    * encodes a byte array as a string.
    *
    * <BR><BR><B>Supported API: </B>false
    *
    * @param     byteArray  the byte array to encode
    * @return    String
    **/

   public String encode( byte[] byteArray ) {

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

}