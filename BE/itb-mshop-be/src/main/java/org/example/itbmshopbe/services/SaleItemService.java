package org.example.itbmshopbe.services;

import lombok.RequiredArgsConstructor;
import org.example.itbmshopbe.dtos.SaleItemDetailDto;
import org.example.itbmshopbe.dtos.SaleItemGalleryDto;
import org.example.itbmshopbe.dtos.SaleItemPagedResponseDto;
import org.example.itbmshopbe.dtos.SaleItemRequestDto;
import org.example.itbmshopbe.entities.Brand;
import org.example.itbmshopbe.entities.SaleItem;
import org.example.itbmshopbe.exceptions.ItemNotFoundException;
import org.example.itbmshopbe.repositories.BrandRepository;
import org.example.itbmshopbe.repositories.SaleItemRepository;
import org.example.itbmshopbe.utils.SaleItemUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static org.example.itbmshopbe.utils.Util.trimFirstAndLastSentence;

@Service
@RequiredArgsConstructor
public class SaleItemService {
    private final SaleItemRepository saleItemRepository;
    private final BrandRepository brandRepository;
    private final SaleItemUtil saleItemUtil;

    private SaleItem findSaleItemById(Integer id){
        return saleItemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("SaleItem not found for this id :: " + id));
    }

    private SaleItemGalleryDto convertToGalleryDto(SaleItem saleItem){
        SaleItemGalleryDto dto = new SaleItemGalleryDto();
        dto.setId(saleItem.getId());
        dto.setModel(saleItem.getModel());
        dto.setBrandName(saleItem.getBrand().getName());
        dto.setPrice(saleItem.getPrice());
        dto.setRamGb(saleItem.getRamGb());
        dto.setStorageGb(saleItem.getStorageGb());
        dto.setColor(saleItem.getColor());
        return dto;
    }

    private SaleItemDetailDto convertToDetailDto(SaleItem saleItem) {
        SaleItemDetailDto dto = new SaleItemDetailDto();
        dto.setId(saleItem.getId());
        dto.setModel(saleItem.getModel());
        dto.setBrandName(saleItem.getBrand() != null ? saleItem.getBrand().getName() : "Unknown");
        dto.setDescription(saleItem.getDescription());
        dto.setPrice(saleItem.getPrice());
        dto.setRamGb(saleItem.getRamGb());
        dto.setScreenSizeInch(saleItem.getScreenSizeInch());
        dto.setQuantity(saleItem.getQuantity());
        dto.setStorageGb(saleItem.getStorageGb());
        dto.setColor(saleItem.getColor());
        dto.setCreatedOn(saleItem.getCreatedOn());
        dto.setUpdatedOn(saleItem.getUpdatedOn());
        return dto;
    }


    public List<SaleItemGalleryDto> getAllSaleItemsForGallery(){
        return saleItemRepository.findAll().stream()
                .map(saleItem -> convertToGalleryDto(saleItem))
                .collect(Collectors.toList());
    }

    public SaleItemDetailDto getSaleItemDetails(Integer id){
        return convertToDetailDto(findSaleItemById(id));
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SaleItemDetailDto addSaleItem(SaleItemRequestDto dto) {

        saleItemUtil.validateRequiredFields(dto);

        String color = saleItemUtil.sanitizeColor(dto.getColor());
        int quantity = saleItemUtil.resolveQuantity(dto.getQuantity());

        SaleItem newItem = new SaleItem();
        newItem.setBrand(saleItemUtil.resolveBrand(dto));
        newItem.setModel(trimFirstAndLastSentence(dto.getModel()));
        newItem.setDescription(trimFirstAndLastSentence(dto.getDescription()));
        newItem.setPrice(dto.getPrice());
        newItem.setRamGb(dto.getRamGb());
        newItem.setScreenSizeInch(dto.getScreenSizeInch());
        newItem.setQuantity(quantity);
        newItem.setStorageGb(dto.getStorageGb());
        newItem.setColor(color);
        newItem.setCreatedOn(Instant.now());
        newItem.setUpdatedOn(Instant.now());

        return convertToDetailDto(saleItemRepository.saveAndFlush(newItem));
    }


    @Transactional
    public SaleItemDetailDto updateSaleItem(Integer id, SaleItemRequestDto requestDto) {
        SaleItem saleItemToUpdate = findSaleItemById(id);

        String model = trimFirstAndLastSentence(requestDto.getModel());
        String description = trimFirstAndLastSentence(requestDto.getDescription());
        saleItemUtil.validateRequiredFields(requestDto);
        Brand brand = saleItemUtil.resolveBrand(requestDto);

        String color = saleItemUtil.sanitizeColor(requestDto.getColor());
        int quantity = saleItemUtil.resolveQuantity(requestDto.getQuantity());

        saleItemToUpdate.setBrand(brand);
        saleItemToUpdate.setModel(model);
        saleItemToUpdate.setDescription(description);
        saleItemToUpdate.setQuantity(quantity);
        saleItemToUpdate.setPrice(requestDto.getPrice());
        saleItemToUpdate.setScreenSizeInch(requestDto.getScreenSizeInch());
        saleItemToUpdate.setRamGb(requestDto.getRamGb());
        saleItemToUpdate.setStorageGb(requestDto.getStorageGb());
        saleItemToUpdate.setColor(color);
        saleItemToUpdate.setUpdatedOn(Instant.now());

        return convertToDetailDto(saleItemRepository.save(saleItemToUpdate));
    }

    public void deleteSaleItem(Integer id) {
        if (! saleItemRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "SaleItem with id " + id + " not found"
            );
        }
        saleItemRepository.deleteById(id);
    }

   public SaleItemPagedResponseDto getAllSaleItemsPaginatedAndFiltered(
           List<String> filterBrands,
           Integer page,
           Integer size,
           String sortField,
           String sortDirection) {

       if (page == null || page < 0) {
           throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page parameter is required and must be non-negative.");
       }
       int pageSize = (size == null || size <= 0) ? 10 : size;

       Sort.Direction direction = Sort.Direction.ASC;
       if ("desc".equalsIgnoreCase(sortDirection)) {
           direction = Sort.Direction.DESC;
       }

       String sortBy = (sortField == null || sortField.isBlank()) ? "createdOn" : sortField;
       Sort.Order order = new Sort.Order(direction, sortBy).ignoreCase();
       Pageable pageable = PageRequest.of(page, pageSize, Sort.by(order));

       Page<SaleItem> saleItemsPage;

       if (filterBrands != null && !filterBrands.isEmpty()) {
           List<String> validBrandNames = brandRepository.findAll().stream()
                   .map(brand -> brand.getName().toLowerCase())
                   .filter(name -> filterBrands.stream()
                           .map(String::toLowerCase)
                           .collect(Collectors.toSet())
                           .contains(name))
                   .collect(Collectors.toList());

           if (validBrandNames.isEmpty()) {
               saleItemsPage = Page.empty(pageable);
           } else {
               saleItemsPage = saleItemRepository.findByBrand_NameIgnoreCaseIn(validBrandNames, pageable);
           }

       } else {
           saleItemsPage = saleItemRepository.findAll(pageable);
       }

       List<SaleItemDetailDto> content = saleItemsPage.getContent().stream()
               .map(this::convertToDetailDto)
               .collect(Collectors.toList());

       SaleItemPagedResponseDto responseDto = new SaleItemPagedResponseDto();
       responseDto.setContent(content);
       responseDto.setLast(saleItemsPage.isLast());
       responseDto.setFirst(saleItemsPage.isFirst());
       responseDto.setTotalPages(saleItemsPage.getTotalPages());
       responseDto.setTotalElements(saleItemsPage.getTotalElements());
       responseDto.setSize(saleItemsPage.getSize());
       responseDto.setPage(saleItemsPage.getNumber());
       responseDto.setSort(saleItemsPage.getSort().isSorted() ? saleItemsPage.getSort().toString().replace(": ", ":") : null);

       return responseDto;
   }
}