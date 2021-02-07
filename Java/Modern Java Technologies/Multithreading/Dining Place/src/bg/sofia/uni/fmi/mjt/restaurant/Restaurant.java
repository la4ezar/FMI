package bg.sofia.uni.fmi.mjt.restaurant;

public interface Restaurant {

    /**
     * Adds an order.
     **/
    public void submitOrder(Order order);

    /**
     * Returns the next order to be cooked
     * and removes it from the pending orders
     **/
    public Order nextOrder();

    /**
     * Returns the total number of submitted orders.
     **/
    public int getOrdersCount();

    /**
     * Returns the restaurant's chefs.
     **/
    public Chef[] getChefs();

    /**
     * Prepares the restaurant for closing. When this method is called,
     * the chefs complete any pending orders and then finish work.
     **/
    public void close();

}