# ============================================================================
# Feature: Sales Call Management & Sentiment Analysis
# ============================================================================
# Business Domain: Customer Interaction Tracking
# Criticality: HIGH - Core sales workflow
#
# Business Rules:
#   - BR-CALL-001: Every call must be linked to an existing contact
#   - BR-CALL-002: Calls without an order auto-generate a new order
#   - BR-CALL-003: N8N webhook integration provides AI sentiment analysis
#   - BR-CALL-004: Sentiment data includes label, percentage score, and transcript
# ============================================================================

@calls @high
Feature: Sales Call Recording & AI Sentiment Analysis

  As a sales representative
  I want to record and analyze my customer calls with AI
  So that I can track conversation sentiment and improve sales outcomes

  # --------------------------------------------------------------------------
  # Background: Test Environment Setup
  # --------------------------------------------------------------------------
  Background:
    Given I am authenticated as a valid user
    And a contact exists for call testing
    And the system is ready for call management

  # ==========================================================================
  # HAPPY PATH SCENARIOS
  # ==========================================================================

  @smoke @positive @BR-CALL-001
  Scenario: Sales representative records a new customer call
    # Verifies that a call can be logged against an existing contact
    When I record a call with the following details:
      | callTitle          | callDirection | recordingFilePath   |
      | Follow-up Call     | OUTGOING      | /uploads/audio1.mp3 |
    Then the call should be recorded successfully
    And the call should have an ID

  @positive @BR-CALL-003 @BR-CALL-004
  Scenario: AI sentiment analysis updates call with customer satisfaction data
    # Business Value: N8N webhook processes call audio and returns sentiment insights
    Given a call exists in the system
    When the N8N webhook sends sentiment analysis data:
      | sentimentLabel       | sentimentPercentage | summary                        | transcript              |
      | Extremely Positive   | 98                  | Customer very satisfied        | Great product, will buy |
    Then the call should be updated with sentiment data
    And the sentiment label should be "Extremely Positive"
    And the sentiment percentage should be 98

  @positive
  Scenario: Sales representative retrieves call details by ID
    Given a call exists in the system
    When I retrieve the call details
    Then I should see the call information
    And the call should include the correct title

  @positive @BR-CALL-002
  Scenario: Recording a call automatically creates an order for the contact
    # Business Rule: System auto-generates order tracking when no order is specified
    When I record a call for that contact without specifying an order
    Then the call should be recorded successfully
    And an order should be automatically created for the call

  @positive
  Scenario: Call is linked to an existing customer order
    # Business Value: Enables tracking of all communications related to a specific order
    Given a call exists in the system
    And an order exists for the call's contact
    When I link the call to the order
    Then the call should be associated with the order

  # ==========================================================================
  # ERROR HANDLING & VALIDATION
  # ==========================================================================

  @negative
  Scenario: System returns error when retrieving a call that does not exist
    When I retrieve call with ID 99999
    Then the call retrieval should fail
    And I should see error "Call not found"

  @negative @BR-CALL-001
  Scenario: Call creation rejected when no contact is specified
    # Business Rule: A contact reference is mandatory for all calls
    When I attempt to create a call without a contact ID
    Then the call creation should fail
    And I should see error "The given id must not be null"

  @negative @BR-CALL-001
  Scenario: Call creation rejected when referenced contact does not exist
    # Business Rule: Referential integrity - contact must exist in the system
    When I attempt to create a call with contact ID 99999
    Then the call creation should fail
    And I should see error "Contact not found"

  @negative
  Scenario: Call creation rejected when referenced user does not exist
    # Business Rule: Referential integrity - user must exist in the system
    When I attempt to create a call with user ID 99999
    Then the call creation should fail
    And I should see error "User not found"