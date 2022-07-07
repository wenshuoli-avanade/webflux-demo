package com.avanade.wsl.webfluxdemo;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * unit test for the product relevant api
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProductAPITests {

    // webTestClient, which is used for calling the product relevant api
    @Autowired
    private WebTestClient webTestClient;

    // wiremock server, which is used for providing mocking upStream service
    private static WireMockServer wireMockServer;

    /**
     * override the upstream url that webclient called in our application (replace it with the wiremock server url)
     *
     * @param dynamicPropertyRegistry injected by SpringBootTest
     */
    @DynamicPropertySource
    static void overrideWebClientBaseUrl(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("product_base_url", wireMockServer::baseUrl);
    }

    /**
     * start up the wiremock server before the unit test running
     */
    @BeforeAll
    static void startWireMock() {

        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig()
                .dynamicPort());

        wireMockServer.start();
    }

    /**
     * clean stored stubbings before running each test case
     */
    @BeforeEach
    void clearWireMock() {
        wireMockServer.resetAll();
    }

    /**
     * shutdown wiremock after the whole unit test procession finished
     */
    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    /**
     * make sure the wiremock could run normally
     */
    @Test
    void setUpWireMockTest() {
        System.out.println(wireMockServer.baseUrl());
        assertTrue(wireMockServer.isRunning());
    }

    /**
     * use empty array response of wiremock for test
     */
    @Test
    void basicWireMockExampleTest() {
        wireMockServer.stubFor(
                WireMock.get("/products")
                        .willReturn(aResponse()
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody("[]"))
        );

        this.webTestClient
                .get()
                .uri("/v1/product/")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.length()").isEqualTo(0);
    }

    /**
     * use invalid url of wiremock for test
     */
    @Test
    void wireMockRequestMatchingTest() {
        wireMockServer.stubFor(
                WireMock.get(WireMock.urlEqualTo("/users"))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody("[]"))
        );

        this.webTestClient
                .get()
                .uri("/v1/product/")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    /**
     * test for find all products function
     */
    @Test
    void findAllProductTest() {
        wireMockServer.stubFor(
                WireMock.get(WireMock.urlEqualTo("/products"))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBodyFile("product-api/all_products_response_200.json")
                                .withFixedDelay(1_000))
        );

        //comparing the response with the canned response
        this.webTestClient
                .get()
                .uri("/v1/product/")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(3) //comparing the response with the canned response
                .jsonPath("$[0].productId").isEqualTo("p0001")
                .jsonPath("$[0].content").isEqualTo("Circus show")
                .jsonPath("$[0].experienceDetail").isEqualTo("2 hours great show proformed by the world famous circus")
                .jsonPath("$[0].inventories.length()").isEqualTo(3)
                .jsonPath("$[0].inventories[0].inventoryId").isEqualTo("p0001i0001")
                .jsonPath("$[0].inventories[0].booked").isEqualTo(true)
                .jsonPath("$[0].inventories[1].inventoryId").isEqualTo("p0001i0002")
                .jsonPath("$[0].inventories[1].booked").isEqualTo(false)
                .jsonPath("$[0].inventories[2].inventoryId").isEqualTo("p0001i0003")
                .jsonPath("$[0].inventories[2].booked").isEqualTo(false);
    }

    /**
     * single product query test
     */
    @Test
    void findSingleProductTest() {
        wireMockServer.stubFor(
                WireMock.get(WireMock.urlEqualTo("/product/p0003"))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBodyFile("product-api/single_product_response_200.json")
                                .withFixedDelay(1_000))
        );

        // query with invalid productId, which does not exist, return 5xx error
        this.webTestClient
                .get()
                .uri("/v1/product/p0001111dsfdf")
                .exchange()
                .expectStatus().is5xxServerError();

        //comparing the response with the canned response
        this.webTestClient
                .get()
                .uri("/v1/product/p0003")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.productId").isEqualTo("p0003")
                .jsonPath("$.content").isEqualTo("Lunch meal")
                .jsonPath("$.experienceDetail").isEqualTo("Enjoy great lunch meal servered in the castle")
                .jsonPath("$.inventories.length()").isEqualTo(3)
                .jsonPath("$.inventories[0].inventoryId").isEqualTo("p0003i0001")
                .jsonPath("$.inventories[0].booked").isEqualTo(false)
                .jsonPath("$.inventories[1].inventoryId").isEqualTo("p0003i0002")
                .jsonPath("$.inventories[1].booked").isEqualTo(true)
                .jsonPath("$.inventories[2].inventoryId").isEqualTo("p0003i0003")
                .jsonPath("$.inventories[2].booked").isEqualTo(true);
    }

    /**
     * function test: order by inventoryId
     */
    @Test
    void orderProductByInventoryIdTest() {
        // canned response for posting book request
        wireMockServer.stubFor(
                WireMock.post(WireMock.urlEqualTo("/product/p0003/inventory/i0001"))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBodyFile("product-api/order_product_by_inventoryId_response_200.json")
                                .withFixedDelay(1_000))
        );
        // canned response for querying that make sure the target inventory has not been booked
        wireMockServer.stubFor(
                WireMock.get(WireMock.urlEqualTo("/product/p0003/inventory/i0001"))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBodyFile("product-api/single_product_response_200.json")
                                .withFixedDelay(1_000))
        );


        //comparing the response with the canned response
        this.webTestClient
                .post()
                .uri("/v1/product/p0003i0001")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.productId").isEqualTo("p0003")
                .jsonPath("$.content").isEqualTo("Lunch meal")
                .jsonPath("$.experienceDetail").isEqualTo("Enjoy great lunch meal servered in the castle")
                .jsonPath("$.inventories.length()").isEqualTo(3)
                .jsonPath("$.inventories[0].inventoryId").isEqualTo("p0003i0001")
                .jsonPath("$.inventories[0].booked").isEqualTo(true)
                .jsonPath("$.inventories[1].inventoryId").isEqualTo("p0003i0002")
                .jsonPath("$.inventories[1].booked").isEqualTo(true)
                .jsonPath("$.inventories[2].inventoryId").isEqualTo("p0003i0003")
                .jsonPath("$.inventories[2].booked").isEqualTo(true);
    }
}
