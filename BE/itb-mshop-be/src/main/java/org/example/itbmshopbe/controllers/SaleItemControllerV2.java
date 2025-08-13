package org.example.itbmshopbe.controllers;

import lombok.RequiredArgsConstructor;
import org.example.itbmshopbe.dtos.SaleItemDetailWithImagesDto;
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
            @RequestParam(required = false) List<Integer> filterStorages,
            @RequestParam(required = false) Integer filterPriceLower,
            @RequestParam(required = false) Integer filterPriceUpper,
            @RequestParam @Valid Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortDirection
    ) throws NoSuchFieldException {
        SaleItemPagedResponseDto responseDto = saleItemService.getAllSaleItemsPaginatedAndFiltered(
               filterBrands,filterStorages,filterPriceLower,filterPriceUpper,page,size,sortField,sortDirection
        );
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaleItemDetailWithImagesDto> getSaleItemById(@PathVariable Integer id){
        return ResponseEntity.ok(saleItemService.getSaleItemDetailWithImages(id));
    }
}