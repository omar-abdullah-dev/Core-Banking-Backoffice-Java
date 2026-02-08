package com.finance.bank.model;

import com.finance.bank.util.NationalIdValidator;

import static java.util.UUID.randomUUID;

public abstract class Person {

    private final String systemId;       // immutable
    private String name;                 // mutable (optional)
    private final String nationalId;     // immutable
    private String emailAddress;
    private String phoneNumber;

    // Constructor 1 (Basic)
    public Person(String name, String nationalId) {
        validateName(name);
        NationalIdValidator.validateNationalId(nationalId);

        this.systemId = randomUUID().toString();
        this.name = name;
        this.nationalId = nationalId;
    }


    // Constructor 2 (Full)
    public Person(String systemId, String name, String nationalId, String emailAddress, String phoneNumber) {
        validateName(name);
        NationalIdValidator.validateNationalId(nationalId);

        this.systemId = (systemId == null || systemId.isBlank()) ? randomUUID().toString() : systemId;
        this.name = name;
        this.nationalId = nationalId;
        this.emailAddress = emailAddress;
        this.phoneNumber = phoneNumber;
    }
    public Person(String systemId, String name, String nationalId) {
        validateName(name);
        NationalIdValidator.validateNationalId(nationalId);
        this.systemId = (systemId == null || systemId.isBlank()) ? randomUUID().toString() : systemId;
        this.name = name;
        this.nationalId = nationalId;
    }
    // Getters
    public String getSystemId() {
        return systemId;
    }

    public String getName() {
        return name;
    }

    public String getNationalId() {
        return nationalId;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    // Allowed setters
    public void setName(String name) {
        validateName(name);
        this.name = name;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }


    // helpers
    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null, empty, or blank");
        }
    }
}
