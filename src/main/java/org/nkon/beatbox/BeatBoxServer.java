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
    ServerSocket serverSocket;
    final List<ClientHandler> clientHandlerList = new ArrayList<>();
    int userId;
    private void init() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::closeServerSocket));
        try {
            serverSocket = new ServerSocket(4242);
            ExecutorService threadPool = Executors.newCachedThreadPool();

            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clientHandlerList.add(clientHandler);

                clientHandler.objectOutputStream.writeObject(userId++);

                threadPool.execute(clientHandler);
                System.out.println("Got a connection");
            }
        } catch (IOException e) {
            closeServerSocket();
        }

    }

    private void closeServerSocket() {
        System.out.println("Shutdown!");
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            Runtime.getRuntime().halt(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessageAll(Object one, Object two) {
        for (ClientHandler clientHandler: clientHandlerList) {
            try {
                clientHandler.objectOutputStream.writeObject(one);
                clientHandler.objectOutputStream.writeObject(two);
            } catch (IOException e) {
                clientHandler.close();
            }
        }
    }

    private class ClientHandler implements Runnable {
        private ObjectOutputStream objectOutputStream;
        private ObjectInputStream objectInputStream;
        private Socket socket;

        public ClientHandler(Socket socket) {
            try {
                this.socket = socket;
                objectInputStream = new ObjectInputStream(this.socket.getInputStream());
                objectOutputStream = new ObjectOutputStream(this.socket.getOutputStream());
            } catch (IOException e) {
                close();
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
                close();
            }
        }

        private void removeClientHandler() {
            clientHandlerList.remove(this);
            System.out.println("A user has disconnected!");
        }

        private void close() {
            removeClientHandler();
            try {
                if (objectInputStream != null) {
                    objectInputStream.close();
                }
                if (objectOutputStream != null) {
                    objectOutputStream.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new BeatBoxServer().init();
    }
}
