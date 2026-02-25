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

public class UserApiTest extends BaseApiTest {
    private UserClient userClient;
    private String email;
    private String password;
    private String name;
    private User user;

    @Before
    public void setUp() {
        userClient = new UserClient();
        email = RandomStringUtils.randomAlphabetic(10) + "@test.com";
        password = RandomStringUtils.randomAlphabetic(8);
        name = RandomStringUtils.randomAlphabetic(8);
        user = new User(email, password, name);
    }

    @Test
    @DisplayName("Успешное создание уникального пользователя")
    @Description("Проверка создания нового пользователя с уникальными данными")
    public void createUniqueUserSuccess() {
        ValidatableResponse response = userClient.createUser(user);

        response.statusCode(SC_OK)
                .body("success", is(true))
                .body("user.email", is(email))
                .body("user.name", is(name))
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue());
    }

    @Test
    @DisplayName("Ошибка при создании дублирующегося пользователя")
    @Description("Проверка, что нельзя создать пользователя с уже существующими данными")
    public void createDuplicateUserFail() {
        // Создаем первого пользователя
        userClient.createUser(user);

        // Пытаемся создать такого же
        ValidatableResponse response = userClient.createUser(user);

        response.statusCode(SC_FORBIDDEN)
                .body("success", is(false))
                .body("message", is("User already exists"));
    }

    @Test
    @DisplayName("Ошибка при создании пользователя без email")
    @Description("Проверка, что нельзя создать пользователя без указания email")
    public void createUserWithoutEmailFail() {
        User userWithoutEmail = new User(null, password, name);
        ValidatableResponse response = userClient.createUser(userWithoutEmail);

        response.statusCode(SC_FORBIDDEN)
                .body("success", is(false))
                .body("message", is("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Ошибка при создании пользователя без password")
    @Description("Проверка, что нельзя создать пользователя без указания пароля")
    public void createUserWithoutPasswordFail() {
        User userWithoutPassword = new User(email, null, name);
        ValidatableResponse response = userClient.createUser(userWithoutPassword);

        response.statusCode(SC_FORBIDDEN)
                .body("success", is(false))
                .body("message", is("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Ошибка при создании пользователя без name")
    @Description("Проверка, что нельзя создать пользователя без указания имени")
    public void createUserWithoutNameFail() {
        User userWithoutName = new User(email, password, null);
        ValidatableResponse response = userClient.createUser(userWithoutName);

        response.statusCode(SC_FORBIDDEN)
                .body("success", is(false))
                .body("message", is("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Успешный логин существующего пользователя")
    @Description("Проверка, что можно залогиниться с корректными данными существующего пользователя")
    public void loginExistingUserSuccess() {
        // Создаем пользователя
        userClient.createUser(user);

        // Логинимся
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
        // Создаем пользователя
        userClient.createUser(user);

        // Логинимся с неверным паролем
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
        // Создаем пользователя
        userClient.createUser(user);

        // Логинимся с неверным email
        UserCredentials wrongEmailCredentials = new UserCredentials("wrong@test.com", password);
        ValidatableResponse response = userClient.loginUser(wrongEmailCredentials);

        response.statusCode(SC_UNAUTHORIZED)
                .body("success", is(false))
                .body("message", is("email or password are incorrect"));
    }
}
