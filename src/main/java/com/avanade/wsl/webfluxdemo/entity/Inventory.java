package com.avanade.wsl.webfluxdemo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Inventory {

    private String inventoryId;

    private boolean booked;

}
