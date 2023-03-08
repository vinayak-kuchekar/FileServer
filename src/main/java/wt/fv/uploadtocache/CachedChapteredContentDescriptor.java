/* bcwti
 *
 * Copyright (c) 2012 Parametric Technology Corporation (PTC). All Rights Reserved.
 *
 * This software is the confidential and proprietary information of PTC
 * and is subject to the terms of a software license agreement. You shall
 * not disclose such confidential information and shall use it only in accordance
 * with the terms of the license agreement.
 *
 * ecwti
 */
package wt.fv.uploadtocache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;


/**
 *
 * This class holds the information for the chapters. It will typically get persisted as FvFraction.
 *
 * <BR>
 * <BR>
 * <B>Supported API: </B>false <BR>
 * <BR>
 * <B>Extendable: </B>false
 */
public class CachedChapteredContentDescriptor extends CachedContentDescriptor {

    private static final String RESOURCE = CachedChapteredContentDescriptor.class.getName();

    private static final long serialVersionUID = 1L;

    /**
     * Names of retained chapters
     */
    private String [] retainedChapters;

    /**
     * Names of all chapters
     */
    private String[] chapterNames;

    /**
     * Sizes of all chapters.
     */
    private long[] fileSizes;

    /**
     * Checksums of all chapters
     */
    private String[] checksums;

    /**
     * This is the chapters storage information in the file encoded as a string
     */
    private String[] infos;

    /**
     * The chapter delegate name
     */
    private String delegateName;

    /**
     * This constructor creates a CachedChapteredContentDescriptor object based on the passed stringifed cccd. The
     * string passed to this constructor should be a valid base 64 encoded form of a cccd object. This string gets decoded in
     * the constructor and a new CCCD object gets created with this information
     *
     * <BR>
     * <BR>
     * <B>Supported API: </B>false
     *
     * @param strCCCD
     *            Encoded format of CachedChapteredContentDescriptor
     * @exception wt.util.WTException
     **/
    public CachedChapteredContentDescriptor(String strCCCD)
            throws Exception {
        CachedChapteredContentDescriptor cccd = null;
        byte[] decodedCCCDByteArr = Base64.decodeBase64(strCCCD);

        try (ByteArrayInputStream bais = new ByteArrayInputStream(decodedCCCDByteArr);
                ObjectInputStream ois = new ObjectInputStream(bais);) {
            cccd = (CachedChapteredContentDescriptor) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new Exception(e);
        }

        if (cccd != null) {
            copyAttributes(cccd, this);
            validateCCCDArrLengths();
        }
    }

    public CachedChapteredContentDescriptor() {
    }

    /**
     * This constructor creates a CachedChapteredContentDescriptor object based on the passed parameters.
     *
     * @param stream_id
     * @param folder_id
     * @param file_size
     * @param checksum
     * @param content_identity
     * @param chapterNames
     * @param fileSizes
     * @param checksums
     * @param infos
     * @param delegateName
     * @param prevStreamID
     * @param isChapterNewArr
     * @throws WTException
     */
    public CachedChapteredContentDescriptor(long stream_id, long folder_id, long file_size, long checksum,
            String content_identity, String[] chapterNames, long[] fileSizes, String[] checksums,
            String[] infos, String delegateName) throws Exception {
        this(stream_id, folder_id, file_size, checksum, content_identity, "NONE",  chapterNames,
                fileSizes, checksums, infos, delegateName);
    }

    /**
     * This constructor creates a CachedChapteredContentDescriptor object based on the passed parameters.
     * This constructor accepts the security labels and security check parameter.
     *
     * @throws WTException
     */
    public CachedChapteredContentDescriptor(long stream_id, long folder_id, long file_size, long checksum,
            String content_identity, String securityLabels,  String[] chapterNames, long[] fileSizes, String[] checksums,
            String[] infos, String delegateName) throws Exception {
        super(stream_id, folder_id, file_size, checksum, content_identity, securityLabels, null);
        this.chapterNames = chapterNames;
        this.fileSizes = fileSizes;
        this.checksums = checksums;
        this.infos = infos;
        this.delegateName = delegateName;

        validateCCCDArrLengths();
    }

    public String toString() {
        String arrayDataSep = ", ";
        StringBuilder strRep = new StringBuilder(super.toString());

        String fileSizesStr = joinArray(this.fileSizes, arrayDataSep);
        String chapterNamesStr = joinArray(this.chapterNames, arrayDataSep);
        String checksumsStr = (this.checksums != null) ? joinArray(this.checksums, arrayDataSep) : "null";
        String infosStr = (this.infos != null) ? joinArray(this.infos, arrayDataSep) : "null";
        String retChapters = (retainedChapters != null) ? joinArray(retainedChapters, arrayDataSep): "null";
        strRep.append(", ");
        strRep.append("chapterNames=" + chapterNamesStr + ", ");
        strRep.append("chapterFileSizes=" + fileSizesStr + ", ");
        strRep.append("checksums=" + checksumsStr + ", ");
        strRep.append("infos=" + infosStr + ", ");
        strRep.append("delegateName=" + delegateName + ", ");
        strRep.append("retainedChapters=" + retChapters);

        return strRep.toString();
    }

    /**
     * Returns encoded format for CachedChapteredContentDescriptor
     *
     * @return
     */
    public String getEncodedCCD() {
        if (encodedCCD == null) {
            encodedCCD = getStringFormat();
        }
        return encodedCCD;
    }

    @Override
    protected String getStringFormat() {

        validateCCCDArrLengths();

        try(ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream( baos );){
            oos.writeObject(this);
            return Base64.encodeBase64String( baos.toByteArray() );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param fileSizes
     */
    public void setFileSizes(long[] fileSizes) {
        this.fileSizes = Arrays.copyOf(fileSizes, fileSizes.length);
    }

    /**
     *
     * @param chapterNames
     */
    public void setChapterNames(String[] chapterNames) {
        this.chapterNames = Arrays.copyOf(chapterNames, chapterNames.length);
    }

    /**
     *
     * @param checksums
     */
    public void setChecksums(String[] checksums) {
        this.checksums = Arrays.copyOf(checksums, checksums.length);
    }

    /**
     * @param infos
     *            the infos to set
     */
    public void setInfos(String[] infos) {
        this.infos = Arrays.copyOf(infos, infos.length);
    }

    /**
     * @return the infos
     */
    public String[] getInfos() {
        return infos;
    }

    /**
     * @return the chapterNames
     */
    public String[] getChapterNames() {
        return chapterNames;
    }

    /**
     * @return the fileSizes
     */
    public long[] getFileSizes() {
        return fileSizes;
    }

    /**
     * @return the checksums
     */
    public String[] getChecksums() {
        return checksums;
    }

    /**
     * @return the delegateName
     */
    public String getDelegateName() {
        return delegateName;
    }

    /**
     * @param delegateName
     *            the delegateName to set
     */
    public void setDelegateName(String delegateName) {
        this.delegateName = delegateName;
    }

    /*****************************************************************************************************
     ********************************** Utility Methods **************************************************
     *****************************************************************************************************/

    /**
     * Converts the long array to string format using the supplied delimiter.
     *
     * @param longArr
     *            long[]
     * @param delimiter
     * @return
     */
    private String joinArray(long[] longArr, String delimiter) {
        if (longArr == null || longArr.length == 0) {
            return "";
        }
        StringBuilder strBuilder = new StringBuilder("" + longArr[0]);

        for (int i = 1; i < longArr.length; i++) {
            strBuilder.append(delimiter);
            strBuilder.append(longArr[i]);
        }
        return strBuilder.toString();
    }

    /**
     * Converts the array to string format using the supplied delimiter.
     *
     * @param strArr
     *            String array
     * @param delimitor
     * @return
     */
    private String joinArray(String[] strArr, String delimitor) {
        return StringUtils.join(strArr, delimitor);
    }

    /**
     * Validates that all the array instance variables have same length as that of chapterNames array.
     */
    private void validateCCCDArrLengths() {
        if (chapterNames == null) {
            return;
        }
        String[] nullStrArr = new String[]{"null"};

        //If checksums == null we may get a null array ["null"]. So exception is not thrown in this case
        if (checksums != null && !Arrays.equals(checksums, nullStrArr) && checksums.length != chapterNames.length) {
            throw new RuntimeException( "CHECK_SUM_ARR_LEN_INCORRECT");
        }
        if (fileSizes != null && fileSizes.length != chapterNames.length) {
            throw new RuntimeException( "FILE_SIZES_ARR_LEN_INCORRECT");
        }
        //If infos == null we may get a null array ["null"]. So exception is not thrown in this case
        if (infos != null && !Arrays.equals(infos, nullStrArr) && infos.length != chapterNames.length) {
            throw new RuntimeException( "INFOS_ARR_LEN_INCORRECT");
        }
    }

    /**
     * Validates encodedCCD to determine if it is a valid format to construct CachedChapteredContentDescriptor.
     *
     * @param encodedCCD
     *            encodedCCD to validate
     * @return
     */
    public static boolean isValidEncodedCCD(String encodedCCD) {
        boolean isValid = false;
        if (encodedCCD == null) {
            return false;
        }
        try {
            byte[] decodedCCCDByteArr = Base64.decodeBase64(encodedCCD);
            try (ByteArrayInputStream bais = new ByteArrayInputStream(decodedCCCDByteArr);
                    ObjectInputStream ois = new ObjectInputStream(bais);) {
                Object cccdObj = ois.readObject();
                if (cccdObj instanceof CachedChapteredContentDescriptor) {
                    isValid = true;
                }
            } catch (IOException | ClassNotFoundException e) {
                isValid = false;
            }
        } catch (IllegalArgumentException iae) {
            // if input string is not valid, Base64.decodeBase64 throws exception now - commons-codec.jar version 1.13
            isValid = false;
        }
        return isValid;
    }

    /**
    * @return the retainedChapters
    */
   public String[] getRetainedChapters() {
      return retainedChapters;
   }

   /**
    * @param retainedChapters the retainedChapters to set
    */
   public void setRetainedChapters(String[] retainedChapters) {
      this.retainedChapters = retainedChapters;
   }

   /**
    * This method will copy the attributes from fromCCCD to toCCCD
    *
    * @param fromCCCD
    *            CachedChapteredContentDescriptor to populate attributes from
    * @param toCCCD
    *            CachedChapteredContentDescriptor to populate attributes to
    */
   protected void copyAttributes(CachedChapteredContentDescriptor fromCCCD, CachedChapteredContentDescriptor toCCCD) {
       super.copyAttributes(fromCCCD, toCCCD);
       toCCCD.delegateName = fromCCCD.delegateName;
       toCCCD.chapterNames = fromCCCD.chapterNames;
       toCCCD.fileSizes = fromCCCD.fileSizes;
       toCCCD.checksums = fromCCCD.checksums;
       toCCCD.infos = fromCCCD.infos;
       toCCCD.retainedChapters = fromCCCD.retainedChapters;
   }
}