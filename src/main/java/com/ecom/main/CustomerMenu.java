package com.ecom.main;

import java.util.List;

import com.ecom.exception.CustomerNotFoundException;
import com.ecom.exception.DuplicateEmailException;
import com.ecom.exception.InvalidCustomerDataException;
import com.ecom.model.Customers;
import com.ecom.model.Order;
import com.ecom.repository.OrdersRepository;
import com.ecom.service.CartService;
import com.ecom.service.CustomersService;
import com.ecom.service.OrdersService;
import com.ecom.service.ProductsService;

/**
 * Handles all Customer panel interactions (register, login, shopping, orders).
 */
public class CustomerMenu {

    private final ProductsService  productsService;
    private final CustomersService customersService;
    private final CartService      cartService;
    private final OrdersService    ordersService;
    private final OrdersRepository ordersRepo;

    public CustomerMenu(ProductsService productsService,
                        CustomersService customersService,
                        CartService cartService,
                        OrdersService ordersService,
                        OrdersRepository ordersRepo) {
        this.productsService  = productsService;
        this.customersService = customersService;
        this.cartService      = cartService;
        this.ordersService    = ordersService;
        this.ordersRepo       = ordersRepo;
    }

    public Customers authenticate() {
        System.out.println("\n+---------------------------+");
        System.out.println("|  1. Register  2. Login    |");
        System.out.println("+---------------------------+");
        int opt = ConsoleHelper.readInt("  Option: ");
        try {
            return switch (opt) {
                case 1 -> register();
                case 2 -> login();
                default -> { System.out.println("  Invalid option."); yield null; }
            };
        } catch (Exception e) {
            System.out.println("  ERROR: " + e.getMessage());
            return null;
        }
    }

    public void show() {
        Customers customer = authenticate();
        if (customer != null) showDashboard(customer);
    }

    private Customers register() {
        System.out.println("\n  --- Customer Registration ---");
        String name     = ConsoleHelper.readRequired("  Name      : ");
        String email    = ConsoleHelper.readRequired("  Email     : ");
        String password = ConsoleHelper.readPassword("  Password  : ");
        String mobile   = ConsoleHelper.readRequired("  Mobile    : ");
        String address  = ConsoleHelper.readRequired("  Address   : ");
        return customersService.register(name, email, password, mobile, address);
    }

    private Customers login() {
        System.out.println("\n  --- Customer Login ---");
        String email    = ConsoleHelper.readRequired("  Email    : ");
        String password = ConsoleHelper.readPassword("  Password : ");
        return customersService.login(email, password);
    }

    // Package-private so GuestMenu can hand off to dashboard after cart transfer
    void showDashboard(Customers customer) {
        boolean back = false;
        while (!back) {
            printDashboard(customer.getName());
            int choice = ConsoleHelper.readInt("Choice: ");
            try {
                switch (choice) {
                    case 1  -> productsService.viewAllProducts();
                    case 2  -> searchProduct();
                    case 3  -> cartService.addToCart(customer.getCustomerId(),
                                    ConsoleHelper.readInt("\n  Product ID: "),
                                    ConsoleHelper.readInt("  Quantity  : "));
                    case 4  -> cartService.viewCart(customer.getCustomerId());
                    case 5  -> cartService.removeFromCart(customer.getCustomerId(),
                                    ConsoleHelper.readInt("  Product ID to remove: "));
                    case 6  -> changeCartQuantity(customer.getCustomerId());
                    case 7  -> {
                        System.out.print("  Clear entire cart? (yes/no): ");
                        if (ConsoleHelper.sc.nextLine().trim().equalsIgnoreCase("yes"))
                            cartService.clearCart(customer.getCustomerId());
                    }
                    case 8  -> placeOrder(customer.getCustomerId());
                    case 9  -> ordersService.viewCustomerOrders(customer.getCustomerId());
                    case 10 -> ordersService.viewOrder(ConsoleHelper.readRequired("  Order ID: "));
                    case 11 -> cancelOrder(customer.getCustomerId());
                    case 12 -> customersService.viewCustomer(customer.getCustomerId());
                    case 13 -> updateAddress(customer);
                    case 14 -> changePassword(customer.getCustomerId());
                    case 0  -> { System.out.println("\n  Logged out successfully."); back = true; }
                    default -> System.out.println("  Invalid choice.");
                }
            } catch (Exception e) {
                System.out.println("  ERROR: " + e.getMessage());
            }
        }
    }

    private void printDashboard(String name) {
        System.out.println("\n+------------------------------------+");
        System.out.println("|  Hello, " + name + "!");
        System.out.println("|  CUSTOMER DASHBOARD                |");
        System.out.println("+------------------------------------+");
        System.out.println("|  1.  View All Products             |");
        System.out.println("|  2.  Search Product                |");
        System.out.println("|  3.  Add to Cart                   |");
        System.out.println("|  4.  View Cart                     |");
        System.out.println("|  5.  Remove from Cart              |");
        System.out.println("|  6.  Change Cart Quantity          |");
        System.out.println("|  7.  Clear Cart                    |");
        System.out.println("|  8.  Place Order                   |");
        System.out.println("|  9.  My Orders                     |");
        System.out.println("|  10. View Order by ID              |");
        System.out.println("|  11. Cancel Order                  |");
        System.out.println("|  12. My Profile                    |");
        System.out.println("|  13. Update Address                |");
        System.out.println("|  14. Change Password               |");
        System.out.println("|  0.  Logout                        |");
        System.out.println("+------------------------------------+");
    }

    private void searchProduct() {
        System.out.println("\n  Search by: 1.ID  2.Name  3.Category  4.Brand  5.Price Range");
        int opt = ConsoleHelper.readInt("  Option: ");
        switch (opt) {
            case 1 -> productsService.searchById(ConsoleHelper.readInt("  Product ID: "));
            case 2 -> productsService.searchByName(ConsoleHelper.readRequired("  Name: "));
            case 3 -> productsService.searchByCategory(ConsoleHelper.readRequired("  Category: "));
            case 4 -> productsService.searchByBrand(ConsoleHelper.readRequired("  Brand: "));
            case 5 -> {
                double min = ConsoleHelper.readDouble("  Min Price: $");
                double max = ConsoleHelper.readDouble("  Max Price: $");
                productsService.searchByPriceRange(min, max);
            }
            default -> System.out.println("  Invalid option.");
        }
    }

    private void changeCartQuantity(int customerId) {
        int productId = ConsoleHelper.readInt("  Product ID: ");
        System.out.println("  1. Increase  2. Decrease");
        int opt = ConsoleHelper.readInt("  Option: ");
        int qty = ConsoleHelper.readInt("  Qty   : ");
        if      (opt == 1) cartService.increaseQuantity(customerId, productId, qty);
        else if (opt == 2) cartService.decreaseQuantity(customerId, productId, qty);
        else               System.out.println("  Invalid option.");
    }

    private void placeOrder(int customerId) {
        cartService.viewCart(customerId);
        System.out.print("  Confirm order? (yes/no): ");
        if (ConsoleHelper.sc.nextLine().trim().equalsIgnoreCase("yes")) {
            ordersService.placeOrder(customerId);
        } else {
            System.out.println("  Order not placed.");
        }
    }

    private void cancelOrder(int customerId) {
        List<Order> orders = ordersRepo.findByCustomerId(customerId);
        if (orders.isEmpty()) { System.out.println("  You have no orders to cancel."); return; }
        ordersService.cancelOrder(ConsoleHelper.readRequired("  Enter Order ID to cancel: "), customerId);
    }

    private void changePassword(int customerId) {
        String oldPass = ConsoleHelper.readPassword("  Current Password : ");
        String newPass = ConsoleHelper.readPassword("  New Password     : ");
        customersService.updatePassword(customerId, oldPass, newPass);
    }

    private void updateAddress(Customers customer) {
        System.out.println("\n  --- Update Address ---");
        System.out.println("  Current Address: " + customer.getAddress());
        String newAddress = ConsoleHelper.readRequired("  New Address    : ");
        customersService.updateAddress(customer.getCustomerId(), newAddress);
        customer.setAddress(newAddress);
    }
}
