package org.example.itbmshopbe.services;

import lombok.RequiredArgsConstructor;
import org.example.itbmshopbe.dtos.*;
import org.example.itbmshopbe.entities.SaleItem;
import org.example.itbmshopbe.exceptions.ItemNotFoundException;
import org.example.itbmshopbe.repositories.BrandRepository;
import org.example.itbmshopbe.repositories.SaleItemRepository;
import org.example.itbmshopbe.utils.ListMapper;
import org.example.itbmshopbe.utils.SaleItemUtil;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


import java.util.List;


import static org.example.itbmshopbe.utils.Util.trimFirstAndLastSentence;

@Service
@RequiredArgsConstructor
public class SaleItemService {
    private final SaleItemRepository saleItemRepository;
    private final BrandRepository brandRepository;
    private final SaleItemUtil saleItemUtil;
    private final ListMapper listMapper;
    private final ModelMapper modelMapper;

    private SaleItem findSaleItemById(Integer id){
        return saleItemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("SaleItem not found for this id :: " + id));
    }

    public List<SaleItemGalleryDto> getAllSaleItemsForGallery(){
        List<SaleItem> saleItems = saleItemRepository.findAll();
        return listMapper.mapList(saleItems, SaleItemGalleryDto.class, modelMapper);
    }

    public SaleItemDetailDto getSaleItemDetails(Integer id){
        SaleItem saleItem = findSaleItemById(id);
        return modelMapper.map(saleItem, SaleItemDetailDto.class);
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SaleItemDetailDto addSaleItem(SaleItemRequestDto dto) {

        saleItemUtil.validateRequiredFields(dto);

        String color = saleItemUtil.sanitizeColor(dto.getColor());
        int quantity = saleItemUtil.resolveQuantity(dto.getQuantity());

        SaleItem newItem = new SaleItem();
        newItem.setBrand(saleItemUtil.resolveBrand(dto));
        newItem.setModel(trimFirstAndLastSentence(dto.getModel()));
        newItem.setColor(color);
        newItem.setQuantity(quantity);
        newItem.setDescription(trimFirstAndLastSentence(dto.getDescription()));
        newItem.setPrice(dto.getPrice());
        newItem.setRamGb(dto.getRamGb());
        newItem.setStorageGb(dto.getStorageGb());
        newItem.setScreenSizeInch(dto.getScreenSizeInch());

        SaleItem saleItem = saleItemRepository.save(newItem);
        return modelMapper.map(saleItem, SaleItemDetailDto.class);
    }


    @Transactional
    public SaleItemDetailDto updateSaleItem(Integer id, SaleItemRequestDto dto) {
            SaleItem existingSaleItem = saleItemRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "SaleItem not found for id :: " + id));
            saleItemUtil.validateRequiredFields(dto);
            String trimmedModel = trimFirstAndLastSentence(dto.getModel());
            String trimmedDescription = trimFirstAndLastSentence(dto.getDescription());
            String sanitizedColor = saleItemUtil.sanitizeColor(dto.getColor());
            int resolvedQuantity = saleItemUtil.resolveQuantity(dto.getQuantity());
            var brand = saleItemUtil.resolveBrand(dto);
            existingSaleItem.setModel(trimmedModel);
            existingSaleItem.setDescription(trimmedDescription);
            existingSaleItem.setColor(sanitizedColor);
            existingSaleItem.setQuantity(resolvedQuantity);
            existingSaleItem.setPrice(dto.getPrice());
            existingSaleItem.setBrand(brand);
            existingSaleItem.setRamGb(dto.getRamGb());
            existingSaleItem.setStorageGb(dto.getStorageGb());
            existingSaleItem.setScreenSizeInch(dto.getScreenSizeInch());

            SaleItem updatedSaleItem = saleItemRepository.save(existingSaleItem);
            return modelMapper.map(updatedSaleItem, SaleItemDetailDto.class);
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
            List<Integer> filterStorages,
            Integer filterPriceLower,
            Integer filterPriceUpper,
            Integer page,
            Integer size,
            String sortField,
            String sortDirection) throws NoSuchFieldException {

        if (page == null || page < 0) { //check page is not -
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page parameter is required and must be non-negative.");
        }
        int pageSize = (size == null || size <= 0) ? 10 : size; //Check if size is missing or invalid,

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        //Default sort is ascending unless sortDirection=desc
        String sortBy = (sortField == null || sortField.isBlank()) ? "createdOn" : sortField;
        //Default sort field is createdOn unless another is given.

        Sort.Order order;
        if (String.class.equals(SaleItem.class.getDeclaredField(sortBy).getType())) {
            order = new Sort.Order(direction, sortBy).ignoreCase();
        } else {
            order = new Sort.Order(direction, sortBy);
        }

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(order));

        Page<SaleItem> saleItemsPage = saleItemRepository.filterSaleItem(
                (filterBrands == null || filterBrands.isEmpty()) ? null : filterBrands.stream().map(String::toLowerCase).toList(),
                filterPriceLower,
                filterPriceUpper,
                (filterStorages == null || filterStorages.isEmpty()) ? null : filterStorages,
                pageable
        );//use repo to filter

        List<SaleItemDetailDto> content = listMapper.mapList(saleItemsPage.getContent(), SaleItemDetailDto.class, modelMapper);

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