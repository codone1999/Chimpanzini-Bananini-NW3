package org.example.itbmshopbe.controllers;

import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.example.itbmshopbe.dtos.SaleItemDetailDto;
import org.example.itbmshopbe.dtos.SaleItemGalleryDto;
import org.example.itbmshopbe.services.SaleItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

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
}
