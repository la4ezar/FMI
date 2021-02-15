package bg.sofia.uni.fmi.mjt.cryptocurrency.wallet.manager.offer;

import java.util.Objects;

public class Offer {
    private String asset_id;
    private String name;
    private int type_is_crypto;
    private double price_usd;

    public Offer(String asset_id, String name, int type_is_crypto, double price_usd) {
        this.asset_id = asset_id;
        this.name = name;
        this.type_is_crypto = type_is_crypto;
        this.price_usd = price_usd;
    }

    public int getIsCrypto() {
        return type_is_crypto;
    }

    public String getAssetId() {
        return asset_id;
    }

    public String getName() {
        return name;
    }

    public double getPriceUsd() {
        return price_usd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Offer offer = (Offer) o;
        return Objects.equals(asset_id, offer.asset_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(asset_id);
    }
}