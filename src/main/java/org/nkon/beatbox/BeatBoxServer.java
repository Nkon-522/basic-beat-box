package org.nkon.beatbox;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BeatBoxServer {
    final List<ObjectOutputStream> clientOutputStreams = new ArrayList<>();
    int userId;
    private void init() {
        try {
            ServerSocket serverSocket = new ServerSocket(4242);
            ExecutorService threadPool = Executors.newCachedThreadPool();

            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());

                objectOutputStream.writeObject(userId++);

                clientOutputStreams.add(objectOutputStream);

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                threadPool.execute(clientHandler);
                System.out.println("Got a connection");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessageAll(Object one, Object two) {
        for (ObjectOutputStream clientOutputStream: clientOutputStreams) {
            try {
                clientOutputStream.writeObject(one);
                clientOutputStream.writeObject(two);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ClientHandler implements Runnable {
        private ObjectInputStream objectInputStream;

        public ClientHandler(Socket socket) {
            try {
                objectInputStream = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            Object userName;
            Object beatSequence;
            try {
                while ((userName = objectInputStream.readObject()) != null) {
                    beatSequence = objectInputStream.readObject();

                    System.out.println("Read two objects!");
                    sendMessageAll(userName, beatSequence);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new BeatBoxServer().init();
    }
}
