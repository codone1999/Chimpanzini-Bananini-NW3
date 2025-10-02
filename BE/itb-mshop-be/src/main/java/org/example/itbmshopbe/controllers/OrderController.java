package org.example.itbmshopbe.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "${frontend.url}")
public class OrderController {

}
