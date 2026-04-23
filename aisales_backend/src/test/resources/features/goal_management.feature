# ============================================================================
# Feature: Sales Goal Setting & Progress Tracking
# ============================================================================
# Business Domain: Performance Management & KPIs
# Criticality: MEDIUM - Motivational and analytical tool
#
# Business Rules:
#   - BR-GOAL-001: Goals are personal to each user (not shared across company)
#   - BR-GOAL-002: Goal status is calculated automatically (Not Started/In Progress/Completed)
#   - BR-GOAL-003: Progress of 0% = "Not Started", 1-99% = "In Progress", 100%+ = "Completed"
#   - BR-GOAL-004: Goal name is required for creation
# ============================================================================

@goals @medium
Feature: Sales Performance Goals & KPI Tracking

  As a sales representative
  I want to set personal sales goals and track my progress
  So that I can measure my performance and stay motivated to achieve targets

  # --------------------------------------------------------------------------
  # Background: Test Environment Setup
  # --------------------------------------------------------------------------
  Background:
    Given I am authenticated as a valid user for goal tests
    And the system is ready for goal management

  # ==========================================================================
  # GOAL LIFECYCLE - HAPPY PATH
  # ==========================================================================

  @smoke @positive @BR-GOAL-002
  Scenario: Sales representative defines a new quarterly sales target
    # Business Value: Enables goal-setting with automatic status calculation
    When I create a goal with the following details:
      | name              | targetRevenue | startDate  | endDate    | company    | priority |
      | Q1 Sales Target   | 100000        | 2024-01-01 | 2024-03-31 | Acme Corp  | High     |
    Then the goal should be created successfully
    And the goal should have an ID
    And the goal status should be "Not Started"

  @positive
  Scenario: Sales representative views details of a specific goal
    Given a goal exists with name "Monthly Target"
    When I retrieve the goal by ID
    Then I should see the goal details
    And the goal name should be "Monthly Target"

  @positive @BR-GOAL-001
  Scenario: Sales representative views all their personal goals
    # Business Rule: Goals are private to each user - only their own goals appear
    Given the following goals exist for the current user:
      | name         | targetRevenue | status       |
      | Goal 1       | 50000         | In Progress  |
      | Goal 2       | 75000         | Completed    |
      | Goal 3       | 100000        | Not Started  |
    When I request all my goals
    Then I should receive at least 3 goals

  @positive @regression @BR-GOAL-002
  Scenario: Sales representative updates progress on an active goal
    # Business Value: Reflects real-time progress as deals close
    Given a goal exists with name "Progress Test Goal"
    When I update the goal progress to 60000
    Then the goal should be updated successfully
    And the goal progress should be updated

  @positive @BR-GOAL-003
  Scenario: Goal automatically marked as completed when progress reaches target
    # Business Rule: System auto-calculates completion when progress >= target revenue
    Given a goal exists with name "Completion Goal"
    When I update the goal progress to 100000
    Then the goal status should automatically change to "Completed"

  @positive
  Scenario: Sales representative removes a goal they no longer need
    Given a goal exists with name "Temporary Goal"
    When I delete the goal
    Then the goal should be deleted successfully

  # ==========================================================================
  # ERROR HANDLING & VALIDATION
  # ==========================================================================

  @negative
  Scenario: System returns error when accessing a goal that does not exist
    When I attempt to retrieve goal with ID 99999
    Then the goal request should fail
    And I should see goal error "Goal not found"

  @negative @BR-GOAL-004
  Scenario: Goal creation rejected when required name field is blank
    # Business Rule: Every goal must have a name to be meaningful
    When I attempt to create a goal with:
      | name | targetRevenue | startDate  | endDate    | company   | priority |
      |      | 10000         | 2024-01-01 | 2024-12-31 | Test Corp | High     |
    Then the goal creation should fail
    And I should see goal validation errors