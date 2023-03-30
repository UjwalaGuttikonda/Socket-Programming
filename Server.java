import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class Server {

    private static final int PORT = 4007;

    public static void main(String[] args) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server listening on port " + PORT);
            AtomicBoolean continueRunning = new AtomicBoolean(true);
            while (continueRunning.get()) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket);

                // create a new thread
                Thread thread = new Thread(() -> {
                    try {
                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        String line = in.readLine().trim(); // reading the input
                        String[] parts = line.split("\\s+", 2); // split the input message 
                        String command = parts[0];
                        String filename = parts.length > 1 ? parts[1] : ""; // get the filename from the parts array, or set it to an empty string if there is no second part
                        if (command.equals("get")) {
                            sendFile(clientSocket, filename);
                        } else if (command.equals("upload")) {
                            receiveFile(clientSocket, filename);
                        }
                        else if (command.length() == 2 && (parts[0]).equals("exit")
							&& (parts[1]).equals("ftpclient")) {
                                continueRunning.set(false);
            } 
                        clientSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } 
                });

                thread.start();
            }
        }
    }

    private static void sendFile(Socket clientSocket, String filename) throws IOException {
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("File not found: " + filename);
            return;
        }
        System.out.println("Sending file: " + filename);
    
        // send file size to client
        try (DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
             FileInputStream fileIn = new FileInputStream(file)) {
            out.writeLong(file.length());
    
            // send file content to client
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileIn.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            
            out.flush(); 
        }
        System.out.println("File sent: " + filename);
    }
    
    

    private static void receiveFile(Socket clientSocket, String filename) throws IOException {
        System.out.println("Receiving file: " + filename);

        // receive file size from client
        DataInputStream in = new DataInputStream(clientSocket.getInputStream());
        long fileSize = in.readLong();

        try (// receive file content from client
        FileOutputStream fileOut = new FileOutputStream("new" +filename)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while (fileSize > 0 && (bytesRead = in.read(buffer, 0, (int) Math.min(buffer.length, fileSize))) != -1) {
                fileOut.write(buffer, 0, bytesRead);
                fileSize -= bytesRead;
            }
        }
        System.out.println("File received: " + filename);
    }
}
