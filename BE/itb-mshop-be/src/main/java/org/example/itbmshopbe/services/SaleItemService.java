package org.example.itbmshopbe.services;

import lombok.RequiredArgsConstructor;
import org.example.itbmshopbe.dtos.SaleItemDetailDto;
import org.example.itbmshopbe.dtos.SaleItemGalleryDto;
import org.example.itbmshopbe.dtos.SaleItemPagedResponseDto;
import org.example.itbmshopbe.dtos.SaleItemRequestDto;
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

        SaleItem saleItem = saleItemRepository.save(newItem);
        return modelMapper.map(saleItem, SaleItemDetailDto.class);
    }


   @Transactional
   public SaleItemDetailDto updateSaleItem(Integer id,SaleItemRequestDto dto) {
        SaleItem saleItemToUpdate = findSaleItemById(id);
        saleItemUtil.validateRequiredFields(dto);
        modelMapper.map(dto, saleItemToUpdate);
        saleItemToUpdate.setBrand(saleItemUtil.resolveBrand(dto));
        saleItemToUpdate.setModel(trimFirstAndLastSentence(dto.getModel()));
        saleItemToUpdate.setDescription(trimFirstAndLastSentence(dto.getDescription()));
        saleItemToUpdate.setQuantity(saleItemUtil.resolveQuantity(dto.getQuantity()));
        saleItemToUpdate.setColor(saleItemUtil.sanitizeColor(dto.getColor()));

        return modelMapper.map(saleItemToUpdate, SaleItemDetailDto.class);
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

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String sortBy = (sortField == null || sortField.isBlank()) ? "createdOn" : sortField;
        Sort.Order order = new Sort.Order(direction, sortBy).ignoreCase();
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(order));

        Page<SaleItem> saleItemsPage;

        if (filterBrands != null && !filterBrands.isEmpty()) {
            saleItemsPage = saleItemRepository.findByBrand_NameIgnoreCaseIn(filterBrands, pageable);
        } else {
            saleItemsPage = saleItemRepository.findAll(pageable);
        }

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