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

public class AdminMenuTest {

    private AdminMenu adminMenu;

    @BeforeEach
    void setUp() {
        ProductsRepository pRepo = new ProductsRepository();
        CustomersRepository cRepo = new CustomersRepository();
        OrdersRepository oRepo = new OrdersRepository();
        CartRepository cartRepo = new CartRepository();

        ProductsService pService = new ProductsService(pRepo);
        CustomersService cService = new CustomersService(cRepo);
        CartService cartService = new CartService(cartRepo, pService);
        OrdersService oService = new OrdersService(oRepo, cartService, pService, pRepo);

        adminMenu = new AdminMenu(pService, cService, oService);
    }

    @AfterEach
    void tearDown() {
        ConsoleHelper.setScanner(new Scanner(System.in));
    }

    private void runWithInput(String input) {
        ConsoleHelper.setScanner(new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8))));
        assertDoesNotThrow(() -> adminMenu.show());
    }

    @Test
    void testMenuNavigationAndSearches() {
        String input = String.join("\n",
            "1",
            "4", "1",
            "5", "Laptop",
            "6", "Electronics",
            "7", "Brand",
            "8", "10", "1000",
            "12",
            "14",
            "999",
            "0"
        ) + "\n";

        runWithInput(input);
    }

    @Test
    void testProductManagementOptions() {
        String input = String.join("\n",
            "2", "AdminProd_" + System.currentTimeMillis(), "Tech", "AdminBrand", "99.0", "5",
            "3", "non_existent_file.csv",
            "9", "1", "109.99",
            "10", "1", "20",
            "11", "-999",
            "13", "-999",
            "15", "NON_EXISTENT_ORDER",
            "16", "NON_EXISTENT_ORDER", "1",
            "0"
        ) + "\n";

        runWithInput(input);

        ProductsRepository pRepo = new ProductsRepository();
        for (Products p : pRepo.findByName("AdminProd_")) {
            pRepo.deleteProduct(p.getProductId());
        }
    }
}
