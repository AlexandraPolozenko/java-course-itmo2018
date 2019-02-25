package ru.ifmo.rain.polozenko.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HelloUDPClient implements HelloClient {
    public static void main(String[] args) {
        try {
            HelloUDPClient client = new HelloUDPClient();
            client.run(args[0], Integer.parseInt(args[1]), args[2], Integer.parseInt(args[3]), Integer.parseInt(args[4]));
        } catch (NumberFormatException | NullPointerException | ArrayIndexOutOfBoundsException e) {
            System.err.println("Invalid arguments");
        }
    }

    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        SocketAddress serverAddress = new InetSocketAddress(host, port);
        ExecutorService senderPool = Executors.newFixedThreadPool(threads);
        Collection<Callable<Void>> requestWorkers = new ArrayList<>();

        for (int threadIndex = 0; threadIndex < threads; threadIndex++) {
            final int threadNumber = threadIndex;
            requestWorkers.add(() -> {
                DatagramSocket socket = new DatagramSocket();
                socket.setSoTimeout(100);
                int bufferSize = socket.getReceiveBufferSize();
                DatagramPacket response = new DatagramPacket(new byte[bufferSize], bufferSize);

                for (int requestIndex = 0; requestIndex < requests; requestIndex++) {
                    String query = prefix + threadNumber + "_" + requestIndex;
                    byte data[] = query.getBytes("UTF-8");
                    DatagramPacket request = new DatagramPacket(data, data.length, serverAddress);

                    while (!Thread.interrupted()) {
                        try {
                            socket.send(request);
                            try {
                                socket.receive(response);
                                String responseString = new String(response.getData(), response.getOffset(),
                                        response.getLength(), "UTF-8");
                                String expectedResponse = new String(("Hello, " + query).getBytes("UTF-8"));
                                if (!expectedResponse.equals(responseString)) {
                                    continue;
                                }
                                System.out.println(new String(responseString.getBytes(Charset.defaultCharset())));
                                break;
                            } catch (IOException e) {
                                System.err.println("Unable to receive packet: " + e.getMessage());
                            }
                        } catch (IOException e) {
                            System.err.println("Unable to send packet: " + e.getMessage());
                        }
                    }
                }

                socket.close();
                return null;
            });
        }
        try {
            senderPool.invokeAll(requestWorkers);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            senderPool.shutdown();
        }
    }
}