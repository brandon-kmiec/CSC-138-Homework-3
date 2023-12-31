// Brandon Kmiec
// 12-01-2023
// WebServerA: multi-threaded webserver

import java.io.*;
import java.net.*;
import java.util.*;

public final class WebServerA {
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
            HttpRequestA request = new HttpRequestA(listenSocket);

            // Create a new thread to process the request
            Thread thread = new Thread(request);

            // Start the thread
            thread.start();
        }//end while
    }//end main
}//end WebServerA


final class HttpRequestA implements Runnable {
    final static String CRLF = "\r\n";
    Socket socket;

    // Constructor
    public HttpRequestA(Socket socket) throws Exception {
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
}//end HttpRequestA