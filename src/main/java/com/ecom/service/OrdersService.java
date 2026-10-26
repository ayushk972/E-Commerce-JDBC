package com.ecom.service;

import java.util.List;
import java.util.UUID;

import com.ecom.exception.EmptyCartException;
import com.ecom.exception.InsufficientStockException;
import com.ecom.exception.InvalidOrderStatusTransitionException;
import com.ecom.exception.OrderAlreadyCancelledException;
import com.ecom.exception.OrderAlreadyDeliveredException;
import com.ecom.exception.UnauthorizedOrderAccessException;
import com.ecom.model.CartItem;
import com.ecom.model.Order;
import com.ecom.model.OrderStatus;
import com.ecom.model.Products;
import com.ecom.repository.OrdersRepository;
import com.ecom.repository.ProductsRepository;

public class OrdersService {

	private final OrdersRepository orderRepository;
	private final CartService cartService;
	private final ProductsService productsService;
	private final ProductsRepository productsRepoRef;

	public OrdersService(OrdersRepository orderRepository, CartService cartService, ProductsService productsService,
			ProductsRepository productsRepository) {
		this.orderRepository = orderRepository;
		this.cartService = cartService;
		this.productsService = productsService;
		this.productsRepoRef = productsRepository;
	}

	// Place an order
	public String placeOrder(int customerId) {
		List<CartItem> cartItems = cartService.getCartItems(customerId);

		// EmptyCartException — specific, not generic InvalidOrderException
		if (cartItems.isEmpty()) {
			throw new EmptyCartException(
					"EmptyCartException: Cannot place order. Your cart is empty. Please add items first.");
		}

		// Validate stock for every item before touching the DB
		for (CartItem item : cartItems) {
			Products product = productsService.getProduct(item.getProductId());
			if (product.getQuantity() < item.getQuantity()) {
				throw new InsufficientStockException(
						"InsufficientStockException: Insufficient stock for '" + item.getProductName()
								+ "'. Available: " + product.getQuantity() + ", Requested: " + item.getQuantity());
			}
		}

		// Deduct stock from DB
		for (CartItem item : cartItems) {
			Products product = productsService.getProduct(item.getProductId());
			int newQty = product.getQuantity() - item.getQuantity();
			productsRepoRef.updateQuantity(item.getProductId(), newQty);
		}

		double total = cartService.calculateTotal(customerId);
		String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

		Order order = new Order(orderId, customerId, cartItems, total);
		orderRepository.saveOrder(order);

		cartService.clearCart(customerId);

		System.out.println("\n   Order placed successfully!");
		System.out.println("  Order ID: " + orderId);
		System.out.printf("  Total Amount: $%.2f%n", total);
		return orderId;
	}

	// View order details (customer view)
	public void viewOrder(String orderId) {
		Order order = orderRepository.findById(orderId);
		System.out.println(order);
	}

	// View order details (admin view with createdAt and updatedAt)
	public void viewOrderAdmin(String orderId) {
		Order order = orderRepository.findById(orderId);
		System.out.println(order.toAdminString());
	}

	// View all orders of a customer
	public void viewCustomerOrders(int customerId) {
		List<Order> orders = orderRepository.findByCustomerId(customerId);
		if (orders.isEmpty()) {
			System.out.println("  You have no orders yet.");
			return;
		}
		System.out.println("\n  ===== YOUR ORDERS (" + orders.size() + ") =====");
		orders.forEach(System.out::println);
	}

	// Cancel an order
	public void cancelOrder(String orderId, int customerId) {
		Order order = orderRepository.findById(orderId);

		// UnauthorizedOrderAccessException — not generic InvalidOrderException
		if (order.getCustomerId() != customerId) {
			throw new UnauthorizedOrderAccessException(
					"UnauthorizedOrderAccessException: You are not authorized to cancel order [" + orderId + "].");
		}

		// OrderAlreadyDeliveredException
		if (order.getOrderStatus() == OrderStatus.DELIVERED) {
			throw new OrderAlreadyDeliveredException("OrderAlreadyDeliveredException: Cannot cancel order [" + orderId
					+ "] — it has already been delivered.");
		}

		// OrderAlreadyCancelledException
		if (order.getOrderStatus() == OrderStatus.CANCELLED) {
			throw new OrderAlreadyCancelledException(
					"OrderAlreadyCancelledException: Order [" + orderId + "] is already cancelled.");
		}

		// Restore inventory in DB
		for (CartItem item : order.getItems()) {
			Products product = productsService.getProduct(item.getProductId());
			int restoredQty = product.getQuantity() + item.getQuantity();
			productsRepoRef.updateQuantity(item.getProductId(), restoredQty);
		}

		orderRepository.updateOrderStatus(orderId, OrderStatus.CANCELLED);
		System.out.println("  Order [" + orderId + "] has been cancelled. Inventory restored.");
	}

	// View all orders (admin view with createdAt and updatedAt)
	public void viewAllOrdersAdmin() {
		List<Order> orders = orderRepository.getAllOrders();
		if (orders.isEmpty()) {
			System.out.println("  No orders placed yet.");
			return;
		}
		System.out.println("\n  ===== ALL ORDERS (ADMIN) (" + orders.size() + ") =====");
		orders.forEach(o -> System.out.println(o.toAdminString()));
	}

	// Update order status (admin)
	public void updateOrderStatus(String orderId, OrderStatus newStatus) {
		Order order = orderRepository.findById(orderId);
		OrderStatus current = order.getOrderStatus();

		// OrderAlreadyCancelledException
		if (current == OrderStatus.CANCELLED) {
			throw new OrderAlreadyCancelledException(
					"OrderAlreadyCancelledException: Cannot update order [" + orderId + "] — it is already CANCELLED.");
		}

		// InvalidOrderStatusTransitionException — backward or same-level moves
		if (newStatus.ordinal() <= current.ordinal()) {
			throw new InvalidOrderStatusTransitionException(
					"InvalidOrderStatusTransitionException: Cannot move status from " + current + " to " + newStatus
							+ ". Order status can only move forward.");
		}

		orderRepository.updateOrderStatus(orderId, newStatus);
		System.out.println("  Order [" + orderId + "] status updated: " + current + " -> " + newStatus);
	}
}
