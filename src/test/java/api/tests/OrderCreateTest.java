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
import java.util.Collections;
import java.util.List;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.CoreMatchers.*;

@DisplayName("Тесты создания заказа (POST /api/orders)")
public class OrderCreateTest extends BaseApiTest {
    private UserClient userClient;
    private OrderClient orderClient;
    private String accessToken;
    private User user;
    private String email;
    private String password;
    private String name;

    private static final String BUN_INGREDIENT_HASH = "60d3b41abdacab0026a733c6";
    private static final String SAUCE_INGREDIENT_HASH = "609646e4dc916e00276b2870";
    private static final String INVALID_INGREDIENT_HASH = "invalid_hash_12345";

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
    @DisplayName("Успешное создание заказа с авторизацией")
    @Description("Проверка создания заказа с двумя ингредиентами для авторизованного пользователя")
    public void createOrderWithAuthAndIngredientsSuccess() {
        List<String> ingredients = Arrays.asList(BUN_INGREDIENT_HASH, SAUCE_INGREDIENT_HASH);
        Order order = new Order(ingredients);

        ValidatableResponse response = orderClient.createOrder(order, accessToken);

        response.statusCode(SC_OK)
                .body("success", is(true))
                .body("name", notNullValue())
                .body("order.number", notNullValue());
    }

    @Test
    @DisplayName("Успешное создание заказа без авторизации")
    @Description("Проверка создания заказа без авторизации (неавторизованный пользователь)")
    public void createOrderWithoutAuthSuccess() {
        List<String> ingredients = Arrays.asList(BUN_INGREDIENT_HASH, SAUCE_INGREDIENT_HASH);
        Order order = new Order(ingredients);

        ValidatableResponse response = orderClient.createOrder(order);

        response.statusCode(SC_OK)
                .body("success", is(true))
                .body("name", notNullValue())
                .body("order.number", notNullValue());
    }

    @Test
    @DisplayName("Ошибка при создании заказа без ингредиентов")
    @Description("Проверка, что нельзя создать заказ с пустым списком ингредиентов")
    public void createOrderWithoutIngredientsFail() {
        Order emptyOrder = new Order(Collections.emptyList());

        ValidatableResponse response = orderClient.createOrder(emptyOrder, accessToken);

        response.statusCode(SC_BAD_REQUEST)
                .body("success", is(false))
                .body("message", is("Ingredient ids must be provided"));
    }

    @Test
    @DisplayName("Ошибка при создании заказа с невалидным хешем")
    @Description("Проверка, что сервер возвращает 500 при передаче невалидного хеша ингредиента")
    public void createOrderWithInvalidIngredientHashFail() {
        List<String> ingredients = Arrays.asList(BUN_INGREDIENT_HASH, INVALID_INGREDIENT_HASH);
        Order order = new Order(ingredients);

        ValidatableResponse response = orderClient.createOrder(order, accessToken);

        response.statusCode(SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("Успешное создание заказа с одним ингредиентом")
    @Description("Проверка создания заказа с одним валидным ингредиентом")
    public void createOrderWithAuthAndSingleIngredientSuccess() {
        List<String> ingredients = Collections.singletonList(BUN_INGREDIENT_HASH);
        Order order = new Order(ingredients);

        ValidatableResponse response = orderClient.createOrder(order, accessToken);

        response.statusCode(SC_OK)
                .body("success", is(true))
                .body("name", notNullValue())
                .body("order.number", notNullValue());
    }
}
