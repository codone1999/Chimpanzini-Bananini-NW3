package org.example.itbmshopbe.utils;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.example.itbmshopbe.entities.*;
import org.springframework.data.jpa.domain.Specification;

public class OrderSpecifications {
    public static Specification<Order> belongsToAccount(Integer accountId) {
        return (root, query, cb) -> {
            query.distinct(true);
            Join<Order, OrderItem> orderItems = root.join("orderItems", JoinType.LEFT);
            Join<OrderItem, SaleItem> saleItem = orderItems.join("saleItem", JoinType.LEFT);
            Join<SaleItem, Seller> seller = saleItem.join("seller", JoinType.LEFT);
            Join<Seller, Account> sellerAccount = seller.join("account", JoinType.LEFT);
            Predicate customerPredicate = cb.equal(root.get("customer").get("id"), accountId);
            Predicate sellerPredicate = cb.equal(sellerAccount.get("id"), accountId);

            return cb.or(customerPredicate, sellerPredicate);
        };
    }
}
