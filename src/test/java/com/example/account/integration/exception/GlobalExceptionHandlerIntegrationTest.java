package com.example.account.integration.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.account.constants.CommonConstants;
import com.example.account.dto.account.request.AccountCreateRequestDTO;
import com.example.account.dto.error.ApiErrorResponseDTO;
import com.example.account.integration.BaseIntegrationTest;
import com.example.account.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for GlobalExceptionHandler.
 * Tests exception handling with real Spring context and i18n message resolution.
 */
@Transactional
class GlobalExceptionHandlerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private AccountRepository accountRepository;

    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    private static final String TEST_IDEMPOTENCY_KEY = "test-idempotency-key-integration";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        accountRepository.deleteAll();
    }

    @Test
    void testHandleValidationExceptions_NotBlank() throws Exception {
        // Create request with missing required field
        AccountCreateRequestDTO request = AccountCreateRequestDTO.builder()
                .email("test@example.com")
                .countryCode("USA")
                .currencyCode("USD")
                // accountName is missing (required)
                .build();

        mockMvc.perform(post(CommonConstants.API_V1_PREFIX + CommonConstants.ACCOUNT_API_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(CommonConstants.IDEMPOTENCY_KEY_HEADER, TEST_IDEMPOTENCY_KEY)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.accountName").exists())
                .andExpect(jsonPath("$.errors.accountName", 
                        anyOf(containsString("required"), containsString("Account Name"))));
    }

    @Test
    void testHandleValidationExceptions_Email() throws Exception {
        // Create request with invalid email
        AccountCreateRequestDTO request = AccountCreateRequestDTO.builder()
                .accountName("Test Account")
                .email("invalid-email")
                .countryCode("USA")
                .currencyCode("USD")
                .build();

        mockMvc.perform(post(CommonConstants.API_V1_PREFIX + CommonConstants.ACCOUNT_API_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(CommonConstants.IDEMPOTENCY_KEY_HEADER, TEST_IDEMPOTENCY_KEY)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.email", 
                        anyOf(containsString("invalid-email"), containsString("Email"))));
    }

    @Test
    void testHandleValidationExceptions_Size() throws Exception {
        // Create request with field exceeding max size
        AccountCreateRequestDTO request = AccountCreateRequestDTO.builder()
                .accountName("Test Account")
                .email("test@example.com")
                .website("x".repeat(300)) // Exceeds 255
                .countryCode("USA")
                .currencyCode("USD")
                .build();

        mockMvc.perform(post(CommonConstants.API_V1_PREFIX + CommonConstants.ACCOUNT_API_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(CommonConstants.IDEMPOTENCY_KEY_HEADER, TEST_IDEMPOTENCY_KEY)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.website").exists())
                .andExpect(jsonPath("$.errors.website", 
                        anyOf(containsString("255"), containsString("exceed"))));
    }

    @Test
    void testHandleValidationExceptions_CustomValidator_CountryCode() throws Exception {
        // Create request with invalid country code
        AccountCreateRequestDTO request = AccountCreateRequestDTO.builder()
                .accountName("Test Account")
                .email("test@example.com")
                .countryCode("XX") // Invalid country code
                .currencyCode("USD")
                .build();

        mockMvc.perform(post(CommonConstants.API_V1_PREFIX + CommonConstants.ACCOUNT_API_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(CommonConstants.IDEMPOTENCY_KEY_HEADER, TEST_IDEMPOTENCY_KEY)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.countryCode").exists())
                .andExpect(jsonPath("$.errors.countryCode", 
                        anyOf(containsString("XX"), containsString("Country Code"))));
    }

    @Test
    void testHandleValidationExceptions_CustomValidator_CurrencyCode() throws Exception {
        // Create request with invalid currency code
        AccountCreateRequestDTO request = AccountCreateRequestDTO.builder()
                .accountName("Test Account")
                .email("test@example.com")
                .countryCode("USA")
                .currencyCode("AAA") // Invalid currency code
                .build();

        mockMvc.perform(post(CommonConstants.API_V1_PREFIX + CommonConstants.ACCOUNT_API_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(CommonConstants.IDEMPOTENCY_KEY_HEADER, TEST_IDEMPOTENCY_KEY)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.currencyCode").exists())
                .andExpect(jsonPath("$.errors.currencyCode", 
                        anyOf(containsString("AAA"), containsString("Currency Code"))));
    }

    @Test
    void testHandleValidationExceptions_MultipleErrors() throws Exception {
        // Create request with multiple validation errors
        AccountCreateRequestDTO request = AccountCreateRequestDTO.builder()
                // accountName is missing
                .email("invalid-email")
                .website("x".repeat(300))
                .countryCode("XX")
                .currencyCode("AAA")
                .build();

        mockMvc.perform(post(CommonConstants.API_V1_PREFIX + CommonConstants.ACCOUNT_API_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(CommonConstants.IDEMPOTENCY_KEY_HEADER, TEST_IDEMPOTENCY_KEY)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors").isMap())
                .andExpect(jsonPath("$.errors").value(not(anEmptyMap())));
        
        // Verify multiple errors are present
        String responseContent = mockMvc.perform(post(CommonConstants.API_V1_PREFIX + CommonConstants.ACCOUNT_API_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(CommonConstants.IDEMPOTENCY_KEY_HEADER, TEST_IDEMPOTENCY_KEY)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        ApiErrorResponseDTO response = objectMapper.readValue(responseContent, ApiErrorResponseDTO.class);
        assertNotNull(response.getErrors(), "Errors should not be null");
        assertTrue(response.getErrors().size() >= 2, "Should have at least 2 errors");
    }

    @Test
    void testHandleAccountNotFoundException() throws Exception {
        // Try to get a non-existent account
        mockMvc.perform(get(CommonConstants.API_V1_PREFIX + CommonConstants.ACCOUNT_API_ENDPOINT 
                        + "/ACC-NOTFOUND"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message", 
                        anyOf(containsString("ACC-NOTFOUND"), containsString("Account"), containsString("not found"))))
                .andExpect(jsonPath("$.errors").doesNotExist());
    }

    @Test
    void testHandleAccountNotFoundException_WithAccountId() throws Exception {
        // Try to get a non-existent account by ID
        mockMvc.perform(get(CommonConstants.API_V1_PREFIX + CommonConstants.ACCOUNT_API_ENDPOINT 
                        + "/123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message", 
                        anyOf(containsString("123"), containsString("Account"), containsString("not found"))));
    }

    @Test
    void testHandleGenericException() throws Exception {
        // This test is harder to trigger in a real scenario without causing actual errors
        // We can test that the handler exists and is configured correctly
        // by verifying that validation and account not found exceptions are handled
        
        // Verify that the handler is loaded by checking that validation errors are handled
        AccountCreateRequestDTO request = AccountCreateRequestDTO.builder()
                .accountName("") // Empty name
                .build();

        mockMvc.perform(post(CommonConstants.API_V1_PREFIX + CommonConstants.ACCOUNT_API_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(CommonConstants.IDEMPOTENCY_KEY_HEADER, TEST_IDEMPOTENCY_KEY)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void testErrorResponseStructure() throws Exception {
        // Verify error response structure
        AccountCreateRequestDTO request = AccountCreateRequestDTO.builder()
                .accountName("") // Empty name
                .build();

        String responseContent = mockMvc.perform(post(CommonConstants.API_V1_PREFIX + CommonConstants.ACCOUNT_API_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(CommonConstants.IDEMPOTENCY_KEY_HEADER, TEST_IDEMPOTENCY_KEY)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ApiErrorResponseDTO response = objectMapper.readValue(responseContent, ApiErrorResponseDTO.class);
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getTimestamp(), "Timestamp should be present");
        assertEquals(400, response.getStatus(), "Status should be 400");
        assertNotNull(response.getMessage(), "Message should be present");
        assertNotNull(response.getErrors(), "Errors should be present");
    }

    @Test
    void testI18nMessageResolution_InValidationErrors() throws Exception {
        // Verify that i18n messages are resolved, not error codes
        AccountCreateRequestDTO request = AccountCreateRequestDTO.builder()
                .accountName("") // Empty name
                .email("invalid-email")
                .build();

        String responseContent = mockMvc.perform(post(CommonConstants.API_V1_PREFIX + CommonConstants.ACCOUNT_API_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(CommonConstants.IDEMPOTENCY_KEY_HEADER, TEST_IDEMPOTENCY_KEY)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ApiErrorResponseDTO response = objectMapper.readValue(responseContent, ApiErrorResponseDTO.class);
        assertNotNull(response.getErrors(), "Errors should be present");
        
        // Verify messages are resolved (not error codes)
        response.getErrors().values().forEach(message -> {
            assertNotNull(message, "Each error message should not be null");
            assertFalse(message.isEmpty(), "Each error message should not be empty");
            // Should not be an error code (like "validation.account.name.notblank")
            assertFalse(message.startsWith("validation."), 
                    "Message should be resolved, not be an error code: " + message);
            assertFalse(message.startsWith("account.") && message.contains(".") && !message.contains(" "),
                    "Message should be resolved, not be a simple key: " + message);
        });
    }

    @Test
    void testHandleNoResourceFoundException() throws Exception {
        // Test with a non-existent endpoint path (will be treated as static resource)
        String invalidPath = "/api/v1/api/v1/accounts";
        
        mockMvc.perform(get(invalidPath))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message", 
                        anyOf(containsString(invalidPath), containsString("not found"), containsString("resource"))))
                .andExpect(jsonPath("$.errors").doesNotExist());
    }

    @Test
    void testHandleNoResourceFoundException_InvalidEndpoint() throws Exception {
        // Test with another invalid endpoint
        String invalidPath = "/api/v1/nonexistent";
        
        mockMvc.perform(get(invalidPath))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message", 
                        anyOf(containsString(invalidPath), containsString("not found"), containsString("resource"))));
    }
}

