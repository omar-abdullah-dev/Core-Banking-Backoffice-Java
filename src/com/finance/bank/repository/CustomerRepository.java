package com.finance.bank.repository;

import com.finance.bank.exception.DuplicateNationalIdException;
import com.finance.bank.exception.InvalidNationalIdException;
import com.finance.bank.model.Customer;
import com.finance.bank.util.IdGenerator;
import com.finance.bank.util.NationalIdValidator;

import java.util.*;

public class CustomerRepository {
    private final Map<String, Customer> customersByNationalId = new HashMap<>();
    private final Map<String, Customer> customersBySystemId = new HashMap<>();

    /**
     * Creates and saves a new customer
     * @param name Customer name
     * @param nationalId Egyptian National ID
     * @return Created customer
     * @throws DuplicateNationalIdException if National ID already exists
     * @throws InvalidNationalIdException if National ID is invalid
     */
    public Customer save(String name, String nationalId)
            throws DuplicateNationalIdException, InvalidNationalIdException {

        // Validate National ID
        NationalIdValidator.validateNationalId(nationalId);

        // Check for duplicates
        if (customersByNationalId.containsKey(nationalId)) {
            throw new DuplicateNationalIdException(
                    "Customer with National ID " + nationalId + " already exists"
            );
        }

        // Generate system ID and create customer
        String systemId = IdGenerator.generateCustomerId();
        Customer customer = new Customer(systemId, name, nationalId);

        // Store in both maps for efficient lookup
        customersByNationalId.put(nationalId, customer);
        customersBySystemId.put(systemId, customer);

        return customer;
    }

    /**
     * Finds customer by National ID
     * @param nationalId Egyptian National ID
     * @return Customer if found, null otherwise
     */
    public Customer findByNationalId(String nationalId) {
        if (nationalId == null) {
            return null;
        }
        return customersByNationalId.get(nationalId.trim());
    }

    /**
     * Finds customer by system-generated ID
     * @param systemId System ID
     * @return Customer if found, null otherwise
     */
    public Customer findBySystemId(String systemId) {
        return customersBySystemId.get(systemId);
    }

    /**
     * Checks if a customer exists with given National ID
     * @param nationalId National ID to check
     * @return true if exists, false otherwise
     */
    public boolean existsByNationalId(String nationalId) {
        return customersByNationalId.containsKey(nationalId);
    }

    /**
     * Returns all customers as a list
     * @return List of all customers
     */
    public List<Customer> findAll() {
        return new ArrayList<>(customersByNationalId.values());
    }

    /**
     * CRITICAL FOR TESTING: Clears all customers
     * Use in BankService.reset() for test isolation
     */
    public void clear() {
        customersByNationalId.clear();
        customersBySystemId.clear();
    }
}