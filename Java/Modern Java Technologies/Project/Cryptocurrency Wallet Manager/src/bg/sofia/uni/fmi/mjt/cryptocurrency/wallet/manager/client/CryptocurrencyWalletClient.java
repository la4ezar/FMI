package bg.sofia.uni.fmi.mjt.cryptocurrency.wallet.manager.client;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CryptocurrencyWalletClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5555;

    private boolean isClientWorking = true;

    public static void main(String[] args) {
        new CryptocurrencyWalletClient().start();
    }

    public void start() {
        try (SocketChannel socketChannel = SocketChannel.open();
             BufferedReader reader = new BufferedReader(Channels.newReader(socketChannel, StandardCharsets.UTF_8));
             PrintWriter writer = new PrintWriter(
                     Channels.newWriter(socketChannel, StandardCharsets.UTF_8), true);
             Scanner scanner = new Scanner(System.in)) {

            socketChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));

            System.out.println("Connected to the server.");

            ExecutorService executor = Executors.newFixedThreadPool(1);
            runConsoleReader(executor, reader);

            isClientWorking = true;
            while (isClientWorking) {
                String message = scanner.nextLine();
                writer.println(message);
            }

            executor.shutdown();
        } catch (IOException e) {
            System.err.println("Unable to connect to the server. Try again later or contact administrator.");
            try {
                e.printStackTrace(new PrintWriter(
                        new FileOutputStream("client_errors.txt", true), true));
            } catch (FileNotFoundException fileNotFoundException) {
                System.out.println("File 'client_errors.txt' not found.");
                fileNotFoundException.printStackTrace();
            }
        }
    }

    private void runConsoleReader(ExecutorService thread, BufferedReader reader) {
        Callable<String> consoleReader = () -> {
            String line;
            while (true) {
                if ((line = reader.readLine()) != null) {
                    System.out.println(line);
                    if (line.equals("Disconnected from the server.")) {
                        isClientWorking = false;
                        return line;
                    }
                }
            }
        };
        thread.submit(consoleReader);
    }
}
