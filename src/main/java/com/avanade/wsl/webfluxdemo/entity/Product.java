package com.avanade.wsl.webfluxdemo.entity;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Product {

    private String productId;

    private String content;

    private String experienceDetail;

    private List<Inventory> inventories;
}
