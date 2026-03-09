package com.pern.merch.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private String category; // T-Shirts, Hoodies, Vinyl, Posters, Accessories

    private String size; // S, M, L, XL, ONE_SIZE

    private String imageUrl;

    @Column(nullable = false)
    private int stockQuantity;
}
