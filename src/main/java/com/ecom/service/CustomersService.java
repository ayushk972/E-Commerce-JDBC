package com.ecom.service;

import java.util.List;
import java.util.Optional;

import com.ecom.exception.CustomerNotFoundException;
import com.ecom.exception.DuplicateEmailException;
import com.ecom.exception.InvalidCustomerDataException;
import com.ecom.model.Customers;
import com.ecom.repository.CustomersRepository;
import com.ecom.util.Validator;

public class CustomersService {

    private final CustomersRepository customerRepository;

    public CustomersService(CustomersRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    // Register a new customer (now includes password)
    public Customers register(String name, String email, String password, String mobile, String address) {
        Validator.validateName(name);
        Validator.validateEmail(email);
        Validator.validatePassword(password);
        Validator.validateMobile(mobile);
        Validator.validateAddress(address);

        if (customerRepository.emailExists(email)) {
            throw new DuplicateEmailException(
                "DuplicateEmailException: Email '" + email + "' is already registered. Please login.");
        }

        Customers customer = new Customers(0, name, email, password, mobile, address);
        int id = customerRepository.addCustomer(customer);
        System.out.println("\n  Registration successful! Your Customer ID: " + id);
        return customerRepository.findById(id);
    }

    // Login by email + password
    public Customers login(String email, String password) {
        if (email == null || email.isBlank())
            throw new InvalidCustomerDataException("InvalidCustomerDataException: Email cannot be blank.");
        if (password == null || password.isBlank())
            throw new InvalidCustomerDataException("InvalidCustomerDataException: Password cannot be blank.");

        Optional<Customers> customer = customerRepository.findByEmail(email);
        if (customer.isEmpty()) {
            throw new CustomerNotFoundException(
                "CustomerNotFoundException: No account found with email '" + email + "'.");
        }
        if (!customer.get().getPassword().equals(password)) {
            throw new InvalidCustomerDataException(
                "InvalidCustomerDataException: Incorrect password. Please try again.");
        }
        System.out.println("\n  Welcome back, " + customer.get().getName() + "!");
        return customer.get();
    }

    // View customer details
    public void viewCustomer(int id) {
        System.out.println(customerRepository.findById(id));
    }

    // Search customer by ID
    public Customers searchById(int id) {
        return customerRepository.findById(id);
    }

    // Update customer address
    public void updateAddress(int id, String newAddress) {
        Validator.validateAddress(newAddress);
        Customers customer = customerRepository.findById(id);
        String oldAddress = customer.getAddress();
        customerRepository.updateAddress(id, newAddress);
        System.out.println("  Previous Address: " + oldAddress);
        System.out.println("  New Address     : " + newAddress);
        System.out.println("  Address updated successfully for Customer [ID: " + id + "]");
    }

    // Update password
    public void updatePassword(int id, String oldPassword, String newPassword) {
        Customers customer = customerRepository.findById(id);
        if (!customer.getPassword().equals(oldPassword)) {
            throw new InvalidCustomerDataException(
                "InvalidCustomerDataException: Old password is incorrect.");
        }
        Validator.validatePassword(newPassword);
        customerRepository.updatePassword(id, newPassword);
        System.out.println("  Password updated successfully.");
    }

    // View all customers (Customer standard view)
    public void viewAllCustomers() {
        List<Customers> list = customerRepository.getAllCustomers();
        if (list.isEmpty()) {
            System.out.println("  No customers registered yet.");
            return;
        }
        System.out.println("\n  ===== ALL CUSTOMERS (" + list.size() + ") =====");
        list.forEach(System.out::println);
    }

    // View all customers (Admin view with createdAt and updatedAt)
    public void viewAllCustomersAdmin() {
        List<Customers> list = customerRepository.getAllCustomers();
        if (list.isEmpty()) {
            System.out.println("  No customers registered yet.");
            return;
        }
        System.out.println("\n  ===== ALL CUSTOMERS (ADMIN) (" + list.size() + ") =====");
        list.forEach(c -> System.out.println(c.toAdminString()));
    }
}
