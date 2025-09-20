package org.example.itbmshopbe.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.itbmshopbe.dtos.SaleItemDTO.SaleItemDetailDto;
import org.example.itbmshopbe.dtos.SaleItemDTO.SaleItemDetailWithImagesDto;
import org.example.itbmshopbe.dtos.SaleItemDTO.SaleItemPagedResponseDto;
import org.example.itbmshopbe.dtos.SaleItemDTO.SaleItemRequestDto;
import org.example.itbmshopbe.entities.SaleItem;
import org.example.itbmshopbe.services.SaleItemPictureService;
import org.example.itbmshopbe.services.SaleItemService;
import org.example.itbmshopbe.utils.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/v2/seller")
@RequiredArgsConstructor
@CrossOrigin(origins = "${frontend.url}")
public class SellerController {
    private final SaleItemService saleItemService;
    private final SaleItemPictureService saleItemPictureService;

    @GetMapping("/{id}/sale-item")
    public ResponseEntity<SaleItemPagedResponseDto> getSellerSaleItem(
            @PathVariable Integer id,
            @RequestParam(required = false) List<String> filterBrands,
            @RequestParam(required = false) List<Integer> filterStorages,
            @RequestParam(required = false) Integer filterPriceLower,
            @RequestParam(required = false) Integer filterPriceUpper,
            @RequestParam @Valid Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortDirection,
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(required = false, defaultValue = "false") Boolean filterNullStorage,
            HttpServletRequest request
    ) throws NoSuchFieldException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Token");
        }
        String token = authHeader.substring(7);

        Integer tokenUserId = JwtTokenUtil.getIdFromToken(token);
        String tokenUserRole = JwtTokenUtil.getRoleFromToken(token);

        if (!"SELLER".equalsIgnoreCase(tokenUserRole)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only sellers can access sale items");
        }
        if (!id.equals(tokenUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Request seller id not matched with id in access token");
        }

        SaleItemPagedResponseDto responseDto = saleItemService.getAllSaleItemsPaginatedAndFiltered(
                filterBrands, filterStorages, filterPriceLower, filterPriceUpper,
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
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Token");
        }
        String token = authHeader.substring(7);

        Integer tokenUserId = JwtTokenUtil.getIdFromToken(token);
        String tokenUserRole = JwtTokenUtil.getRoleFromToken(token);

        if (!"SELLER".equalsIgnoreCase(tokenUserRole)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only sellers can add sale items");
        }
        if (!id.equals(tokenUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Request seller id not matched with id in access token");
        }

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
}
