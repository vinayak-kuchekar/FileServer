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
package com.ptc.windchill.uwgm.proesrv.upload;

/*
 24-Feb-07  X10-95   nkothari  created for proj: 14219816
 06-Aug-07  X10-114  kboora    Fixed SPR
 */

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.servlet.http.HttpServletResponse;

import wt.fv.uploadtocache.CachedChapteredContentDescriptor;
import wt.fv.uploadtocache.CachedContentDescriptor;
import wt.fv.uploadtocache.DoUploadToCache_Server;
import wt.fv.uploadtocache.FormGeneratorDelegate;

public class EPMUploadResponseDelegate implements FormGeneratorDelegate
{

  public void generateForm(HttpServletResponse response, Hashtable fields, Hashtable hidddenFields)
   throws Exception
  {
    response.setHeader("Content-Type", "text/text;charset=UTF-8");
    OutputStreamWriter os = null;
    try
    {
      os = new OutputStreamWriter(response.getOutputStream(),"UTF8");
      Vector v = (Vector) fields.get(DoUploadToCache_Server.UPLOAD_FEEDBACK);
      if (v != null && v.size() > 0)
        System.out.println("Upload content failed with message : " + v);

      Enumeration enumration = hidddenFields.keys();
      StringBuffer ccdStr = new StringBuffer(128);
      while (enumration.hasMoreElements())
      {
        String key = (String) enumration.nextElement();
        Object value = hidddenFields.get(key);
        if (value==null)
        {
            System.out.println(key+": has no value!");
          continue;
        }

        if (value instanceof CachedChapteredContentDescriptor){
        	CachedChapteredContentDescriptor cccd = (CachedChapteredContentDescriptor) value ;
        	    System.out.println(cccd.toString()) ;
        	ccdStr.append(cccd.getStreamId()).append(":").append(cccd.getFileSize())
        	.append("=");
        	long chapterSizes[] = cccd.getFileSizes();
        	for(int i = 0 ; i < chapterSizes.length-1; i++){
        		ccdStr.append(chapterSizes[i]).append(",") ;
        	}
        	ccdStr.append(chapterSizes[chapterSizes.length-1]) ;
        	ccdStr.append(":") ;
        	ccdStr.append(cccd.getEncodedCCD()).append(";");
        } else if (value instanceof CachedContentDescriptor) {
          CachedContentDescriptor ccd = (CachedContentDescriptor)value;
          System.out.println(ccd.toString()) ;
          ccdStr.append(ccd.getStreamId()).append(":").append(ccd.getFileSize()).
              append(":").append(ccd.getEncodedCCD()).append(";");
        }
      }

      String respStr = ccdStr.toString();
      System.out.println("Response Delegate String ="+respStr);
      int idx = respStr.lastIndexOf(";");
      if (idx !=-1)
        respStr = respStr.substring(0,idx);

      os.write(respStr);
    }
    catch (IOException e)
    {
      e.printStackTrace();
      response.setStatus(500); // error
    }
    finally
    {
      if (os !=null)
      {
        try
        {
          os.flush();
          os.close();
        }
        catch (IOException e)
        {
          e.printStackTrace();
          response.setStatus(500); // error
        }
      }

    }

  }


  /**
   * logging
   */
}