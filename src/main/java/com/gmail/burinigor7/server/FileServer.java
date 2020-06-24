package com.gmail.burinigor7.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class FileServer {
    public static final String GROUP_ADDRESS = "230.0.0.0";
    public static final int GROUP_PORT = 10000;
    public static final int SERVER_PORT = 20000;
    static final String SERVER_REPOSITORY = "resources/server";


    public static void main(String[] args) {
        new DatagramListenerThread();
        try(ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {

            while (true) {
                Socket client = serverSocket.accept();
                try {
                    new FileTransferThread(client);
                } catch (IOException e) {
                    client.close();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException();
        }

    }
}
