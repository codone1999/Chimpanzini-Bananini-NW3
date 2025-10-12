package org.example.itbmshopbe.utils;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.regex.Pattern;

@Component
public class ValidationUtil {
    private static final Pattern MOBILE_PATTERN = Pattern.compile("0\\d{9}");
    private static final Pattern NATIONAL_ID_PATTERN = Pattern.compile("\\d{13}");
    private static final Pattern BANK_ACCOUNT_PATTERN = Pattern.compile("\\d{10,12}");

    public void validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    fieldName + " is required"
            );
        }
    }
    public <T> void validateRequired(T value, String fieldName) {
        if (value == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    fieldName + " is required"
            );
        }
    }
    public void validateMobile(String mobile) {
        if (mobile == null || !MOBILE_PATTERN.matcher(mobile).matches()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid mobile number format. Must be 10 digits starting with 0"
            );
        }
    }
    public void validateNationalId(String nationalId) {
        if (nationalId == null || !NATIONAL_ID_PATTERN.matcher(nationalId).matches()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid National ID format. Must be 13 digits"
            );
        }
    }
    public void validateBankAccount(String bankAccount) {
        if (bankAccount == null || !BANK_ACCOUNT_PATTERN.matcher(bankAccount).matches()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid bank account number format. Must be 10-12 digits"
            );
        }
    }
    public void validatePageParameter(Integer page) {
        if (page == null || page < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page parameter is required and must be non-negative"
            );
        }
    }
    public <T> void validateNotEmpty(Iterable<T> items, String fieldName) {
        if (items == null || !items.iterator().hasNext()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    fieldName + " cannot be empty"
            );
        }
    }
    public <T> void validateMaxSize(Iterable<T> items, int maxSize, String fieldName) {
        if (items == null) return;

        int count = 0;
        for (T item : items) {
            count++;
            if (count > maxSize) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        fieldName + " cannot exceed " + maxSize + " items"
                );
            }
        }
    }
}
