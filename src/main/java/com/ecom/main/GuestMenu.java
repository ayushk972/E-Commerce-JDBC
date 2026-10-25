package com.ecom.main;

import java.util.ArrayList;
import java.util.List;

import com.ecom.exception.InsufficientStockException;
import com.ecom.exception.InvalidQuantityException;
import com.ecom.model.CartItem;
import com.ecom.model.Customers;
import com.ecom.model.Products;
import com.ecom.service.CartService;
import com.ecom.service.OrdersService;
import com.ecom.service.ProductsService;

/**
 * Handles the Guest (Explore) panel.
 * Guests can browse products, add items to a temporary cart, and when placing
 * an order, are prompted to login or register. The cart is then transferred to
 * that customer and the guest cart is cleared.
 */
public class GuestMenu {

    private final ProductsService productsService;
    private final CartService     cartService;
    private final CustomerMenu    customerMenu;
    private final OrdersService   ordersService;

    // Temporary in-memory cart for the guest session
    private final List<CartItem> guestCart = new ArrayList<>();

    public GuestMenu(ProductsService productsService,
                     CartService cartService,
                     CustomerMenu customerMenu,
                     OrdersService ordersService) {
        this.productsService = productsService;
        this.cartService     = cartService;
        this.customerMenu    = customerMenu;
        this.ordersService   = ordersService;
    }

    public void show() {
        boolean back = false;
        while (!back) {
            printMenu();
            int choice = ConsoleHelper.readInt("Choose: ");
            try {
                switch (choice) {
                    case 1  -> productsService.viewAllProducts();
                    case 2  -> productsService.searchById(ConsoleHelper.readInt("  Product ID: "));
                    case 3  -> productsService.searchByName(ConsoleHelper.readRequired("  Name to search: "));
                    case 4  -> productsService.searchByCategory(ConsoleHelper.readRequired("  Category: "));
                    case 5  -> productsService.searchByBrand(ConsoleHelper.readRequired("  Brand: "));
                    case 6  -> searchByPrice();
                    case 7  -> addToGuestCart();
                    case 8  -> cartService.viewCart(guestCart);
                    case 9  -> removeFromGuestCart();
                    case 10 -> placeOrder();
                    case 0  -> {
                        guestCart.clear();
                        System.out.println("  Back to Main Menu.");
                        back = true;
                    }
                    default -> System.out.println("  Invalid choice.");
                }
            } catch (Exception e) {
                System.out.println("  ERROR: " + e.getMessage());
            }
        }
    }

    private void searchByPrice() {
        double min = ConsoleHelper.readDouble("  Min Price: $");
        double max = ConsoleHelper.readDouble("  Max Price: $");
        productsService.searchByPriceRange(min, max);
    }

    // ==================== GUEST CART ====================

    private void addToGuestCart() {
        int productId = ConsoleHelper.readInt("\n  Product ID to add: ");
        int quantity  = ConsoleHelper.readInt("  Quantity         : ");
        if (quantity <= 0)
            throw new InvalidQuantityException("InvalidQuantityException: Quantity must be greater than 0.");

        Products product = productsService.getProduct(productId);
        int existingQty = guestCart.stream()
                .filter(i -> i.getProductId() == productId)
                .mapToInt(CartItem::getQuantity).sum();

        if (product.getQuantity() < existingQty + quantity) {
            throw new InsufficientStockException("InsufficientStockException: Only "
                    + product.getQuantity() + " unit(s) available in stock.");
        }

        for (CartItem item : guestCart) {
            if (item.getProductId() == productId) {
                item.setQuantity(item.getQuantity() + quantity);
                System.out.println("  Updated guest cart: '" + product.getProductName()
                        + "' quantity is now " + item.getQuantity());
                return;
            }
        }
        guestCart.add(new CartItem(productId, product.getProductName(), product.getPrice(), quantity));
        System.out.println("  Added to guest cart: '" + product.getProductName() + "' x" + quantity);
    }

    private void removeFromGuestCart() {
        if (guestCart.isEmpty()) {
            System.out.println("  Guest cart is empty.");
            return;
        }
        cartService.viewCart(guestCart);
        int productId = ConsoleHelper.readInt("  Product ID to remove: ");
        boolean removed = guestCart.removeIf(i -> i.getProductId() == productId);
        System.out.println(removed
                ? "  Product [ID: " + productId + "] removed from guest cart."
                : "  Product [ID: " + productId + "] not found in guest cart.");
    }

    // ==================== PLACE ORDER ====================

    private void placeOrder() {
        if (guestCart.isEmpty()) {
            System.out.println("\n  Your cart is empty. Please add items to cart first.");
            return;
        }
        cartService.viewCart(guestCart);
        System.out.println("\n  Please Login or Register to place your order:");
        Customers customer = customerMenu.authenticate();
        if (customer == null) {
            System.out.println("  Order cancelled. You remain as Guest.");
            return;
        }

        System.out.println("\n  Transferring cart items to your account (" + customer.getName() + ")...");
        for (CartItem item : guestCart) {
            cartService.addToCart(customer.getCustomerId(), item.getProductId(), item.getQuantity());
        }
        guestCart.clear();

        cartService.viewCart(customer.getCustomerId());
        System.out.print("  Confirm order? (yes/no): ");
        if (ConsoleHelper.sc.nextLine().trim().equalsIgnoreCase("yes")) {
            ordersService.placeOrder(customer.getCustomerId());
        } else {
            System.out.println("  Order not placed. Items saved in your cart.");
        }
        customerMenu.showDashboard(customer);
    }

    private void printMenu() {
        System.out.println("\n+------------------------------------+");
        System.out.println("|        EXPLORE (GUEST)             |");
        System.out.println("+------------------------------------+");
        System.out.println("|  1. View All Products              |");
        System.out.println("|  2. Search Product by ID           |");
        System.out.println("|  3. Search Products by Name        |");
        System.out.println("|  4. Search Products by Category    |");
        System.out.println("|  5. Search Products by Brand       |");
        System.out.println("|  6. Search by Price Range          |");
        System.out.println("|  7. Add to Cart                    |");
        System.out.println("|  8. View Cart                      |");
        System.out.println("|  9. Remove from Cart               |");
        System.out.println("| 10. Place Order                    |");
        System.out.println("|  0. Back to Main Menu              |");
        System.out.println("+------------------------------------+");
        if (!guestCart.isEmpty()) {
            System.out.println("  [Guest Cart: " + guestCart.size() + " item(s)]");
        }
    }
}
