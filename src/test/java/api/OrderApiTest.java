package api;

import api.models.*;
import io.restassured.response.ValidatableResponse;
import org.junit.Before;
import org.junit.Test;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;

public class OrderApiTest extends BaseApiTest {
    private UserClient userClient;
    private OrderClient orderClient;
    private String accessToken;
    private User user;
    private String email;
    private String password;
    private String name;

    // Валидные хеши ингредиентов из документации
    private static final String VALID_INGREDIENT_HASH_1 = "60d3b41abdacab0026a733c6";
    private static final String VALID_INGREDIENT_HASH_2 = "609646e4dc916e00276b2870";
    private static final String INVALID_INGREDIENT_HASH = "invalid_hash_12345";

    @Before
    public void setUp() {
        userClient = new UserClient();
        orderClient = new OrderClient();

        // Генерируем данные пользователя
        email = RandomStringUtils.randomAlphabetic(10) + "@test.com";
        password = RandomStringUtils.randomAlphabetic(8);
        name = RandomStringUtils.randomAlphabetic(8);
        user = new User(email, password, name);

        // Создаем пользователя и получаем токен
        ValidatableResponse createResponse = userClient.createUser(user);
        accessToken = createResponse.extract().path("accessToken");

        // Если токен не пришел при создании, логинимся
        if (accessToken == null) {
            UserCredentials credentials = UserCredentials.fromUser(user);
            ValidatableResponse loginResponse = userClient.loginUser(credentials);
            accessToken = loginResponse.extract().path("accessToken");
        }
    }
    @Test
    public void createOrderWithAuthAndIngredientsSuccess() {
        // Успешное создание заказа с авторизацией и двумя валидными ингредиентами
        // Ожидаем: код 200, success: true, имя заказа и номер заказа не должны быть null
        List<String> ingredients = Arrays.asList(VALID_INGREDIENT_HASH_1, VALID_INGREDIENT_HASH_2);
        Order order = new Order(ingredients);

        ValidatableResponse response = orderClient.createOrder(order, accessToken);

        response.statusCode(200)
                .body("success", is(true))
                .body("name", notNullValue())
                .body("order.number", notNullValue());
    }

    @Test
    public void createOrderWithoutAuthSuccess() {
        // Успешное создание заказа БЕЗ авторизации (неавторизованный пользователь)
        // Ожидаем: код 200, success: true, имя заказа и номер заказа не должны быть null
        List<String> ingredients = Arrays.asList(VALID_INGREDIENT_HASH_1, VALID_INGREDIENT_HASH_2);
        Order order = new Order(ingredients);

        ValidatableResponse response = orderClient.createOrder(order);

        response.statusCode(200)
                .body("success", is(true))
                .body("name", notNullValue())
                .body("order.number", notNullValue());
    }

    @Test
    public void createOrderWithoutIngredientsFail() {
        // Создание заказа без ингредиентов (пустой список)
        // Ожидаем: код 400, success: false, сообщение об ошибке "Ingredient ids must be provided"
        Order emptyOrder = new Order(Collections.emptyList());

        ValidatableResponse response = orderClient.createOrder(emptyOrder, accessToken);

        response.statusCode(400)
                .body("success", is(false))
                .body("message", is("Ingredient ids must be provided"));
    }

    @Test
    public void createOrderWithInvalidIngredientHashFail() {
        // Создание заказа с невалидным хешем ингредиента
        // Ожидаем: код 500 Internal Server Error (согласно документации)
        List<String> ingredients = Arrays.asList(VALID_INGREDIENT_HASH_1, INVALID_INGREDIENT_HASH);
        Order order = new Order(ingredients);

        ValidatableResponse response = orderClient.createOrder(order, accessToken);

        response.statusCode(500);
    }

    @Test
    public void createOrderWithAuthAndSingleIngredientSuccess() {
        // Успешное создание заказа с авторизацией и ОДНИМ валидным ингредиентом
        // Проверяем, что заказ можно создать даже с одним ингредиентом
        // Ожидаем: код 200, success: true, имя заказа и номер заказа не должны быть null
        List<String> ingredients = Collections.singletonList(VALID_INGREDIENT_HASH_1);
        Order order = new Order(ingredients);

        ValidatableResponse response = orderClient.createOrder(order, accessToken);

        response.statusCode(200)
                .body("success", is(true))
                .body("name", notNullValue())
                .body("order.number", notNullValue());
    }
}
