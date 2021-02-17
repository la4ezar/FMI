package bg.sofia.uni.fmi.mjt.cryptocurrency.wallet.manager.user;

import bg.sofia.uni.fmi.mjt.cryptocurrency.wallet.manager.cryptocurrency.Cryptocurrency;
import bg.sofia.uni.fmi.mjt.cryptocurrency.wallet.manager.offer.Offer;
import bg.sofia.uni.fmi.mjt.cryptocurrency.wallet.manager.wallet.Wallet;

import java.util.List;
import java.util.Objects;

public class User {
    private final String name;
    private String password;
    private boolean is_logged;
    private final Wallet wallet;

    public User(String name, String password) {
        this.name = name;
        this.password = password;
        this.is_logged = true;  // login on register
        this.wallet = new Wallet();
    }

    public void login() {
        is_logged = true;
    }

    public void logout() {
        is_logged = false;
    }

    public void deposit(double money) {
        this.wallet.deposit(money);
    }

    public boolean buy(Offer offer, double money_amount) {
        if (!wallet.withdraw(money_amount)) {
            return false;
        } else {
            double crypto_amount = money_amount / offer.getPriceUsd();
            wallet.addCrypto(new Cryptocurrency
                    (offer.getName(), offer.getAssetId(), money_amount, crypto_amount, offer.getPriceUsd()));
            return true;
        }
    }

    public boolean sell(Offer crypto) {
        return wallet.sellCrypto(crypto);
    }

    //summary
    public String summary() {
        return wallet.summary();
    }

    //overall summary
    public String overall_summary(List<Offer> offers) {
        return wallet.overall_summary(offers);
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean getIsLogged() {
        return is_logged;
    }

    public Wallet getWallet() {
        return wallet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(name, user.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
