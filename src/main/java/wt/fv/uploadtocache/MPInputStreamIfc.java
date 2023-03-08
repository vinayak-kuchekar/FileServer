package wt.fv.uploadtocache;
import java.io.IOException;
import java.util.Enumeration;

public interface MPInputStreamIfc {

   public void setEncoding(String encoding);
   public String getEncoding();
   public boolean hasMoreObjectBodies() throws IOException;
   public Enumeration getBodyHeaders();
   public String getBodyHeader(String s);
   public boolean containsBodyHeader(String s);
   public int read() throws IOException;
   public int read(byte b[]) throws IOException;
   public int read(byte b[], int off, int len) throws IOException;
   public long skip(long n) throws IOException;
   public int available();
   public String readString() throws IOException;
   
}