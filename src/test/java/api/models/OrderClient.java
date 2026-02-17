package api.models;

import api.BaseApiTest;
import api.models.Order;
import io.restassured.response.ValidatableResponse;

import static io.restassured.RestAssured.given;

public class OrderClient extends BaseApiTest {
    private static final String ORDERS_PATH = "/api/orders";

    public ValidatableResponse createOrder(Order order, String accessToken) {
        return given()
                .spec(getBaseSpec())
                .header("Authorization", accessToken)
                .body(order)
                .when()
                .post(ORDERS_PATH)
                .then();
    }

    public ValidatableResponse createOrder(Order order) {
        return given()
                .spec(getBaseSpec())
                .body(order)
                .when()
                .post(ORDERS_PATH)
                .then();
    }
}
