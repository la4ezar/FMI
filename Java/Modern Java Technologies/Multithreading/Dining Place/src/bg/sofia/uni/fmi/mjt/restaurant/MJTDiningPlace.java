package bg.sofia.uni.fmi.mjt.restaurant;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

public class MJTDiningPlace implements Restaurant {
    private final Queue<Order> vipOrders;
    private final Queue<Order> ordinaryOrders;
    private Chef[] chefs;
    private static int currentId = 0;
    private int ordersNum = 0;
    private boolean runnable = true;

    public MJTDiningPlace(int chefsNum) {
        Comparator<Order> mealTimeComparator =
                (Order o1, Order o2) -> o2.meal().getCookingTime() - o1.meal().getCookingTime();

        vipOrders = new PriorityQueue<>(mealTimeComparator);
        ordinaryOrders = new PriorityQueue<>(mealTimeComparator);
        chefs = new Chef[chefsNum];

        // run chefs
        for (int i = 0; i < chefsNum; ++i) {
            chefs[i] = new Chef(currentId, this);
            ++currentId;
            chefs[i].start();
        }
    }

    /**
    * Adds an order.
    **/
    public void submitOrder(Order order) {
        synchronized (this) {
            if (order.customer().hasVipCard()) {
                vipOrders.add(order);
            } else {
                ordinaryOrders.add(order);
            }
            this.notifyAll();
            ++ordersNum;
        }
    }

    /**
     * Returns the next order to be cooked
     * and removes it from the pending orders
     **/
    public Order nextOrder() {
        synchronized (this) {
            while (vipOrders.size() < 1 && ordinaryOrders.size() < 1 && runnable) {
                try {
                    this.wait(500);
                } catch (InterruptedException e) {
                    // Shouldn't happen
                    e.printStackTrace();
                }
            }

            if (vipOrders.size() > 0) {
                return vipOrders.poll();
            } else if (ordinaryOrders.size() > 0) {
                return ordinaryOrders.poll();
            } else {
                return null;
            }
        }
    }

    /**
     * Returns the total number of submitted orders.
     **/
    public int getOrdersCount() {
        return ordersNum;
    }

    /**
     * Returns the restaurant's chefs.
     **/
    public Chef[] getChefs() {
        return chefs;
    }

    /**
     * Prepares the restaurant for closing. When this method is called,
     * the chefs complete any pending orders and then finish work.
     **/
    public void close() {
        runnable = false;
        for (int i = 0; i < chefs.length; ++i) {
            try {
                chefs[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
