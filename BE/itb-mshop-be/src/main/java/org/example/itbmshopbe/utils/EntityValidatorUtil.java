package org.example.itbmshopbe.utils;

import lombok.RequiredArgsConstructor;
import org.example.itbmshopbe.entities.Account;
import org.example.itbmshopbe.entities.Order;
import org.example.itbmshopbe.repositories.AccountRepository;
import org.example.itbmshopbe.repositories.OrderItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class EntityValidatorUtil {
    private final AccountRepository accountRepository;
    private final OrderItemRepository orderItemRepository;

    public Account validateActiveAccount(Integer accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Account not found"
                ));

        if (account.getStatus() != Account.Status.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "User is not active"
            );
        }

        return account;
    }

    public boolean validateOrderAccess(Order order, Integer currentUserId) {
        boolean isBuyer = order.getCustomer().getId().equals(currentUserId);
        boolean isSeller = order.getOrderItems().stream()
                .anyMatch(oi -> oi.getSaleItem().getSeller().getId().equals(currentUserId));

        if (!isBuyer && !isSeller) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "User is not an owner (seller/buyer) of the order"
            );
        }

        return isBuyer;
    }

    public boolean isItemAlreadyOrdered(Integer userId, Integer saleItemId) {
        return orderItemRepository.existsByOrderCustomerIdAndSaleItemId(userId, saleItemId);
    }

    public void validateNotOwnItem(Integer sellerId, Integer buyerId, String errorMessage) {
        if (sellerId.equals(buyerId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    errorMessage
            );
        }
    }
}
