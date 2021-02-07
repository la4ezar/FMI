package bg.sofia.uni.fmi.mjt.restaurant.customer;

import bg.sofia.uni.fmi.mjt.restaurant.Meal;
import bg.sofia.uni.fmi.mjt.restaurant.Order;
import bg.sofia.uni.fmi.mjt.restaurant.Restaurant;

import java.util.Random;


public abstract class AbstractCustomer extends Thread {
    Restaurant restaurant;

    public AbstractCustomer(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    @Override
    public void run() {
        Random rand = new Random();
        try {
            Meal meal = Meal.chooseFromMenu();
            Thread.sleep(rand.nextInt(5) + 1);
            restaurant.submitOrder(new Order(meal, this));
        } catch (InterruptedException e) {
            // Shouldn't happen
            e.printStackTrace();
        }
    }

    public abstract boolean hasVipCard();

}
