package org.example.itbmshopbe.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.itbmshopbe.dtos.SaleItemDetailDto;
import org.example.itbmshopbe.dtos.SaleItemGalleryDto;
import org.example.itbmshopbe.dtos.SaleItemPictureResponseDTO;
import org.example.itbmshopbe.dtos.SaleItemRequestDto;
import org.example.itbmshopbe.entities.SaleItemPicture;
import org.example.itbmshopbe.exceptions.ItemNotFoundException;
import org.example.itbmshopbe.services.FileService;
import org.example.itbmshopbe.services.SaleItemPictureService;
import org.example.itbmshopbe.services.SaleItemService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/v1/sale-items")
@RequiredArgsConstructor
@CrossOrigin(origins = "${frontend.url}")
public class SaleItemController {
    private final SaleItemService saleItemService;
    private final SaleItemPictureService saleItemPictureService;
    private final FileService fileService;

    @GetMapping("")
    public ResponseEntity<List<SaleItemGalleryDto>> getAllSaleItemsForGallery(){
        return ResponseEntity.ok(saleItemService.getAllSaleItemsForGallery());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaleItemDetailDto> getSaleItemDetails(@PathVariable Integer id){
        return ResponseEntity.ok(saleItemService.getSaleItemDetails(id));
    }

    @PostMapping("")
    public ResponseEntity<?> addSaleItem(
            @RequestPart("data") SaleItemRequestDto requestDto,
            @RequestPart(value = "files",required = false) List<MultipartFile> files)
    {
       try {
            SaleItemDetailDto createdSaleItem = saleItemService.addSaleItem(requestDto);
            if (files != null && !files.isEmpty()) {
                if (files.size() > 4) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum 4 picture");
                }
                saleItemPictureService.storePicture(createdSaleItem.getId(), files);
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    saleItemService.getSaleItemDetails(createdSaleItem.getId())
            );
       } catch (Exception e) {
           throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Sale item creation failed", e);
       }
    }


    @PutMapping("/{id}")
    public ResponseEntity<SaleItemDetailDto> updateSaleItem(
            @PathVariable Integer id,
            @RequestPart("data") SaleItemRequestDto requestDto,
            @RequestPart(value = "files",required = false) List<MultipartFile> newFiles,
            @RequestPart(value = "deletePictureIds",required = false) List<Integer> deletePictureIds,
            @RequestPart(value = "orderedPictureIds", required = false) List<Integer> orderedPictureIds
            //,@RequestPart(value = "deletePictureIds", required = false) String deletePictureIdsJson
            ) {
        try {
            //List<Integer> deletePictureIds = new ObjectMapper().readValue(deletePictureIdsJson, new TypeReference<List<Integer>>() {});
            SaleItemDetailDto updatedSaleItem = saleItemService.updateSaleItem(id, requestDto);

            //delete old picture
            if (deletePictureIds != null && !deletePictureIds.isEmpty()) {
                saleItemPictureService.deletePictureByIds(deletePictureIds);
            }
            int existingCount = saleItemPictureService.countPicturesBySaleItemId(id);

            if(newFiles != null && !newFiles.isEmpty()) {
                if (existingCount + newFiles.size() > 4) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum 4 picture");
                }
                saleItemPictureService.storePicture(updatedSaleItem.getId(), newFiles);
            }
            if (orderedPictureIds != null && !orderedPictureIds.isEmpty()) {
                saleItemPictureService.updatePictureOrder(id,orderedPictureIds);
            }
            return ResponseEntity.ok(saleItemService.getSaleItemDetails(id));
        } catch (ItemNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sale item not found", e);
       } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Sale item update failed", e);
       }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSaleItem(@PathVariable Integer id) {
        saleItemService.deleteSaleItem(id);
    }

    @PostMapping("/{id}/pictures")
    public ResponseEntity<List<SaleItemPictureResponseDTO>> uploadPictures(
            @PathVariable Integer id,
            @RequestPart("files") List<MultipartFile> files) {

        if (files.size() > 4) {
            return ResponseEntity.badRequest().build();
        }

        List<SaleItemPictureResponseDTO> savedPictures = saleItemPictureService.storePicture(id, files);
        return ResponseEntity.ok(savedPictures);
    }

    @GetMapping("/picture/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(
            @PathVariable String filename) {
        Resource file = fileService.loadFileAsResource(filename);
        return ResponseEntity.
                ok()
                .contentType(MediaType.
                        IMAGE_JPEG).body(file);
    }
}
