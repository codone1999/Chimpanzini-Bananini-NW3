package org.example.itbmshopbe.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.example.itbmshopbe.dtos.CreateSaleItemDto;
import org.example.itbmshopbe.dtos.SaleItemDetailDto;
import org.example.itbmshopbe.dtos.SaleItemGalleryDto;
import org.example.itbmshopbe.exceptions.ItemNotFoundException;
import org.example.itbmshopbe.services.SaleItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerErrorException;

import java.util.List;

@RestController
@RequestMapping("/v1/sale-items")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://ip24nw3.sit.kmutt.ac.th")
public class SaleItemController {
    private final SaleItemService saleItemService;

    @GetMapping("")
    public ResponseEntity<List<SaleItemGalleryDto>> getAllSaleItemsForGallery(){
        return ResponseEntity.ok(saleItemService.getAllSaleItemsForGallery());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaleItemDetailDto> getSaleItemDetails(@PathVariable Integer id){
        return ResponseEntity.ok(saleItemService.getSaleItemDetails(id));
    }

    @PostMapping("")
    public ResponseEntity<SaleItemDetailDto> addSaleItem(@Valid @RequestBody CreateSaleItemDto createSaleItemDto){
        try{
            SaleItemDetailDto createdSaleItem = saleItemService.addSaleItem(createSaleItemDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSaleItem);
        } catch (Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Sale item creation failed", e);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<SaleItemDetailDto> updateSaleItem(
            @PathVariable Integer id,
            @RequestBody CreateSaleItemDto updateSaleItemDto) {

        try {
            SaleItemDetailDto updatedSaleItem = saleItemService.updateSaleItem(id, updateSaleItemDto);
            return ResponseEntity.ok(updatedSaleItem);
        } catch (ItemNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sale item not found", e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Sale item update failed", e);
        }
    }
}
