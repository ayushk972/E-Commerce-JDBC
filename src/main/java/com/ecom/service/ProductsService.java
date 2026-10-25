package com.ecom.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.ecom.exception.InvalidSearchCriteriaException;
import com.ecom.model.Products;
import com.ecom.repository.ProductsRepository;
import com.ecom.util.Validator;

public class ProductsService {

    private final ProductsRepository productRepository;

    public ProductsService(ProductsRepository productRepository) {
        this.productRepository = productRepository;
    }

    public void addProduct(String name, String category, double price, int quantity, String brand) {
        Validator.validateQuantity(quantity);
        Validator.validatePrice(price);
        Products product = new Products(0, name, category, price, quantity, brand);
        int id = productRepository.addProduct(product);
        System.out.println("\n  Product added successfully! [ID: " + id + "]");
    }

    // Batch add products from a file (CSV or delimiter-separated)
    public void addProductsFromFile(String filePath) {
        String cleanPath = filePath.trim().replace("\"", "");
        File file = new File(cleanPath);
        if (!file.exists() || !file.isFile()) {
            System.out.println("  ERROR: File not found at path: " + filePath);
            return;
        }

        List<Products> batch = new ArrayList<>();
        int lineNum = 0, skipped = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                lineNum++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                if (lineNum == 1 && line.toLowerCase().contains("name")) continue; // Skip header

                String[] parts = line.split("[,|\t]");
                if (parts.length < 5) {
                    System.out.println("  Line " + lineNum + ": Skipped (requires 5 fields: name,category,price,qty,brand)");
                    skipped++;
                    continue;
                }
                try {
                    String name = parts[0].trim();
                    String category = parts[1].trim();
                    double price = Double.parseDouble(parts[2].trim());
                    int qty = Integer.parseInt(parts[3].trim());
                    String brand = parts[4].trim();

                    Validator.validatePrice(price);
                    Validator.validateQuantity(qty);
                    batch.add(new Products(0, name, category, price, qty, brand));
                } catch (Exception ex) {
                    System.out.println("  Line " + lineNum + ": Skipped (" + ex.getMessage() + ")");
                    skipped++;
                }
            }
        } catch (IOException e) {
            System.out.println("  ERROR reading file: " + e.getMessage());
            return;
        }

        if (batch.isEmpty()) {
            System.out.println("  No valid products found in file.");
            return;
        }

        int added = productRepository.addProductsBatch(batch);
        System.out.println("\n  === BATCH UPLOAD SUMMARY ===");
        System.out.println("  Products successfully added: " + added);
        if (skipped > 0) System.out.println("  Lines skipped/invalid     : " + skipped);
        System.out.println("  ============================");
    }

    public void viewAllProducts() {
        List<Products> list = productRepository.getAllProducts();
        if (list.isEmpty()) { System.out.println("  No products available."); return; }
        System.out.println("\n  ===== ALL PRODUCTS (" + list.size() + ") =====");
        list.forEach(System.out::println);
    }

    public void viewAllProductsAdmin() {
        List<Products> list = productRepository.getAllProducts();
        if (list.isEmpty()) { System.out.println("  No products available."); return; }
        System.out.println("\n  ===== ALL PRODUCTS (ADMIN) (" + list.size() + ") =====");
        list.forEach(p -> System.out.println(p.toAdminString()));
    }

    public Products searchById(int id) {
        Products p = productRepository.findById(id);
        System.out.println(p);
        return p;
    }

    public Products searchByIdAdmin(int id) {
        Products p = productRepository.findById(id);
        System.out.println(p.toAdminString());
        return p;
    }

    public void searchByName(String name) {
        if (name == null || name.isBlank())
            throw new InvalidSearchCriteriaException("InvalidSearchCriteriaException: Search name cannot be blank.");
        printResults(productRepository.findByName(name), "name '" + name + "'");
    }

    public void searchByCategory(String category) {
        if (category == null || category.isBlank())
            throw new InvalidSearchCriteriaException("InvalidSearchCriteriaException: Category cannot be blank.");
        printResults(productRepository.findByCategory(category), "category '" + category + "'");
    }

    public void searchByBrand(String brand) {
        if (brand == null || brand.isBlank())
            throw new InvalidSearchCriteriaException("InvalidSearchCriteriaException: Brand cannot be blank.");
        printResults(productRepository.findByBrand(brand), "brand '" + brand + "'");
    }

    public void searchByPriceRange(double min, double max) {
        if (min < 0 || max < 0)
            throw new InvalidSearchCriteriaException("InvalidSearchCriteriaException: Price values cannot be negative.");
        if (min > max)
            throw new InvalidSearchCriteriaException(
                "InvalidSearchCriteriaException: Min price ($" + min + ") cannot exceed max price ($" + max + ").");
        printResults(productRepository.findByPriceRange(min, max), "price range $" + min + " - $" + max);
    }

    public void updatePrice(int id, double newPrice) {
        Validator.validatePrice(newPrice);
        productRepository.findById(id);
        productRepository.updatePrice(id, newPrice);
        System.out.println("  Price updated to $" + newPrice + " for product [ID: " + id + "]");
    }

    public void updateQuantity(int id, int newQuantity) {
        Validator.validateQuantity(newQuantity);
        productRepository.findById(id);
        productRepository.updateQuantity(id, newQuantity);
        System.out.println("  Quantity updated to " + newQuantity + " for product [ID: " + id + "]");
    }

    public void deleteProduct(int id) {
        productRepository.deleteProduct(id);
        System.out.println("  Product [ID: " + id + "] deleted successfully.");
    }

    public Products getProduct(int id) {
        return productRepository.findById(id);
    }

    private void printResults(List<Products> results, String searchFor) {
        if (results.isEmpty()) { System.out.println("  No products found for " + searchFor + "."); return; }
        System.out.println("\n  ===== SEARCH RESULTS (" + results.size() + ") for " + searchFor + " =====");
        results.forEach(System.out::println);
    }
}
