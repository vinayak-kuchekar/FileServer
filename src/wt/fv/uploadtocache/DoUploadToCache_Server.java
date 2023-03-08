package wt.fv.uploadtocache;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ptc.windchill.uwgm.proesrv.upload.EPMUploadResponseDelegate;

import wt.fv.replica.StandardReplicaService;

public class DoUploadToCache_Server extends HttpServlet{
    private static String container = "container-in-us";
    private static final String CGI_MULTIPART_BOUNDARY = "cgi.multipart_boundary";
    private static final String MULTI_PART_HEADER_FILENAME = "filename";
    private static final String MULTI_PART_HEADER_NAME = "name";
    private static final String PREV_CONTENT_DATA_SEPERATOR = "|";
    private static final String PREV_CONTENT_DATA_LOCAL = "local";
    private static final String PREV_CONTENT_DATA_REMOTE = "remote";
    private static final String CACHE_DESCRIPTOR_SEPARATOR = ":";
    private static final String CACHE_DESCRIPTOR_END_DELIMITER = ";";
    private static final String CD_ARRAY_HDR = "CacheDescriptor_array";
    private static final String CCD_ARRAY_HDR = "ChapteredCacheDescriptor_array";
    private static final String MASTER_URL_HDR = "Master_URL";
    public static final String UPLOAD_FEEDBACK = "uploadFeedback";
    private static final String FOLDER_TAG                      = "FolderId";
    static final String VAULT_TAG                       = "VaultId";
    private static final String STRING_OF_ZEROES = "00000000000000";

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
        System.out.println("DoUploadToCache_Server.doExecute(req, response)");
        try {
            String boundary = req.getHeader(CGI_MULTIPART_BOUNDARY);
            System.out.println("----boundary1 = "+boundary);
            if(boundary == null) {
                String contentType = req.getHeader("Content-Type");
                System.out.println("----contentType = "+contentType);
                if(contentType.contains("boundary=")) {
                    boundary = contentType.split("boundary=")[1];
                }
                System.out.println("----boundary2 = "+boundary);
            }
//            String sasURL = req.getParameter("sasURL");
//            System.out.println("----sasURL = "+sasURL);
            String folderIdStr = req.getParameter(FOLDER_TAG);
            long folderId = -1;
            if(folderIdStr != null)
                folderId = Long.parseLong(folderIdStr);
            String[] cacheDescriptorArray = null;
            String masterURL = null;
            MPInputStream mis = new MPInputStream(req.getInputStream(), boundary);
            mis.setEncoding("UTF8");
            int fileCount = 0;
            Hashtable hashTab = new Hashtable();
            Hashtable hashTabHidden = new Hashtable();
            while (mis.hasMoreObjectBodies()) {
                if (mis.containsBodyHeader(MULTI_PART_HEADER_FILENAME)
                        && mis.getBodyHeader(MULTI_PART_HEADER_FILENAME) != null
                        && mis.getBodyHeader(MULTI_PART_HEADER_FILENAME).length() > 0) {
                    try {
                        String cacheDescriptor = cacheDescriptorArray[fileCount] ;
                        CachedContentDescriptor ccd = storeSingleFile(masterURL, folderId, cacheDescriptor, false, null, mis, hashTab) ;
                        hashTabHidden.put(mis.getBodyHeader(MULTI_PART_HEADER_NAME), ccd);
                        fileCount++;
                    } catch(Exception wte) {
                        wte.printStackTrace();
                        Vector v = (Vector) hashTab.get(UPLOAD_FEEDBACK);
                        if(v == null) {
                            v = new Vector();
                            hashTab.put(UPLOAD_FEEDBACK, v);
                        }
                        v.add(0, "UPLOAD_FAILED_ERROR_MESSAGE_2");
                        byte[] buf = new byte[1024];
                        while (mis.read(buf, 0, 1024) > 0);
                    }
                } else if (mis.containsBodyHeader(MULTI_PART_HEADER_NAME)) {
                    String key = mis.getBodyHeader(MULTI_PART_HEADER_NAME).trim();
                    String value = mis.readString();
                    System.out.println("---key="+key);
                    System.out.println("---value="+value);
                    if (key.equalsIgnoreCase(CD_ARRAY_HDR)) {
                        cacheDescriptorArray = value.split(CACHE_DESCRIPTOR_END_DELIMITER);
                    }
                    if (key.equalsIgnoreCase(CCD_ARRAY_HDR)) {
                        cacheDescriptorArray = value.split(CACHE_DESCRIPTOR_END_DELIMITER);
                    }
                    if (key.equalsIgnoreCase(MASTER_URL_HDR)){
                        masterURL = value;
                    }
                    if (hashTab.containsKey(key)) {
                        Vector v = (Vector) hashTab.get(key);
                        v.add(value);
                        hashTab.put(key, v);
                    } else {
                        Vector v = new Vector();
                        v.add(value);
                        hashTab.put(key, v);
                    }
                }
            }
            String delegateClass = req.getParameter("delegate");
            FormGeneratorDelegate delegate = FormGeneratorDelegateFactory.getDelegate(delegateClass);
                delegate.generateForm(response, hashTab, hashTabHidden);
        }catch(Exception e) {
            e.printStackTrace();
        }
        
    }
    
    
    private CachedContentDescriptor storeSingleFile(String masterURL,  long folderId, String cd, boolean isReplica, String cLocale, InputStream is,
            Hashtable hashTab) throws Exception {
        CachedContentDescriptor ccd = null;
        String[] pair = cd.split(CACHE_DESCRIPTOR_SEPARATOR);
        for(String pairElem : pair) {
            System.out.println("---storeSingleFile() pair="+pairElem);
        }
        long streamId = Long.parseLong(pair[0]);
        String sasURL = pair[1];
        System.out.println("----sasURL = "+sasURL);
        String contentId = null;
        if (pair.length >= 3)
            contentId = pair[2];
        long filesize = -1;
        if (pair.length == 4)
            filesize = Long.parseLong(pair[3]);
        String securityLabels = null;
        if (pair.length == 5)
            securityLabels = pair[4];
        String sasToken = null;
        if (pair.length == 6)
            sasToken = pair[5];
        
        System.out.println("---storeSingleFile() sasURL="+sasURL);
//        String[] tokens = sasURL.split("\\?");
//        sasURL = "";
//        int count=0;
//        for(String token:tokens) {
//            if(count == 0) {
//                sasURL += token;
//                if(sasURL.endsWith("/"))
//                    sasURL += buildFileName(fileName);
//                else
//                    sasURL += "/"+buildFileName(fileName);
//            }
//            else {
//                sasURL += "?" + token;
//            }
//            count++;
//        }
//        System.out.println("---storeSingleFile() sasURL="+sasURL);
        EncodingConverter ec = new EncodingConverter();
        sasURL = ec.decode(sasURL);
        System.out.println("---storeSingleFile() decoded sasURL="+sasURL);
        //https://learn.microsoft.com/en-us/rest/api/storageservices/put-blob
        URL url = new URL(sasURL);
        HttpURLConnection  con = (HttpURLConnection) url.openConnection();
        con.setAllowUserInteraction (false);
        con.setDoOutput(true);
        con.setDoInput (true);
        con.setRequestMethod("PUT");
        con.setRequestProperty("content-type", "application/octet-stream");
        con.setRequestProperty("x-ms-blob-type", "BlockBlob");
        long length = 0;
        OutputStream os = con.getOutputStream();
        byte[] buffer = new byte[1024];
        int len = -1;
        while((len = is.read(buffer)) > -1) {
            os.write(buffer, 0 , len);
            length += len;
        }
        os.flush();
        if (con instanceof HttpURLConnection) {
            HttpURLConnection httpCon = (HttpURLConnection) con;
            int respCode = httpCon.getResponseCode();
            System.out.println("-----------respCode="+respCode);
        }
        ccd = new CachedContentDescriptor(
                streamId, folderId, length, 0, contentId, null, null);
        return ccd;
    }
    
    private static String buildFileName( long a_Number ){
        String fileName = STRING_OF_ZEROES;
        String hexString = Long.toHexString(a_Number);
        return  fileName.substring(0, fileName.length()-hexString.length()) + hexString;
      }
}