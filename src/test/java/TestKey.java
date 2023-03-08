import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.zip.CRC32;

public class TestKey {

    public static void main(String[] args) throws Exception{
        
        
        String input = "11802,11805";
        CRC32 crc = new CRC32();
        crc.update(input.getBytes());
        System.out.println("input:"+input);
        System.out.println("CRC32:"+crc.getValue());//4241489823
        
//        // TODO Auto-generated method stub
//        String keyFileName = "D:\\ptc\\temp\\main.pubkey";
//        FileInputStream keyFile = new FileInputStream( keyFileName );
//        ObjectInputStream key_is = new ObjectInputStream( keyFile );
//        PublicKey publick = readKey(key_is);
//      //converting public key to byte            
//        byte[] byte_pubkey = publick.getEncoded();
//        System.out.println("\nBYTE KEY::: " + byte_pubkey);
//
//        //converting byte to String 
//        String str_key = Base64.getEncoder().encodeToString(byte_pubkey);
//        // String str_key = new String(byte_pubkey,Charset.);
//        System.out.println("\nSTRING KEY::" + str_key);
    }
    
    private static PublicKey readKey(ObjectInputStream key_is)
            throws IOException, Exception{
        Object tmp = null;
        try{
            tmp = key_is.readObject();
        }
        catch( ClassNotFoundException e ){
            throw new Exception( e);
        }
        if( !(tmp instanceof byte[]) ){
            throw new Exception( "ILLEGAL_IMPORTED_OBJECT");
        }
        byte[] keyBytes = (byte[])tmp;
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec (keyBytes);
        PublicKey pubKey = null;
        try{
            KeyFactory kf = KeyFactory.getInstance("RSA");
            pubKey = kf.generatePublic(x509KeySpec);
        }
        catch(NoSuchAlgorithmException nsae){
            throw new Exception(nsae);
        }
        catch(InvalidKeySpecException ikse){
            throw new Exception(ikse);
        }
        return pubKey;
    }
}
