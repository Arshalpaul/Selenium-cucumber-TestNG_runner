Feature: Login functionality

  Scenario: Invalid login attempts
    Given I am on the login page
    When I enter "email@gmail.com" and "password"
    And I click Sign in
    Then I should see an error message

  Scenario: Invalid login attempts with email field empty
    Given I am on the login page
    When I enter "" and "passworddd"
    And I click Sign in
    Then I should see an error message
