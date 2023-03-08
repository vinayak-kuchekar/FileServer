package com.ptc.windchill.enterprise.attachments.server;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.servlet.http.HttpServletResponse;

import wt.fv.uploadtocache.CachedContentDescriptor;
import wt.fv.uploadtocache.FormGeneratorDelegate;

import java.net.URLEncoder;

public class AttachmentsFormGeneratorDelegate implements FormGeneratorDelegate {

    static {
        try {
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public void generateForm(HttpServletResponse response, Hashtable hashTab,
            Hashtable hashTabHidden) throws Exception {
        try {
            response.setHeader("Content-Type", "text/html;charset=UTF-8");
            String submissionURL = "";
            String redirectURL = "";
            Vector vecUrl = (Vector) hashTab.remove("SubmissionURL");
            Vector vecRedirect = (Vector) hashTab.remove("redirect");
            StringBuffer sb = new StringBuffer("");
            sb.append("<HTML>");
            StringBuffer sbForRedirect = new StringBuffer("");

            if(vecUrl != null && vecUrl.size() > 0) {
                submissionURL = (String)vecUrl.get(0);
                sb.append("<BODY onLoad=\"document.contentReturnForm.submit();\">");
                sb.append("<FORM name=\"contentReturnForm\" method=\"POST\" action=\"" + submissionURL + "\">");
            }else if(vecRedirect != null && vecRedirect.size() > 0){
                //Redirect to the URL as sent by the master server to handle cross-domain uploads
                redirectURL = (String)vecRedirect.get(0);
                sb.append("<BODY>");
                sb.append("<FORM name=\"contentReturnForm\">");
            }else{
                sb.append("<BODY>");
                sb.append("<FORM name=\"contentReturnForm\">");
            }
            Enumeration enumration = hashTab.keys();
            while(enumration.hasMoreElements()){
                String key = (String) enumration.nextElement();
                Vector  v = (Vector) hashTab.get(key);
                for (int i =0 ; i < v.size(); i++){
                    String value = (String)v.get(i);
                    if(value != null) {
                        String escapedValue = value;//HTMLEncoder.encodeForHTMLContent(value);
                        if(key.endsWith("textarea")){
                            sb.append("<TEXTAREA name=\""+key+"\" >"+escapedValue+"</TEXTAREA>");
                        }else{
                            sb.append("<INPUT type=\"text\" name=\""+key+"\" value=\""+escapedValue+"\">");
                        }
                        sbForRedirect.append(key + "=" + escapedValue + "&");
                    }
                    else {
                        sb.append("<INPUT type=\"text\" name=\""+key+"\" value=\"\">");
                        sbForRedirect.append(key + "=" +  "&");
                    }
                }
            }
            enumration = hashTabHidden.keys();
            while(enumration.hasMoreElements()){
                String key = (String) enumration.nextElement();
                String value = "";
                if (hashTabHidden.get(key) != null){
                    Object obj = hashTabHidden.get(key);
                    if (obj instanceof CachedContentDescriptor){
                        CachedContentDescriptor ccd = (CachedContentDescriptor)obj;
                        value = ccd.getEncodedCCD();
                    }
                    else
                        value = obj.toString();
                }
                sb.append("<INPUT type=\"hidden\" name=\""+key+"\" value=\""+value+"\">");
                sbForRedirect.append(key + "=" + value + "&");
            }
            if (!"".equals(redirectURL)){
                sb.append("</FORM>");
                sb.append("</BODY>");
                sb.append("</HTML>");
                String str = URLEncoder.encode(sbForRedirect.toString(), "UTF-8")
                                        .replaceAll("\\+", "%20")
                                        .replaceAll("\\%21", "!")
                                        .replaceAll("\\%27", "'")
                                        .replaceAll("\\%28", "(")
                                        .replaceAll("\\%29", ")")
                                        .replaceAll("\\%7E", "~");
                redirectURL = String.format(redirectURL,str);
                //Set status to HttpServletResponse.SC_MOVED_TEMPORARILY - 302 - to let the server know of redirection to new page.
                response.setStatus(302);
                response.setHeader("Location", redirectURL);
            }else{
                OutputStreamWriter os = new OutputStreamWriter(response.getOutputStream(),"UTF8");
                os.write(sb.toString());
                os.write("<BR>");
                os.write("</FORM>");
                os.write("</BODY>");
                os.write("</HTML>");
                os.flush();
                os.close();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}