package org.example.itbmshopbe.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "brand")
public class Brand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 30)
    @NotNull
    @Column(name = "name", nullable = false, length = 30)
    private String name;

    @Size(max = 40)
    @Column(name = "websiteUrl", length = 40)
    private String websiteUrl;

    @Column(name = "isActive")
    private Boolean isActive;

    @Size(max = 80)
    @Column(length = 80)
    private String countryOfOrigin;

    @NotNull
    @Column(name = "createdOn", nullable = false)
    private Instant createdOn;

    @NotNull
    @Column(name = "updatedOn", nullable = false)
    private Instant updatedOn;

    @OneToMany(mappedBy = "brand")
    private Set<SaleItem> saleItems = new LinkedHashSet<>();

}