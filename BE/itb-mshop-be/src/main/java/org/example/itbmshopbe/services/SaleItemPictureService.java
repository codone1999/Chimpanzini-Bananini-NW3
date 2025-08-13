package org.example.itbmshopbe.services;

import lombok.RequiredArgsConstructor;
import org.example.itbmshopbe.dtos.SaleItemPictureResponseDTO;
import org.example.itbmshopbe.entities.SaleItem;
import org.example.itbmshopbe.entities.SaleItemPicture;
import org.example.itbmshopbe.repositories.SaleItemPictureRepository;
import org.example.itbmshopbe.repositories.SaleItemRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SaleItemPictureService {
    private final SaleItemPictureRepository saleItemPictureRepository;
    private final SaleItemRepository saleItemRepository;
    private final FileService fileService;
    private final ModelMapper modelMapper;

    public List<SaleItemPictureResponseDTO> storePicture(
            Integer saleItemId,
            List<MultipartFile> files
    ) {
        SaleItem saleItem = saleItemRepository.findById(saleItemId)
                .orElseThrow(() -> new RuntimeException("SaleItem not found"));
        List<SaleItemPicture> picturesToSave = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            String oldName = file.getOriginalFilename();  // Use original filename as oldPictureName

            String newFileName;
            try {
                newFileName = fileService.storeFile(file, saleItemId, i + 1);
            } catch (IOException e) {
                throw new RuntimeException("Failed to store file " + oldName, e);
            }

            SaleItemPicture picture = new SaleItemPicture();
            picture.setSaleItem(saleItem);
            picture.setOldPictureName(oldName);
            picture.setNewPictureName(newFileName);
            picture.setFileSizeBytes((int) file.getSize());
            picture.setDisplayOrder(i + 1);
            picture.setCreatedOn(Instant.now());
            picturesToSave.add(picture);
        }

        List<SaleItemPicture> savedPictures = (List<SaleItemPicture>) saleItemPictureRepository.saveAll(picturesToSave);

        return savedPictures.stream()
                .map(picture -> modelMapper.map(picture, SaleItemPictureResponseDTO.class))
                .toList();
    }

    public void deletePictureByIds(List<Integer> ids) {
        List<SaleItemPicture> pictures = saleItemPictureRepository.findAllById(ids);
        for (SaleItemPicture picture : pictures) {
            Path filePath = fileService.getFileStorageLocation().resolve(picture.getNewPictureName());
            try{
                Files.deleteIfExists(filePath);
            }catch (IOException e) {
                throw new RuntimeException("Failed to delete file " + picture.getNewPictureName(), e);
            }
        }
        saleItemPictureRepository.deleteAll(pictures);
    }

    public void updatePictureOrder(Integer saleItemId, List<Integer> orderedIds) {
        List<SaleItemPicture> pictures = saleItemPictureRepository.findBySaleItemId(saleItemId);
        for (int i = 0; i < orderedIds.size(); i++) {
            int newOrder = i + 1;
            Integer pictureId = orderedIds.get(i);

            pictures.stream()
                    .filter(p -> p.getId().equals(pictureId))
                    .findFirst()
                    .ifPresent(p -> p.setDisplayOrder(newOrder));
        }

        saleItemPictureRepository.saveAll(pictures);
    }

    public int countPicturesBySaleItemId(Integer saleItemId) {
        return saleItemPictureRepository.countBySaleItemId(saleItemId);
    }

}
