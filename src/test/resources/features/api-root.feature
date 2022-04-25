Feature: API root

  Scenario: displaying API info
    When I navigate to API root with host header localhost:8080
    Then Swagger docs URI in response equals to http://localhost:8080/swagger-ui.html
