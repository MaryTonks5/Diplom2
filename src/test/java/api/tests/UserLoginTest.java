package api.tests;

import api.BaseApiTest;
import api.clients.UserClient;
import api.models.User;
import api.models.UserCredentials;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.CoreMatchers.*;

@DisplayName("Тесты логина пользователей")
public class UserLoginTest extends BaseApiTest {
    private UserClient userClient;
    private String email;
    private String password;
    private String name;
    private User user;
    private String accessToken;

    @Before
    public void setUp() {
        userClient = new UserClient();
        email = RandomStringUtils.randomAlphabetic(10) + "@test.com";
        password = RandomStringUtils.randomAlphabetic(8);
        name = RandomStringUtils.randomAlphabetic(8);
        user = new User(email, password, name);

        // Создаем пользователя для тестов логина
        ValidatableResponse createResponse = userClient.createUser(user);
        accessToken = createResponse.extract().path("accessToken");
    }

    @Test
    @DisplayName("Успешный логин существующего пользователя")
    @Description("Проверка, что можно залогиниться с корректными данными существующего пользователя")
    public void loginExistingUserSuccess() {
        UserCredentials credentials = UserCredentials.fromUser(user);
        ValidatableResponse response = userClient.loginUser(credentials);

        response.statusCode(SC_OK)
                .body("success", is(true))
                .body("user.email", is(email))
                .body("user.name", is(name))
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue());
    }

    @Test
    @DisplayName("Ошибка при логине с неверным паролем")
    @Description("Проверка, что нельзя залогиниться с правильным email, но неверным паролем")
    public void loginWithWrongPasswordFail() {
        UserCredentials wrongPasswordCredentials = new UserCredentials(email, "wrongpassword");
        ValidatableResponse response = userClient.loginUser(wrongPasswordCredentials);

        response.statusCode(SC_UNAUTHORIZED)
                .body("success", is(false))
                .body("message", is("email or password are incorrect"));
    }

    @Test
    @DisplayName("Ошибка при логине с неверным email")
    @Description("Проверка, что нельзя залогиниться с неверным email, но правильным паролем")
    public void loginWithWrongEmailFail() {
        UserCredentials wrongEmailCredentials = new UserCredentials("wrong@test.com", password);
        ValidatableResponse response = userClient.loginUser(wrongEmailCredentials);

        response.statusCode(SC_UNAUTHORIZED)
                .body("success", is(false))
                .body("message", is("email or password are incorrect"));
    }
}
