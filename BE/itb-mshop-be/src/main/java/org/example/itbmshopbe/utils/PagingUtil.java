package org.example.itbmshopbe.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class PagingUtil {
    public Pageable createPageable(Integer page, Integer size, String sortField, String sortDirection, String defaultSortField) {
        if (page == null || page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page parameter is required and must be non-negative.");
        }

        int pageSize = (size == null || size <= 0) ? 10 : size;
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String sortBy = (sortField == null || sortField.isBlank()) ? defaultSortField : sortField;
        Sort.Order order = new Sort.Order(direction, sortBy);

        return PageRequest.of(page, pageSize, Sort.by(order));
    }
}
