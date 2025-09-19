Feature: Login functionality

  Scenario: Valid login with correct email and password
    Given I am on the login page
    When I enter "uptimeuser@vecvnet.com" and "VolvoEicher@080825"
    And I click Sign in
    Then I should be logged in successfully

  Scenario: Invalid login attempts
    Given I am on the login page
    When I enter "email@gmail.com" and "password"
    And I click Sign in
    Then I should see an error message

  Scenario: Invalid login attempts with email field empty
    Given I am on the login page
    When I enter "" and "password"
    And I click Sign in
    Then I should see an error message
