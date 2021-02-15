package bg.sofia.uni.fmi.mjt.cryptocurrency.wallet.manager.server;

import bg.sofia.uni.fmi.mjt.cryptocurrency.wallet.manager.command.CommandCreator;
import bg.sofia.uni.fmi.mjt.cryptocurrency.wallet.manager.command.CommandExecutor;
import bg.sofia.uni.fmi.mjt.cryptocurrency.wallet.manager.user.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CryptocurrencyWalletServer {
    private static final String REQUEST_OFFERINGS = "request-list-offerings";

    private static final int BUFFER_SIZE = 4096;
    private static final String HOST = "localhost";

    private final CommandExecutor commandExecutor;

    private final Map<String, User> users;

    private final int port;
    private boolean isServerWorking;

    private ByteBuffer buffer;
    private Selector selector;

    public CryptocurrencyWalletServer(int port) {
        this.users = new HashMap<>();
        this.port = port;
        this.commandExecutor = new CommandExecutor(users);
    }

    public void start() {
        // read users from file
        readUsers();

        // run thread for server console input
        ExecutorService consoleReader = Executors.newFixedThreadPool(1);
        runReader(consoleReader);

        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            selector = Selector.open();
            configureServerSocketChannel(serverSocketChannel, selector);
            this.buffer = ByteBuffer.allocate(BUFFER_SIZE);
            isServerWorking = true;

            // requesting offerings every 30 minutes
            ScheduledExecutorService list_offerings_thread = Executors.newScheduledThreadPool(1);
            request_offerings(list_offerings_thread);

            while (isServerWorking) {
                try {
                    int readyChannels = selector.select(1);
                    if (readyChannels == 0) {
                        continue;
                    }

                    Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                    while (keyIterator.hasNext()) {
                        SelectionKey key = keyIterator.next();
                        if (key.isReadable()) {
                            SocketChannel clientChannel = (SocketChannel) key.channel();
                            try {
                                String clientInput = getClientInput(clientChannel);

                                if (clientInput == null) {
                                    continue;
                                }
                                System.out.println(clientInput);

                                String output = commandExecutor.execute((User) key.attachment(), CommandCreator.newCommand(clientInput));

                                // attach/detach the SelectionKey to/from its user
                                attachKeyToUser(key, output);

                                writeClientOutput(clientChannel, output + System.lineSeparator());
                            } catch (IOException e) {
                                clientChannel.close();
                                System.out.println("Error occurred while processing client request: " + e.getMessage());
                                e.printStackTrace(new PrintWriter(
                                        new FileOutputStream("errors.txt", true)));
                            }
                        } else if (key.isAcceptable()) {
                            accept(selector, key);
                        }

                        keyIterator.remove();
                    }
                } catch (URISyntaxException e) {
                    System.out.println("Error occurred while creating the URI for offerings request: "
                            + e.getMessage());
                    e.printStackTrace(new PrintWriter(new FileOutputStream("errors.txt", true)));
                }
            }

            list_offerings_thread.shutdown();
            consoleReader.shutdown();
        } catch (IOException e) {
            consoleReader.shutdown();
            throw new UncheckedIOException("Failed to start server.", e);
            //System.out.println("Failed to start the server.");
            //e.printStackTrace(new PrintWriter("server_errors.txt"));
        }
    }

    public void stop() {
        this.isServerWorking = false;
        if (selector.isOpen()) {
            selector.wakeup();
        }

        storeUsers();
    }

    private void attachKeyToUser(SelectionKey key, String server_output) {
        if (server_output.contains("successfully registered")) {
            String username = server_output.split(" ")[0];
            key.attach(users.get(username));
        } else if (server_output.contains("successfully logged")) {
            String username = server_output.split(" ")[0];
            key.attach(users.get(username));
        } else if (server_output.contains("successfully logout") || server_output.contains("Disconnected")) {
            key.attach(null);
        }
    }

    private void request_offerings(ScheduledExecutorService thread) {
        Runnable get_list_offerings = () -> {
            try {
                getListOfferings();
            } catch (InterruptedException | IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        };
        thread.scheduleAtFixedRate(get_list_offerings, 0, 30, TimeUnit.MINUTES);
    }

    private void configureServerSocketChannel(ServerSocketChannel channel, Selector selector) throws IOException {
        channel.bind(new InetSocketAddress(HOST, this.port));
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_ACCEPT);
    }

    private String getClientInput(SocketChannel clientChannel) throws IOException {
        buffer.clear();

        int readBytes = clientChannel.read(buffer);
        if (readBytes < 0) {
            clientChannel.close();
            return null;
        }

        buffer.flip();

        byte[] clientInputBytes = new byte[buffer.remaining()];
        buffer.get(clientInputBytes);

        return new String(clientInputBytes, StandardCharsets.UTF_8);
    }

    private void writeClientOutput(SocketChannel clientChannel, String output) throws IOException {
        while (output.length() > BUFFER_SIZE) {
            buffer.clear();
            buffer.put(output.substring(0, BUFFER_SIZE).getBytes());
            buffer.flip();

            clientChannel.write(buffer);
            output = output.substring(BUFFER_SIZE);
        }
        buffer.clear();
        buffer.put(output.getBytes());
        buffer.flip();

        clientChannel.write(buffer);
    }

    private void accept(Selector selector, SelectionKey key) throws IOException {
        key.attach(null);
        ServerSocketChannel sockChannel = (ServerSocketChannel) key.channel();
        SocketChannel accept = sockChannel.accept();

        accept.configureBlocking(false);
        accept.register(selector, SelectionKey.OP_READ);
    }

    private void getListOfferings() throws InterruptedException, IOException, URISyntaxException {
        String response = commandExecutor.execute(null, CommandCreator.newCommand(REQUEST_OFFERINGS));
        System.out.println(response);
    }

    private void runReader(ExecutorService thread) {
        Scanner scanner = new Scanner(System.in);
        Runnable runnable = () -> {
            String line;
            while (true) {
                if ((line = scanner.nextLine()) != null) {
                    if (line.equals("stop")) {
                        System.out.println("Server stopped.");
                        stop();
                        break;
                    }
                }
            }
        };
        thread.submit(runnable);
    }

    private void storeUsers() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("users.json"))) {
            Gson gson = new Gson();
            // logout every user
            users.forEach((name, user) -> {
                user.logout();
                // encode the password
                user.setPassword(Base64.getEncoder().encodeToString(user.getPassword().getBytes()));
            });
            writer.write(gson.toJson(users));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readUsers() {
        if (!(new File("users.json").exists()) || new File("users.json").length() == 0) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader("users.json"))) {
            Gson gson = new Gson();
            Type type = new TypeToken<HashMap<String, User>>() {
            }.getType();
            Map<String, User> fileUsers = gson.fromJson(reader, type);
            fileUsers.forEach((name, user) -> {
                user.setPassword(new String(Base64.getDecoder().decode(user.getPassword())));
            });
            users.putAll(fileUsers);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        //new CryptocurrencyWalletServer(5555).start();

        CryptocurrencyWalletServer server = new CryptocurrencyWalletServer(5555);
        Thread serverThread = new Thread(server::start);
        serverThread.start();
        serverThread.join();
        server.stop();
    }
}
