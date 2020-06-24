package com.gmail.burinigor7.client;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static com.gmail.burinigor7.server.FileServer.*;

public class FileClient {
    static final String CLIENT_REPOSITORY = "resources/client";
    public static void main(String[] args) {
        List<InetAddress> servers = available();
        if(servers.size() != 0) {
            Scanner scanner = new Scanner(System.in);
            int serverNum = -1;
            while (true) {
                System.out.print("Available servers:\n");
                for (int i = 0; i < servers.size(); ++i) {
                    InetAddress addr = servers.get(i);
                    System.out.println(i + 1 + " ---> IP: " + addr.getHostAddress()
                            + " ; Host name: " + addr.getHostName());
                }
                System.out.print("Specify address ---> ");
                serverNum = scanner.nextInt() - 1;
                if (serverNum >= 0 && serverNum < servers.size()) break;
                else System.out.println("Incorrect server number\n");
            }
            InetAddress serverAddr = servers.get(serverNum);
            try(Socket server = new Socket(serverAddr, SERVER_PORT)) {
                DataInputStream in = new DataInputStream(
                        new BufferedInputStream(
                                server.getInputStream()));
                DataOutputStream out = new DataOutputStream(
                        new BufferedOutputStream(
                                server.getOutputStream()));
                int dirCount = in.readInt();
                String answer = in.readUTF();
                int directoryNum;
                while (true) {
                    System.out.println(answer);
                    System.out.print("Specify directory ---> ");
                    directoryNum = scanner.nextInt() - 1;
                    if(directoryNum >= 0 && directoryNum < dirCount) break;
                    else System.out.println("Incorrect directory number");
                }
                out.writeInt(directoryNum); out.flush();
                try(BufferedOutputStream fileOut = new BufferedOutputStream(
                        new FileOutputStream(CLIENT_REPOSITORY + "/" + in.readUTF() + ".zip"))) {
                    int i;
                    while ((i = in.read()) != -1)
                        fileOut.write(i);
                    fileOut.flush();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else
            System.out.println("There aren't available servers");
    }

    private static List<InetAddress> available() {
        try(MulticastSocket datagramSender = new MulticastSocket()) {
            datagramSender.setNetworkInterface(NetworkInterface.getByInetAddress(
                    InetAddress.getLocalHost()));
            byte[] buff = new byte[256];
            InetAddress group = InetAddress.getByName(GROUP_ADDRESS);
            DatagramPacket packet =
                    new DatagramPacket(buff, buff.length, group, GROUP_PORT);
            datagramSender.send(packet);
            datagramSender.setSoTimeout(1000);
            packet = new DatagramPacket(buff, buff.length);
            List<InetAddress> res = new ArrayList<>();
            while (true) {
                try {
                    datagramSender.receive(packet);
                    res.add(packet.getAddress());
                } catch (SocketTimeoutException e) {
                    break;
                }
            }
            return res;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}