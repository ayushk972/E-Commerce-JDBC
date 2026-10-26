package com.ecom.service;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.ecom.exception.*;
import com.ecom.model.Products;
import com.ecom.repository.ProductsRepository;

public class ProductsServiceTest {

    private ProductsRepository repository;
    private ProductsService service;
    private int createdProductId;
    private String prodName;

    @BeforeEach
    void setUp() {
        repository = new ProductsRepository();
        service = new ProductsService(repository);
        prodName = "SrvProd_" + System.currentTimeMillis();
        Products p = new Products(0, prodName, "Electronics", 150.0, 10, "TestBrand");
        repository.addProduct(p);
        List<Products> list = repository.findByName(prodName);
        createdProductId = list.isEmpty() ? 1 : list.get(0).getProductId();
    }

    @AfterEach
    void tearDown() {
        if (createdProductId > 0) {
            try { repository.deleteProduct(createdProductId); } catch (Exception ignored) {}
        }
    }

    @Test
    void testAddProductValidation() {
        assertThrows(InvalidPriceException.class, () -> service.addProduct("P", "C", -10.0, 5, "B"));
        assertThrows(InvalidQuantityException.class, () -> service.addProduct("P", "C", 10.0, -5, "B"));
        assertDoesNotThrow(() -> service.addProduct("ValidP_" + System.currentTimeMillis(), "C", 50.0, 5, "B"));

        List<Products> list = repository.findByName("ValidP_");
        for (Products p : list) repository.deleteProduct(p.getProductId());
    }

    @Test
    void testSearchAndGetProducts() {
        assertNotNull(service.searchById(createdProductId));
        assertNotNull(service.searchByIdAdmin(createdProductId));
        assertNotNull(service.getProduct(createdProductId));

        assertDoesNotThrow(() -> service.viewAllProducts());
        assertDoesNotThrow(() -> service.viewAllProductsAdmin());
        assertDoesNotThrow(() -> service.searchByName(prodName));
        assertDoesNotThrow(() -> service.searchByCategory("Electronics"));
        assertDoesNotThrow(() -> service.searchByBrand("TestBrand"));
        assertDoesNotThrow(() -> service.searchByPriceRange(100.0, 200.0));
    }

    @Test
    void testSearchInvalidCriteria() {
        assertThrows(InvalidSearchCriteriaException.class, () -> service.searchByName("   "));
        assertThrows(InvalidSearchCriteriaException.class, () -> service.searchByCategory("   "));
        assertThrows(InvalidSearchCriteriaException.class, () -> service.searchByBrand("   "));
        assertThrows(InvalidSearchCriteriaException.class, () -> service.searchByPriceRange(-5.0, 10.0));
        assertThrows(InvalidSearchCriteriaException.class, () -> service.searchByPriceRange(50.0, 20.0));
    }

    @Test
    void testUpdatesAndDelete() {
        assertThrows(InvalidPriceException.class, () -> service.updatePrice(createdProductId, -1.0));
        assertThrows(InvalidQuantityException.class, () -> service.updateQuantity(createdProductId, -5));

        assertDoesNotThrow(() -> service.updatePrice(createdProductId, 199.99));
        assertDoesNotThrow(() -> service.updateQuantity(createdProductId, 25));
        assertDoesNotThrow(() -> service.deleteProduct(createdProductId));
        createdProductId = -1;
    }

    @Test
    void testAddProductsFromFile(@TempDir File tempDir) throws Exception {
        File csvFile = new File(tempDir, "products.csv");
        try (FileWriter writer = new FileWriter(csvFile)) {
            writer.write("productName,category,price,quantity,brand\n");
            writer.write("CSVProduct1,Electronics,299.99,10,BrandA\n");
            writer.write("invalid,line\n");
        }
        service.addProductsFromFile(csvFile.getAbsolutePath());
        service.addProductsFromFile("non_existent_file.csv");

        List<Products> list = repository.findByName("CSVProduct1");
        for (Products p : list) repository.deleteProduct(p.getProductId());
    }
}
