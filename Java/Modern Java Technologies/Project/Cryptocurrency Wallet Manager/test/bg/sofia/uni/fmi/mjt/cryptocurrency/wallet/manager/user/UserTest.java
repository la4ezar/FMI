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
    User testUser;

    @Before
    public void setUp() {
        testUser = new User("Lacho", "123");
    }

    @Test
    public void testLogin() {
        testUser.login();

        assertTrue("Unexpected behaviour for 'login'", testUser.getIsLogged());
    }

    @Test
    public void testLogout() {
        testUser.logout();

        assertFalse("Unexpected behaviour for 'logout'", testUser.getIsLogged());
    }

    @Test
    public void testDeposit() {
        assertEquals("Unexpected behaviour for 'deposit'",
                0, testUser.getWallet().getMoney(), 0.0);

        testUser.deposit(1000.50);

        assertEquals("Unexpected behaviour for 'deposit'",
                1000.50, testUser.getWallet().getMoney(), 0.0);
    }

    @Test
    public void testBuyWithNotEnoughMoney() {
        boolean actual = testUser.buy(new Offer("BTC", "Bitcoin", 1, 45000.00),
                1000.00);

        assertFalse("Unexpected behaviour for 'buy'", actual);
    }

    @Test
    public void testBuySuccess() {
        testUser.deposit(1000.00);
        boolean actual = testUser.buy(new Offer("BTC", "Bitcoin", 1, 45000.00),
                1000.00);

        assertTrue("Unexpected behaviour for 'buy'", actual);
    }

    @Test
    public void testSellSuccess() {
        testUser.getWallet().addCrypto(new Cryptocurrency
                ("Bitcoin", "BTC", 1000, 0.01, 10000));

        boolean actual = testUser.sell(new Offer("BTC", "Bitcoin", 1, 10000));

        assertTrue("Unexpected behaviour for 'buy'", actual);
    }

    @Test
    public void testSellWithoutCryptoInWallet() {
        boolean actual = testUser.sell(new Offer("BTC", "Bitcoin", 1, 10000));

        assertFalse("Unexpected behaviour for 'buy'", actual);
    }

    @Test
    public void testSummary() {
        String firstLineExpected = "Amount of money in the wallet:";
        String secondLineExpected = "Cryptocurrencies:";

        String actual = testUser.summary();
        String[] actualLines = actual.split(System.lineSeparator());
        assertTrue("Unexpected return for 'summary'", actualLines.length >= 2);

        String firstLineActual = actualLines[0].substring(0, actualLines[0].lastIndexOf(':') + 1);
        String secondLineActual = actualLines[1];

        assertEquals("Unexpected return for 'summary'", firstLineExpected, firstLineActual);
        assertEquals("Unexpected return for 'summary'", secondLineExpected, secondLineActual);
    }

    @Test
    public void testOverallSummary() {
        String firstLineExpected = "Amount of money in the wallet:";
        String secondLineExpected = "Overall Net P&L:";
        String thirdLineExpected = String.format("%-5s %-20s %-30s %-30s %s",
                "ID", "Cryptocurrency", "Net P&L", "Current price", "Change");

        String actual = testUser.overall_summary(new ArrayList<>());
        String[] actualLines = actual.split(System.lineSeparator());
        assertTrue("Unexpected return for 'overall_summary'", actualLines.length >= 3);

        String firstLineActual = actualLines[0].substring(0, actualLines[0].lastIndexOf(':') + 1);
        String secondLineActual = actualLines[1].substring(0, actualLines[1].lastIndexOf(':') + 1);
        String thirdLineActual = actualLines[2];

        assertEquals("Unexpected return for 'overall_summary'", firstLineExpected, firstLineActual);
        assertEquals("Unexpected return for 'overall_summary'", secondLineExpected, secondLineActual);
        assertEquals("Unexpected return for 'overall_summary'", thirdLineExpected, thirdLineActual);
    }
}
