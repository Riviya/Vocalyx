# ============================================================================
# Feature: User Authentication & Security
# ============================================================================
# Business Domain: Identity & Access Management
# Criticality: CRITICAL - Blocks all other features
#
# Business Rules:
#   - BR-AUTH-001: First user in system automatically gets ADMIN role
#   - BR-AUTH-002: Duplicate email registration is not allowed
#   - BR-AUTH-003: Invalid login attempts return generic error (security best practice)
#   - BR-AUTH-004: Password reset requires current password verification
#   - BR-AUTH-005: All fields validated before account creation
# ============================================================================

@authentication @critical
Feature: User Authentication & Account Security

  As a sales representative
  I want to securely authenticate with the system
  So that my sales data remains protected and only I can access my workspace

  # --------------------------------------------------------------------------
  # Background: Test Environment Setup
  # --------------------------------------------------------------------------
  Background:
    Given the system is ready for testing

  # ==========================================================================
  # HAPPY PATH SCENARIOS
  # ==========================================================================

  @smoke @positive @critical @BR-AUTH-001
  Scenario: New user registers an account successfully
    # Verifies that a new user can create an account and receives ADMIN privileges
    # (First user or initial account creation)
    When I register a new user with valid details:
      | firstName | lastName | email                  | password    |
      | John      | Doe      | john.doe@vocalyx.com   | Pass@123    |
    Then the registration should be successful
    And I should receive a user ID
    And the user role should be "ADMIN"

  @smoke @positive @critical @BR-AUTH-001
  Scenario: The very first user in the system receives admin role
    # Business Rule: System automatically assigns ADMIN role when no other users exist
    Given no users exist in the system
    When I register a new user with valid details:
      | firstName | lastName | email                    | password    |
      | Admin     | User     | admin@vocalyx.com        | Admin@123   |
    Then the registration should be successful
    And the user role should be "ADMIN"

  @smoke @positive @critical
  Scenario: Registered user logs in with correct credentials
    # Verifies the standard login flow returns a valid authentication token
    Given a user exists with email "test.user@vocalyx.com" and password "ValidPass@123"
    When I login with email "test.user@vocalyx.com" and password "ValidPass@123"
    Then the login should be successful
    And I should receive a valid JWT token

  # ==========================================================================
  # ERROR HANDLING & EDGE CASES
  # ==========================================================================

  @negative @BR-AUTH-002
  Scenario: Registration is rejected when email already exists
    # Business Rule: Each email address can only be registered once
    Given a user exists with email "existing@vocalyx.com" and password "Pass@123"
    When I attempt to register with email "existing@vocalyx.com"
    Then the registration should fail
    And I should see error message "User with this email already exists"

  @negative @BR-AUTH-003
  Scenario Outline: Login fails when credentials do not match any account
    # Business Rule: System returns generic error (does not reveal if email exists)
    Given a user exists with email "valid@vocalyx.com" and password "CorrectPass@123"
    When I login with email "<email>" and password "<password>"
    Then the login should fail
    And I should see an authentication error

    Examples:
      | email                  | password         |
      | valid@vocalyx.com      | WrongPass@123    |
      | wrong@vocalyx.com      | CorrectPass@123  |
      | invalid@email          | Pass@123         |

  @negative @BR-AUTH-005
  Scenario: Registration rejected when form fields contain invalid data
    # Business Rule: First name, last name, email, and password must all be valid
    When I attempt to register with invalid data:
      | firstName | lastName | email         | password |
      | J         | D        | notanemail    | 123      |
    Then the registration should fail
    And I should see validation errors

  # ==========================================================================
  # PASSWORD MANAGEMENT
  # ==========================================================================

  @regression @BR-AUTH-004
  Scenario: Authenticated user successfully changes their password
    # Business Rule: Users must verify current password before setting a new one
    Given a user exists with email "reset@vocalyx.com" and password "OldPass@123"
    And I am logged in as "reset@vocalyx.com"
    When I reset my password from "OldPass@123" to "NewPass@123"
    Then the password reset should be successful
    And I should be able to login with the new password

  @negative @BR-AUTH-004
  Scenario: Password reset rejected when current password is incorrect
    # Business Rule: Prevents unauthorized password changes
    Given a user exists with email "user@vocalyx.com" and password "CurrentPass@123"
    And I am logged in as "user@vocalyx.com"
    When I attempt to reset password with incorrect current password
    Then the password reset should fail
    And I should see error message "Current password is incorrect"