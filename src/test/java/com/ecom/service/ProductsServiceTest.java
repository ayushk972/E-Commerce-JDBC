package com.ecom.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.ecom.exception.*;
import com.ecom.model.Products;
import com.ecom.repository.ProductsRepository;

public class ProductsServiceTest {

    @Mock
    private ProductsRepository productRepository;

    @InjectMocks
    private ProductsService productsService;

    private Products sampleProduct;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sampleProduct = new Products(1, "Laptop", "Tech", 1200.0, 5, "BrandX");
    }

    @Test
    void testAddProductSuccess() {
        when(productRepository.addProduct(any(Products.class))).thenReturn(1);
        assertDoesNotThrow(() -> productsService.addProduct("Laptop", "Tech", 1200.0, 5, "BrandX"));
        verify(productRepository, times(1)).addProduct(any(Products.class));
    }

    @Test
    void testAddProductInvalidValues() {
        assertThrows(InvalidPriceException.class,
            () -> productsService.addProduct("Laptop", "Tech", -10.0, 5, "BrandX"));
        assertThrows(InvalidQuantityException.class,
            () -> productsService.addProduct("Laptop", "Tech", 100.0, -1, "BrandX"));
    }

    @Test
    void testViewAllProducts() {
        when(productRepository.getAllProducts()).thenReturn(new ArrayList<>());
        productsService.viewAllProducts();

        List<Products> list = List.of(sampleProduct);
        when(productRepository.getAllProducts()).thenReturn(list);
        productsService.viewAllProducts();
        productsService.viewAllProductsAdmin();
    }

    @Test
    void testSearchById() {
        when(productRepository.findById(1)).thenReturn(sampleProduct);
        Products p1 = productsService.searchById(1);
        Products p2 = productsService.searchByIdAdmin(1);
        assertEquals(sampleProduct, p1);
        assertEquals(sampleProduct, p2);
    }

    @Test
    void testSearchByFilters() {
        when(productRepository.findByName("lap")).thenReturn(List.of(sampleProduct));
        when(productRepository.findByCategory("tech")).thenReturn(List.of(sampleProduct));
        when(productRepository.findByBrand("brand")).thenReturn(List.of(sampleProduct));

        assertDoesNotThrow(() -> productsService.searchByName("lap"));
        assertDoesNotThrow(() -> productsService.searchByCategory("tech"));
        assertDoesNotThrow(() -> productsService.searchByBrand("brand"));

        assertThrows(InvalidSearchCriteriaException.class, () -> productsService.searchByName(" "));
        assertThrows(InvalidSearchCriteriaException.class, () -> productsService.searchByCategory(""));
        assertThrows(InvalidSearchCriteriaException.class, () -> productsService.searchByBrand(null));
    }

    @Test
    void testSearchByPriceRange() {
        when(productRepository.findByPriceRange(100.0, 1500.0)).thenReturn(List.of(sampleProduct));
        assertDoesNotThrow(() -> productsService.searchByPriceRange(100.0, 1500.0));

        assertThrows(InvalidSearchCriteriaException.class, () -> productsService.searchByPriceRange(-1.0, 100.0));
        assertThrows(InvalidSearchCriteriaException.class, () -> productsService.searchByPriceRange(500.0, 100.0));
    }

    @Test
    void testUpdatesAndDelete() {
        when(productRepository.findById(1)).thenReturn(sampleProduct);
        assertDoesNotThrow(() -> productsService.updatePrice(1, 1300.0));
        assertDoesNotThrow(() -> productsService.updateQuantity(1, 10));
        assertDoesNotThrow(() -> productsService.deleteProduct(1));
        assertEquals(sampleProduct, productsService.getProduct(1));

        assertThrows(InvalidPriceException.class, () -> productsService.updatePrice(1, -5.0));
        assertThrows(InvalidQuantityException.class, () -> productsService.updateQuantity(1, -2));
    }

    @Test
    void testAddProductsFromFile(@TempDir File tempDir) throws Exception {
        File csv = new File(tempDir, "products.csv");
        try (FileWriter writer = new FileWriter(csv)) {
            writer.write("name,category,price,qty,brand\n");
            writer.write("Item1,Cat1,50.0,10,B1\n");
            writer.write("Item2,Cat2,invalid_price,10,B2\n");
            writer.write("ShortLine\n");
        }
        when(productRepository.addProductsBatch(anyList())).thenReturn(1);
        productsService.addProductsFromFile(csv.getAbsolutePath());

        // File not found test
        productsService.addProductsFromFile("non_existent_file.csv");
    }
}
