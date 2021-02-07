package bg.sofia.uni.fmi.mjt.restaurant;

public class Chef extends Thread {

    private final int id;
    private final Restaurant restaurant;
    private int mealsCooked = 0;

    public Chef(int id, Restaurant restaurant) {
        this.id = id;
        this.restaurant = restaurant;
    }

    @Override
    public void run() {
        Order currentOrder;
        while ((currentOrder = restaurant.nextOrder()) != null) {
            try {
                Thread.sleep(currentOrder.meal().getCookingTime());
                ++mealsCooked;
            } catch (InterruptedException e) {
                // Shouldn't happen
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns the total number of meals that this chef has cooked.
     **/
    public int getTotalCookedMeals() {
        return mealsCooked;
    }

    public int getChefId() {
        return id;
    }
}