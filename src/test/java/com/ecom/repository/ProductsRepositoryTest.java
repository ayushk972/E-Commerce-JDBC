package com.ecom.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.ecom.exception.ProductNotFoundException;
import com.ecom.model.Products;

public class ProductsRepositoryTest {

    private final ProductsRepository repository = new ProductsRepository();

    @Test
    void testProductCrudAndSearch() {
        String uniqueName = "TestP_" + System.currentTimeMillis();
        Products p = new Products(0, uniqueName, "Electronics", 999.99, 10, "TestBrand");
        int res = repository.addProduct(p);
        assertEquals(1, res, "addProduct should return 1 row affected");

        List<Products> insertedList = repository.findByName(uniqueName);
        assertFalse(insertedList.isEmpty());
        int id = insertedList.get(0).getProductId();

        try {
            assertTrue(repository.existsById(id));

            Products found = repository.findById(id);
            assertNotNull(found);
            assertEquals(uniqueName, found.getProductName());
            assertEquals("Electronics", found.getCategory());
            assertEquals("TestBrand", found.getBrand());
  
            repository.updatePrice(id, 888.88);
            repository.updateQuantity(id, 15);

            Products updated = repository.findById(id);
            assertEquals(888.88, updated.getPrice());
            assertEquals(15, updated.getQuantity());

            List<Products> byName = repository.findByName(uniqueName);
            assertFalse(byName.isEmpty());

            List<Products> byCat = repository.findByCategory("Electronics");
            assertFalse(byCat.isEmpty());

            List<Products> byBrand = repository.findByBrand("TestBrand");
            assertFalse(byBrand.isEmpty());

            List<Products> byPrice = repository.findByPriceRange(800.0, 1000.0);
            assertFalse(byPrice.isEmpty());

            List<Products> all = repository.getAllProducts();
            assertFalse(all.isEmpty());
        } finally {
            repository.deleteProduct(id);
            assertFalse(repository.existsById(id));
        }
    }

    @Test
    void testFindByIdNotFound() {
        assertThrows(ProductNotFoundException.class, () -> repository.findById(-9999));
    }

    @Test
    void testAddProductsBatch() {
        String b1Name = "Batch1_" + System.currentTimeMillis();
        String b2Name = "Batch2_" + System.currentTimeMillis();
        Products p1 = new Products(0, b1Name, "Test", 10.0, 5, "B1");
        Products p2 = new Products(0, b2Name, "Test", 20.0, 5, "B2");
        int count = repository.addProductsBatch(List.of(p1, p2));
        assertTrue(count >= 0);

        List<Products> b1 = repository.findByName(b1Name);
        List<Products> b2 = repository.findByName(b2Name);
        for (Products p : b1) repository.deleteProduct(p.getProductId());
        for (Products p : b2) repository.deleteProduct(p.getProductId());
    }
}
