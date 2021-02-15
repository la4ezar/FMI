package bg.sofia.uni.fmi.mjt.cryptocurrency.wallet.manager.command;

import bg.sofia.uni.fmi.mjt.cryptocurrency.wallet.manager.offer.Offer;
import bg.sofia.uni.fmi.mjt.cryptocurrency.wallet.manager.user.User;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;


public class CommandExecutorTest {
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
    private static final String REQUEST_OFFERINGS = "request-list-offerings";

    private final User testUser = new User("Lacho", "123");

    private static Map<String, User> users;
    private static CommandExecutor commandExecutor;


    @BeforeClass
    public static void offeringsRequest() throws URISyntaxException {
        users = new HashMap<>();
        commandExecutor = new CommandExecutor(users);
        commandExecutor.request_offerings();
    }

    @Test
    public void testDefault() throws URISyntaxException {
        Command unknown_command = new Command("Unknown command", new String[]{});

        String expected = "Unknown command.";
        String actual = commandExecutor.execute(null, unknown_command);

        assertEquals("Unexpected return for 'default' section.", expected, actual);
    }

    @Test
    public void testRegisterSuccess() throws URISyntaxException {
        users.clear();
        Command register = new Command(REGISTER, new String[]{testUser.getName(), testUser.getPassword()});

        String expected = String.format("%s successfully registered.", testUser.getName());
        String actual = commandExecutor.execute(null, register);

        assertEquals("Unexpected return for 'register'", expected, actual);
    }

    @Test
    public void testRegisterAlreadyRegistered() throws URISyntaxException {
        Command register = new Command(REGISTER, new String[]{testUser.getName(), testUser.getPassword()});

        String expected = "User already registered.";

        commandExecutor.execute(null, register);
        String actual = commandExecutor.execute(null, register);

        assertEquals("Unexpected return for 'register'", expected, actual);
    }

    @Test
    public void testRegisterWhenCallerIsLoggedIn() throws URISyntaxException {
        Command register = new Command(REGISTER, new String[]{testUser.getName(), testUser.getPassword()});

        String expected = "You are already logged in.";
        String actual = commandExecutor.execute(testUser, register);

        assertEquals("Unexpected return for 'register'", expected, actual);
    }

    @Test
    public void testRegisterWithLessArguments() throws URISyntaxException {
        // register with name only
        Command register = new Command(REGISTER, new String[]{testUser.getName()});

        String expected = String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT,
                REGISTER, 2, REGISTER + " <username> <password>");

        String actual = commandExecutor.execute(testUser, register);

        assertEquals("Unexpected return for 'register' when the provided arguments are less than 2",
                expected, actual);
    }

    @Test
    public void testRegisterWithMoreArguments() throws URISyntaxException {
        Command register = new Command(REGISTER,
                new String[]{testUser.getName(), testUser.getPassword(), "Third Argument"});

        String expected = String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT,
                REGISTER, 2, REGISTER + " <username> <password>");

        String actual = commandExecutor.execute(testUser, register);

        assertEquals("Unexpected return for 'register' when the provided arguments are less than 2",
                expected, actual);
    }

    @Test
    public void testLoginSuccess() throws URISyntaxException {
        Command login = new Command(LOGIN, new String[]{testUser.getName(), testUser.getPassword()});
        users.put("Lacho", new User("Lacho", "123"));
        users.get("Lacho").logout();

        String expected = String.format("%s successfully logged in.", testUser.getName());
        String actual = commandExecutor.execute(null, login);

        assertEquals("Unexpected return for 'login'", expected, actual);
    }

    @Test
    public void testLoginWithNoSuchUser() throws URISyntaxException {
        Command login = new Command(LOGIN, new String[]{"RandomName", testUser.getPassword()});

        String expected = "No such user.";
        String actual = commandExecutor.execute(null, login);

        assertEquals("Unexpected return for 'login'", expected, actual);
    }

    @Test
    public void testLoginWhenCallerAlreadyLoggedIn() throws URISyntaxException {
        Command login = new Command(LOGIN, new String[]{testUser.getName(), testUser.getPassword()});

        String expected = "You are already logged in.";
        String actual = commandExecutor.execute(testUser, login);

        assertEquals("Unexpected return for 'login'", expected, actual);
    }

    @Test
    public void testLoginWhenOtherUserIsAlreadyLoggedIn() throws URISyntaxException {
        Command login = new Command(LOGIN, new String[]{testUser.getName(), testUser.getPassword()});
        users.put("Lacho", new User("Lacho", "123"));

        String expected = "Other user already logged in.";
        String actual = commandExecutor.execute(null, login);

        assertEquals("Unexpected return for 'login'", expected, actual);
    }

    @Test
    public void testLoginWithLessArguments() throws URISyntaxException {
        // login with name only
        Command login = new Command(LOGIN, new String[]{testUser.getName()});

        String expected = String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT,
                LOGIN, 2, LOGIN + " <username> <password>");

        String actual = commandExecutor.execute(testUser, login);

        assertEquals("Unexpected return for 'login' when the provided arguments are less than 2",
                expected, actual);
    }

    @Test
    public void testLoginWithMoreArguments() throws URISyntaxException {
        Command login = new Command(LOGIN,
                new String[]{testUser.getName(), testUser.getPassword(), "Third Argument"});

        String expected = String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT,
                LOGIN, 2, LOGIN + " <username> <password>");

        String actual = commandExecutor.execute(testUser, login);

        assertEquals("Unexpected return for 'login' when the provided arguments are less than 2",
                expected, actual);
    }

    @Test
    public void testDepositSuccess() throws URISyntaxException {
        Command deposit = new Command(DEPOSIT, new String[]{"1000"});

        String expected = String.format("%s successfully deposited %f", testUser.getName(), 1000.00);
        String actual = commandExecutor.execute(testUser, deposit);

        assertEquals("Unexpected return for 'deposit-money'", expected, actual);
    }

    @Test
    public void testDepositWhenNotLoggedIn() throws URISyntaxException {
        Command deposit = new Command(DEPOSIT, new String[]{"1000,00"});

        String expected = "You are not logged in.";
        String actual = commandExecutor.execute(null, deposit);

        assertEquals("Unexpected return for 'deposit-money'", expected, actual);
    }

    @Test
    public void testDepositWithNoArguments() throws URISyntaxException {
        Command deposit = new Command(DEPOSIT, new String[]{});

        String expected = String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT,
                DEPOSIT, 1, DEPOSIT + " <money_amount>");

        String actual = commandExecutor.execute(testUser, deposit);

        assertEquals("Unexpected return for 'deposit' when the provided arguments are 0",
                expected, actual);
    }

    @Test
    public void testDepositWithMoreArguments() throws URISyntaxException {
        Command deposit = new Command(DEPOSIT, new String[]{testUser.getName(), testUser.getPassword(), "1000.00"});

        String expected = String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT,
                DEPOSIT, 1, DEPOSIT + " <money_amount>");

        String actual = commandExecutor.execute(testUser, deposit);

        assertEquals("Unexpected return for 'deposit' when the provided arguments more than 1",
                expected, actual);
    }

    @Test
    public void testLogoutSuccess() throws URISyntaxException {
        Command logout = new Command(LOGOUT, new String[]{});

        String expected = String.format("%s successfully logout.", testUser.getName());
        String actual = commandExecutor.execute(testUser, logout);

        assertEquals("Unexpected return for 'logout'", expected, actual);
    }

    @Test
    public void testLogoutWhenNotLoggedIn() throws URISyntaxException {
        Command logout = new Command(LOGOUT, new String[]{});

        String expected = "You are not logged in.";
        String actual = commandExecutor.execute(null, logout);

        assertEquals("Unexpected return for 'logout'", expected, actual);
    }

    @Test
    public void testLogoutWithMoreArguments() throws URISyntaxException {
        Command logout = new Command(LOGOUT, new String[]{"RandomArgument"});

        String expected = String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT,
                LOGOUT, 0, LOGOUT);

        String actual = commandExecutor.execute(null, logout);

        assertEquals("Unexpected return for 'logout' when the provided arguments are more",
                expected, actual);
    }

    @Test
    public void testDisconnectSuccessWhenNotLoggedIn() throws URISyntaxException {
        Command disconnect = new Command(DISCONNECT, new String[]{});

        String expected = "Disconnected from the server.";
        String actual = commandExecutor.execute(null, disconnect);

        assertEquals("Unexpected return for 'disconnect'", expected, actual);
    }

    @Test
    public void testDisconnectSuccessWhenLoggedIn() throws URISyntaxException {
        Command disconnect = new Command(DISCONNECT, new String[]{});

        String expected = "Disconnected from the server.";
        String actual = commandExecutor.execute(testUser, disconnect);

        assertEquals("Unexpected return for 'disconnect'", expected, actual);
        assertFalse("Unexpected behaviour for 'disconnect' the user is not logged out", testUser.getIsLogged());
    }

    @Test
    public void testDisconnectWithMoreArguments() throws URISyntaxException {
        Command disconnect = new Command(DISCONNECT, new String[]{"Extra Arguments"});

        String expected = String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT,
                DISCONNECT, 0, DISCONNECT);
        String actual = commandExecutor.execute(testUser, disconnect);

        assertEquals("Unexpected return for 'disconnect' when the provided arguments are more",
                expected, actual);
    }

    @Test
    public void testHelp() throws URISyntaxException {
        Command help = new Command(HELP, new String[]{});

        String actual = commandExecutor.execute(null, help);

        String expected = String.format("%s %s%n", REGISTER, " <username> <password>") +
                String.format("%s %s%n", LOGIN, " <username> <password>") +
                String.format("%s %n", OFFERINGS) +
                String.format("%s %s%n", DEPOSIT, " <money_amount>") +
                String.format("%s %s%n", BUY, " <--offering=id> <--money=amount>") +
                String.format("%s %s%n", SELL, " <--offering=id>") +
                String.format("%s%n", SUMMARY) +
                String.format("%s%n", OVERALL_SUMMARY) +
                String.format("%s%n", LOGOUT) +
                String.format("%s%n", DISCONNECT);
        assertEquals("Unexpected return for 'help'", expected, actual);
    }

    @Test
    public void testHelpWithMoreArguments() throws URISyntaxException {
        Command help = new Command(HELP, new String[]{"Extra Arguments"});

        String expected = String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT,
                HELP, 0, HELP);
        String actual = commandExecutor.execute(testUser, help);

        assertEquals("Unexpected return for 'help' when the provided arguments are more",
                expected, actual);
    }

    @Test
    public void testListOfferingsWhenNotLoggedIn() throws URISyntaxException {
        Command listofferings = new Command(OFFERINGS, new String[]{});

        String expected = "You are not logged in.";
        String actual = commandExecutor.execute(null, listofferings);

        assertEquals("Unexpected return for 'list-offerings'", expected, actual);
    }

    @Test
    public void testListOfferingsSuccess() throws URISyntaxException {
        Command listofferings = new Command(OFFERINGS, new String[]{});

        String expected = "Offerings:";
        String actual = commandExecutor.execute(testUser, listofferings);
        String[] actualLines = actual.split(System.lineSeparator());
        assertTrue("Unexpected return for 'list-offerings'", actualLines.length > 0);
        assertEquals("Unexpected return for 'list-offerings'", expected, actualLines[0]);
        assertTrue("Unexpected offerings number return for 'list-offerings'",
                actualLines.length - 1 <= 100);

    }

    @Test
    public void testListOfferingsWithMoreArguments() throws URISyntaxException {
        Command listofferings = new Command(OFFERINGS, new String[]{"Extra Arguments"});

        String expected = String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT,
                OFFERINGS, 0, OFFERINGS);
        String actual = commandExecutor.execute(testUser, listofferings);

        assertEquals("Unexpected return for 'list-offerings' when the provided arguments are more",
                expected, actual);
    }

    @Test
    public void testRequestOfferings() throws URISyntaxException {
        Command requestofferings = new Command(REQUEST_OFFERINGS, new String[]{});

        String expected = "Successfully requested offerings.";
        String actual = commandExecutor.execute(testUser, requestofferings);

        assertEquals("Unexpected return for server requesting offerings", expected, actual);
    }

    @Test
    public void testBuyWhenNotLoggedIn() throws URISyntaxException {
        Command buy = new Command(BUY, new String[]{"--offering=something", "--money=something"});

        String expected = "You are not logged in.";
        String actual = commandExecutor.execute(null, buy);

        assertEquals("Unexpected return for 'buy'", expected, actual);
    }

    @Test
    public void testBuyWhenNoSuchCrypto() throws URISyntaxException {
        Command buy = new Command(BUY, new String[]{"--offering=something", "--money=something"});

        String expected = "No such cryptocurrency available in the offers.";
        String actual = commandExecutor.execute(testUser, buy);

        assertEquals("Unexpected return for 'buy'", expected, actual);
    }

    @Test
    public void testBuyWhenUserDontHaveEnoughMoney() throws URISyntaxException {
        Command buy = new Command(BUY, new String[]{"--offering=BTC", "--money=1000000"});

        String expected = String.format("%s don't have enough money.", testUser.getName());
        String actual = commandExecutor.execute(testUser, buy);

        assertEquals("Unexpected return for 'buy'", expected, actual);
    }

    @Test
    public void testBuySuccess() throws URISyntaxException {
        testUser.deposit(10000);
        Command buy = new Command(BUY, new String[]{"--offering=BTC", "--money=10000"});

        String expected = String.format("%s successfully buy %s for %f USD.",
                testUser.getName(), "Bitcoin", 10000.00);
        String actual = commandExecutor.execute(testUser, buy);

        assertEquals("Unexpected return for 'buy'", expected, actual);
    }

    @Test
    public void testBuyWhenMoreArguments() throws URISyntaxException {
        Command buy = new Command(BUY, new String[]{"--offering=BTC", "--money=10000", "Third argument"});

        String expected = String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, BUY, 2,
                BUY + " --offering=<offering_code> --money=<amount>");
        String actual = commandExecutor.execute(testUser, buy);

        assertEquals("Unexpected return for 'buy'", expected, actual);
    }

    @Test
    public void testBuyWhenLessArguments() throws URISyntaxException {

        Command buy = new Command(BUY, new String[]{"--offering=BTC"});

        String expected = String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, BUY, 2,
                BUY + " --offering=<offering_code> --money=<amount>");
        String actual = commandExecutor.execute(testUser, buy);

        assertEquals("Unexpected return for 'buy'", expected, actual);
    }

    @Test
    public void testBuyWhenFirstArgumentIsNotCorrect() throws URISyntaxException {
        Command buy = new Command(BUY, new String[]{"--crypto=BTC", "--money=10000"});

        String expected = String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, BUY, 2,
                BUY + " --offering=<offering_code> --money=<amount>");
        String actual = commandExecutor.execute(testUser, buy);

        assertEquals("Unexpected return for 'buy'", expected, actual);
    }

    @Test
    public void testBuyWhenSecondArgumentIsNotCorrect() throws URISyntaxException {
        Command buy = new Command(BUY, new String[]{"--offering=BTC", "--dollars=10000"});

        String expected = String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, BUY, 2,
                BUY + " --offering=<offering_code> --money=<amount>");
        String actual = commandExecutor.execute(testUser, buy);

        assertEquals("Unexpected return for 'buy'", expected, actual);
    }

    @Test
    public void testBuyWhenFirstArgumentDoNotHaveCryptoID() throws URISyntaxException {
        Command buy = new Command(BUY, new String[]{"--offering=", "--dollars=10000"});

        String expected = String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, BUY, 2,
                BUY + " --offering=<offering_code> --money=<amount>");
        String actual = commandExecutor.execute(testUser, buy);

        assertEquals("Unexpected return for 'buy'", expected, actual);
    }

    @Test
    public void testBuyWhenSecondArgumentDoNotHaveMoney() throws URISyntaxException {
        Command buy = new Command(BUY, new String[]{"--offering=BTC", "--dollars="});

        String expected = String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, BUY, 2,
                BUY + " --offering=<offering_code> --money=<amount>");
        String actual = commandExecutor.execute(testUser, buy);

        assertEquals("Unexpected return for 'buy'", expected, actual);
    }

    @Test
    public void testSellWhenNotLoggedIn() throws URISyntaxException {
        Command sell = new Command(SELL, new String[]{"--offering=BTC"});

        String expected = "You are not logged in.";
        String actual = commandExecutor.execute(null, sell);

        assertEquals("Unexpected return for 'sell'", expected, actual);
    }

    @Test
    public void testSellSuccess() throws URISyntaxException {
        testUser.deposit(1);
        testUser.buy(new Offer("BTC", "Bitcoin", 1, 1.00), 1.00);
        Command sell = new Command(SELL, new String[]{"--offering=BTC"});

        String expected = String.format("%s successfully sold %s", testUser.getName(), "BTC");
        String actual = commandExecutor.execute(testUser, sell);

        assertEquals("Unexpected return for 'sell'", expected, actual);
    }

    @Test
    public void testSellWhenNoSuchCryptoInWallet() throws URISyntaxException {
        Command sell = new Command(SELL, new String[]{"--offering=BTC"});

        String expected = "Cryptocurrency not available in the wallet.";
        String actual = commandExecutor.execute(testUser, sell);

        assertEquals("Unexpected return for 'sell'", expected, actual);
    }

    @Test
    public void testSellWhenNoSuchCryptoInOffers() throws URISyntaxException {
        Command sell = new Command(SELL, new String[]{"--offering=StrangeCryptocurrency"});

        String expected = "No such cryptocurrency available in the offers.";
        String actual = commandExecutor.execute(testUser, sell);

        assertEquals("Unexpected return for 'sell'", expected, actual);
    }

    @Test
    public void testSellWithMoreArguments() throws URISyntaxException {
        Command sell = new Command(SELL, new String[]{"--offering=BTC", "Extra argument"});

        String expected = String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT,
                SELL, 1, SELL + " --offering=<offering_code>");
        String actual = commandExecutor.execute(testUser, sell);

        assertEquals("Unexpected return for 'sell'", expected, actual);
    }

    @Test
    public void testSellWithLessArguments() throws URISyntaxException {
        Command sell = new Command(SELL, new String[]{});

        String expected = String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT,
                SELL, 1, SELL + " --offering=<offering_code>");
        String actual = commandExecutor.execute(testUser, sell);

        assertEquals("Unexpected return for 'sell'", expected, actual);
    }

    @Test
    public void testSellWhenArgumentDoNotHaveCryptoID() throws URISyntaxException {
        Command sell = new Command(SELL, new String[]{"--offering="});

        String expected = String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT,
                SELL, 1, SELL + " --offering=<offering_code>");
        String actual = commandExecutor.execute(testUser, sell);

        assertEquals("Unexpected return for 'sell'", expected, actual);
    }

    @Test
    public void testWalletSummaryWhenNotLoggedIn() throws URISyntaxException {
        Command summary = new Command(SUMMARY, new String[]{});

        String expected = "You are not logged in.";
        String actual = commandExecutor.execute(null, summary);

        assertEquals("Unexpected return for 'summary'", expected, actual);
    }

    @Test
    public void testWalletSummarySuccess() throws URISyntaxException {
        Command summary = new Command(SUMMARY, new String[]{});

        String firstLineExpected = "Amount of money in the wallet:";
        String secondLineExpected = "Amount of money invested:";
        String thirdLineExpected = String.format("%-5s %-20s %-30s %-30s %-30s",
                "ID", "Cryptocurrency", "Money amount(USD)", "Crypto amount", "Crypto price(USD)");
        String fourthLineExpected = String.format("%-5s %-20s %-30s %-30s %-30s",
                "_".repeat(5),  "_".repeat(20),  "_".repeat(30),  "_".repeat(30),  "_".repeat(30));

        String actual = commandExecutor.execute(testUser, summary);
        String[] actualLines = actual.split(System.lineSeparator());
        assertTrue("Unexpected return for 'summary'", actualLines.length >= 4);

        String firstLineActual = actualLines[0].substring(0, actualLines[0].lastIndexOf(':') + 1);
        String secondLineActual = actualLines[1].substring(0, actualLines[1].lastIndexOf(':') + 1);
        String thirdLineActual = actualLines[2];
        String fourthLineActual = actualLines[3];

        assertEquals("Unexpected return for 'summary'", firstLineExpected, firstLineActual);
        assertEquals("Unexpected return for 'summary'", secondLineExpected, secondLineActual);
        assertEquals("Unexpected return for 'summary'", thirdLineExpected, thirdLineActual);
        assertEquals("Unexpected return for 'summary'", fourthLineExpected, fourthLineActual);
    }

    @Test
    public void testWalletSummaryMoreArguments() throws URISyntaxException {
        Command summary = new Command(SUMMARY, new String[]{"Extra_argument"});

        String expected = String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, SUMMARY, 0, SUMMARY);
        String actual = commandExecutor.execute(testUser, summary);

        assertEquals("Unexpected return for 'summary'", expected, actual);
    }

    @Test
    public void testWalletOverallSummaryWhenNotLoggedIn() throws URISyntaxException {
        Command overall_summary = new Command(OVERALL_SUMMARY, new String[]{});

        String expected = "You are not logged in.";
        String actual = commandExecutor.execute(null, overall_summary);

        assertEquals("Unexpected return for 'overall_summary'", expected, actual);
    }

    @Test
    public void testWalletOverallSummarySuccess() throws URISyntaxException {
        Command overall_summary = new Command(OVERALL_SUMMARY, new String[]{});

        String firstLineExpected = "Amount of money in the wallet:";

        String secondLineExpected = String.format("Amount of money invested:");
        String thirdLineExpected = "Overall Net P&L:";
        String fourthLineExpected = String.format("%-5s %-20s %-30s %-30s %s",
                "ID", "Cryptocurrency", "Net P&L", "Current price(USD)", "Change(%)");
        // underlines
        String fifthLineExpected = String.format("%-5s %-20s %-30s %-30s %s",
                "_".repeat(5),  "_".repeat(20),  "_".repeat(30),  "_".repeat(30),  "_".repeat(10));

        String actual = commandExecutor.execute(testUser, overall_summary);
        String[] actualLines = actual.split(System.lineSeparator());
        assertTrue("Unexpected return for 'overall_summary'", actualLines.length >= 5);

        String firstLineActual = actualLines[0].substring(0, actualLines[0].lastIndexOf(':') + 1);
        String secondLineActual = actualLines[1].substring(0, actualLines[1].lastIndexOf(':') + 1);
        String thirdLineActual = actualLines[2].substring(0, actualLines[2].lastIndexOf(':') + 1);
        String fourthLineActual = actualLines[3];
        String fifthLineActual = actualLines[4];

        assertEquals("Unexpected return for 'overall_summary'", firstLineExpected, firstLineActual);
        assertEquals("Unexpected return for 'overall_summary'", secondLineExpected, secondLineActual);
        assertEquals("Unexpected return for 'overall_summary'", thirdLineExpected, thirdLineActual);
        assertEquals("Unexpected return for 'overall_summary'", fourthLineExpected, fourthLineActual);
        assertEquals("Unexpected return for 'overall_summary'", fifthLineExpected, fifthLineActual);
    }

    @Test
    public void testWalletOverallSummaryMoreArguments() throws URISyntaxException {
        Command overall_summary = new Command(OVERALL_SUMMARY, new String[]{"Extra_argument"});

        String expected = String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, OVERALL_SUMMARY, 0, OVERALL_SUMMARY);
        String actual = commandExecutor.execute(testUser, overall_summary);

        assertEquals("Unexpected return for 'overall_summary'", expected, actual);
    }
}
