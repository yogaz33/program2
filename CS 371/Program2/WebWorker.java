/*
   Yarely Ogaz
   CS 371: Program 2
   WebWorker.java
*/

import java.net.Socket;
import java.lang.Runnable;
import java.io.*;
import java.util.Date;
import java.text.DateFormat;
import java.util.TimeZone;

public class WebWorker implements Runnable {

private Socket socket;

//Constructor
public WebWorker( Socket s ) {
   socket = s;
} // end constructor


public void run() {

   String path = ""; 
   String type = "";
   
   System.err.println("Handling connection...");
   
   try {
   
      //In and Out put streams 
      InputStream  is = socket.getInputStream();
      OutputStream os = socket.getOutputStream();
      
      // Get the entire path name from the "GET " location.
      path = readHTTPRequest(is); 
      
     //Checks what type is in the file
      if ( path.toLowerCase().contains(".gif") ) {
         type = "image/gif";
         System.err.println( type );
      } 
	  
      else if ( path.toLowerCase().contains(".jpg") ) {
         type = "image/jpg";
         System.err.println( type );
      } 
            
      else if ( path.toLowerCase().contains(".png") ) {
         type = "image/png";
         System.err.println( type );
      } 
      
      else if ( path.toLowerCase().contains("ico") ) {
         type = "image/x-icon";
         System.err.println( type );
      } 
            
      else {
         type = "text/html";
         System.err.println( type );
      }
            
     
      writeHTTPHeader( os, type, path );
      writeContent( os, type, path );
      
      os.flush();
      socket.close();
      
   } 
   
   catch (Exception e) {
      System.err.println("Output error: "+e);
   }
   
   System.err.println("Done handling connection.");
   
   return;
   
}//end run


private String readHTTPRequest(InputStream is) {
   
   String line;
   String originalPath = "";
   int count = 0;
   
   BufferedReader r = new BufferedReader(new InputStreamReader(is));

   while ( true ) {
   
      try {
      
         while (!r.ready()) 
            Thread.sleep(1);
         
         line = r.readLine();        
         System.err.println("Request line: (" + line + ")" );
         
         //extracts file path name if it contains GET
         if ( line.contains("GET ") ) {
            
            //ignores the word GET and the space
            originalPath = line.substring(4);
            
            while ( !( originalPath.charAt( count ) == ' ' ) )
                count++;
            
            //updates the path name 
            originalPath = originalPath.substring( 0, count );
          
         }

         if ( line.length()== 0 ) 
      
            break;
            
      }catch (Exception e) {
   
         System.err.println("Request error: "+e);
         break;
         
      } 
      
   }//end while.
   
   return originalPath;
   
}// end readHTTPRequest


private void writeHTTPHeader(OutputStream os, String type, String path ) throws Exception {
 
   //gets date and time
   Date d = new Date();
   DateFormat df = DateFormat.getDateTimeInstance();
   df.setTimeZone(TimeZone.getTimeZone("MST7MDT"));
   
    //concatenation of current working directory and path from GET
   String pathCopy = System.getProperty("user.dir") + path.substring( 0, path.length( ) );
  
   try {
	  //Searches for path name
      FileReader inputFile = new FileReader ( pathCopy );
      BufferedReader bufferedFile = new BufferedReader ( inputFile );
      os.write("HTTP/1.1 200 OK\n".getBytes());
      
	  //Server header
      os.write("Date: ".getBytes());
      os.write((df.format(d)).getBytes());
      os.write("\n".getBytes());
      os.write("Server: FINALLY OPENED THE SERVER!\n".getBytes());   
      os.write("Connection: close\n".getBytes());
      os.write("str-Type: ".getBytes());
      os.write(type.getBytes());
      os.write("\n\n".getBytes());
       
   }catch (FileNotFoundException e) {
   
      os.write( "HTTP/1.1 404 Not Found \n".getBytes() );
     
      os.write("Date: ".getBytes());
      os.write((df.format(d)).getBytes());
      os.write("\n".getBytes());
      os.write("Server: FINALLY OPENED THE SERVER!\n".getBytes());   
      os.write("Connection: close\n".getBytes());
      os.write("str-Type: ".getBytes());
      os.write("text/html".getBytes());
      os.write("\n\n".getBytes());
                          
   }
   
   return;
   
}// end writeHTTPHeader

private void writeContent(OutputStream os, String type, String path ) throws Exception {

   String str = "";
   
   //path name now includes current directory
   String pathCopy = System.getProperty("user.dir") + path.substring( 0, path.length( ) );
   
   //Date objected created to update the date
   Date d = new Date();
   DateFormat df = DateFormat.getDateTimeInstance();
   df.setTimeZone(TimeZone.getTimeZone("MST7MDT"));
     
   if ( type.equals("text/html") ) {
   
      try {
	     //checks if file path is valid
         File inputFile = new File ( pathCopy );         
         FileReader readerFile = new FileReader ( inputFile );
         BufferedReader bufferedFile = new BufferedReader ( readerFile );
                 
         //reads through all lines and changes tags if found
         while ( (str = bufferedFile.readLine()) != null) {  
      
            if(str.contains("cs371date"))
		       str = str.replace("cs371date", df.format(d));  
        	   
	        if(str.contains("cs371server"))
		       str = str.replace("cs371server", "FINALLY IT WORKS!!!\n"); 
               
	           os.write( str.getBytes() );
               os.write("\n".getBytes() );
             
	     }//end while
      
      } // end try.
   
      catch ( FileNotFoundException e) {             
         
         System.err.println("File not found: " + path);
       
         os.write ( "<html>\n".getBytes( ) );
         os.write ( "<head>\n<title>ERROR 404</title></head>\n".getBytes( ) );
         os.write ( "<body>\n".getBytes( ) );
         os.write ( "<h1>404 Not Found</h1>\n".getBytes() );
         os.write ( "</body>\n".getBytes() );  
         os.write ( "</html>\n".getBytes( ) );               
                
      }// end catch. 
      
   } // end if.
      
   else if ( type.contains("image") ) {
   
      try {
         
		 //checks if file path is valid
         File inputFile = new File ( pathCopy );  
         FileInputStream bytesIn = new FileInputStream ( inputFile );
         
         //reading bytes into an array
     	 byte imageBytes [ ] = new byte [ (int) inputFile.length() ];
         bytesIn.read( imageBytes );
         
         //outputs bytes to os
         DataOutputStream bytesOut = new DataOutputStream( os );
         bytesOut.write( imageBytes );
      
      }catch ( FileNotFoundException e) {             
         
         System.err.println("File not found: " + path);
      
         os.write ( "<html>\n".getBytes( ) );
         os.write ( "<head>\n<title>ERROR 404</title></head>\n".getBytes( ) );
         os.write ( "<body>\n".getBytes( ) );
         os.write ( "<h1>404 Not Found</h1>\n".getBytes() );
        os.write ( "</body>\n".getBytes() );  
         os.write ( "</html>\n".getBytes( ) );               
             
      } 
      
   }
   
}//end writeContent
}//end class
