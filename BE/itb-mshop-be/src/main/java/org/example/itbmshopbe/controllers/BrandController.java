package org.example.itbmshopbe.controllers;

import lombok.RequiredArgsConstructor;
import org.example.itbmshopbe.dtos.BrandDto;
import org.example.itbmshopbe.services.BrandService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/brands")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://ip24nw3.sit.kmutt.ac.th")
public class BrandController {
    private final BrandService brandService;

    @GetMapping("")
    public ResponseEntity<List<BrandDto>> getAllBrands(){
        return ResponseEntity.ok(brandService.getAllBrands());
    }
}
