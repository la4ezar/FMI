package bg.sofia.uni.fmi.mjt.cryptocurrency.wallet.manager.wallet;


import bg.sofia.uni.fmi.mjt.cryptocurrency.wallet.manager.cryptocurrency.Cryptocurrency;
import bg.sofia.uni.fmi.mjt.cryptocurrency.wallet.manager.offer.Offer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Wallet {
    int numberCryptos;
    private double money;
    // offering code -> crypto
    private final Map<Integer, Cryptocurrency> cryptos;

    public Wallet() {
        money = 0;
        numberCryptos = 0;
        cryptos = new HashMap<>();
    }

    public void deposit(double money) {
        this.money += money;
    }

    public boolean withdraw(double money) {
        if (this.money >= money) {
            this.money -= money;
            return true;
        } else {
            return false;
        }
    }

    public double getMoney() {
        return money;
    }

    public Map<Integer, Cryptocurrency> getCryptos() {
        return cryptos;
    }

    public void addCrypto(Cryptocurrency crypto) {
        cryptos.put(numberCryptos++, crypto);
    }

    public boolean sellCrypto(Offer offer) {
        for (Iterator<Map.Entry<Integer, Cryptocurrency>> it = cryptos.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Integer, Cryptocurrency> entry = it.next();
            // sell the first opened position only
            if (entry.getValue().getId().equals(offer.getAssetId())) {
                this.money +=
                        offer.getPriceUsd() * entry.getValue().getCrypto_amount() - entry.getValue().getMoney_amount();
                it.remove();
                return true;
            }
        }

        return false;
    }

    public String summary() {
        StringBuilder result = new StringBuilder(String.format("Amount of money in the wallet: %f%n", money));
        result.append(String.format("Cryptocurrencies:%n"));
        for (Iterator<Map.Entry<Integer, Cryptocurrency>> it = cryptos.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Integer, Cryptocurrency> entry = it.next();
            result.append(String.format("%f %s(%s) for %f USD total%n",
                    entry.getValue().getCrypto_amount(), entry.getValue().getName(),
                    entry.getValue().getId(), entry.getValue().getMoney_amount()));
        }
        return result.toString();
    }

    private double get_overall_pl(List<Offer> offers) {
        double result = 0;
        for (Map.Entry<Integer, Cryptocurrency> entry : cryptos.entrySet()) {
            int ind = offers.lastIndexOf(new Offer(entry.getValue().getId(), "", 0, 0));
            if (ind != -1) {
                result += offers.get(ind).getPriceUsd() * entry.getValue().getCrypto_amount()
                          - entry.getValue().getMoney_amount();
            }
        }
        return result;
    }

    public String overall_summary(List<Offer> offers) {
        StringBuilder result = new StringBuilder(String.format("Amount of money in the wallet: %f%n", money));
        Double overall_pl = get_overall_pl(offers);
        result.append(String.format("Overall Net P&L: %f%n", overall_pl));
        result.append(String.format("%-5s %-20s %-30s %-30s %s%n",
                "ID", "Cryptocurrency", "Net P&L", "Current price", "Change"));
        for (Map.Entry<Integer, Cryptocurrency> entry : cryptos.entrySet()) {
            Cryptocurrency current_crypto = entry.getValue();
            int ind = offers.lastIndexOf(new Offer(current_crypto.getId(), "", 0, 0));
            if (ind == -1) {
                result.append(String.format("%-5s %-20s %-30s %-30s %s%n",
                        current_crypto.getId(), current_crypto.getName(),
                        "Not available", "Not available", "Not available"));
            } else {
                double profit_and_loss_statement = offers.get(ind).getPriceUsd() * current_crypto.getCrypto_amount()
                        - current_crypto.getMoney_amount();
                double percent_change = (offers.get(ind).getPriceUsd() - current_crypto.getPrice())
                        * 100 / current_crypto.getPrice();
                result.append(String.format("%-5s %-20s %-30f %-30f %.2f%%%n",
                        current_crypto.getId(), current_crypto.getName(),
                        profit_and_loss_statement, offers.get(ind).getPriceUsd(), percent_change));
            }
        }
        return result.toString();
    }
}
