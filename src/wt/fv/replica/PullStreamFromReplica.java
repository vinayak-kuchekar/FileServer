package wt.fv.replica;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;


public class PullStreamFromReplica extends HttpServlet {

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
        System.out.println("PullStreamFromReplica.doExecute(req, response)");
        try {
            String sasURL = (String) req.getParameter("sasURL");
            System.out.println("sasURL = "+sasURL);
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
                   OutputStream os = response.getOutputStream();
                   InputStream is = httpCon.getInputStream();
                   IOUtils.copy(is, os);
                   os.flush();
                   os.close();
                   is.close();
               }
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
}
