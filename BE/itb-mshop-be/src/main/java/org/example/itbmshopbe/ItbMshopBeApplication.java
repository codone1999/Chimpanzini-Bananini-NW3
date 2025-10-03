package org.example.itbmshopbe;

import org.example.itbmshopbe.dtos.OrderDTO.OrderItemRequestDto;
import org.example.itbmshopbe.dtos.OrderDTO.OrderSellerDetailDto;
import org.example.itbmshopbe.dtos.OrderDTO.OrderSellerResponseDto;
import org.example.itbmshopbe.entities.OrderItem;
import org.example.itbmshopbe.entities.Seller;
import org.example.itbmshopbe.utils.FileStorageProperties;
import org.example.itbmshopbe.utils.ListMapper;
import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties({FileStorageProperties.class})
public class ItbMshopBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ItbMshopBeApplication.class, args);
    }

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.createTypeMap(OrderItem.class, OrderItemRequestDto.class)
                .addMapping(src -> src.getSaleItem().getId(), OrderItemRequestDto::setSaleItemId)
                .addMapping(OrderItem::getPriceEach, OrderItemRequestDto::setPrice);

        // Custom mapping for Seller to OrderSellerDetailDto
        mapper.createTypeMap(Seller.class, OrderSellerDetailDto.class)
                .addMapping(src -> src.getAccount().getEmail(), OrderSellerDetailDto::setEmail)
                .addMapping(src -> src.getAccount().getFullname(), OrderSellerDetailDto::setFullName)
                .addMapping(src -> src.getAccount().getRole(), OrderSellerDetailDto::setUserType)
                .addMapping(src -> src.getAccount().getNickname(), OrderSellerDetailDto::setNickName);

        // Custom mapping for Seller to OrderSellerResponseDto
        mapper.createTypeMap(Seller.class, OrderSellerResponseDto.class)
                .addMapping(src -> src.getAccount().getFullname(), OrderSellerResponseDto::setSellerName);

        return mapper;
    }

    @Bean
    public ListMapper listMapper() {
        return ListMapper.getInstance();
    }


}
