package com.example.aisales_backend.cucumber.stepdefinitions;

import com.example.aisales_backend.dto.*;
import com.example.aisales_backend.entity.*;
import com.example.aisales_backend.repository.CallRepository;
import com.example.aisales_backend.repository.ContactRepository;
import com.example.aisales_backend.repository.OrderRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

public class CallSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private CallRepository callRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Response response;
    private CallResponse callResponse;
    private Long testContactId;
    private Long testUserId;
    private Long testCallId;
    private CallRequest callRequest;
    private String jwtToken;

    @Given("I am authenticated as a valid user")
    public void iAmAuthenticatedAsAValidUser() {
        RestAssured.port = port;

        String testEmail = "call.tester@vocalyx.com";
        String testPassword = "TestPass@123";

        // Create user directly in DB
        User user = userRepository.findByEmail(testEmail).orElse(null);

        if (user == null) {
            user = User.builder()
                    .firstName("Call")
                    .lastName("Tester")
                    .email(testEmail)
                    .password(passwordEncoder.encode(testPassword))
                    .role(UserRole.ADMIN)
                    .build();
            user = userRepository.save(user);
            System.out.println("Created user directly in DB with ID: " + user.getId());
        } else {
            System.out.println("Found existing user with ID: " + user.getId());
        }

        testUserId = user.getId();
        assertNotNull(testUserId, "Test user ID should not be null");

        // Generate JWT token directly instead of calling login API
        jwtToken = jwtTokenProvider.generateTokenForUser(user);
        assertNotNull(jwtToken, "JWT token should not be null");
        assertFalse(jwtToken.isEmpty(), "JWT token should not be empty");
        System.out.println("Generated JWT token directly");
    }

    @Given("a contact exists for call testing")
    public void aContactExistsForCallTesting() {
        String testEmail = "call.target@company.com";

        // Check if contact already exists
        Contact existingContact = contactRepository.findByEmailIgnoreCase(testEmail).orElse(null);

        if (existingContact != null) {
            testContactId = existingContact.getId();
            System.out.println("Found existing contact with ID: " + testContactId);
            return;
        }

        // Create contact directly in DB
        Contact contact = Contact.builder()
                .salutation(Contact.Salutation.MR)
                .firstName("Call")
                .lastName("Target")
                .email(testEmail)
                .phoneNumber("+1234567890")
                .department(Contact.Department.SALES)
                .status(Contact.ContactStatus.ACTIVE)
                .companyName("Test Corp")
                .build();

        contact = contactRepository.save(contact);
        testContactId = contact.getId();
        System.out.println("Created contact directly in DB with ID: " + testContactId);

        assertNotNull(testContactId, "Test contact ID should not be null");
    }

    @Given("the system is ready for call management")
    public void theSystemIsReadyForCallManagement() {
        RestAssured.port = port;
        RestAssured.basePath = "";  // EXPLICITLY clear basePath
    }

    @When("I record a call with the following details:")
    public void iRecordACallWithTheFollowingDetails(io.cucumber.datatable.DataTable dataTable) {
        Map<String, String> data = dataTable.asMaps().get(0);

        callRequest = CallRequest.builder()
                .callTitle(data.get("callTitle"))
                .callDirection(Call.CallDirection.valueOf(data.get("callDirection")))
                .recordingFilePath(data.get("recordingFilePath"))
                .callDateTime(LocalDateTime.now())
                .contactId(testContactId)
                .userId(testUserId)
                .build();

        response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + jwtToken)
                .body(callRequest)
                .when()
                .post("/api/calls");

        System.out.println("Call creation status: " + response.statusCode());

        if (response.statusCode() == 200 || response.statusCode() == 201) {
            callResponse = response.as(CallResponse.class);
            testCallId = callResponse.getId();
        } else {
            System.out.println("Call creation body: " + response.body().asString());
        }
    }

    @Then("the call should be recorded successfully")
    public void theCallShouldBeRecordedSuccessfully() {
        assertTrue(response.statusCode() == 200 || response.statusCode() == 201,
                "Expected 200 or 201 but got: " + response.statusCode() +
                        " body: " + response.body().asString());
        assertNotNull(callResponse);
    }

    @And("the call should have an ID")
    public void theCallShouldHaveAnID() {
        assertNotNull(callResponse.getId());
        assertTrue(callResponse.getId() > 0);
    }

    @Given("a call exists in the system")
    public void aCallExistsInTheSystem() {
        if (testCallId != null && callRepository.findById(testCallId).isPresent()) {
            return;
        }

        callRequest = CallRequest.builder()
                .callTitle("Test Call")
                .callDirection(Call.CallDirection.OUTGOING)
                .recordingFilePath("/uploads/test.mp3")
                .callDateTime(LocalDateTime.now())
                .contactId(testContactId)
                .userId(testUserId)
                .build();

        response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + jwtToken)
                .body(callRequest)
                .when()
                .post("/api/calls");

        System.out.println("Call exists creation status: " + response.statusCode());

        if (response.statusCode() == 200 || response.statusCode() == 201) {
            callResponse = response.as(CallResponse.class);
            testCallId = callResponse.getId();
            System.out.println("Created call with ID: " + testCallId);
        }

        assertNotNull(testCallId, "Test call ID should not be null");
    }

    @When("the N8N webhook sends sentiment analysis data:")
    public void theN8NWebhookSendsSentimentAnalysisData(io.cucumber.datatable.DataTable dataTable) {
        Map<String, String> data = dataTable.asMaps().get(0);

        Map<String, Object> output = Map.of(
                "sentimentLabel", data.get("sentimentLabel"),
                "sentimentPercentage", Integer.parseInt(data.get("sentimentPercentage")),
                "summary", data.get("summary"),
                "transcript", data.get("transcript")
        );

        List<Map<String, Object>> webhookPayload = List.of(
                Map.of("output", output)
        );

        response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(webhookPayload)
                .when()
                .post("/api/calls/public-webhook/" + testCallId);

        System.out.println("Webhook status: " + response.statusCode());
    }

    @Then("the call should be updated with sentiment data")
    public void theCallShouldBeUpdatedWithSentimentData() {
        assertEquals(200, response.statusCode());
    }

    @And("the sentiment label should be {string}")
    public void theSentimentLabelShouldBe(String expectedLabel) {
        Call updatedCall = callRepository.findByIdWithRelationships(testCallId);
        assertNotNull(updatedCall, "Call should exist");
        assertNotNull(updatedCall.getSentimentLabel(), "Sentiment label should not be null");
        assertEquals(expectedLabel, updatedCall.getSentimentLabel());
    }

    @And("the sentiment percentage should be {int}")
    public void theSentimentPercentageShouldBe(int expectedPercentage) {
        Call updatedCall = callRepository.findByIdWithRelationships(testCallId);
        assertNotNull(updatedCall, "Call should exist");
        assertNotNull(updatedCall.getSentimentPercentage(), "Sentiment percentage should not be null");
        assertEquals(expectedPercentage, updatedCall.getSentimentPercentage());
    }

    @When("I retrieve the call details")
    public void iRetrieveTheCallDetails() {
        response = given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get("/api/calls/" + testCallId);

        System.out.println("Get call status: " + response.statusCode());

        if (response.statusCode() == 200) {
            callResponse = response.as(CallResponse.class);
        }
    }

    @Then("I should see the call information")
    public void iShouldSeeTheCallInformation() {
        assertEquals(200, response.statusCode());
        assertNotNull(callResponse);
    }

    @And("the call should include the correct title")
    public void theCallShouldIncludeTheCorrectTitle() {
        assertNotNull(callResponse.getCallTitle());
        assertFalse(callResponse.getCallTitle().isEmpty());
    }

    @When("I retrieve call with ID {int}")
    public void iRetrieveCallWithID(int callId) {
        response = given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get("/api/calls/" + callId);
    }

    @Then("the call retrieval should fail")
    public void theCallRetrievalShouldFail() {
        assertTrue(response.statusCode() >= 400, "Expected error status but got: " + response.statusCode());
    }

    @When("I attempt to create a call without a contact ID")
    public void iAttemptToCreateACallWithoutAContactID() {
        callRequest = CallRequest.builder()
                .callTitle("Invalid Call")
                .callDirection(Call.CallDirection.OUTGOING)
                .recordingFilePath("/uploads/test.mp3")
                .callDateTime(LocalDateTime.now())
                .userId(testUserId)
                .build();

        response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + jwtToken)
                .body(callRequest)
                .when()
                .post("/api/calls");
    }

    @Then("the call creation should fail")
    public void theCallCreationShouldFail() {
        assertTrue(response.statusCode() >= 400, "Expected error status but got: " + response.statusCode() +
                " body: " + response.body().asString());
    }

    @And("I should see error {string}")
    public void iShouldSeeError(String expectedError) {
        String responseBody = response.body().asString();
        assertTrue(responseBody.contains(expectedError),
                "Expected error to contain: '" + expectedError + "' but got: " + responseBody);
    }

    @When("I attempt to create a call with contact ID {int}")
    public void iAttemptToCreateACallWithContactID(int contactId) {
        callRequest = CallRequest.builder()
                .callTitle("Invalid Call")
                .callDirection(Call.CallDirection.OUTGOING)
                .recordingFilePath("/uploads/test.mp3")
                .callDateTime(LocalDateTime.now())
                .contactId((long) contactId)
                .userId(testUserId)
                .build();

        response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + jwtToken)
                .body(callRequest)
                .when()
                .post("/api/calls");
    }

    @When("I attempt to create a call with user ID {int}")
    public void iAttemptToCreateACallWithUserID(int userId) {
        callRequest = CallRequest.builder()
                .callTitle("Invalid Call")
                .callDirection(Call.CallDirection.OUTGOING)
                .recordingFilePath("/uploads/test.mp3")
                .callDateTime(LocalDateTime.now())
                .contactId(testContactId)
                .userId((long) userId)
                .build();

        response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + jwtToken)
                .body(callRequest)
                .when()
                .post("/api/calls");
    }

    @And("an order exists for the call's contact")
    public void anOrderExistsForTheCallsContact() {
        if (orderRepository.count() > 0) {
            return;
        }

        Order order = Order.builder()
                .orderNumber("ORD-" + System.currentTimeMillis())
                .orderDate(LocalDateTime.now())
                .orderValue(1000.0)
                .build();

        orderRepository.save(order);
        System.out.println("Created order: " + order.getOrderNumber());
    }

    @When("I link the call to the order")
    public void iLinkTheCallToTheOrder() {
        Order order = orderRepository.findAll().get(0);

        callRequest.setOrderId(order.getId());

        response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + jwtToken)
                .body(callRequest)
                .when()
                .post("/api/calls");

        if (response.statusCode() == 200 || response.statusCode() == 201) {
            callResponse = response.as(CallResponse.class);
        }
    }

    @Then("the call should be associated with the order")
    public void theCallShouldBeAssociatedWithTheOrder() {
        assertNotNull(callResponse);
        assertNotNull(callResponse.getOrderId(), "Order ID should not be null");
        assertNotNull(callResponse.getOrderNumber(), "Order number should not be null");
    }

    @When("I record a call for that contact without specifying an order")
    public void iRecordACallForThatContactWithoutSpecifyingAnOrder() {
        callRequest = CallRequest.builder()
                .callTitle("Auto-order Call")
                .callDirection(Call.CallDirection.OUTGOING)
                .recordingFilePath("/uploads/auto-order.mp3")
                .callDateTime(LocalDateTime.now())
                .contactId(testContactId)
                .userId(testUserId)
                .build();

        response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + jwtToken)
                .body(callRequest)
                .when()
                .post("/api/calls");

        System.out.println("Auto-order call status: " + response.statusCode());

        if (response.statusCode() == 200 || response.statusCode() == 201) {
            callResponse = response.as(CallResponse.class);
        }
    }

    @And("an order should be automatically created for the call")
    public void anOrderShouldBeAutomaticallyCreatedForTheCall() {
        assertNotNull(callResponse);
        assertNotNull(callResponse.getOrderId(), "Order should be auto-created");
        assertNotNull(callResponse.getOrderNumber(), "Order number should not be null");
        assertTrue(callResponse.getOrderNumber().startsWith("ORD-"),
                "Order number should start with 'ORD-', but got: " + callResponse.getOrderNumber());
    }
}