package bg.sofia.uni.fmi.mjt.cryptocurrency.wallet.manager.wallet;

import bg.sofia.uni.fmi.mjt.cryptocurrency.wallet.manager.cryptocurrency.Cryptocurrency;
import bg.sofia.uni.fmi.mjt.cryptocurrency.wallet.manager.offer.Offer;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class WalletTest {
    private Double delta15 = 0.000000000000001;
    private Double delta10 = 0.0000000001;
    private Double delta5 = 0.00001;
    private Double delta1 = 0.1;

    private Wallet testWallet;

    @Before
    public void setUp() {
        testWallet = new Wallet();
    }

    @Test
    public void testDeposit() {
        assertEquals(0, testWallet.getMoney(), delta5);

        testWallet.deposit(1000.03);

        assertEquals("Unexpected behaviour of 'deposit'", 1000.03, testWallet.getMoney(), delta5);
    }

    @Test
    public void testWithdrawSuccess() {
        testWallet.deposit(1000.03123123);

        boolean actual = testWallet.withdraw(1000);

        assertTrue("Unexpected behaviour of 'withdraw'", actual);
        assertEquals("Unexpected behaviour of 'withdraw'", 0.03123123, testWallet.getMoney(), delta10);
    }

    @Test
    public void testWithdrawWithoutEnoughMoney() {
        boolean actual = testWallet.withdraw(1000);

        assertFalse("Unexpected behaviour of 'withdraw'", actual);
    }

    @Test
    public void testWithdrawWithoutEnoughMoney2() {
        testWallet.deposit(999.9999999999);
        boolean actual = testWallet.withdraw(1000);

        assertFalse("Unexpected behaviour of 'withdraw'", actual);
    }

    @Test
    public void testAddCrypto() {
        testWallet.addCrypto(new Cryptocurrency(
                "Bitcoin", "BTC", 1000, 0.1, 10000));
        Cryptocurrency crypto = testWallet.getCryptos().get(0);

        assertEquals("Unexpected behaviour of 'addCrypto'", "BTC", crypto.getId());
        assertEquals("Unexpected behaviour of 'addCrypto'", "Bitcoin", crypto.getName());
        assertEquals("Unexpected behaviour of 'addCrypto'", 1000, crypto.getMoney_amount(), delta15);
        assertEquals("Unexpected behaviour of 'addCrypto'", 0.1, crypto.getCrypto_amount(), delta1);
        assertEquals("Unexpected behaviour of 'addCrypto'", 10000, crypto.getPrice(), delta15);

    }

    @Test
    public void testSellCryptoWithOneEqualCrypto() {
        testWallet.addCrypto(new Cryptocurrency(
                "Bitcoin", "BTC", 1000, 1, 1000));
        boolean actual = testWallet.sellCrypto(new Offer("BTC", "Bitcoin", 1, 1050));

        assertTrue("Unexpected behaviour of 'sellCrypto'", actual);
        // the difference is 50 USD
        assertEquals("Unexpected behaviour of 'sellCrypto'", 50, testWallet.getMoney(), delta15);

    }

    @Test
    public void testSellCryptoWithOneEqualCrypto2() {
        testWallet.addCrypto(new Cryptocurrency(
                "Ethereum", "ETH", 1000, 1, 100));
        testWallet.addCrypto(new Cryptocurrency(
                "Bitcoin", "BTC", 1000, 1, 1000));
        boolean actual = testWallet.sellCrypto(new Offer("BTC", "Bitcoin", 1, 1050));

        assertTrue("Unexpected behaviour of 'sellCrypto'", actual);
        // the difference is 50 USD
        assertEquals("Unexpected behaviour of 'sellCrypto'", 50, testWallet.getMoney(), delta15);
    }

    @Test
    public void testSellCryptoWithMoreEqualCrypto() {
        testWallet.addCrypto(new Cryptocurrency(
                "Bitcoin", "BTC", 100, 0.1, 1000));
        testWallet.addCrypto(new Cryptocurrency(
                "Bitcoin", "BTC", 1000, 1, 1005));
        boolean sell1 = testWallet.sellCrypto(new Offer("BTC", "Bitcoin", 1, 1050));

        assertTrue("Unexpected behaviour of 'sellCrypto'", sell1);
        // the difference is 5 USD
        assertEquals("Unexpected behaviour of 'sellCrypto'", 5, testWallet.getMoney(), delta15);

        boolean sell2 = testWallet.sellCrypto(new Offer("BTC", "Bitcoin", 1, 1050));
        // the old 5 USD + the new difference which is 50 USD = 55 USD
        assertEquals("Unexpected behaviour of 'sellCrypto'", 55, testWallet.getMoney(), delta15);

    }

    @Test
    public void testSellCryptoWithoutSuchCrypto() {
        boolean actual = testWallet.sellCrypto(new Offer("BTC", "Bitcoin", 1, 1000));

        assertFalse("Unexpected behaviour of 'sellCrypto'", actual);
    }

    @Test
    public void testSummaryWithNoCryptos() {
        String firstLineExpected = "Amount of money in the wallet:";
        String secondLineExpected = "Amount of money invested:";
        String thirdLineExpected = String.format("%-5s %-20s %-30s %-30s %-30s",
                "ID", "Cryptocurrency", "Money amount(USD)", "Crypto amount", "Crypto price(USD)");
        String fourthLineExpected = String.format("%-5s %-20s %-30s %-30s %-30s",
                "_".repeat(5), "_".repeat(20), "_".repeat(30), "_".repeat(30), "_".repeat(30));

        String actual = testWallet.summary();
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
    public void testSummaryWithCryptos() {
        testWallet.addCrypto(new Cryptocurrency(
                "Bitcoin", "BTC", 100, 0.1, 1000));

        String firstLineExpected = "Amount of money in the wallet:";
        String secondLineExpected = "Amount of money invested:";
        String thirdLineExpected = String.format("%-5s %-20s %-30s %-30s %-30s",
                "ID", "Cryptocurrency", "Money amount(USD)", "Crypto amount", "Crypto price(USD)");
        String fourthLineExpected = String.format("%-5s %-20s %-30s %-30s %-30s",
                "_".repeat(5), "_".repeat(20), "_".repeat(30), "_".repeat(30), "_".repeat(30));
        String fifthLineExpected = String.format("%-5s %-20s %-30f %-30f %-30f",
                "BTC", "Bitcoin", 100.00, 0.1, 1000.00);

        String actual = testWallet.summary();
        String[] actualLines = actual.split(System.lineSeparator());
        assertTrue("Unexpected return for 'summary'", actualLines.length >= 5);

        String firstLineActual = actualLines[0].substring(0, actualLines[0].lastIndexOf(':') + 1);
        String secondLineActual = actualLines[1].substring(0, actualLines[1].lastIndexOf(':') + 1);
        String thirdLineActual = actualLines[2];
        String fourthLineActual = actualLines[3];
        String fifthLineActual = actualLines[4];

        assertEquals("Unexpected return for 'summary'", firstLineExpected, firstLineActual);
        assertEquals("Unexpected return for 'summary'", secondLineExpected, secondLineActual);
        assertEquals("Unexpected return for 'summary'", thirdLineExpected, thirdLineActual);
        assertEquals("Unexpected return for 'summary'", fourthLineExpected, fourthLineActual);
        assertEquals("Unexpected return for 'summary'", fifthLineExpected, fifthLineActual);
    }

    @Test
    public void testOverallSummaryWithNoInvestments() {
        String firstLineExpected = "Amount of money in the wallet:";

        String secondLineExpected = String.format("Amount of money invested:");
        String thirdLineExpected = "Overall Net P&L:";
        String fourthLineExpected = String.format("%-5s %-20s %-30s %-30s %s",
                "ID", "Cryptocurrency", "Net P&L", "Current price(USD)", "Change(%)");
        // underlines
        String fifthLineExpected = String.format("%-5s %-20s %-30s %-30s %s",
                "_".repeat(5), "_".repeat(20), "_".repeat(30), "_".repeat(30), "_".repeat(10));

        String actual = testWallet.overall_summary(new ArrayList<>());
        String[] actualLines = actual.split(System.lineSeparator());
        assertEquals("Unexpected return for 'overall_summary'", 5, actualLines.length);

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
    public void testOverallSummaryWithInvestments() {
        testWallet.addCrypto(new Cryptocurrency(
                "Bitcoin", "BTC", 100, 0.1, 1000.00));

        String firstLineExpected = "Amount of money in the wallet:";

        String secondLineExpected = String.format("Amount of money invested:");
        String thirdLineExpected = "Overall Net P&L:";
        String fourthLineExpected = String.format("%-5s %-20s %-30s %-30s %s",
                "ID", "Cryptocurrency", "Net P&L", "Current price(USD)", "Change(%)");
        // underlines
        String fifthLineExpected = String.format("%-5s %-20s %-30s %-30s %s",
                "_".repeat(5), "_".repeat(20), "_".repeat(30), "_".repeat(30), "_".repeat(10));
        String sixthLineExpected = String.format("%-5s %-20s %-30f %-30f %.2f%%",
                "BTC", "Bitcoin", 900.00, 10000.00, 900.00);

        List<Offer> offers = new ArrayList<>();
        offers.add(new Offer("BTC", "Bitcoin", 1, 10000.00));

        String actual = testWallet.overall_summary(offers);
        String[] actualLines = actual.split(System.lineSeparator());
        assertEquals("Unexpected return for 'overall_summary'", 6, actualLines.length);

        String firstLineActual = actualLines[0].substring(0, actualLines[0].lastIndexOf(':') + 1);
        String secondLineActual = actualLines[1].substring(0, actualLines[1].lastIndexOf(':') + 1);
        String thirdLineActual = actualLines[2].substring(0, actualLines[2].lastIndexOf(':') + 1);
        String fourthLineActual = actualLines[3];
        String fifthLineActual = actualLines[4];
        String sixthLineActual = actualLines[5];

        assertEquals("Unexpected return for 'overall_summary'", firstLineExpected, firstLineActual);
        assertEquals("Unexpected return for 'overall_summary'", secondLineExpected, secondLineActual);
        assertEquals("Unexpected return for 'overall_summary'", thirdLineExpected, thirdLineActual);
        assertEquals("Unexpected return for 'overall_summary'", fourthLineExpected, fourthLineActual);
        assertEquals("Unexpected return for 'overall_summary'", fifthLineExpected, fifthLineActual);
        assertEquals("Unexpected return for 'overall_summary'", sixthLineExpected, sixthLineActual);
    }


    @Test
    public void testOverallSummaryWithNoOffers() {
        testWallet.addCrypto(new Cryptocurrency(
                "Bitcoin", "BTC", 100, 0.1, 1000.00));

        String firstLineExpected = "Amount of money in the wallet:";

        String secondLineExpected = String.format("Amount of money invested:");
        String thirdLineExpected = "Overall Net P&L:";
        String fourthLineExpected = String.format("%-5s %-20s %-30s %-30s %s",
                "ID", "Cryptocurrency", "Net P&L", "Current price(USD)", "Change(%)");
        // underlines
        String fifthLineExpected = String.format("%-5s %-20s %-30s %-30s %s",
                "_".repeat(5), "_".repeat(20), "_".repeat(30), "_".repeat(30), "_".repeat(10));
        String sixthLineExpected = String.format("%-5s %-20s %-30s %-30s %s",
                "BTC", "Bitcoin", "Not available", "Not available", "Not available");

        String actual = testWallet.overall_summary(new ArrayList<>());
        String[] actualLines = actual.split(System.lineSeparator());
        assertEquals("Unexpected return for 'overall_summary'", 6, actualLines.length);

        String firstLineActual = actualLines[0].substring(0, actualLines[0].lastIndexOf(':') + 1);
        String secondLineActual = actualLines[1].substring(0, actualLines[1].lastIndexOf(':') + 1);
        String thirdLineActual = actualLines[2].substring(0, actualLines[2].lastIndexOf(':') + 1);
        String fourthLineActual = actualLines[3];
        String fifthLineActual = actualLines[4];
        String sixthLineActual = actualLines[5];

        assertEquals("Unexpected return for 'overall_summary'", firstLineExpected, firstLineActual);
        assertEquals("Unexpected return for 'overall_summary'", secondLineExpected, secondLineActual);
        assertEquals("Unexpected return for 'overall_summary'", thirdLineExpected, thirdLineActual);
        assertEquals("Unexpected return for 'overall_summary'", fourthLineExpected, fourthLineActual);
        assertEquals("Unexpected return for 'overall_summary'", fifthLineExpected, fifthLineActual);
        assertEquals("Unexpected return for 'overall_summary'", sixthLineExpected, sixthLineActual);
    }
}
