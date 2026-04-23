# ============================================================================
# Feature: Customer Contact Management (CRM)
# ============================================================================
# Business Domain: Customer Relationship Management
# Criticality: HIGH - Foundation for calls, orders, and sales tracking
#
# Business Rules:
#   - BR-CONTACT-001: Contacts are scoped to the user's company
#   - BR-CONTACT-002: All contact fields except phone/job title are required
#   - BR-CONTACT-003: Contact email must be unique within a company
# ============================================================================

@contacts @high
Feature: Customer Contact Directory Management

  As a sales representative
  I want to manage my customer contacts in a centralized directory
  So that I can track all my business relationships and interactions

  # --------------------------------------------------------------------------
  # Background: Test Environment Setup
  # --------------------------------------------------------------------------
  Background:
    Given I am authenticated as a valid user for contact tests
    And the system is ready for contact management

  # ==========================================================================
  # CRUD OPERATIONS - HAPPY PATH
  # ==========================================================================

  @smoke @positive @BR-CONTACT-001
  Scenario: Sales representative adds a new contact to their directory
    # Verifies that a new business contact can be created with all required details
    When I create a contact with the following details:
      | salutation | firstName | lastName | email                  | phone       | department | companyName |
      | MR         | James     | Wilson   | james.w@techcorp.com   | +1234567890 | SALES      | TechCorp    |
    Then the contact should be created successfully
    And the contact should have an ID

  @positive
  Scenario: Sales representative looks up a contact by their ID
    Given a contact exists with email "existing.contact@company.com"
    When I retrieve the contact by ID
    Then I should see the contact details
    And the contact email should be "existing.contact@company.com"

  @positive @BR-CONTACT-001
  Scenario: Sales representative views their complete contact directory
    # Business Value: Provides a full list of all contacts within their company
    Given the following contacts exist:
      | firstName | lastName | email                    | companyName  |
      | Alice     | Brown    | alice.b@companya.com     | Company A    |
      | Bob       | Green    | bob.g@companyb.com       | Company B    |
      | Charlie   | White    | charlie.w@companyc.com   | Company C    |
    When I request all contacts
    Then I should receive a list of contacts

  @positive @regression
  Scenario: Sales representative updates an existing contact's information
    # Business Value: Keeps contact records current as roles and details change
    Given a contact exists with email "update.test@company.com"
    When I update the contact with new details:
      | phoneNumber   | jobTitle          | department  |
      | +9876543210   | Senior Manager    | SALES       |
    Then the contact should be updated successfully
    And the contact phone number should be "+9876543210"
    And the contact job title should be "Senior Manager"

  @positive
  Scenario: Sales representative removes a contact from their directory
    Given a contact exists with email "delete.test@company.com"
    When I delete the contact
    Then the contact should be deleted successfully

  # ==========================================================================
  # ERROR HANDLING & VALIDATION
  # ==========================================================================

  @negative
  Scenario: System returns error when looking up a contact that does not exist
    When I attempt to retrieve contact with ID 99999
    Then the request should fail for contact retrieval
    And I should see error message for contact "Contact not found"

  @negative @BR-CONTACT-002
  Scenario: Contact creation rejected when required fields contain invalid data
    # Business Rule: First name and last name must be valid; email format must be correct
    When I attempt to create a contact with invalid data:
      | firstName | lastName | email           | department |
      | A         | B        | notvalidemail   | SALES      |
    Then the contact creation request should fail
    And I should see contact validation errors