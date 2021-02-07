package bg.sofia.uni.fmi.mjt.city;

public class City implements Comparable<City> {
    private String key;
    private String value;

    public City(String city, String country) {
        this.key = city;
        this.value = country;
    }

    public String getCity() {
        return key;
    }


    public String getCountry() {
        return value;
    }

    @Override
    public int compareTo(City o) {
        return this.key.toLowerCase().compareTo(o.getCity().toLowerCase());
    }
}
