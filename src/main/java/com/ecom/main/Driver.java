package com.ecom.main;

import com.ecom.repository.CartRepository;
import com.ecom.repository.CustomersRepository;
import com.ecom.repository.OrdersRepository;
import com.ecom.repository.ProductsRepository;
import com.ecom.service.CartService;
import com.ecom.service.CustomersService;
import com.ecom.service.OrdersService;
import com.ecom.service.ProductsService;

/**
 * Application entry point.
 * Wires up all repositories, services, and menu classes, then starts the main loop.
 */
public class Driver {

    public static void main(String[] args) {
        // Repositories
        ProductsRepository  productsRepo  = new ProductsRepository();
        CustomersRepository customersRepo = new CustomersRepository();
        OrdersRepository    ordersRepo    = new OrdersRepository();
        CartRepository      cartRepo      = new CartRepository();

        // Services
        ProductsService  productsService  = new ProductsService(productsRepo);
        CustomersService customersService = new CustomersService(customersRepo);
        CartService      cartService      = new CartService(cartRepo, productsService);
        OrdersService    ordersService    = new OrdersService(ordersRepo, cartService, productsService, productsRepo);

        // Menus
        AdminMenu    adminMenu    = new AdminMenu(productsService, customersService, ordersService);
        CustomerMenu customerMenu = new CustomerMenu(productsService, customersService, cartService, ordersService, ordersRepo);
        GuestMenu    guestMenu    = new GuestMenu(productsService, cartService, customerMenu, ordersService);

        System.out.println("\n  *** E-Commerce JDBC Application ***");

        boolean running = true;
        while (running) {
            System.out.println("\n+-----------------------------+");
            System.out.println("|         MAIN MENU           |");
            System.out.println("+-----------------------------+");
            System.out.println("|  1. Admin                   |");
            System.out.println("|  2. Customer                |");
            System.out.println("|  3. Explore (Guest)         |");
            System.out.println("|  0. Exit                    |");
            System.out.println("+-----------------------------+");

            int choice = ConsoleHelper.readInt("Choose: ");
            switch (choice) {
                case 1 -> adminMenu.show();
                case 2 -> customerMenu.show();
                case 3 -> guestMenu.show();
                case 0 -> { System.out.println("\n  Goodbye!"); running = false; }
                default -> System.out.println("  Invalid choice. Try again.");
            }
        }
        ConsoleHelper.sc.close();
    }
}

