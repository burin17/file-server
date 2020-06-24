package com.gmail.burinigor7.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;

import static com.gmail.burinigor7.server.FileServer.GROUP_ADDRESS;
import static com.gmail.burinigor7.server.FileServer.GROUP_PORT;

public class DatagramListenerThread extends Thread {
    public DatagramListenerThread() {
        start();
    }

    @Override
    public void run() {
        try(MulticastSocket datagramReceiver = new MulticastSocket(GROUP_PORT)) {
            datagramReceiver.setNetworkInterface(NetworkInterface.getByInetAddress(
                    InetAddress.getLocalHost()));
            InetAddress group = InetAddress.getByName(GROUP_ADDRESS);
            datagramReceiver.joinGroup(group);
            byte[] buff = new byte[256];
            DatagramPacket packet = new DatagramPacket(buff, buff.length);
            while (true) {
                datagramReceiver.receive(packet);
                InetAddress sender = packet.getAddress();
                int senderPort = packet.getPort();
                packet = new DatagramPacket(buff, buff.length,
                        sender, senderPort);
                datagramReceiver.send(packet);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
