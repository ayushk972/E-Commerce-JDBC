package com.ecom.service;

import java.util.List;

import com.ecom.exception.CartItemNotFoundException;
import com.ecom.exception.InsufficientStockException;
import com.ecom.exception.InvalidQuantityException;
import com.ecom.model.CartItem;
import com.ecom.model.Products;
import com.ecom.repository.CartRepository;

public class CartService {

	private final CartRepository cartRepository;
	private final ProductsService productsService;

	public CartService(CartRepository cartRepository, ProductsService productsService) {
		this.cartRepository = cartRepository;
		this.productsService = productsService;
	}

	// Add product to cart
	public void addToCart(int customerId, int productId, int quantity) {
		if (quantity <= 0)
			throw new InvalidQuantityException("InvalidQuantityException: Quantity must be greater than 0.");

		Products product = productsService.getProduct(productId);

		// Check how much is already in the cart
		List<CartItem> cart = cartRepository.getCartItems(customerId);
		int existingQty = 0;
		for (CartItem item : cart) {
			if (item.getProductId() == productId) {
				existingQty = item.getQuantity();
				break;
			}
		}

		int totalRequested = existingQty + quantity;
		if (product.getQuantity() < totalRequested) {
			if (existingQty > 0) {
				throw new InsufficientStockException("InsufficientStockException: Only " + product.getQuantity()
						+ " unit(s) available. Cart already has " + existingQty + " unit(s).");
			}
			throw new InsufficientStockException("InsufficientStockException: Only " + product.getQuantity()
					+ " unit(s) of '" + product.getProductName() + "' available in stock.");
		}

		CartItem item = new CartItem(productId, product.getProductName(), product.getPrice(), quantity);
		cartRepository.addToCart(customerId, item);

		if (existingQty > 0) {
			System.out.println("  Updated cart: '" + product.getProductName() + "' quantity is now " + totalRequested);
		} else {
			System.out.println("  Added to cart: '" + product.getProductName() + "' x" + quantity);
		}
	}

	// Remove product from cart
	public void removeFromCart(int customerId, int productId) {
		if (!cartRepository.isInCart(customerId, productId)) {
			throw new CartItemNotFoundException(
					"CartItemNotFoundException: Product [ID: " + productId + "] not found in your cart.");
		}
		cartRepository.removeFromCart(customerId, productId);
		System.out.println("  Product [ID: " + productId + "] removed from cart.");
	}

	// Increase quantity
	public void increaseQuantity(int customerId, int productId, int qty) {
		if (qty <= 0)
			throw new InvalidQuantityException("InvalidQuantityException: Qty must be positive.");

		List<CartItem> cart = cartRepository.getCartItems(customerId);
		CartItem item = findCartItem(cart, productId);
		Products product = productsService.getProduct(productId);
		int newQty = item.getQuantity() + qty;

		if (product.getQuantity() < newQty) {
			throw new InsufficientStockException(
					"InsufficientStockException: Only " + product.getQuantity() + " unit(s) available.");
		}

		cartRepository.updateQuantity(customerId, productId, newQty);
		System.out.println("  Quantity increased to " + newQty + " for '" + item.getProductName() + "'");
	}

	// Decrease quantity
	public void decreaseQuantity(int customerId, int productId, int qty) {
		if (qty <= 0)
			throw new InvalidQuantityException("InvalidQuantityException: Qty must be positive.");

		List<CartItem> cart = cartRepository.getCartItems(customerId);
		CartItem item = findCartItem(cart, productId);
		int newQty = item.getQuantity() - qty;

		if (newQty <= 0) {
			cartRepository.removeFromCart(customerId, productId);
			System.out.println("  Item removed from cart (quantity reached 0).");
		} else {
			cartRepository.updateQuantity(customerId, productId, newQty);
			System.out.println("  Quantity decreased to " + newQty + " for '" + item.getProductName() + "'");
		}
	}

	// View cart by customer ID (database cart)
	public void viewCart(int customerId) {
		viewCart(cartRepository.getCartItems(customerId));
	}

	// View cart by items list (shared by customer and guest)
	public void viewCart(List<CartItem> cart) {
		if (cart == null || cart.isEmpty()) {
			System.out.println("  Your cart is empty.");
			return;
		}
		System.out.println("\n  ===== YOUR SHOPPING CART =====");
		cart.forEach(System.out::println);
		System.out.printf("  -------------------------------%n");
		double total = cart.stream().mapToDouble(CartItem::getTotalPrice).sum();
		System.out.printf("  TOTAL: $%.2f%n", total);
		System.out.println("  ================================");
	}

	// Calculate total
	public double calculateTotal(int customerId) {
		return cartRepository.getCartItems(customerId).stream().mapToDouble(CartItem::getTotalPrice).sum();
	}

	// Clear cart
	public void clearCart(int customerId) {
		cartRepository.clearCart(customerId);
		System.out.println("  Cart cleared.");
	}

	// Get cart items (for order placement)
	public List<CartItem> getCartItems(int customerId) {
		return cartRepository.getCartItems(customerId);
	}

	// Internal helper — uses CartItemNotFoundException instead of
	// ProductNotFoundException
	private CartItem findCartItem(List<CartItem> cart, int productId) {
		return cart.stream().filter(item -> item.getProductId() == productId).findFirst()
				.orElseThrow(() -> new CartItemNotFoundException(
						"CartItemNotFoundException: Product [ID: " + productId + "] not found in your cart."));
	}
}
