package bg.sofia.uni.fmi.mjt.restaurant;

import bg.sofia.uni.fmi.mjt.restaurant.customer.AbstractCustomer;
import bg.sofia.uni.fmi.mjt.restaurant.customer.Customer;
import bg.sofia.uni.fmi.mjt.restaurant.customer.VipCustomer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MJTDinnerPlaceTest {
    private MJTDiningPlace restaurant;
    private AbstractCustomer[] customers;

    @Before
    public void setUp() {
        restaurant = new MJTDiningPlace(3);
        customers = new AbstractCustomer[10];
        for (int i = 0; i < 10; ++i) {
            if (i % 2 == 0) {
                customers[i] = new Customer(restaurant);
            } else {
                customers[i] = new VipCustomer(restaurant);
            }
        }
    }

    @Test
    public void testSubmitOrder() {
        for (int i = 0; i < 10; ++i) {
            customers[i].start();
        }

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(10, restaurant.getOrdersCount());

        restaurant.close();
    }

    @Test
    public void testSubmitOrderWithClose() {
        for (int i = 0; i < 10; ++i) {
            customers[i].start();
        }

        restaurant.close();

        assertEquals(10, restaurant.getOrdersCount());
    }

    @Test
    public void testNextOrderIsEmpty() {
        for (int i = 0; i < 10; ++i) {
            customers[i].start();
        }

        restaurant.close();

        assertEquals(null, restaurant.nextOrder());
    }

    @Test
    public void testChefsTotalMeals() {
        for (int i = 0; i < 10; ++i) {
            customers[i].start();
        }

        restaurant.close();

        int totalMeals = 0;
        for (int i = 0; i < restaurant.getChefs().length; ++i) {
            totalMeals += restaurant.getChefs()[i].getTotalCookedMeals();
        }

        assertEquals(10, totalMeals);
    }

    @After
    public void clear() {
        restaurant.close();
    }
}
