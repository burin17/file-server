package com.gmail.burinigor7.server;

import java.io.*;
import java.net.Socket;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.gmail.burinigor7.server.FileServer.SERVER_REPOSITORY;

public class FileTransferThread extends Thread{
    private final Socket client;
    private DataOutputStream out;
    private DataInputStream in;
    private final ZipOutputStream zipOut;


    public FileTransferThread(Socket client) throws IOException {
        this.client = client;
        this.out = new DataOutputStream(
                new BufferedOutputStream(
                        client.getOutputStream()));
        this.in = new DataInputStream(
                new BufferedInputStream(
                        client.getInputStream()));
        this.zipOut = new ZipOutputStream(
                new BufferedOutputStream(
                        client.getOutputStream()));
        start();
    }

    @Override
    public void run() {
        try(client; zipOut) {
            File[] availableDirectories = new File(SERVER_REPOSITORY)
                    .listFiles(File::isDirectory);
            StringBuilder answer = new StringBuilder("\nAvailable directories:\n");
            for(int i = 0; i < availableDirectories.length; ++i)
                answer.append(i + 1).append(" ---> ")
                        .append(availableDirectories[i]).append("\n");
            out.writeInt(availableDirectories.length);
            out.writeUTF(answer.toString()); out.flush();
            File requiredDirectory = availableDirectories[in.readInt()];
            out.writeUTF(requiredDirectory.getName()); out.flush();
            File[] directoryContent = requiredDirectory.listFiles();
            long start = System.currentTimeMillis();
            for(int i = 0; i < directoryContent.length; ++i) {
                zip(directoryContent[i], directoryContent[i].getName());
            }
            long end = System.currentTimeMillis();
            long res = end - start;
            System.out.println("Directory has transferred for " + res + " millis");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void zip(File file, String fileName) throws IOException {
        if(file.isDirectory()) {
            if(fileName.endsWith("/"))
                zipOut.putNextEntry(new ZipEntry(fileName));
            else
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
            zipOut.closeEntry();
            File[] subDirectories = file.listFiles();
            for(File subDirectory : subDirectories)
                zip(subDirectory, fileName + "/" + subDirectory.getName());
            return;
        }
        try(BufferedInputStream fileIn =
                    new BufferedInputStream(new FileInputStream(file))) {
            ZipEntry zipFile = new ZipEntry(fileName);
            zipOut.putNextEntry(zipFile);
            int i;
            while ((i = fileIn.read()) != -1) {
                zipOut.write(i);
            }
            zipOut.closeEntry();
        }
    }
}

