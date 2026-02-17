package api.models;

import api.BaseApiTest;
import api.models.User;
import api.models.UserCredentials;
import api.models.Order;
import io.restassured.response.ValidatableResponse;

import static io.restassured.RestAssured.given;

public class UserClient extends BaseApiTest {
    private static final String CREATE_USER_PATH = "/api/auth/register";
    private static final String LOGIN_USER_PATH = "/api/auth/login";

    public ValidatableResponse createUser(User user) {
        return given()
                .spec(getBaseSpec())
                .body(user)
                .when()
                .post(CREATE_USER_PATH)
                .then();
    }

    public ValidatableResponse loginUser(UserCredentials credentials) {
        return given()
                .spec(getBaseSpec())
                .body(credentials)
                .when()
                .post(LOGIN_USER_PATH)
                .then();
    }
}