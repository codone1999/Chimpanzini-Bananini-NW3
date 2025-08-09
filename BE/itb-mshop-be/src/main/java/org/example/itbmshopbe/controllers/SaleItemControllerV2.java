package org.example.itbmshopbe.controllers;

import lombok.RequiredArgsConstructor;
import org.example.itbmshopbe.dtos.SaleItemFilterRequestDTO;
import org.example.itbmshopbe.dtos.SaleItemGalleryDto;
import org.example.itbmshopbe.dtos.SaleItemPagedResponseDto;
import org.example.itbmshopbe.services.SaleItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/v2/sale-items")
@CrossOrigin(origins = "${frontend.url}")
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

    @GetMapping("/storage-sizes")
    public ResponseEntity<List<SaleItemGalleryDto>> getStorageSizes() {
        return ResponseEntity.ok(saleItemService.getDistinctStorageSizes());
    }

    @GetMapping("/filter")
    public ResponseEntity<List<SaleItemGalleryDto>> filterSaleItems(@RequestBody SaleItemFilterRequestDTO saleItemFilterRequestDTO) {
        return ResponseEntity.ok(saleItemService.filterSaleItems(saleItemFilterRequestDTO));
    }

    @GetMapping("/brands")
    public ResponseEntity<List<SaleItemGalleryDto>> getFilterByBrands() {
        return ResponseEntity.ok(saleItemService.getDistinctBrandNames());
    }
}