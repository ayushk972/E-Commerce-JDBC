package com.ecom.main;

import com.ecom.model.OrderStatus;
import com.ecom.service.CustomersService;
import com.ecom.service.OrdersService;
import com.ecom.service.ProductsService;

/**
 * Handles all Admin panel interactions.
 */
public class AdminMenu {

    private final ProductsService  productsService;
    private final CustomersService customersService;
    private final OrdersService    ordersService;

    public AdminMenu(ProductsService productsService,
                     CustomersService customersService,
                     OrdersService ordersService) {
        this.productsService  = productsService;
        this.customersService = customersService;
        this.ordersService    = ordersService;
    }

    public void show() {
        boolean back = false;
        while (!back) {
            printMenu();
            int choice = ConsoleHelper.readInt("Admin choice: ");
            try {
                switch (choice) {
                    case 1  -> productsService.viewAllProductsAdmin();
                    case 2  -> addProduct();
                    case 3  -> batchAddProducts();
                    case 4  -> productsService.searchByIdAdmin(ConsoleHelper.readInt("  Product ID: "));
                    case 5  -> productsService.searchByName(ConsoleHelper.readRequired("  Name to search: "));
                    case 6  -> productsService.searchByCategory(ConsoleHelper.readRequired("  Category: "));
                    case 7  -> productsService.searchByBrand(ConsoleHelper.readRequired("  Brand: "));
                    case 8  -> {
                        double min = ConsoleHelper.readDouble("  Min Price: $");
                        double max = ConsoleHelper.readDouble("  Max Price: $");
                        productsService.searchByPriceRange(min, max);
                    }
                    case 9  -> updateProductPrice();
                    case 10 -> updateProductQuantity();
                    case 11 -> productsService.deleteProduct(ConsoleHelper.readInt("  Product ID to delete: "));
                    case 12 -> customersService.viewAllCustomersAdmin();
                    case 13 -> System.out.println(customersService.searchById(ConsoleHelper.readInt("  Customer ID: ")).toAdminString());
                    case 14 -> ordersService.viewAllOrdersAdmin();
                    case 15 -> ordersService.viewOrderAdmin(ConsoleHelper.readRequired("  Order ID: "));
                    case 16 -> updateOrderStatus();
                    case 0  -> { System.out.println("\n  Back to Main Menu."); back = true; }
                    default -> System.out.println("  Invalid choice.");
                }
            } catch (Exception e) {
                System.out.println("  ERROR: " + e.getMessage());
            }
        }
    }

    private void printMenu() {
        System.out.println("\n+------------------------------------+");
        System.out.println("|          ADMIN PANEL               |");
        System.out.println("+------------------------------------+");
        System.out.println("|  --- Product Management ---        |");
        System.out.println("|  1.  View All Products             |");
        System.out.println("|  2.  Add Single Product            |");
        System.out.println("|  3.  Batch Add Products from File  |");
        System.out.println("|  4.  Search Product by ID          |");
        System.out.println("|  5.  Search Products by Name       |");
        System.out.println("|  6.  Search Products by Category   |");
        System.out.println("|  7.  Search Products by Brand      |");
        System.out.println("|  8.  Search by Price Range         |");
        System.out.println("|  9.  Update Product Price          |");
        System.out.println("|  10. Update Product Quantity       |");
        System.out.println("|  11. Delete Product                |");
        System.out.println("|  --- Customer Management ---       |");
        System.out.println("|  12. View All Customers            |");
        System.out.println("|  13. Search Customer by ID         |");
        System.out.println("|  --- Order Management ---          |");
        System.out.println("|  14. View All Orders               |");
        System.out.println("|  15. View Order by ID              |");
        System.out.println("|  16. Update Order Status           |");
        System.out.println("|  0.  Back to Main Menu             |");
        System.out.println("+------------------------------------+");
    }

    private void addProduct() {
        System.out.println("\n  --- Add New Product ---");
        String name     = ConsoleHelper.readRequired("  Name     : ");
        String category = ConsoleHelper.readRequired("  Category : ");
        String brand    = ConsoleHelper.readRequired("  Brand    : ");
        double price    = ConsoleHelper.readDouble("  Price    : $");
        int    qty      = ConsoleHelper.readInt("  Quantity : ");
        productsService.addProduct(name, category, price, qty, brand);
    }

    private void batchAddProducts() {
        System.out.println("\n  --- Batch Add Products from File ---");
        System.out.println("  Expected format per line: productName,category,price,quantity,brand");
        System.out.println("  Example: iPad Pro,Electronics,799.99,15,Apple");
        String filePath = ConsoleHelper.readRequired("  Enter file path: ");
        productsService.addProductsFromFile(filePath);
    }

    private void updateProductPrice() {
        int    id    = ConsoleHelper.readInt("  Product ID: ");
        double price = ConsoleHelper.readDouble("  New Price : $");
        productsService.updatePrice(id, price);
    }

    private void updateProductQuantity() {
        int id  = ConsoleHelper.readInt("  Product ID  : ");
        int qty = ConsoleHelper.readInt("  New Quantity: ");
        productsService.updateQuantity(id, qty);
    }

    private void updateOrderStatus() {
        String orderId = ConsoleHelper.readRequired("  Order ID: ");
        System.out.println("  1. CONFIRMED  2. SHIPPED  3. DELIVERED");
        int opt = ConsoleHelper.readInt("  New Status: ");
        OrderStatus status = switch (opt) {
            case 1 -> OrderStatus.CONFIRMED;
            case 2 -> OrderStatus.SHIPPED;
            case 3 -> OrderStatus.DELIVERED;
            default -> throw new IllegalArgumentException("Invalid status option.");
        };
        ordersService.updateOrderStatus(orderId, status);
    }
}
