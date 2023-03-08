package wt.fv.replica;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;

public class StandardReplicaService extends HttpServlet{
    private static String container = "container-in-us";
    public static Map _siteKeys = new HashMap();
    public static Map _vaultKeys = new HashMap();
    public static Map _configCache = new HashMap();
    public static Map _folderKeys = new HashMap();
    public static String currMasterUrl = "";
    public static String _myHostName = null;
    public static final String FILE_NAME_TAG = "fileName";
    public static final String FOLDER_TAG = "folderId";
    public static final String MIME_TAG = "mime";
    public static final String PARAM_SITE = "site";
    private static final String STRING_OF_ZEROES = "00000000000000";
    private static String SIGNATURE_ALGORITHM = "RSA";
    private static int GRACE_PERIOD = 300;
    
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
            HttpServletResponse response) throws ServletException, IOException {
        System.out.println("StandardReplicaService.doExecute(req, response)");
        try {
            String actualFileName = (String) req.getParameter("fileName");
            String mimeType = (String) req.getParameter("mime");
            String sasURL = (String) req.getParameter("sasURL");
            System.out.println("actualFileName = "+actualFileName);
            System.out.println("mimeType = "+mimeType);
            System.out.println("sasURL = "+sasURL);
            String uri = req.getRequestURI();
            System.out.println("uri = "+uri);
            String query = req.getQueryString();
            System.out.println("query = "+query);
            String origURL = uri + "?" + query;
            System.out.println("origURL = "+origURL);
            URL url = new URL(sasURL);
            URLConnection con = (URLConnection) url.openConnection();
            con.setAllowUserInteraction (false);
            con.setDoOutput(true);
            con.setDoInput (true);
            con.setRequestProperty("content-type", "application/octet-stream");
            
            if (con instanceof HttpURLConnection) {
               HttpURLConnection httpCon = (HttpURLConnection) con;
               int respCode = httpCon.getResponseCode();
               System.out.println("-----------respCode="+respCode);
               if(respCode == 200) {
                   response.setHeader("Content-Type", mimeType);
                   String attachOrInline = "attachment";
                   System.out.println("-----actualFileName="+actualFileName);
                   response.setHeader("Content-Disposition", attachOrInline + "; filename*=UTF8" +  "' '" + actualFileName);
                   OutputStream os = response.getOutputStream();
                   InputStream is = httpCon.getInputStream();
                   IOUtils.copy(is, os);
                   os.flush();
                   os.close();
                   is.close();
                   
               }
            }
        }
        catch(Exception e) {
            throw new ServletException(e);
        }
    }
    
    private static String buildFileName( long a_Number ){
        String fileName = STRING_OF_ZEROES;
        String hexString = Long.toHexString(a_Number);
        return  fileName.substring(0, fileName.length()-hexString.length()) + hexString;
      }

}
