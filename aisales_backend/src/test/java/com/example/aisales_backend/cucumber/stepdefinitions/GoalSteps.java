package com.example.aisales_backend.cucumber.stepdefinitions;

import com.example.aisales_backend.dto.GoalRequest;
import com.example.aisales_backend.entity.User;
import com.example.aisales_backend.entity.UserRole;
import com.example.aisales_backend.repository.UserRepository;
import com.example.aisales_backend.security.JwtTokenProvider;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

public class GoalSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Response response;
    private Long testUserId;
    private Long testGoalId;
    private String jwtToken;
    private GoalRequest goalRequest;

    @Given("I am authenticated as a valid user for goal tests")
    public void iAmAuthenticatedAsAValidUserForGoalTests() {
        RestAssured.port = port;
        RestAssured.basePath = "";

        String testEmail = "goal.tester@vocalyx.com";
        String testPassword = "TestPass@123";

        User user = userRepository.findByEmail(testEmail).orElse(null);

        if (user == null) {
            user = User.builder()
                    .firstName("Goal")
                    .lastName("Tester")
                    .email(testEmail)
                    .password(passwordEncoder.encode(testPassword))
                    .role(UserRole.ADMIN)
                    .build();
            user = userRepository.save(user);
        }

        testUserId = user.getId();
        assertNotNull(testUserId, "Test user ID should not be null");

        jwtToken = jwtTokenProvider.generateTokenForUser(user);
        assertNotNull(jwtToken, "JWT token should not be null");
    }

    @Given("the system is ready for goal management")
    public void theSystemIsReadyForGoalManagement() {
        RestAssured.port = port;
        RestAssured.basePath = "";
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getResponseAsMap() {
        return response.jsonPath().getMap("$");
    }

    @When("I create a goal with the following details:")
    public void iCreateAGoalWithTheFollowingDetails(io.cucumber.datatable.DataTable dataTable) {
        Map<String, String> data = dataTable.asMaps().get(0);

        goalRequest = new GoalRequest(
                data.get("name"),
                data.get("description") != null ? data.get("description") : "Test goal description",
                Double.parseDouble(data.get("targetRevenue")),
                0.0,
                LocalDate.parse(data.get("startDate")),
                LocalDate.parse(data.get("endDate")),
                data.get("company"),
                data.get("priority")
        );

        response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + jwtToken)
                .body(goalRequest)
                .when()
                .post("/api/goals");

        System.out.println("Create goal status: " + response.statusCode());

        if (response.statusCode() == 201) {
            Map<String, Object> responseMap = getResponseAsMap();
            testGoalId = ((Number) responseMap.get("id")).longValue();
            System.out.println("Created goal with ID: " + testGoalId);
        } else {
            System.out.println("Create goal body: " + response.body().asString());
        }
    }

    @Then("the goal should be created successfully")
    public void theGoalShouldBeCreatedSuccessfully() {
        assertEquals(201, response.statusCode(),
                "Expected 201 but got: " + response.statusCode() +
                        " body: " + response.body().asString());
    }

    @And("the goal should have an ID")
    public void theGoalShouldHaveAnID() {
        assertNotNull(testGoalId);
        assertTrue(testGoalId > 0);
    }

    @And("the goal status should be {string}")
    public void theGoalStatusShouldBe(String expectedStatus) {
        Map<String, Object> responseMap = getResponseAsMap();
        assertEquals(expectedStatus, responseMap.get("status"));
    }

    @Given("a goal exists with name {string}")
    public void aGoalExistsWithName(String goalName) {
        // Check if goal already exists
        Response listResponse = given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get("/api/goals");

        if (listResponse.statusCode() == 200) {
            String responseBody = listResponse.body().asString();
            // Parse JSON manually to avoid GoalResponse deserialization issue
            List<Map<String, Object>> goals = listResponse.jsonPath().getList("$");
            if (goals != null) {
                for (Map<String, Object> goal : goals) {
                    if (goalName.equals(goal.get("name"))) {
                        testGoalId = ((Number) goal.get("id")).longValue();
                        System.out.println("Found existing goal with ID: " + testGoalId);
                        return;
                    }
                }
            }
        }

        // Create new goal
        goalRequest = new GoalRequest(
                goalName,
                "Test goal description",
                100000.0,
                0.0,
                LocalDate.now(),
                LocalDate.now().plusMonths(3),
                "Test Corp",
                "Medium"
        );

        response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + jwtToken)
                .body(goalRequest)
                .when()
                .post("/api/goals");

        System.out.println("Create goal for test status: " + response.statusCode());

        if (response.statusCode() == 201) {
            Map<String, Object> responseMap = getResponseAsMap();
            testGoalId = ((Number) responseMap.get("id")).longValue();
            System.out.println("Created goal with ID: " + testGoalId);
        }

        assertNotNull(testGoalId, "Test goal ID should not be null for name: " + goalName);
    }

    @When("I retrieve the goal by ID")
    public void iRetrieveTheGoalByID() {
        response = given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get("/api/goals/" + testGoalId);

        System.out.println("Get goal status: " + response.statusCode());
    }

    @Then("I should see the goal details")
    public void iShouldSeeTheGoalDetails() {
        assertEquals(200, response.statusCode());
    }

    @And("the goal name should be {string}")
    public void theGoalNameShouldBe(String expectedName) {
        Map<String, Object> responseMap = getResponseAsMap();
        assertEquals(expectedName, responseMap.get("name"));
    }

    @Given("the following goals exist for the current user:")
    public void theFollowingGoalsExistForTheCurrentUser(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> goals = dataTable.asMaps();

        for (Map<String, String> goalData : goals) {
            goalRequest = new GoalRequest(
                    goalData.get("name"),
                    "Test goal description",
                    Double.parseDouble(goalData.get("targetRevenue")),
                    0.0,
                    LocalDate.now(),
                    LocalDate.now().plusMonths(3),
                    "Test Corp",
                    "Medium"
            );

            given()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .header("Authorization", "Bearer " + jwtToken)
                    .body(goalRequest)
                    .when()
                    .post("/api/goals");
        }
    }

    @When("I request all my goals")
    public void iRequestAllMyGoals() {
        response = given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get("/api/goals");

        System.out.println("Get all goals status: " + response.statusCode());
    }

    @Then("I should receive at least {int} goals")
    public void iShouldReceiveAtLeastGoals(int minGoals) {
        assertEquals(200, response.statusCode());
        List<Map<String, Object>> goals = response.jsonPath().getList("$");
        assertNotNull(goals);
        assertTrue(goals.size() >= minGoals,
                "Expected at least " + minGoals + " goals but got: " + goals.size());
    }

    @When("I update the goal progress to {int}")
    public void iUpdateTheGoalProgressTo(int newProgress) {
        // First get the existing goal
        response = given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get("/api/goals/" + testGoalId);

        if (response.statusCode() == 200) {
            Map<String, Object> existing = getResponseAsMap();

            // Create update request with all fields
            goalRequest = new GoalRequest(
                    (String) existing.get("name"),
                    (String) existing.get("description") != null ? (String) existing.get("description") : "Test description",
                    ((Number) existing.get("targetRevenue")).doubleValue(),
                    (double) newProgress,
                    LocalDate.parse((String) existing.get("startDate")),
                    LocalDate.parse((String) existing.get("endDate")),
                    (String) existing.get("company"),
                    (String) existing.get("priority")
            );

            response = given()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .header("Authorization", "Bearer " + jwtToken)
                    .body(goalRequest)
                    .when()
                    .put("/api/goals/" + testGoalId);

            System.out.println("Update goal status: " + response.statusCode());
        }
    }

    @Then("the goal should be updated successfully")
    public void theGoalShouldBeUpdatedSuccessfully() {
        assertEquals(200, response.statusCode());
    }

    @And("the goal progress should be updated")
    public void theGoalProgressShouldBeUpdated() {
        Map<String, Object> responseMap = getResponseAsMap();
        assertNotNull(responseMap.get("currentProgress"));
    }

    @Then("the goal status should automatically change to {string}")
    public void theGoalStatusShouldAutomaticallyChangeTo(String expectedStatus) {
        assertEquals(200, response.statusCode());
        Map<String, Object> responseMap = getResponseAsMap();
        assertEquals(expectedStatus, responseMap.get("status"));
    }

    @When("I delete the goal")
    public void iDeleteTheGoal() {
        response = given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .delete("/api/goals/" + testGoalId);

        System.out.println("Delete goal status: " + response.statusCode());
    }

    @Then("the goal should be deleted successfully")
    public void theGoalShouldBeDeletedSuccessfully() {
        assertTrue(response.statusCode() == 200 || response.statusCode() == 204,
                "Expected 200 or 204 but got: " + response.statusCode());
    }

    @When("I attempt to retrieve goal with ID {int}")
    public void iAttemptToRetrieveGoalWithID(int goalId) {
        response = given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get("/api/goals/" + goalId);

        System.out.println("Retrieve non-existent goal status: " + response.statusCode());
    }

    @Then("the goal request should fail")
    public void theGoalRequestShouldFail() {
        assertTrue(response.statusCode() >= 400,
                "Expected error status but got: " + response.statusCode());
    }

    @And("I should see goal error {string}")
    public void iShouldSeeGoalError(String expectedError) {
        String responseBody = response.body().asString();
        assertTrue(
                responseBody.contains(expectedError) ||
                        responseBody.contains("Goal not found"),
                "Expected error to contain: '" + expectedError + "' but got: " + responseBody
        );
    }

    @When("I attempt to create a goal with:")
    public void iAttemptToCreateAGoalWith(io.cucumber.datatable.DataTable dataTable) {
        Map<String, String> data = dataTable.asMaps().get(0);

        String name = data.get("name");
        if (name != null && name.equals("[empty]")) {
            name = "";
        }

        double targetRevenue = Double.parseDouble(data.get("targetRevenue"));

        goalRequest = new GoalRequest(
                name,
                "Test description",
                targetRevenue,
                0.0,
                LocalDate.parse(data.get("startDate")),
                LocalDate.parse(data.get("endDate")),
                data.get("company") != null ? data.get("company") : "Test Corp",
                data.get("priority") != null ? data.get("priority") : "Medium"
        );

        response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + jwtToken)
                .body(goalRequest)
                .when()
                .post("/api/goals");

        System.out.println("Invalid goal creation status: " + response.statusCode());
        System.out.println("Invalid goal creation body: " + response.body().asString());
    }

    @Then("the goal creation should fail")
    public void theGoalCreationShouldFail() {
        assertTrue(response.statusCode() >= 400,
                "Expected error status but got: " + response.statusCode());
    }

    @And("I should see goal validation errors")
    public void iShouldSeeGoalValidationErrors() {
        assertEquals(400, response.statusCode());
        assertNotNull(response.body().asString());
    }
}