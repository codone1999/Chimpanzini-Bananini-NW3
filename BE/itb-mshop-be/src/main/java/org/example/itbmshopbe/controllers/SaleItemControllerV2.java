package org.example.itbmshopbe.controllers;

import lombok.RequiredArgsConstructor;
import org.example.itbmshopbe.dtos.*;
import org.example.itbmshopbe.services.SaleItemPictureService;
import org.example.itbmshopbe.services.SaleItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/v2/sale-items")
@CrossOrigin(origins = "${frontend.url}")
@RequiredArgsConstructor
public class SaleItemControllerV2 {

    private final SaleItemService saleItemService;
    private final SaleItemPictureService saleItemPictureService;

    @GetMapping
    public ResponseEntity<SaleItemPagedResponseDto> getAllSaleItems(
            @RequestParam(required = false) List<String> filterBrands,
            @RequestParam(required = false) List<Integer> filterStorages,
            @RequestParam(required = false) Integer filterPriceLower,
            @RequestParam(required = false) Integer filterPriceUpper,
            @RequestParam @Valid Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortDirection,
            @RequestParam(required = false, defaultValue = "false") Boolean filterNullStorage
    ) throws NoSuchFieldException {
        SaleItemPagedResponseDto responseDto = saleItemService.getAllSaleItemsPaginatedAndFiltered(
               filterBrands,filterStorages,filterPriceLower,filterPriceUpper,page,size,sortField,sortDirection,filterNullStorage
        );
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaleItemDetailWithImagesDto> getSaleItemById(@PathVariable Integer id){
        return ResponseEntity.ok(saleItemService.getSaleItemDetailWithImages(id));
    }

    @PostMapping
    public ResponseEntity<SaleItemDetailWithImagesDto> createProduct(
            @ModelAttribute SaleItemRequestDto saleItemDto,
            @RequestParam(required = false) List<MultipartFile> images
    ){
        try {
            SaleItemDetailDto createdSaleItem = saleItemService.addSaleItem(saleItemDto);
            if (images != null && !images.isEmpty()) {
                if (images.size() > 4) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum 4 picture");
                }
                saleItemPictureService.storePicture(createdSaleItem.getId(), images);
            }
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(saleItemService.getSaleItemDetailWithImages(createdSaleItem.getId()));
        }catch (Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"SaleItem Create Failed");
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSaleItemById(@PathVariable Integer id){
        try
            {
            saleItemService.deleteSaleItem(id);
            }catch (Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"SaleItem does not exist");
        }
    }

    @PutMapping("{id}")
    public ResponseEntity<SaleItemDetailWithImagesDto> updateSaleItem(
            @PathVariable Integer id,
            @ModelAttribute SaleItemWithImageInfo saleItemDto
    ){
        if(saleItemDto.getSaleItem() != null){
            saleItemService.updateSaleItem(id, saleItemDto.getSaleItem());
        }
        if(saleItemDto.getImagesInfos() != null && !saleItemDto.getImagesInfos().isEmpty()){
            saleItemPictureService.updatePictureByDisplayOrder(id, saleItemDto.getImagesInfos());
        }
        return ResponseEntity.ok(saleItemService.getSaleItemDetailWithImages(id));
    }
}