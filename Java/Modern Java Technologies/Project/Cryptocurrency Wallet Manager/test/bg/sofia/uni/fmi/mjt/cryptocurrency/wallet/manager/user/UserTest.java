package bg.sofia.uni.fmi.mjt.cryptocurrency.wallet.manager.user;

import bg.sofia.uni.fmi.mjt.cryptocurrency.wallet.manager.cryptocurrency.Cryptocurrency;
import bg.sofia.uni.fmi.mjt.cryptocurrency.wallet.manager.offer.Offer;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class UserTest {
    private Double delta = 0.000000000000001;

    User testUser;

    @Before
    public void setUp() {
        testUser = new User("Lacho", "123");
    }

    @Test
    public void testLogin() {
        testUser.login();

        assertTrue("Unexpected behaviour of 'login'", testUser.getIsLogged());
    }

    @Test
    public void testLogout() {
        testUser.logout();

        assertFalse("Unexpected behaviour of 'logout'", testUser.getIsLogged());
    }

    @Test
    public void testDeposit() {
        assertEquals("Unexpected behaviour of 'deposit'",
                0, testUser.getWallet().getMoney(), delta);

        testUser.deposit(1000.50);

        assertEquals("Unexpected behaviour of 'deposit'",
                1000.50, testUser.getWallet().getMoney(), delta);
    }

    @Test
    public void testBuyWithNotEnoughMoney() {
        boolean actual = testUser.buy(new Offer("BTC", "Bitcoin", 1, 45000.00),
                1000.00);

        assertFalse("Unexpected behaviour of 'buy'", actual);
    }

    @Test
    public void testBuySuccess() {
        testUser.deposit(1000.00);
        boolean actual = testUser.buy(new Offer("BTC", "Bitcoin", 1, 45000.00),
                1000.00);

        assertTrue("Unexpected behaviour of 'buy'", actual);
    }

    @Test
    public void testSellSuccess() {
        testUser.getWallet().addCrypto(new Cryptocurrency
                ("Bitcoin", "BTC", 1000, 0.01, 10000));

        boolean actual = testUser.sell(new Offer("BTC", "Bitcoin", 1, 10000));

        assertTrue("Unexpected behaviour of 'buy'", actual);
    }

    @Test
    public void testSellWithoutCryptoInWallet() {
        boolean actual = testUser.sell(new Offer("BTC", "Bitcoin", 1, 10000));

        assertFalse("Unexpected behaviour of 'buy'", actual);
    }

    @Test
    public void testSummary() {
        String firstLineExpected = "Amount of money in the wallet:";
        String secondLineExpected = "Amount of money invested:";
        String thirdLineExpected = String.format("%-5s %-20s %-30s %-30s %-30s",
                "ID", "Cryptocurrency", "Money amount(USD)", "Crypto amount", "Crypto price(USD)");
        String fourthLineExpected = String.format("%-5s %-20s %-30s %-30s %-30s",
                "_".repeat(5),  "_".repeat(20),  "_".repeat(30),  "_".repeat(30),  "_".repeat(30));

        String actual = testUser.summary();
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
    public void testOverallSummary() {
        String firstLineExpected = "Amount of money in the wallet:";

        String secondLineExpected = String.format("Amount of money invested:");
        String thirdLineExpected = "Overall Net P&L:";
        String fourthLineExpected = String.format("%-5s %-20s %-30s %-30s %s",
                "ID", "Cryptocurrency", "Net P&L", "Current price(USD)", "Change(%)");
        // underlines
        String fifthLineExpected = String.format("%-5s %-20s %-30s %-30s %s",
                "_".repeat(5),  "_".repeat(20),  "_".repeat(30),  "_".repeat(30),  "_".repeat(10));

        String actual = testUser.overall_summary(new ArrayList<>());
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
}
