package org.example.itbmshopbe.utils;

import org.example.itbmshopbe.entities.SaleItem;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class SaleItemSpecifications {

    public static Specification<SaleItem> hasBrands(List<String> brandNames) {
        return (root, query, cb) -> {
            if (brandNames == null || brandNames.isEmpty()) return cb.conjunction();
            return cb.lower(root.get("brand").get("name"))
                    .in(brandNames.stream().map(String::toLowerCase).toList());
        };
    }

    public static Specification<SaleItem> minPrice(Integer minPrice) {
        return (root, query, cb) ->
                minPrice == null ? cb.conjunction()
                        : cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    public static Specification<SaleItem> maxPrice(Integer maxPrice) {
        return (root, query, cb) ->
                maxPrice == null ? cb.conjunction()
                        : cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    public static Specification<SaleItem> hasStorage(List<Integer> storageSize, Boolean filterNullStorage) {
        return (root, query, cb) -> {
            if (Boolean.TRUE.equals(filterNullStorage)) {
                return cb.isNull(root.get("storageGb"));
            } else if (storageSize != null && !storageSize.isEmpty()) {
                return root.get("storageGb").in(storageSize);
            }
            return cb.conjunction();
        };
    }

    public static Specification<SaleItem> keyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return cb.conjunction();
            String likePattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("description")), likePattern),
                    cb.like(cb.lower(root.get("model")), likePattern),
                    cb.like(cb.lower(root.get("color")), likePattern)
            );
        };
    }

    public static Specification<SaleItem> hasSeller(Integer sellerId) {
        return (root, query, cb) -> {
            if (sellerId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("seller").get("id"), sellerId);
        };
    }

    public static Specification<SaleItem> isNotDeleted() {
        return (root, query, cb) -> cb.or(
                cb.isNull(root.get("deleted")),
                cb.isFalse(root.get("deleted"))
        );
    }

    public static Specification<SaleItem> includeDeleted() {
        return (root, query, cb) -> cb.conjunction();
    }
}
