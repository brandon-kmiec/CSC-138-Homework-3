// Brandon Kmiec
// 12-01-2023
// WebServerB: analyze the request and send a response

import java.io.*;
import java.net.*;
import java.util.*;

public class WebServerB {
    public static void main(String[] args) throws Exception {
        // Set the port number
        int port = 6789;

        // Establish the listen socket
        ServerSocket serverSocket = new ServerSocket(port); // TODO: 12/2/2023 might not be ServerSocket?

        // Process HTTP service requests in an infinite loop
        while (true) {
            // Listen for a TCP connection request
            Socket listenSocket = serverSocket.accept();

            // Construct an object to process the HTTP request message
            HttpRequestB request = new HttpRequestB(listenSocket);

            // Create a new thread to process the request
            Thread thread = new Thread(request);

            // Start the thread
            thread.start();
        }//end while
    }//end main
}//end WebServerB


final class HttpRequestB implements Runnable {
    final static String CRLF = "\r\n";
    Socket socket;

    // Constructor
    public HttpRequestB(Socket socket) throws Exception {
        this.socket = socket;
    }//end HttpRequest Constructor

    // Implement the run() method of the Runnable interface
    public void run() {
        try {
            processRequest();
        } catch (Exception e) {
            System.out.println(e);
        }//end try catch
    }//end run

    private void processRequest() throws Exception {
        // Get a reference to the socket's input and output stream
        InputStream is = socket.getInputStream();
        DataOutputStream os = new DataOutputStream(socket.getOutputStream());

        // Set up the input stream filters
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        // Get the request line of the HTTP request message
        String requestLine = br.readLine();

        // Extract the filename from the request line
        StringTokenizer tokens = new StringTokenizer(requestLine);
        tokens.nextToken();  // Skip over the method, which should be "GET"     TODO: assign to a variable for part C
        String fileName = tokens.nextToken();
        String http = tokens.nextToken();

        // Prepend a "." so that file request is within the current directory
        fileName = "." + fileName;

        // Open the requested file
        FileInputStream fis = null;
        boolean fileExists = true;
        try {
            fis = new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            fileExists = false;
        }// end try catch

        // Construct the response message
        String statusLine = null;
        String contentTypeLine = null;
        String entityBody = null;
        if (fileExists) {
            statusLine = http + " 200 OK" + CRLF;
            contentTypeLine = "Content-type: " + contentType(fileName) + CRLF;
        } else {
            statusLine = http + " 404 Not found" + CRLF;
            contentTypeLine = "Content-type: " + contentType(fileName) + CRLF;
            entityBody = "<HTML><HEAD><TITLE>Not Found</TITLE></HEAD><BODY>Not Found</BODY></HTML>";
        }//end if else

        // Send the status line
        os.writeBytes(statusLine);

        // Send the content type line
        os.writeBytes(contentTypeLine);

        // Send a blank line to indicate the end of the header lines
        os.writeBytes(CRLF);

        // Send the entity body
        if (fileExists) {
            sendBytes(fis, os);
            fis.close();
        } else {
            os.writeBytes(entityBody);
        }//end if else

        // Display the request line
        System.out.println();
        System.out.println(requestLine);

        // Get and display the header lines
        String headerLine = null;
        while ((headerLine = br.readLine()).length() != 0) {
            System.out.println(headerLine);
        }//end while

        // Close streams and socket
        os.close();
        br.close();
        socket.close();
    }//end processRequest

    private static void sendBytes(FileInputStream fis, OutputStream os) throws Exception {
        // Construct a 1K buffer to hold bytes on their way to the socket
        byte[] buffer = new byte[1024];
        int bytes = 0;

        // Copy requested file into the socket's output stream
        while ((bytes = fis.read(buffer)) != -1) {
            os.write(buffer, 0, bytes);
        }//end while
    }//end sendBytes

    private static String contentType(String fileName) {
        if (fileName.endsWith(".htm") || fileName.endsWith(".html")) {
            return "text/html";
        }//end if
        if (fileName.endsWith(".gif")) {
            return "image/gif";
        }//end if
        if (fileName.endsWith(".jpeg") || fileName.endsWith(".jpg")) {
            return "image/jpeg";
        }//end if
        return "application/octet-stream";
    }//end contentType
}//end HttpRequest