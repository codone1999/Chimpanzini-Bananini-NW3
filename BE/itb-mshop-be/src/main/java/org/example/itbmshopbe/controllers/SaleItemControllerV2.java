package org.example.itbmshopbe.controllers;

import lombok.RequiredArgsConstructor;
import org.example.itbmshopbe.dtos.SaleItemGalleryDto;
import org.example.itbmshopbe.dtos.SaleItemPagedResponseDto;
import org.example.itbmshopbe.services.SaleItemService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/v2/sale-items")
@CrossOrigin(origins = "http://ip24nw3.sit.kmutt.ac.th")
@RequiredArgsConstructor
public class SaleItemControllerV2 {

    private final SaleItemService saleItemService;

    @GetMapping
    public ResponseEntity<SaleItemPagedResponseDto> getAllSaleItems(
            @RequestParam(required = false) List<String> filterBrands,
            @RequestParam @Valid Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortDirection
    ) {
        SaleItemPagedResponseDto responseDto = saleItemService.getAllSaleItemsPaginatedAndFiltered(
                filterBrands, page, size, sortField, sortDirection
        );
        return ResponseEntity.ok(responseDto);
    }
}