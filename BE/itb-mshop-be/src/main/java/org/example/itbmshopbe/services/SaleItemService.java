package org.example.itbmshopbe.services;

import lombok.RequiredArgsConstructor;
import org.example.itbmshopbe.dtos.SaleItemDTO.*;
import org.example.itbmshopbe.entities.Brand;
import org.example.itbmshopbe.entities.SaleItem;
import org.example.itbmshopbe.entities.SaleItemPicture;
import org.example.itbmshopbe.entities.Seller;
import org.example.itbmshopbe.exceptions.ItemNotFoundException;
import org.example.itbmshopbe.repositories.BrandRepository;
import org.example.itbmshopbe.repositories.SaleItemPictureRepository;
import org.example.itbmshopbe.repositories.SaleItemRepository;
import org.example.itbmshopbe.repositories.SellerRepository;
import org.example.itbmshopbe.utils.ListMapper;
import org.example.itbmshopbe.utils.SaleItemSpecifications;
import org.example.itbmshopbe.utils.SaleItemUtil;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


import static org.example.itbmshopbe.utils.Util.trimFirstAndLastSentence;

@Service
@RequiredArgsConstructor
public class SaleItemService {
    private final SaleItemRepository saleItemRepository;
    private final SaleItemPictureRepository saleItemPictureRepository;
    private final SellerRepository sellerRepository;
    private final BrandRepository brandRepository;
    private final SaleItemUtil saleItemUtil;
    private final ListMapper listMapper;
    private final ModelMapper modelMapper;
    private final FileService fileService;

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
    public SaleItemDetailDto addSaleItem(Integer sellerId,SaleItemRequestDto dto) {

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

        if (sellerId != null) {
            Seller seller = sellerRepository.findById(sellerId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Seller not found for id :: " + sellerId));
            newItem.setSeller(seller);
        }

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
            Brand brand = saleItemUtil.resolveBrand(dto);
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
       List<SaleItemPicture> pictures = saleItemPictureRepository.findBySaleItemId(id);

       // Delete picture file on disk
       for (SaleItemPicture pic : pictures) {
           Path filePath = fileService.getFileStorageLocation().resolve(pic.getNewPictureName());
           try {
               Files.deleteIfExists(filePath);
           } catch (IOException e) {
               throw new RuntimeException("Failed to delete picture file " + pic.getNewPictureName(), e);
           }
       }

       saleItemPictureRepository.deleteAll(pictures);
       saleItemRepository.deleteById(id);
   }
    public SaleItemPagedResponseDto getAllSaleItemsPaginatedAndFiltered(
            Integer sellerId,
            List<String> filterBrands,
            List<Integer> filterStorages,
            Integer filterPriceLower,
            Integer filterPriceUpper,
            Integer page,
            Integer size,
            String sortField,
            String sortDirection,
            Boolean filterNullStorage,
            String searchKeyword) throws NoSuchFieldException {

        if (page == null || page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page parameter is required and must be non-negative.");
        }
        int pageSize = (size == null || size <= 0) ? 10 : size;

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String sortBy = (sortField == null || sortField.isBlank()) ? "createdOn" : sortField;

        Sort.Order order;
        if (String.class.equals(SaleItem.class.getDeclaredField(sortBy).getType())) {
            order = new Sort.Order(direction, sortBy).ignoreCase();
        } else {
            order = new Sort.Order(direction, sortBy);
        }

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(order));

        Specification<SaleItem> spec = Specification
                .where(SaleItemSpecifications.hasSeller(sellerId))
                .and(SaleItemSpecifications.hasBrands(filterBrands))
                .and(SaleItemSpecifications.minPrice(filterPriceLower))
                .and(SaleItemSpecifications.maxPrice(filterPriceUpper))
                .and(SaleItemSpecifications.hasStorage(filterStorages, filterNullStorage))
                .and(SaleItemSpecifications.keyword(searchKeyword));

        Page<SaleItem> saleItemsPage = saleItemRepository.findAll(spec, pageable);
        List<SaleItemDetailDto> content = saleItemsPage.getContent().stream()
                .map(saleItem -> {
                    SaleItemDetailDto dto = modelMapper.map(saleItem, SaleItemDetailDto.class);
                    dto.setSellerId(saleItem.getSeller() != null ? saleItem.getSeller().getId() : null);
                    dto.setSellerName(saleItem.getSeller() != null ? saleItem.getSeller().getAccount().getNickname() : null);
                    return dto;
                })
                .toList();
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

    public SaleItemDetailWithImagesDto getSaleItemDetailWithImages(Integer id) {
        SaleItem saleItem = findSaleItemById(id);
        SaleItemDetailWithImagesDto dto = modelMapper.map(saleItem, SaleItemDetailWithImagesDto.class);
        dto.setBrandName(saleItem.getBrand().getName());
        dto.setSellerId(saleItem.getSeller() != null ? saleItem.getSeller().getId() : null);
        dto.setSellerName(saleItem.getSeller() != null ? saleItem.getSeller().getAccount().getNickname() : null);
        List<SaleItemPicture> pictures = saleItemPictureRepository.findBySaleItemId(id);
        List<SaleItemImageDto> imageDtos = pictures.stream().map(pic -> {
            SaleItemImageDto imgDto = new SaleItemImageDto();
            imgDto.setFileName(pic.getNewPictureName());
            imgDto.setImageViewOrder(pic.getDisplayOrder());
            return imgDto;
        }).toList();
        dto.setSaleItemImages(imageDtos);
        return dto;
    }
}