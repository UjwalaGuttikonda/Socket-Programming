import java.io.*;
import java.net.Socket;
import java.util.Scanner;


public class Client {

    private static final String SERVER_IP = "localhost";

    public static void main(String[] args) throws IOException {

        try (
            Scanner scanner = new Scanner(System.in)) {
                String input;
                 String[] splitInput;
                 boolean isValidInput = false;
    
                 do {
                     System.out.println("Enter command to start the client socket (format: ftpclient <portnumber>): ");
                     input = scanner.nextLine().trim();
                     splitInput = input.split(" ");
                     
                     if (splitInput.length != 2 || !splitInput[0].equals("ftpclient")) {
                         System.out.println("Invalid input. Please enter a command in the format of 'ftpclient <portnumber>'.");
                         continue;
                     }
                     
                     if (!isNumeric(splitInput[1]) || Integer.parseInt(splitInput[1]) != 4007) {
                         System.out.println("Invalid port number. Please enter a valid integer port number of 4007.");
                         continue;
                     }
                     
                     isValidInput = true;
                 } while (!isValidInput);
    
                 String serverPort = splitInput[1];

        // send/receive loop
        while (true) {
           

                Socket socket = new Socket(SERVER_IP, Integer.parseInt(serverPort));
                // get user input
                System.out.print("Enter filename and select upload or get: ");
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                String userInput = reader.readLine().trim();
                if (userInput.length() == 0) {
                	continue;
                }

                // send user input to server
                PrintWriter outwriter = new PrintWriter(socket.getOutputStream(), true);
                outwriter.println(userInput);

                // parse user input
                String[] parts = userInput.split(" ");
                String command = parts[0];
                String filename = parts[1];

                if (command.equals("get")) {
                    receiveFile(socket, filename);
                } else if (command.equals("upload")) {
                    sendFile(socket, filename);
                }
                else if (command.length() == 2 && (parts[0]).equals("exit")
							&& (parts[1]).equals("ftpclient")) {
						break;
            } 
        }
    }
    }

    private static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
    }

    private static void sendFile(Socket socket, String filename) throws IOException {
        System.out.println("Uploading file: " + filename);

        try (
        FileInputStream fileIn = new FileInputStream(filename)) {
            byte[] buffer = new byte[1024];

            // send filename and file size to server
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeLong(fileIn.getChannel().size());

            // send file contents to server
            int bytesRead;
            while ((bytesRead = fileIn.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
        }
        System.out.println("File uploaded: " + filename);
    }

    private static void receiveFile(Socket clientSocket, String filename) throws IOException {
        System.out.println("Receiving file: " + filename);
    
        // receive file size from client
        DataInputStream in = new DataInputStream(clientSocket.getInputStream());
        long fileSize = in.readLong();
    
        try (// receive file contents from client
        FileOutputStream fileOut = new FileOutputStream("new" +filename)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while (fileSize > 0) {
                try {
                    bytesRead = in.read(buffer, 0, (int) Math.min(buffer.length, fileSize));
                    if (bytesRead == -1) {
                        throw new EOFException("End of stream reached before entire file was received");
                    }
                    fileOut.write(buffer, 0, bytesRead);
                    fileSize -= bytesRead;
                } catch (EOFException e) {
                    System.err.println("Error receiving file: " + e.getMessage());
                    break;
                }
            }
        }
        System.out.println("File received: " + filename);
    } 

}
