package com.avanade.wsl.webfluxdemo.controller;

import com.avanade.wsl.webfluxdemo.service.ProductService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/product")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Endpoint of retrieving all products
     *
     * @return all products data in json format
     */
    @GetMapping("/")
    public Flux<String> getAllProducts() {
        return productService.getAllProducts();
    }

    /**
     * Endpoint of retrieving a single product by productId
     *
     * @param id productId
     * @return a single product data in json format
     */
    @GetMapping("/{id}")
    public Mono<String> getProductById(@PathVariable String id) {
        return productService.getProductById(id);
    }

    /**
     * Endpoint of posting a request to book an inventory by inventoryId
     *
     * @param inventoryId inventoryId
     * @return if success, return the booked product which contains a list of inventories; if failed, return a response of bad request with adding info
     */
    @PostMapping("/{inventoryId}")
    public Mono<String> bookInventoryByInventoryId(@PathVariable String inventoryId) {
        return productService.bookInventoryByInventoryId(inventoryId);
    }

}
