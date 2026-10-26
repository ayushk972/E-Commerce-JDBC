package com.ecom.main;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ecom.model.Products;
import com.ecom.repository.*;
import com.ecom.service.*;

public class GuestMenuTest {

    private GuestMenu guestMenu;
    private ProductsRepository pRepo;
    private int prodId;

    @BeforeEach
    void setUp() {
        pRepo = new ProductsRepository();
        CustomersRepository cRepo = new CustomersRepository();
        OrdersRepository oRepo = new OrdersRepository();
        CartRepository cartRepo = new CartRepository();

        ProductsService pService = new ProductsService(pRepo);
        CustomersService cService = new CustomersService(cRepo);
        CartService cartService = new CartService(cartRepo, pService);
        OrdersService oService = new OrdersService(oRepo, cartService, pService, pRepo);
        CustomerMenu cMenu = new CustomerMenu(pService, cService, cartService, oService, oRepo);

        guestMenu = new GuestMenu(pService, cartService, cMenu, oService);

        Products p = new Products(0, "GuestTestProd_" + System.currentTimeMillis(), "Tech", 40.0, 10, "GuestBrand");
        prodId = pRepo.addProduct(p);
    }

    @AfterEach
    void tearDown() {
        ConsoleHelper.setScanner(new Scanner(System.in));
        if (prodId > 0) {
            try { pRepo.deleteProduct(prodId); } catch (Exception ignored) {}
        }
    }

    private void runWithInput(String input) {
        ConsoleHelper.setScanner(new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8))));
        assertDoesNotThrow(() -> guestMenu.show());
    }

    @Test
    void testGuestBrowsingAndCartFlow() {
        String input = String.join("\n",
            "1",
            "2", String.valueOf(prodId),
            "3", "GuestTestProd",
            "4", "Tech",
            "5", "GuestBrand",
            "6", "10", "100",
            "9", // remove when empty
            "10", // place order when empty
            "7", String.valueOf(prodId), "-1", // invalid qty
            "7", String.valueOf(prodId), "20", // insufficient stock
            "7", String.valueOf(prodId), "2", // valid add
            "7", String.valueOf(prodId), "1", // increment
            "8", // view cart
            "10", "0", // place order -> authenticate -> choose 0 (cancel)
            "9", "99999", // remove non-existing
            "9", String.valueOf(prodId), // remove existing
            "999", // invalid menu choice
            "0" // exit
        ) + "\n";

        runWithInput(input);
    }
}
