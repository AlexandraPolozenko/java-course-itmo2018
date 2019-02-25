package ru.ifmo.rain.polozenko.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class HelloUDPServer implements HelloServer {

    private LinkedBlockingQueue<ServerTasks> runningTasks;

    public static void main(String[] args) {
        try (HelloServer server = new HelloUDPServer()) {
            server.start(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        } catch (NumberFormatException | NullPointerException | ArrayIndexOutOfBoundsException e) {
            System.err.println("Invalid arguments");
        }
    }

    public HelloUDPServer() {
        runningTasks = new LinkedBlockingQueue<>();
    }

    @Override
    public void start(int port, int threads) {
        try {
            ServerTasks task = new ServerTasks(port, threads);
            runningTasks.add(task);
            task.run();
        } catch (IOException e) {
            System.err.println("Unable to run client");
        }
    }

    @Override
    public void close() {
        runningTasks.forEach(ServerTasks::close);
    }

    private static class ServerTasks {

        private DatagramSocket socket;
        private ExecutorService threadPool;
        private int bufferSize;
        private int threads;

        ServerTasks(int port, int threads) throws IOException {
            this.threads = threads;
            threadPool = Executors.newFixedThreadPool(threads);
            socket = new DatagramSocket(port);
            bufferSize = socket.getReceiveBufferSize();
        }

        void run() {
            List<Callable<Void>> responseWorkers = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                responseWorkers.add(() -> {
                    DatagramPacket request = new DatagramPacket(new byte[bufferSize], bufferSize);
                    while (!Thread.interrupted()) {
                        try {
                            socket.receive(request);
                            String requestString = new String(request.getData(), request.getOffset(),
                                    request.getLength(), "UTF-8");
                            String responseString = new String(("Hello, " + requestString).getBytes("UTF-8"));

                            byte[] sendData = responseString.getBytes("UTF-8");
                            DatagramPacket response = new DatagramPacket(sendData, sendData.length,
                                    request.getAddress(), request.getPort());
                            try {
                                socket.send(response);
                            } catch (IOException e) {
                                System.err.println("Unable to send response: " + e.getMessage());
                            }
                        } catch (IOException e) {
                            System.err.println("Unable to receive request: " + e.getMessage());
                        }
                    }
                    return null;
                });
            }
            responseWorkers.forEach(task -> threadPool.submit(task));
        }

        void close() {
            threadPool.shutdownNow();
            socket.close();
        }
    }
}