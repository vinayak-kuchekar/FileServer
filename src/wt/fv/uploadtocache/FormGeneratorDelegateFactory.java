package wt.fv.uploadtocache;

import java.lang.String;
import wt.fv.uploadtocache.FormGeneratorDelegate;

/**
 *
 * <BR><BR><B>Supported API: </B>false
 * <BR><BR><B>Extendable: </B>false
 *
 * @version   1.0
 **/
public class FormGeneratorDelegateFactory {
   private static final String RESOURCE = "wt.fv.uploadtocache.uploadtocacheResource";
   private static final String CLASSNAME = FormGeneratorDelegateFactory.class.getName();

   /**
    *
    * <BR><BR><B>Supported API: </B>false
    *
    * @param     a_class
    * @return    FormGeneratorDelegate
    * @exception wt.util.WTException
    **/
   public static FormGeneratorDelegate getDelegate( String a_class )
            throws Exception {
      if (a_class == null)
          throw new Exception("Delegate class not defined");

      FormGeneratorDelegate delegate = null;
      try{
          Class clazz = Class.forName(a_class);
          delegate = (FormGeneratorDelegate)clazz.newInstance();
      }
      catch (Exception e){
          e.printStackTrace();
          throw new Exception(e);
      }
      finally{
          return delegate;
      }
   }
}