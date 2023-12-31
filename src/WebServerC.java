// Brandon Kmiec
// 12-01-2023
// WebServerC: implement MIME types for GIF and JPEG, process HEAD and GET requests, respond with 404 and 405 error codes

import java.io.*;
import java.net.*;
import java.util.*;

public final class WebServerC {
    public static void main(String[] args) throws Exception {
        // Set the port number
        int port = 6789;

        // Establish the listen socket
        ServerSocket serverSocket = new ServerSocket(port);

        // Process HTTP service requests in an infinite loop
        while (true) {
            // Listen for a TCP connection request
            Socket listenSocket = serverSocket.accept();

            // Construct an object to process the HTTP request message
            HttpRequestC request = new HttpRequestC(listenSocket);

            // Create a new thread to process the request
            Thread thread = new Thread(request);

            // Start the thread
            thread.start();
        }//end while
    }//end main
}//end WebServerC


final class HttpRequestC implements Runnable {
    final static String CRLF = "\r\n";
    Socket socket;

    // Constructor
    public HttpRequestC(Socket socket) throws Exception {
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
        String request = tokens.nextToken();
        String fileName = tokens.nextToken();
        String http = tokens.nextToken();

        // Display the request line if there is a HEAD request
        if (requestLine.contains("HEAD")) {
            // Display the request line
            System.out.println();
            System.out.println(requestLine);

            // Get and display the header lines
            String headerLine = null;
            while ((headerLine = br.readLine()).length() != 0) {
                System.out.println(headerLine);
            }//end while
        }//end if
        // Display the request file and construct a response message if there is a GET request
        else if (requestLine.contains("GET")) {
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
                entityBody = "<HTML><HEAD><TITLE>404 Not Found</TITLE></HEAD><BODY>404 Not Found</BODY></HTML>";
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
        }//end else if
        // Respond with 405 HTTP code if a method other than HEAD or GET is requested
        else {
            // Construct the response message
            String statusLine = http + " 405 Method Not Allowed" + CRLF;
            String contentTypeLine = "Content-type: " + contentType(fileName) + CRLF;
            String entityBody = "<HTML><HEAD><TITLE>405 Method Not Allowed</TITLE></HEAD>" +
                    "<BODY>405 Method Not Allowed</BODY></HTML>";

            // Send the status line
            os.writeBytes(statusLine);

            // Send the content type line
            os.writeBytes(contentTypeLine);

            // Send a blank line to indicate the end of the header lines
            os.writeBytes(CRLF);

            // Send the entity body
            os.writeBytes(entityBody);

            // Display the request line
            System.out.println();
            System.out.println(requestLine);

            // Get and display the header lines
            String headerLine = null;
            while ((headerLine = br.readLine()).length() != 0) {
                System.out.println(headerLine);
            }//end while
        }//end else

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
        if (fileName.endsWith(".jpeg") || fileName.endsWith(".jpg") || fileName.endsWith(".jpe") ||
                fileName.endsWith(".jif") || fileName.endsWith(".jfif") || fileName.endsWith(".jfi")) {
            return "image/jpeg";
        }//end if
        return "application/octet-stream";
    }//end contentType
}//end HttpRequestC
