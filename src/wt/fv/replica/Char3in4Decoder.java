package wt.fv.replica;
public class Char3in4Decoder  {


   // --- Attribute Section ---


   private static final String RESOURCE = "wt.util.xml.io.ioResource";
   private static final String CLASSNAME = Char3in4Decoder.class.getName();
   private static final char[] keyArray = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/' };
   private static final byte[] keyByteArray = new byte[256];

   /**
    * equivalent to '='.
    **/
   private static final char padChar = 0x3D;




   // This will create a reverse decoder table to translate from the
   // printable char values to the byte values (e.g.: 'A' <-> 0x00)

   static
   {
      for( int ii=0; ii<255; ii++ )
         keyByteArray[ii] = (byte) -1;
      for( int jj=0; jj<keyArray.length; jj++ )
         keyByteArray[keyArray[jj]] = (byte) jj;
   }



   // --- Operation Section ---

   /**
    * decodes a coded string's byte array to an uncoded byte array.
    *
    * <BR><BR><B>Supported API: </B>false
    *
    * @param     byteArray  the byte array to decode
    * @return    byte[]
    * @exception wt.util.xml.XMLMechanismException
    **/

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
               throw new Exception(CLASSNAME + " Asked to decode a single 6-bit byte; ERROR");
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
               throw new Exception(CLASSNAME + " trying to decode "+iCharToConvert+" incoming char - MUST BE 4; ERROR");
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
               throw new Exception(CLASSNAME + " trying to decode "+iCharToConvert+" incoming char - MUST BE 4; ERROR");
         }
      }
      byte[] actualRtn = new byte[iOutputBufCnt];
      for (int ij=0; ij<iOutputBufCnt; ij++ ) {
         actualRtn[ij] =  outputBuf[ij];
      }
      return actualRtn;

   }

   /**
    * decodes a string to a byte array.
    *
    * <BR><BR><B>Supported API: </B>false
    *
    * @param     codedString  the byte array to encode
    * @return    byte[]
    * @exception wt.util.xml.XMLMechanismException
    **/

   public byte[] decode( String codedString )
            throws Exception {

      return decode(codedString.getBytes());

   }

}
