package bg.sofia.uni.fmi.mjt.cryptocurrency.wallet.manager.command;

import bg.sofia.uni.fmi.mjt.cryptocurrency.wallet.manager.offer.Offer;
import bg.sofia.uni.fmi.mjt.cryptocurrency.wallet.manager.user.User;
import bg.sofia.uni.fmi.mjt.cryptocurrency.wallet.manager.wallet.Wallet;
import org.junit.Before;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
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

    Map<String, User> users;
    private CommandExecutor commandExecutor;


    @Before
    public void setUp() {
        users = new HashMap<>();
        commandExecutor = new CommandExecutor(users);
    }

    @Test
    public void testRegisterSuccess() throws URISyntaxException {
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

        String expected = String.format("Offerings:%n");
        String actual = commandExecutor.execute(testUser, listofferings);

        assertEquals("Unexpected return for 'list-offerings'", expected, actual);
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
    public void testBuyWhenNotLoggedIn() {

    }

    @Test
    public void testBuyWhenNoSuchCrypto() {

    }

    @Test
    public void testBuyWhenUserDontHaveEnoughMoney() {

    }

    @Test
    public void testBuySuccess() {

    }

    @Test
    public void testBuyWhenMoreArguments() {

    }

    @Test
    public void testBuyWhenLessArguments() {

    }

    @Test
    public void testBuyWhenFirstArgumentIsNotCorrect() {

    }

    @Test
    public void testBuyWhenSecondArgumentIsNotCorrect() {

    }

    @Test
    public void testBuyWhenFirstArgumentDontHaveCryptoID() {

    }

    @Test
    public void testBuyWhenSecondArgumentDontHaveMoney() {

    }

}
