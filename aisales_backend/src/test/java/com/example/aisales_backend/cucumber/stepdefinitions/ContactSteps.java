package com.example.aisales_backend.cucumber.stepdefinitions;

import com.example.aisales_backend.dto.ContactRequest;
import com.example.aisales_backend.dto.ContactResponse;
import com.example.aisales_backend.entity.Company;
import com.example.aisales_backend.entity.Contact;
import com.example.aisales_backend.entity.User;
import com.example.aisales_backend.entity.UserRole;
import com.example.aisales_backend.repository.CompanyRepository;
import com.example.aisales_backend.repository.ContactRepository;
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

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

public class ContactSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private CompanyRepository companyRepository;

    private Response response;
    private ContactResponse contactResponse;
    private Long testUserId;
    private Long testCompanyId;
    private Long testContactId;
    private String jwtToken;
    private ContactRequest contactRequest;

    @Given("I am authenticated as a valid user for contact tests")
    public void iAmAuthenticatedAsAValidUserForContactTests() {
        RestAssured.port = port;
        RestAssured.basePath = "";

        String testEmail = "contact.tester@vocalyx.com";
        String testPassword = "TestPass@123";

        // Create or find user directly in DB
        User user = userRepository.findByEmail(testEmail).orElse(null);

        if (user == null) {
            user = User.builder()
                    .firstName("Contact")
                    .lastName("Tester")
                    .email(testEmail)
                    .password(passwordEncoder.encode(testPassword))
                    .role(UserRole.ADMIN)
                    .build();
            user = userRepository.save(user);
        }

        // Check if user has a company, if not create one
        if (user.getCompany() == null) {
            Company company = Company.builder()
                    .companyName("Contact Test Company")
                    .industry("Technology")
                    .address("123 Test St")
                    .build();
            company = companyRepository.save(company);

            user.setCompany(company);
            user = userRepository.save(user);

            System.out.println("Created company with ID: " + company.getId() + " for user: " + user.getEmail());
        } else {
            System.out.println("User already has company with ID: " + user.getCompany().getId());
        }

        testUserId = user.getId();
        testCompanyId = user.getCompany().getId();
        assertNotNull(testUserId, "Test user ID should not be null");
        assertNotNull(testCompanyId, "Test company ID should not be null");

        // Generate JWT token directly
        jwtToken = jwtTokenProvider.generateTokenForUser(user);
        assertNotNull(jwtToken, "JWT token should not be null");
    }

    @Given("the system is ready for contact management")
    public void theSystemIsReadyForContactManagement() {
        RestAssured.port = port;
        RestAssured.basePath = "";
    }

    @When("I create a contact with the following details:")
    public void iCreateAContactWithTheFollowingDetails(io.cucumber.datatable.DataTable dataTable) {
        Map<String, String> data = dataTable.asMaps().get(0);

        contactRequest = ContactRequest.builder()
                .salutation(Contact.Salutation.valueOf(data.get("salutation")))
                .firstName(data.get("firstName"))
                .lastName(data.get("lastName"))
                .email(data.get("email"))
                .phoneNumber(data.get("phone"))
                .department(Contact.Department.valueOf(data.get("department")))
                .companyName(data.get("companyName"))
                .status(Contact.ContactStatus.ACTIVE)
                .build();

        response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + jwtToken)
                .body(contactRequest)
                .when()
                .post("/api/contacts");

        System.out.println("Create contact status: " + response.statusCode());
        System.out.println("Create contact body: " + response.body().asString());

        if (response.statusCode() == 200 || response.statusCode() == 201) {
            contactResponse = response.as(ContactResponse.class);
            testContactId = contactResponse.getId();
        }
    }

    @Then("the contact should be created successfully")
    public void theContactShouldBeCreatedSuccessfully() {
        assertTrue(response.statusCode() == 200 || response.statusCode() == 201,
                "Expected 200 or 201 but got: " + response.statusCode() +
                        " body: " + response.body().asString());
        assertNotNull(contactResponse);
    }

    @And("the contact should have an ID")
    public void theContactShouldHaveAnID() {
        assertNotNull(contactResponse.getId());
        assertTrue(contactResponse.getId() > 0);
    }

    @Given("a contact exists with email {string}")
    public void aContactExistsWithEmail(String email) {
        Contact existingContact = contactRepository.findByEmailIgnoreCase(email).orElse(null);

        if (existingContact != null) {
            testContactId = existingContact.getId();
            System.out.println("Found existing contact with ID: " + testContactId);
            return;
        }

        contactRequest = ContactRequest.builder()
                .salutation(Contact.Salutation.MR)
                .firstName("Test")
                .lastName("Contact")
                .email(email)
                .phoneNumber("+1234567890")
                .department(Contact.Department.SALES)
                .status(Contact.ContactStatus.ACTIVE)
                .companyName("Test Company")
                .build();

        response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + jwtToken)
                .body(contactRequest)
                .when()
                .post("/api/contacts");

        System.out.println("Contact exists creation status: " + response.statusCode());
        System.out.println("Contact exists creation body: " + response.body().asString());

        if (response.statusCode() == 200 || response.statusCode() == 201) {
            contactResponse = response.as(ContactResponse.class);
            testContactId = contactResponse.getId();
        } else {
            Contact contact = contactRepository.findByEmailIgnoreCase(email).orElse(null);
            if (contact != null) {
                testContactId = contact.getId();
            }
        }

        assertNotNull(testContactId, "Test contact ID should not be null for email: " + email);
    }

    @When("I retrieve the contact by ID")
    public void iRetrieveTheContactByID() {
        response = given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get("/api/contacts/" + testContactId);

        System.out.println("Get contact status: " + response.statusCode());

        if (response.statusCode() == 200) {
            contactResponse = response.as(ContactResponse.class);
        }
    }

    @Then("I should see the contact details")
    public void iShouldSeeTheContactDetails() {
        assertEquals(200, response.statusCode());
        assertNotNull(contactResponse);
    }

    @And("the contact email should be {string}")
    public void theContactEmailShouldBe(String expectedEmail) {
        assertNotNull(contactResponse.getEmail());
        assertEquals(expectedEmail.toLowerCase(), contactResponse.getEmail().toLowerCase());
    }

    @Given("the following contacts exist:")
    public void theFollowingContactsExist(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> contacts = dataTable.asMaps();

        for (Map<String, String> contactData : contacts) {
            String email = contactData.get("email");

            if (contactRepository.findByEmailIgnoreCase(email).isPresent()) {
                continue;
            }

            ContactRequest request = ContactRequest.builder()
                    .salutation(Contact.Salutation.MR)
                    .firstName(contactData.get("firstName"))
                    .lastName(contactData.get("lastName"))
                    .email(email)
                    .phoneNumber("+1234567890")
                    .department(Contact.Department.SALES)
                    .status(Contact.ContactStatus.ACTIVE)
                    .companyName(contactData.get("companyName"))
                    .build();

            given()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .header("Authorization", "Bearer " + jwtToken)
                    .body(request)
                    .when()
                    .post("/api/contacts");
        }
    }

    @When("I request all contacts")
    public void iRequestAllContacts() {
        response = given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get("/api/contacts");

        System.out.println("Get all contacts status: " + response.statusCode());
    }

    @Then("I should receive a list of contacts")
    public void iShouldReceiveAListOfContacts() {
        assertEquals(200, response.statusCode());
        List<?> contacts = response.jsonPath().getList("$");
        assertNotNull(contacts);
        assertTrue(contacts.size() > 0, "Should have at least one contact");
    }

    @When("I update the contact with new details:")
    public void iUpdateTheContactWithNewDetails(io.cucumber.datatable.DataTable dataTable) {
        Map<String, String> data = dataTable.asMaps().get(0);

        response = given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get("/api/contacts/" + testContactId);

        System.out.println("Get contact for update status: " + response.statusCode());

        if (response.statusCode() == 200) {
            ContactResponse existing = response.as(ContactResponse.class);

            contactRequest = ContactRequest.builder()
                    .salutation(existing.getSalutation())
                    .firstName(existing.getFirstName())
                    .lastName(existing.getLastName())
                    .email(existing.getEmail())
                    .phoneNumber(data.get("phoneNumber"))
                    .jobTitle(data.get("jobTitle"))
                    .department(Contact.Department.valueOf(data.get("department")))
                    .status(existing.getStatus())
                    .companyName(existing.getCompanyName())
                    .build();

            response = given()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .header("Authorization", "Bearer " + jwtToken)
                    .body(contactRequest)
                    .when()
                    .put("/api/contacts/" + testContactId);

            System.out.println("Update contact status: " + response.statusCode());

            if (response.statusCode() == 200) {
                contactResponse = response.as(ContactResponse.class);
            }
        }
    }

    @Then("the contact should be updated successfully")
    public void theContactShouldBeUpdatedSuccessfully() {
        assertEquals(200, response.statusCode());
        assertNotNull(contactResponse);
    }

    @And("the contact phone number should be {string}")
    public void theContactPhoneNumberShouldBe(String expectedPhone) {
        assertEquals(expectedPhone, contactResponse.getPhoneNumber());
    }

    @And("the contact job title should be {string}")
    public void theContactJobTitleShouldBe(String expectedJobTitle) {
        assertEquals(expectedJobTitle, contactResponse.getJobTitle());
    }

    @When("I delete the contact")
    public void iDeleteTheContact() {
        response = given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .delete("/api/contacts/" + testContactId);

        System.out.println("Delete contact status: " + response.statusCode());
    }

    @Then("the contact should be deleted successfully")
    public void theContactShouldBeDeletedSuccessfully() {
        assertTrue(response.statusCode() == 200 || response.statusCode() == 204,
                "Expected 200 or 204 but got: " + response.statusCode());
    }

    @When("I attempt to retrieve contact with ID {int}")
    public void iAttemptToRetrieveContactWithID(int contactId) {
        response = given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .get("/api/contacts/" + contactId);

        System.out.println("Retrieve non-existent contact status: " + response.statusCode());
    }

    @Then("the request should fail for contact retrieval")
    public void theRequestShouldFailForContactRetrieval() {
        assertTrue(response.statusCode() >= 400,
                "Expected error status but got: " + response.statusCode() +
                        " body: " + response.body().asString());
    }

    @And("I should see error message for contact {string}")
    public void iShouldSeeErrorMessageForContact(String expectedError) {
        String responseBody = response.body().asString();
        assertTrue(
                responseBody.contains(expectedError) ||
                        responseBody.contains("Contact not found") ||
                        responseBody.contains("not found"),
                "Expected error to contain: '" + expectedError + "' but got: " + responseBody
        );
    }

    @When("I attempt to create a contact with invalid data:")
    public void iAttemptToCreateAContactWithInvalidData(io.cucumber.datatable.DataTable dataTable) {
        Map<String, String> data = dataTable.asMaps().get(0);

        contactRequest = ContactRequest.builder()
                .firstName(data.get("firstName"))
                .lastName(data.get("lastName"))
                .email(data.get("email"))
                .salutation(Contact.Salutation.MR)
                .department(Contact.Department.valueOf(data.get("department")))
                .status(Contact.ContactStatus.ACTIVE)
                .companyName("Test Company")
                .phoneNumber("+1234567890")
                .build();

        response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + jwtToken)
                .body(contactRequest)
                .when()
                .post("/api/contacts");

        System.out.println("Invalid contact creation status: " + response.statusCode());
        System.out.println("Invalid contact creation body: " + response.body().asString());
    }

    @Then("the contact creation request should fail")
    public void theContactCreationRequestShouldFail() {
        assertTrue(response.statusCode() >= 400,
                "Expected error status but got: " + response.statusCode() +
                        " body: " + response.body().asString());
    }

    @And("I should see contact validation errors")
    public void iShouldSeeContactValidationErrors() {
        assertEquals(400, response.statusCode());
        assertNotNull(response.body());
    }
}