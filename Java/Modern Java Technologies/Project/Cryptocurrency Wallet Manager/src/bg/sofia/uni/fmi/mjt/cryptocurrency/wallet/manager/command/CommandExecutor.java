package bg.sofia.uni.fmi.mjt.cryptocurrency.wallet.manager.command;

import bg.sofia.uni.fmi.mjt.cryptocurrency.wallet.manager.offer.Offer;
import bg.sofia.uni.fmi.mjt.cryptocurrency.wallet.manager.user.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class CommandExecutor {
    // Client commands
    private static final String INVALID_ARGS_COUNT_MESSAGE_FORMAT
            = "Invalid count of arguments: \"%s\" expects %d arguments. Example: \"%s\"";
    private static final String REGISTER = "register";
    private static final String LOGIN = "login";
    private static final String DEPOSIT = "deposit-money";
    private static final String OFFERINGS = "list-offerings";
    private static final String BUY = "buy";
    private static final String SELL = "sell";
    private static final String SUMMARY = "get-wallet-summary";
    private static final String OVERALL_SUMMARY = "get-wallet-overall-summary";
    private static final String LOGOUT = "logout";
    private static final String DISCONNECT = "disconnect";
    private static final String HELP = "help";
    // Server commands
    private static final String REQUEST_OFFERINGS = "request-list-offerings";

    private Map<String, User> users;
    private final List<Offer> offers;

    // COIN API KEY
    // 55F0BA5A-E044-4BA5-BC0F-79ADE6277F6C

    public CommandExecutor(Map<String, User> users) {
        this.users = users;
        offers = new ArrayList<>();
    }

    public String execute(User user, Command cmd) throws URISyntaxException {
        return switch (cmd.command()) {
            case OFFERINGS -> list_offerings(cmd.arguments());
            case REGISTER -> register(user, cmd.arguments());
            case LOGIN -> login(user, cmd.arguments());
            case DEPOSIT -> deposit(user, cmd.arguments());
            case BUY -> buy(user, cmd.arguments());
            case SELL -> sell(user, cmd.arguments());
            case SUMMARY -> summary(user, cmd.arguments());
            case OVERALL_SUMMARY -> overall_summary(user, cmd.arguments());
            case LOGOUT -> logout(user, cmd.arguments());
            case DISCONNECT -> disconnect(user);
            case HELP -> help();
            case REQUEST_OFFERINGS -> request_offerings();
            default -> "Unknown command.";
        };
    }

    public String request_offerings() throws URISyntaxException {
        HttpClient client = HttpClient.newBuilder().build();
        URI uri = new URI("https", "rest.coinapi.io", "/v1/assets/", null);
        HttpRequest request = HttpRequest.newBuilder()
                .header("X-CoinAPI-Key", "55F0BA5A-E044-4BA5-BC0F-79ADE6277F6C")
                .uri(uri)
                .build();
        Gson gson = new Gson();
        Type type = new TypeToken<List<Offer>>(){}.getType();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body).thenAcceptAsync(x -> {
            offers.addAll(gson.fromJson(x, type));
            offers.removeIf(value -> value.getIsCrypto() == 0 || value.getPriceUsd() == 0);
        }).join();

        return "Successfully requested offerings.";
    }

    public String list_offerings(String[] args) throws URISyntaxException {
        if (args.length != 0) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, OFFERINGS, 0, OFFERINGS);
        }
        offers.clear();

        request_offerings();

        StringBuilder response = new StringBuilder(String.format("Offerings:%n"));

        offers.stream()
                .limit(100)
                .filter(crypto -> crypto.getPriceUsd() > 0)
                .forEach(crypto -> response.append(String.format("[id: %s, name: %s, price: %f]%n",
                        crypto.getAssetId(), crypto.getName(), crypto.getPriceUsd())));

        return response.toString();
    }

    public String register(User user, String[] args) {
        if (args.length != 2) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, REGISTER, 2, REGISTER + " <username> <password>");
        }
        StringBuilder response = new StringBuilder("");
        if (user != null) {
            return "You are already logged in.";
        }
        if (users.containsKey(args[0])) {
            return "User already registered.";
        } else {
            users.put(args[0], new User(args[0], args[1]));
            response.append(String.format("%s successfully registered.", args[0]));
            return response.toString();
        }
    }

    public String login(User user, String[] args) {
        if (args.length != 2) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, LOGIN, 2, LOGIN + " <username> <password>");
        }
        if (user != null) {
            return ("You are already logged in.");
        }

        User current_user = users.get(args[0]);
        if (current_user == null) {
            return "No such user.";
        } else {
            if (current_user.getIsLogged()) {
                return "User already logged.";
            } else {
                StringBuilder response = new StringBuilder("");
                response.append(String.format("%s successfully logged.", args[0]));
                current_user.login();
                return response.toString();
            }
        }
    }

    public String deposit(User user, String[] args) {
        if (args.length != 1) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, DEPOSIT, 1, DEPOSIT + " <money_amount>");
        }
        if (user == null) {
            return "You are not logged in.";
        }
        user.deposit(Double.parseDouble(args[0]));

        return String.format("%s successfully deposited %f", user.getName(), Double.parseDouble(args[0]));
    }

    public String buy(User user, String[] args) {
        if (args.length != 2) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, BUY, 2,
                    BUY + " --offering=<offering_code> --money=<amount>");
        }
        if (!args[0].contains("--offering=") || !args[1].contains("--money=")) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, BUY, 2,
                    BUY + " --offering=<offering_code> --money=<amount>");
        }
        args[0] = args[0].replace("--offering=", "");
        args[1] = args[1].replace("--money=", "");
        if (args[0].length() == 0 || args[1].length() == 0) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, BUY, 2,
                    BUY + " --offering=<offering_code> --money=<amount>");
        }

        if (user == null) {
            return "You are not logged in.";
        }

        int ind = offers.lastIndexOf(new Offer(args[0], "", 0, 0));
        if (ind == -1) {
            return "No such cryptocurrency available in the offers.";
        } else {
            if (user.buy(offers.get(ind), Double.parseDouble(args[1]))) {
                return String.format("%s successfully buy %s for %f USD.",
                        user.getName(), offers.get(ind).getName(), Double.parseDouble(args[1]));
            } else {
                return String.format("%s don't have enough money.", user.getName());
            }
        }
    }

    public String sell(User user, String[] args) {
        if (args.length != 1) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, SELL, 1, SELL + " --offering=<offering_code>");
        }
        if (!args[0].contains("--offering=")) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, SELL, 1, SELL + " --offering=<offering_code>");
        }
        args[0] = args[0].replace("--offering=", "");
        if (args[0].length() == 0) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, SELL, 1, SELL + " --offering=<offering_code>");
        }

        if (user == null) {
            return "You are not logged in.";
        }

        int ind = offers.lastIndexOf(new Offer(args[0], "", 0, 0));
        if (ind == -1) {
            return "No such cryptocurrency available in the offers.";
        } else {
            if (user.sell(offers.get(ind))) {
                return String.format("%s successfully sold %s", user.getName(), args[0]);
            } else {
                return "Cryptocurrency not available in the wallet.";
            }
        }
    }

    public String summary(User user, String[] args) {
        if (args.length != 0) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, SUMMARY, 0, SUMMARY);
        }
        if (user == null) {
            return "You are not logged in.";
        }

        return user.summary();
    }

    public String overall_summary(User user, String[] args) {
        if (args.length != 0) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, OVERALL_SUMMARY, 0, OVERALL_SUMMARY);
        }
        if (user == null) {
            return "You are not logged in.";
        }

        return user.overall_summary(offers);
    }

    public String logout(User user, String[] args) {
        if (args.length != 0) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, LOGOUT, 0, LOGOUT);
        }
        if (user == null) {
            return "You are not logged in.";
        }
        user.logout();
        StringBuilder response = new StringBuilder("");
        response.append(String.format("%s successfully logout.", user.getName()));
        return response.toString();
    }

    public String disconnect(User user) {
        if (user != null) {
            user.logout();
        }
        return "Disconnected from the server.";
    }

    public String help() {
        StringBuilder response = new StringBuilder("");
        response.append(String.format("%s %s%n", REGISTER, " <username> <password>"));
        response.append(String.format("%s %s%n", LOGIN, " <username> <password>"));
        response.append(String.format("%s %n", OFFERINGS));
        response.append(String.format("%s %s%n", DEPOSIT, " <money_amount>"));
        response.append(String.format("%s %s%n", BUY, " <--offering=id> <--money=amount>"));
        response.append(String.format("%s %s%n", SELL, " <--offering=id>"));
        response.append(String.format("%s%n", SUMMARY));
        response.append(String.format("%s%n", OVERALL_SUMMARY));
        response.append(String.format("%s%n", LOGOUT));
        response.append(String.format("%s%n", DISCONNECT));
        return response.toString();
    }
}
