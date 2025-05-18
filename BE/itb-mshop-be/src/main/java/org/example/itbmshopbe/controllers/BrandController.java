package org.example.itbmshopbe.controllers;

import lombok.RequiredArgsConstructor;
import org.example.itbmshopbe.dtos.BrandDetailsDto;
import org.example.itbmshopbe.dtos.BrandRequestDto;
import org.example.itbmshopbe.services.BrandService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/v1/brands")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://ip24nw3.sit.kmutt.ac.th")
public class BrandController {
    private final BrandService brandService;

    @GetMapping("")
    public ResponseEntity<List<BrandDetailsDto>> getAllBrands() {
        return ResponseEntity.ok(brandService.getAllBrands());
    }
    @PostMapping("")
    public ResponseEntity<BrandDetailsDto> createBrand(@RequestBody BrandRequestDto requestDto) {
        BrandDetailsDto createdBrand = brandService.createBrand(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBrand);
    }


    @GetMapping("/{id}")
    public ResponseEntity<BrandDetailsDto> getBrandById(@PathVariable Integer id){
        return ResponseEntity.ok(brandService.getBrandById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BrandDetailsDto> updateBrand(@PathVariable Integer id,
                                                       @RequestBody BrandRequestDto requestDto) {
        BrandDetailsDto updatedBrand = brandService.updateBrand(id, requestDto);
        return ResponseEntity.ok(updatedBrand);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBrand(@PathVariable Integer id) {
        brandService.deleteBrandById(id);
    }

}
