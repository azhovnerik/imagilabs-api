Feature: API root

  Scenario: displaying API info
    Given I'm an anonymous user
    When I navigate to API root
    Then I see welcome message
