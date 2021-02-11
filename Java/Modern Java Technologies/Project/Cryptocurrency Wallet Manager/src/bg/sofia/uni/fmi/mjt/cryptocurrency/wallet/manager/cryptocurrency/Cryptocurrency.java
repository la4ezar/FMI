package bg.sofia.uni.fmi.mjt.cryptocurrency.wallet.manager.cryptocurrency;

import java.util.Objects;

public class Cryptocurrency {
    String name;
    String id;
    double money_amount;
    double crypto_amount;
    double price;

    public Cryptocurrency(String name, String id, double money_amount, double crypto_amount, double price) {
        this.name = name;
        this.id = id;
        this.money_amount = money_amount;
        this.crypto_amount = crypto_amount;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public double getMoney_amount() {
        return money_amount;
    }

    public double getCrypto_amount() {
        return crypto_amount;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cryptocurrency that = (Cryptocurrency) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
