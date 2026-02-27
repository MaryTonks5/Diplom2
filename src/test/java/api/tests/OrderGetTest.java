package api.tests;

import api.BaseApiTest;
import api.clients.OrderClient;
import api.clients.UserClient;
import api.models.Order;
import api.models.User;
import api.models.UserCredentials;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.CoreMatchers.*;

@DisplayName("Тесты получения заказов (GET /api/orders)")
public class OrderGetTest extends BaseApiTest {
    private UserClient userClient;
    private OrderClient orderClient;
    private String accessToken;
    private User user;
    private String email;
    private String password;
    private String name;

    private static final String BUN_INGREDIENT_HASH = "60d3b41abdacab0026a733c6";
    private static final String SAUCE_INGREDIENT_HASH = "609646e4dc916e00276b2870";

    @Before
    public void setUp() {
        userClient = new UserClient();
        orderClient = new OrderClient();

        email = RandomStringUtils.randomAlphabetic(10) + "@test.com";
        password = RandomStringUtils.randomAlphabetic(8);
        name = RandomStringUtils.randomAlphabetic(8);
        user = new User(email, password, name);

        ValidatableResponse createResponse = userClient.createUser(user);
        accessToken = createResponse.extract().path("accessToken");

        if (accessToken == null) {
            UserCredentials credentials = UserCredentials.fromUser(user);
            ValidatableResponse loginResponse = userClient.loginUser(credentials);
            accessToken = loginResponse.extract().path("accessToken");
        }
    }

    @Test
    @DisplayName("Получение заказов авторизованного пользователя")
    @Description("Проверка, что авторизованный пользователь может получить список своих заказов")
    public void getUserOrdersWithAuthSuccess() {
        // Создаем заказ для пользователя
        List<String> ingredients = Arrays.asList(BUN_INGREDIENT_HASH, SAUCE_INGREDIENT_HASH);
        Order order = new Order(ingredients);
        orderClient.createOrder(order, accessToken);

        // Получаем заказы авторизованного пользователя
        ValidatableResponse response = orderClient.getUserOrders(accessToken);

        response.statusCode(SC_OK)
                .body("success", is(true))
                .body("orders", notNullValue())
                .body("total", notNullValue())
                .body("totalToday", notNullValue());
    }

    @Test
    @DisplayName("Получение заказов без авторизации")
    @Description("Проверка, что неавторизованный пользователь не может получить заказы")
    public void getUserOrdersWithoutAuthFail() {
        ValidatableResponse response = orderClient.getUserOrders();

        response.statusCode(SC_UNAUTHORIZED)
                .body("success", is(false))
                .body("message", is("You should be authorised"));
    }
}