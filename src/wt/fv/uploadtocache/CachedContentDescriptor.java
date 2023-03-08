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

package wt.fv.uploadtocache;

import java.io.Serializable;


/**
 *
 * <BR><BR><B>Supported API: </B>false
 * <BR><BR><B>Extendable: </B>false
 *
 * @version   1.0
 **/
public class CachedContentDescriptor implements Serializable {
   private static final String RESOURCE = "wt.fv.uploadtocache.uploadtocacheResource";
   private static final String CLASSNAME = CachedContentDescriptor.class.getName();
   private long streamId;
   private long fileSize;
   private long folderId;
   private String contentIdentity;
   private String securityLabels;
   private long checksum;
//   private UploadToCacheSecurityCheck uploadToCacheSecurityCheck;

   public static final String STR_FORMAT_SEPARATOR = ":";
   
   /**
    * Minimum attribute count for CachedContentDescriptor
    */
   public static final int MIN_ATTRIBUTES_COUNT = 4;

   protected String encodedCCD = null;

   /**
    * Gets the value of the attribute: streamId.
    *
    * <BR><BR><B>Supported API: </B>false
    *
    * @return    long
    **/
   public long getStreamId() {
      return streamId;
   }

   /**
    * Sets the value of the attribute: streamId.
    *
    * <BR><BR><B>Supported API: </B>false
    *
    * @param     a_StreamId
    **/
   public void setStreamId( long a_StreamId ) {
      streamId = a_StreamId;
   }

   /**
    * Gets the value of the attribute: fileSize.
    *
    * <BR><BR><B>Supported API: </B>false
    *
    * @return    long
    **/
   public long getFileSize() {
      return fileSize;
   }

   /**
    * Sets the value of the attribute: fileSize.
    *
    * <BR><BR><B>Supported API: </B>false
    *
    * @param     a_FileSize
    **/
   public void setFileSize( long a_FileSize ) {
      fileSize = a_FileSize;
   }

   /**
    * Gets the value of the attribute: folderId.
    *
    * <BR><BR><B>Supported API: </B>false
    *
    * @return    long
    **/
   public long getFolderId() {
      return folderId;
   }

   /**
    * Sets the value of the attribute: folderId.
    *
    * <BR><BR><B>Supported API: </B>false
    *
    * @param     a_FolderId
    **/
   public void setFolderId( long a_FolderId ) {
      folderId = a_FolderId;
   }

   /**
    * Gets the value of the attribute: contentIdentity; This field is used
    * to uniquely identify a content. This could be either the actual full
    * path of the content or any other client specific value which can uniquely
    * identify the content
    *
    * <BR><BR><B>Supported API: </B>false
    *
    * @return    String
    **/
   public String getContentIdentity() {
      return contentIdentity;
   }

   /**
    * Sets the value of the attribute: contentIdentity; This field is used
    * to uniquely identify a content. This could be either the actual full
    * path of the content or any other client specific value which can uniquely
    * identify the content
    *
    * <BR><BR><B>Supported API: </B>false
    *
    * @param     a_ContentIdentity
    **/
   public void setContentIdentity( String a_ContentIdentity ) {
      contentIdentity = a_ContentIdentity;
   }
   
   /**
    * Gets the value of the attribute: securityLabels; This field is used
    * to validate the upload site. This could be either empty string or string in 
    * valid format. Following is one of valid formats:
    * 1,Key=value
    *
    * <BR><BR><B>Supported API: </B>false
    *
    * @return    String
    **/
   public String getSecurityLabels() {
      return securityLabels;
   }

   /**
    * Sets the value of the attribute: securityLabels; This field is used
    * to validate the upload site. This could be either empty string or string in
    * valid format. Following is one of valid formats:
    * 1,Key=value
    *
    * <BR><BR><B>Supported API: </B>false
    *
    * @param     securityLabels
    **/
   public void setSecurityLabels( String securityLabels ) {
//       EncodingConverter decode = new EncodingConverter();
//       this.securityLabels = decode.decode(securityLabels);
       this.securityLabels = securityLabels;
   }

   /**
    * Gets the value of the attribute: checksum.
    *
    * <BR><BR><B>Supported API: </B>false
    *
    * @return    long
    **/
   public long getChecksum() {
      return checksum;
   }

   /**
    * Sets the value of the attribute: checksum.
    *
    * <BR><BR><B>Supported API: </B>false
    *
    * @param     a_Checksum
    **/
   public void setChecksum( long a_Checksum ) {
      checksum = a_Checksum;
   }
   
   /**
    * Gets the value of attribute : UploadToCacheSecurityCheck
    * 
    * @return SecurityCheck
    */
//   public UploadToCacheSecurityCheck getUploadToCacheSecurityCheck() {
//       return this.uploadToCacheSecurityCheck;
//   }

   /**
    * Sets the value of attribute : UploadToCacheSecurityCheck
    * 
    * @param uploadToCacheSecurityCheck
    */
//   void setUploadToCacheSecurityCheck(UploadToCacheSecurityCheck uploadToCacheSecurityCheck) {
//       this.uploadToCacheSecurityCheck = uploadToCacheSecurityCheck;
//   }

/**
    * This constructor creates a CachedContentDescripor object based on
    * the passed stringifed ccd. The string passed to this constructor should
    * be a valid encoded form of a ccd object. This string gets decoded
    * in the constructor and a new CCD object gets created with this information
    *
    * <BR><BR><B>Supported API: </B>false
    *
    * @param     strCCD
    * @exception wt.util.WTException
    **/
   public CachedContentDescriptor( String strCCD )
            throws Exception {
	   encodedCCD = strCCD;
	   EncodingConverter decoder = new EncodingConverter();
	   String decodedCCD = decoder.decode(strCCD);
	   String [] attributes = decodedCCD.split(STR_FORMAT_SEPARATOR);		
	   streamId = Long.parseLong(attributes[0]);
	   fileSize = Long.parseLong(attributes[1]);
	   folderId = Long.parseLong(attributes[2]);
	   contentIdentity = attributes[3];
	   
	   if(attributes.length > 4){
	       securityLabels = attributes[4];
	   }
	   else {
	       securityLabels = "NONE_SEC_LBL";
	   }
	   if(attributes.length > 5) {
//	       uploadToCacheSecurityCheck = UploadToCacheSecurityCheck.getInstance(Integer.parseInt(attributes[5]));
	   }
	   else{
//	       uploadToCacheSecurityCheck = UploadToCacheSecurityCheck.NONE; 
	   }
   }
   
   private boolean isSecureUpload() {
//       return this.uploadToCacheSecurityCheck != null && this.uploadToCacheSecurityCheck.isSecureUpload();
       return false;
   }

   /**
    * Default Constructor
    *
    * <BR><BR><B>Supported API: </B>false
    *
    **/
   public CachedContentDescriptor() {
       this.securityLabels = "NONE_SEC_LBL";
//       this.uploadToCacheSecurityCheck = UploadToCacheSecurityCheck.NONE; 
   }

   /**
    * This constructor creates a CachedContentDescripor object based on
    * the passed parameters.
    *
    * <BR><BR><B>Supported API: </B>false
    *
    * @param     stream_id
    * @param     folder_id
    * @param     file_size
    * @param     checksum
    * @param     content_identity
    * @exception wt.util.WTException
    **/
   public CachedContentDescriptor( long stream_id, long folder_id, long file_size, long checksum, String content_identity )
            throws Exception {
       this(stream_id, folder_id, file_size, checksum, content_identity, "NONE_SEC_LBL", null);
   }

   /**
    * This constructor creates a CachedContentDescripor object based on the passed parameters.
    * This overloaded constructor accepts the security labels and SecurityCheck parameter.
    * 
    * @param stream_id
    * @param folder_id
    * @param file_size
    * @param checksum
    * @param content_identity
    * @param securityLabels
    * @param uploadToCacheSecurityCheck
    * @throws WTException
    */
   public CachedContentDescriptor( long stream_id, long folder_id, long file_size, long checksum, String content_identity, String securityLabels, Object uploadToCacheSecurityCheck)
           throws Exception {
       this.streamId = stream_id;
       this.folderId = folder_id;
       this.fileSize = file_size;
       this.checksum = checksum;
       this.contentIdentity = content_identity;
       EncodingConverter decode = new EncodingConverter();

       this.securityLabels = decode.decode(securityLabels);
//       this.uploadToCacheSecurityCheck = uploadToCacheSecurityCheck;
  }

   public String toString() {
        String res = getClass().getSimpleName() + ": ";
        res += "streamId=" + streamId + ", ";
        res += "fileSize=" + fileSize + ", ";
        res += "folderId=" + folderId;

        if (isSecureUpload()) {
            res += ", " + "contentIdentity="+ contentIdentity + ", ";
            res += "securityLabels="+securityLabels+", ";
//            res += "securityCheck=" + uploadToCacheSecurityCheck;
        }
        return res;
    }

    /**
     * Returns encoded format for CachedContentDescriptor
     * 
     * @return
     */
    public String getEncodedCCD() {
        if (encodedCCD == null) {
            String strCCD = getStringFormat();
            EncodingConverter encoder = new EncodingConverter();
            encodedCCD = encoder.encode(strCCD);
        }
        return encodedCCD;
    }
    
    /**
     * This method converts the object to a suitable string format. STR_FORMAT_SEPERATOR is used as a separator.
     * String format is
     * StreamId:FileSize:FolderId:ContentIdentity:SecurityLabels:SecurityCheck
     * 
     * We always output the string in above format so that the parameters are communicated in same size all the time.
     * 
     * @return String format of CCD
     */
    protected String getStringFormat() {
        String strCCD = String.valueOf(this.getStreamId()) + STR_FORMAT_SEPARATOR 
                + String.valueOf(this.getFileSize()) + STR_FORMAT_SEPARATOR
                + String.valueOf(this.getFolderId()) + STR_FORMAT_SEPARATOR
                + ((this.getContentIdentity() != null) ? this.getContentIdentity() : "null");
                
                if (this.isSecureUpload()) {
                    strCCD = strCCD + STR_FORMAT_SEPARATOR
                    + this.getSecurityLabels() + STR_FORMAT_SEPARATOR;
//                    + String.valueOf(uploadToCacheSecurityCheck.getCode());
                }
                
        return strCCD;
    }

    /**
     * This method will copy the attributes from fromCCD to toCCD
     * 
     * @param fromCCD
     *            CachedContentDescriptor to populate attributes from
     * @param toCCD
     *            CachedContentDescriptor to populate attributes to
     */
    protected void copyAttributes(CachedContentDescriptor fromCCD, CachedContentDescriptor toCCD) {
        toCCD.streamId = fromCCD.streamId;
        toCCD.fileSize = fromCCD.fileSize;
        toCCD.folderId = fromCCD.folderId;
        toCCD.contentIdentity = fromCCD.contentIdentity;
        toCCD.securityLabels = fromCCD.securityLabels;
//        toCCD.uploadToCacheSecurityCheck = fromCCD.uploadToCacheSecurityCheck;
        toCCD.checksum = fromCCD.checksum;
    }
    
    /**
     * Validates encodedCCD to determine if it is a valid format to construct CachedContentDescriptor.
     * 
     * @param encodedCCD
     *            encodedCCD to validate
     * @return
     */
    public static boolean isValidEncodedCCD(String encodedCCD) {
        boolean isValid = false;
        if (encodedCCD == null) {
            return isValid;
        }

        EncodingConverter decoder = new EncodingConverter();
        String decodedCCD = decoder.decode(encodedCCD);
        String[] attributes = decodedCCD.split(STR_FORMAT_SEPARATOR);

        try {
            // Check first 3 attributes are long
            Long.parseLong(attributes[0]);//reading streamId
            Long.parseLong(attributes[1]);//reading fileSize
            Long.parseLong(attributes[2]);//reading folderId
            
            //attributes[3] = content identities (optional) - no need to check
            //attributes[4] = security labels (optional) - no need to check
            
            //check 6th attribute is integer
            if(attributes.length > 5){
                Integer.parseInt(attributes[5]); //reading securityCheck
            }
            // If no exception is thrown the encodedCCD is valid
            isValid = true;
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            isValid = false;
        }
        return isValid;
    }
}