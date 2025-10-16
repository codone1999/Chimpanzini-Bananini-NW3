package org.example.itbmshopbe.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.itbmshopbe.dtos.OrderDTO.OrderSellerResponseDto;
import org.example.itbmshopbe.dtos.OrderDTO.OrderSellerViewResponseDto;
import org.example.itbmshopbe.dtos.SaleItemDTO.SaleItemDetailDto;
import org.example.itbmshopbe.dtos.SaleItemDTO.SaleItemDetailWithImagesDto;
import org.example.itbmshopbe.dtos.SaleItemDTO.SaleItemPagedResponseDto;
import org.example.itbmshopbe.dtos.SaleItemDTO.SaleItemRequestDto;
import org.example.itbmshopbe.entities.Account;
import org.example.itbmshopbe.entities.Seller;
import org.example.itbmshopbe.repositories.SellerRepository;
import org.example.itbmshopbe.services.OrderService;
import org.example.itbmshopbe.services.SaleItemPictureService;
import org.example.itbmshopbe.services.SaleItemService;
import org.example.itbmshopbe.utils.Util;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/v2/seller")
@RequiredArgsConstructor
@CrossOrigin(origins = "${frontend.url}")
public class SellerController {
    private final SaleItemService saleItemService;
    private final SaleItemPictureService saleItemPictureService;
    private final SellerRepository sellerRepository;
    private final OrderService orderService;

    @GetMapping("/{id}/sale-item")
    public ResponseEntity<SaleItemPagedResponseDto> getSellerSaleItem(
            @PathVariable Integer id,
            @RequestParam(required = false) List<String> filterBrands,
            @RequestParam(required = false) List<Integer> filterStorages,
            @RequestParam(required = false) Integer filterPriceLower,
            @RequestParam(required = false) Integer filterPriceUpper,
            @RequestParam(required = false)@Valid Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortDirection,
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(required = false, defaultValue = "false") Boolean filterNullStorage,
            HttpServletRequest request
    ) throws NoSuchFieldException {
        Integer tokenUserId = Util.validateAndGetSellerUserId(request, id);
        Optional<Seller> seller = sellerRepository.findById(tokenUserId);
        if (seller.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Seller not found");
        }

        if (seller.get().getAccount().getStatus() != Account.Status.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not active");
        }
        SaleItemPagedResponseDto responseDto = saleItemService.getAllSaleItemsPaginatedAndFiltered(
                tokenUserId,filterBrands, filterStorages, filterPriceLower, filterPriceUpper,
                page, size, sortField, sortDirection, filterNullStorage, searchKeyword
        );
        return ResponseEntity.ok(responseDto);
    }
    @PostMapping("/{id}/sale-item")
    public ResponseEntity<SaleItemDetailWithImagesDto> addSellerSaleItem(
            @PathVariable Integer id,
            @ModelAttribute SaleItemRequestDto saleItemDto,
            @RequestParam(required = false) List<MultipartFile> images,
            HttpServletRequest request
    ) {
        Integer tokenUserId = Util.validateAndGetSellerUserId(request, id);

        SaleItemDetailDto createdSaleItem = saleItemService.addSaleItem(tokenUserId, saleItemDto);

        if (images != null && !images.isEmpty()) {
            if (images.size() > 4) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum 4 pictures allowed");
            }
            saleItemPictureService.storePicture(createdSaleItem.getId(), images);
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(saleItemService.getSaleItemDetailWithImages(createdSaleItem.getId()));
    }

    @GetMapping("/{id}/orders")
    public ResponseEntity<List<OrderSellerViewResponseDto>> getSellerOrders(
            @PathVariable Integer id,
            @RequestParam(required = false) @Valid Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortDirection,
            HttpServletRequest request
    ) {
        Integer tokenUserId = Util.validateAndGetSellerUserId(request, id);
        List<OrderSellerViewResponseDto> response = orderService.getAllOrdersForSeller(
                tokenUserId,page,size,sortField,sortDirection);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/sale-item/{saleItemId}")
    public ResponseEntity<Void> deleteSellerSaleItem(
            @PathVariable Integer id,
            @PathVariable Integer saleItemId,
            HttpServletRequest request
    ) {
        Integer tokenUserId = Util.validateAndGetSellerUserId(request, id);
        Optional<Seller> seller = sellerRepository.findById(tokenUserId);
        if (seller.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Seller not found");
        }

        if (seller.get().getAccount().getStatus() != Account.Status.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not active");
        }
        saleItemService.deleteSellerSaleItem(tokenUserId, saleItemId);

        return ResponseEntity.noContent().build();
    }

}
