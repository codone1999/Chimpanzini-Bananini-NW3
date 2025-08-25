package org.example.itbmshopbe.services;

import lombok.RequiredArgsConstructor;
import org.example.itbmshopbe.dtos.SaleItemPictureRequest;
import org.example.itbmshopbe.dtos.SaleItemPictureResponseDTO;
import org.example.itbmshopbe.entities.SaleItem;
import org.example.itbmshopbe.entities.SaleItemPicture;
import org.example.itbmshopbe.repositories.SaleItemPictureRepository;
import org.example.itbmshopbe.repositories.SaleItemRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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
                newFileName = fileService.storeFile(file, saleItemId, i + 1,false);
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
    @Transactional
    public void updatePictureByDisplayOrder(Integer saleItemId, List<SaleItemPictureRequest> pictures) {
        List<SaleItemPicture> existingPictures = saleItemPictureRepository.findBySaleItemId(saleItemId);
        for (SaleItemPictureRequest pictureReq : pictures) {
            String status = pictureReq.getStatus();
            switch (status.toLowerCase()) {
                case"add":
                    handleAddPicture(saleItemId, existingPictures, pictureReq);
                    break;
                case"remove":
                    handleRemovePicture(existingPictures, pictureReq);
                    break;
                case"replace":
                    handleReplacePicture(saleItemId, existingPictures, pictureReq);
                    break;
                default:
                    throw new RuntimeException("Unknown status: " + status);
            }
            existingPictures = saleItemPictureRepository.findBySaleItemId(saleItemId)
                    .stream()
                    .sorted((a, b) -> a.getDisplayOrder().compareTo(b.getDisplayOrder()))
                    .toList();

            int order = 1;
            for (SaleItemPicture pic : existingPictures) {
                pic.setDisplayOrder(order++);
                saleItemPictureRepository.save(pic);
            }
        }
    }
    private void handleAddPicture(Integer saleItemId, List<SaleItemPicture> existingPictures,
                                  SaleItemPictureRequest pictureReq) {
        if (existingPictures.size() >= 4) {
            throw new RuntimeException("Maximum number of pictures exceeded");
        }

        int newOrder;
        if (pictureReq.getOrder() == null) {
            newOrder = existingPictures.size() + 1;
        } else {
            newOrder = pictureReq.getOrder();
            for (SaleItemPicture picture : existingPictures) {
                if (picture.getDisplayOrder() >= newOrder) {
                    int oldOrder = picture.getDisplayOrder();
                    int newShiftedOrder = oldOrder + 1;

                    // Gen new picture name base on order
                    String extension = fileService.getFileExtension(picture.getNewPictureName());
                    String newFileName = saleItemId + "_" + newShiftedOrder + (extension.isEmpty() ? "" : "." + extension);

                    Path oldPath = fileService.getFileStorageLocation().resolve(picture.getNewPictureName());
                    Path newPath = fileService.getFileStorageLocation().resolve(newFileName);
                    try {
                        Files.move(oldPath, newPath,StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to rename file " + oldPath + " → " + newPath, e);
                    }
                    picture.setDisplayOrder(newShiftedOrder);
                    picture.setNewPictureName(newFileName);
                    saleItemPictureRepository.save(picture);
                }
            }
        }

        String newPictureName;
        try {
            newPictureName = fileService.storeFile(pictureReq.getPictureFile(), saleItemId, newOrder,false);
        } catch (IOException e) {
            throw new RuntimeException("Failed to add file " + pictureReq.getPictureFile(), e);
        }

        SaleItem saleItem = saleItemRepository.findById(saleItemId)
                .orElseThrow(() -> new RuntimeException("SaleItem not found"));

        SaleItemPicture newSaleItemPicture = new SaleItemPicture();
        newSaleItemPicture.setSaleItem(saleItem);
        newSaleItemPicture.setOldPictureName(pictureReq.getPictureFile().getOriginalFilename());
        newSaleItemPicture.setNewPictureName(newPictureName);
        newSaleItemPicture.setFileSizeBytes((int) pictureReq.getPictureFile().getSize());
        newSaleItemPicture.setDisplayOrder(newOrder);
        newSaleItemPicture.setCreatedOn(Instant.now());
        saleItemPictureRepository.save(newSaleItemPicture);
    }


    private void handleRemovePicture(List<SaleItemPicture> existingPictures, SaleItemPictureRequest pictureRequest) {
        SaleItemPicture toRemove = existingPictures.stream()
                .filter(pic -> pic.getDisplayOrder().equals(pictureRequest.getOrder()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No picture found for order"));

        Path filePath = fileService.getFileStorageLocation().resolve(toRemove.getNewPictureName());
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file " + toRemove.getNewPictureName(), e);
        }

        saleItemPictureRepository.delete(toRemove);

        existingPictures.stream()
                .filter(pic -> pic.getDisplayOrder() > toRemove.getDisplayOrder())
                .sorted((a, b) -> a.getDisplayOrder().compareTo(b.getDisplayOrder())) // lowest → highest
                .forEach(picture -> {
                    int oldOrder = picture.getDisplayOrder();
                    int newOrder = oldOrder - 1;

                    String extension = fileService.getFileExtension(picture.getNewPictureName());
                    String newFileName = picture.getSaleItem().getId() + "_" + newOrder + (extension.isEmpty() ? "" : "." + extension);

                    Path oldPath = fileService.getFileStorageLocation().resolve(picture.getNewPictureName());
                    Path newPath = fileService.getFileStorageLocation().resolve(newFileName);

                    try {
                        Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to rename file " + oldPath + " → " + newPath, e);
                    }

                    picture.setDisplayOrder(newOrder);
                    picture.setNewPictureName(newFileName);
                    saleItemPictureRepository.save(picture);
                });
    }


    private void handleReplacePicture(Integer saleItemId , List<SaleItemPicture> existingPicture, SaleItemPictureRequest pictureRequest) {
        SaleItemPicture toRePlace =existingPicture.stream()
                .filter(pic -> pic.getDisplayOrder().equals(pictureRequest.getOrder())).findFirst()
                .orElseThrow(() -> new RuntimeException("No picture found for order"));
        Path oldFilePath = fileService.getFileStorageLocation().resolve(toRePlace.getNewPictureName());
        try {
            Files.deleteIfExists(oldFilePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file " + toRePlace.getNewPictureName(), e);
        }
        String newFileName;
        try {
            newFileName = fileService.storeFile(pictureRequest.getPictureFile(),saleItemId, pictureRequest.getOrder(),false);
        }catch (IOException e) {
            throw new RuntimeException("Failed to replace file " + pictureRequest.getPictureFile(), e);
        }
        toRePlace.setOldPictureName(pictureRequest.getPictureFile().getOriginalFilename());
        toRePlace.setNewPictureName(newFileName);
        toRePlace.setFileSizeBytes((int) pictureRequest.getPictureFile().getSize());
        saleItemPictureRepository.save(toRePlace);
    }
}
