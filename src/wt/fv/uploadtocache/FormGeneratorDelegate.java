package wt.fv.uploadtocache;
import java.util.Hashtable;

import javax.servlet.http.HttpServletResponse;

/**
 *
 * <BR><BR><B>Supported API: </B>false
 * <BR><BR><B>Extendable: </B>false
 *
 * @version   1.0
 **/
public interface FormGeneratorDelegate {

   /**
    *
    * <BR><BR><B>Supported API: </B>false
    *
    * @param     response
    * @param     fields
    * @param     hidddenFields
    * @exception wt.util.WTException
    **/
   public void generateForm( HttpServletResponse response, Hashtable fields, Hashtable hidddenFields )
            throws Exception;
}

