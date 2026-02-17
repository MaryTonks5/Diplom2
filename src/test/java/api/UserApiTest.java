package api;

import api.models.User;
import api.models.UserClient;
import api.models.UserCredentials;
import io.restassured.response.ValidatableResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.notNullValue;

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
    public void createUniqueUserSuccess() {
        ValidatableResponse response = userClient.createUser(user);

        response.statusCode(200)
                .body("success", is(true))
                .body("user.email", is(email))
                .body("user.name", is(name))
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue());
    }

    @Test
    public void createDuplicateUserFail() {
        // Создаем первого пользователя
        userClient.createUser(user);

        // Пытаемся создать такого же
        ValidatableResponse response = userClient.createUser(user);

        response.statusCode(403)
                .body("success", is(false))
                .body("message", is("User already exists"));
    }

    @Test
    public void createUserWithoutRequiredFieldFail() {
        // Создаем пользователя без email
        User userWithoutEmail = new User(null, password, name);
        ValidatableResponse responseWithoutEmail = userClient.createUser(userWithoutEmail);
        responseWithoutEmail.statusCode(403)
                .body("success", is(false))
                .body("message", is("Email, password and name are required fields"));

        // Создаем пользователя без password
        User userWithoutPassword = new User(email, null, name);
        ValidatableResponse responseWithoutPassword = userClient.createUser(userWithoutPassword);
        responseWithoutPassword.statusCode(403)
                .body("success", is(false))
                .body("message", is("Email, password and name are required fields"));

        // Создаем пользователя без name
        User userWithoutName = new User(email, password, null);
        ValidatableResponse responseWithoutName = userClient.createUser(userWithoutName);
        responseWithoutName.statusCode(403)
                .body("success", is(false))
                .body("message", is("Email, password and name are required fields"));
    }

    @Test
    public void loginExistingUserSuccess() {
        // Создаем пользователя
        userClient.createUser(user);

        // Логинимся
        UserCredentials credentials = UserCredentials.fromUser(user);
        ValidatableResponse response = userClient.loginUser(credentials);

        response.statusCode(200)
                .body("success", is(true))
                .body("user.email", is(email))
                .body("user.name", is(name))
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue());
    }

    @Test
    public void loginWithInvalidCredentialsFail() {
        // Создаем пользователя
        userClient.createUser(user);

        // Логинимся с неверным паролем
        UserCredentials wrongPasswordCredentials = new UserCredentials(email, "wrongpassword");
        ValidatableResponse wrongPasswordResponse = userClient.loginUser(wrongPasswordCredentials);
        wrongPasswordResponse.statusCode(401)
                .body("success", is(false))
                .body("message", is("email or password are incorrect"));

        // Логинимся с неверным email
        UserCredentials wrongEmailCredentials = new UserCredentials("wrong@test.com", password);
        ValidatableResponse wrongEmailResponse = userClient.loginUser(wrongEmailCredentials);
        wrongEmailResponse.statusCode(401)
                .body("success", is(false))
                .body("message", is("email or password are incorrect"));
    }
}