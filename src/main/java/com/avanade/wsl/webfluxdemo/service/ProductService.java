package com.avanade.wsl.webfluxdemo.service;


import com.avanade.wsl.webfluxdemo.entity.Inventory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service which is relevant to products
 */
@Service
public class ProductService {

    private final WebClient productWebClient;

    public ProductService(WebClient productWebClient) {
        this.productWebClient = productWebClient;
    }

    /**
     * Function: query all products
     * Retriving data from upStream service
     *
     * @return products data in json format
     */
    public Flux<String> getAllProducts() {
        return this.productWebClient
                .get()
                .uri("/products")
                .retrieve()
                .bodyToFlux(String.class);
    }

    /**
     * Function: query product by the providing productId
     * Retriving data from the upStream service
     *
     * @param id productId
     * @return product data in json format
     */
    public Mono<String> getProductById(String id) {
        return this.productWebClient
                .get()
                .uri("/product/" + id)
                .retrieve()
                .bodyToMono(String.class);
    }

    /**
     * Function: book an inventory by the providing inventoryId
     * Will first check if the providing inventoryId is valid
     * Then check if this inventory has been booked
     * Last, if it passed all the checking, call the upstream server of booking an inventory
     *
     * @param inventoryId the ID of  an inventory, which consists two main part, the first 5 char represent its belonging product, the last 5 char represent its inventory number
     * @return if success, return the booked product which contains a list of inventories; if failed, return a response of bad request with adding info
     */
    public Mono<String> bookInventoryByInventoryId(String inventoryId) {

        // check if the providing inventory is valid
        String regex = "[0-9A-Za-z]+";
        if (inventoryId.length() != 10 || (!inventoryId.matches(regex))) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid inventoryId"));
        }
        String productId = inventoryId.substring(0, 5);
        String subInventoryId = inventoryId.substring(5, 10);
        // check if the inventory is booked
        Inventory inventory = this.productWebClient
                .get()
                .uri("/product/" + productId + "/inventory/" + subInventoryId)
                .retrieve()
                .bodyToMono(Inventory.class)
                .block();
        if (inventory.isBooked()) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "This inventory has been booked."));
        }
        // send request to book that specified inventory
        return this.productWebClient
                .post()
                .uri("/product/" + productId + "/inventory/" + subInventoryId)
                .retrieve()
                .bodyToMono(String.class);
    }
}
