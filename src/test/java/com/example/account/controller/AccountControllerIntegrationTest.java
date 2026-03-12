package com.example.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.account.constants.CommonConstants;
import com.example.account.dto.account.request.AccountCreateRequestDTO;
import com.example.account.dto.account.request.AccountListRequestDTO;
import com.example.account.dto.account.request.AccountUpdateRequestDTO;
import com.example.account.model.account.AccountEntity;
import com.example.account.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AccountController.
 * Tests full request/response cycle with database interactions and i18n validation.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AccountControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private AccountRepository accountRepository;

    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    private static final String TEST_IDEMPOTENCY_KEY = "test-idempotency-key-123";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        accountRepository.deleteAll();
    }

    @Test
    void testCreateAccount_Success() throws Exception {
        // Arrange
        AccountCreateRequestDTO request = AccountCreateRequestDTO.builder()
                .accountName("Test Account")
                .email("test@example.com")
                .countryCode("USA")
                .currencyCode("USD")
                .build();

        // Act & Assert
        mockMvc.perform(post(CommonConstants.API_V1_PREFIX + CommonConstants.ACCOUNT_API_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(CommonConstants.IDEMPOTENCY_KEY_HEADER, TEST_IDEMPOTENCY_KEY)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").exists())
                .andExpect(jsonPath("$.accountName").value("Test Account"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        // Verify in database
        assertEquals(1L, accountRepository.count());
    }

    @Test
    void testCreateAccount_ValidationError_MissingAccountName() throws Exception {
        // Arrange
        AccountCreateRequestDTO request = AccountCreateRequestDTO.builder()
                .accountName("")  // Invalid: empty
                .email("test@example.com")
                .build();

        // Act & Assert
        mockMvc.perform(post(CommonConstants.API_V1_PREFIX + CommonConstants.ACCOUNT_API_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("Validation failed")))
                .andExpect(jsonPath("$.errors.accountName").exists())
                .andExpect(jsonPath("$.errors.accountName").value(containsString("Account Name")));

        // Verify no account was created
        assertEquals(0L, accountRepository.count());
    }

    @Test
    void testCreateAccount_ValidationError_InvalidEmail() throws Exception {
        // Arrange
        AccountCreateRequestDTO request = AccountCreateRequestDTO.builder()
                .accountName("Test Account")
                .email("invalid-email")  // Invalid email
                .build();

        // Act & Assert
        mockMvc.perform(post(CommonConstants.API_V1_PREFIX + CommonConstants.ACCOUNT_API_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.email").value(containsString("Email")))
                .andExpect(jsonPath("$.errors.email").value(containsString("invalid-email")));
    }

    @Test
    void testCreateAccount_ValidationError_InvalidCountryCode() throws Exception {
        // Arrange
        AccountCreateRequestDTO request = AccountCreateRequestDTO.builder()
                .accountName("Test Account")
                .email("test@example.com")
                .countryCode("XX")  // Invalid country code
                .build();

        // Act & Assert
        mockMvc.perform(post(CommonConstants.API_V1_PREFIX + CommonConstants.ACCOUNT_API_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.countryCode").exists())
                .andExpect(jsonPath("$.errors.countryCode").value(containsString("Country Code")))
                .andExpect(jsonPath("$.errors.countryCode").value(containsString("XX")));
    }

    @Test
    void testCreateAccount_ValidationError_InvalidCurrencyCode() throws Exception {
        // Arrange
        AccountCreateRequestDTO request = AccountCreateRequestDTO.builder()
                .accountName("Test Account")
                .email("test@example.com")
                .currencyCode("INV")  // Invalid currency code (not in ISO 4217)
                .build();

        // Act & Assert
        mockMvc.perform(post(CommonConstants.API_V1_PREFIX + CommonConstants.ACCOUNT_API_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.currencyCode").exists())
                .andExpect(jsonPath("$.errors.currencyCode").value(containsString("Currency Code")))
                .andExpect(jsonPath("$.errors.currencyCode").value(containsString("INV")));
    }

    @Test
    void testCreateAccount_ValidationError_SizeExceeded() throws Exception {
        // Arrange
        AccountCreateRequestDTO request = AccountCreateRequestDTO.builder()
                .accountName("Test Account")
                .email("test@example.com")
                .website("https://www." + "a".repeat(300) + ".com")  // Exceeds 255 characters
                .build();

        // Act & Assert
        mockMvc.perform(post(CommonConstants.API_V1_PREFIX + CommonConstants.ACCOUNT_API_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.website").exists())
                .andExpect(jsonPath("$.errors.website").value(containsString("Website")));
    }

    @Test
    void testGetAccount_Success() throws Exception {
        // Arrange
        AccountEntity account = AccountEntity.builder()
                .accountId("ACC-000001")
                .accountName("Test Account")
                .email("test@example.com")
                .build();
        // Don't call initializeDefaults() manually - let @PrePersist handle it
        accountRepository.save(account);

        // Act & Assert
        mockMvc.perform(get(CommonConstants.API_V1_PREFIX + CommonConstants.ACCOUNT_API_ENDPOINT 
                        + "/ACC-000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value("ACC-000001"))
                .andExpect(jsonPath("$.accountName").value("Test Account"));
    }

    @Test
    void testGetAccount_NotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get(CommonConstants.API_V1_PREFIX + CommonConstants.ACCOUNT_API_ENDPOINT 
                        + "/ACC-NOTFOUND"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("Account not found")));
    }

    @Test
    void testGetAccounts_WithFilters() throws Exception {
        // Arrange
        AccountEntity account1 = AccountEntity.builder()
                .accountId("ACC-000001")
                .accountName("Test Account 1")
                .email("test1@example.com")
                .city("New York")
                .state("NY")
                .zipcode("10001")
                .build();
        // Don't call initializeDefaults() manually - let @PrePersist handle it
        accountRepository.save(account1);

        AccountEntity account2 = AccountEntity.builder()
                .accountId("ACC-000002")
                .accountName("Test Account 2")
                .email("test2@example.com")
                .city("Los Angeles")
                .state("CA")
                .zipcode("90001")
                .build();
        // Don't call initializeDefaults() manually - let @PrePersist handle it
        accountRepository.save(account2);

        // Act & Assert
        mockMvc.perform(get(CommonConstants.API_V1_PREFIX + CommonConstants.ACCOUNT_API_ENDPOINT)
                        .param("city", "New York")
                        .param("state", "NY")
                        .param("pageNumber", "1")
                        .param("pageSize", "25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageNumber").value(1))
                .andExpect(jsonPath("$.pageSize").value(25))
                .andExpect(jsonPath("$.totalItems").value(1))
                .andExpect(jsonPath("$.accounts[0].accountName").value("Test Account 1"));
    }

    @Test
    void testUpdateAccount_Success() throws Exception {
        // Arrange
        AccountEntity account = AccountEntity.builder()
                .accountId("ACC-000001")
                .accountName("Original Account")
                .email("original@example.com")
                .build();
        // Don't call initializeDefaults() manually - let @PrePersist handle it
        accountRepository.save(account);

        AccountUpdateRequestDTO request = AccountUpdateRequestDTO.builder()
                .accountName("Updated Account")
                .email("updated@example.com")
                .build();

        // Act & Assert
        mockMvc.perform(put(CommonConstants.API_V1_PREFIX + CommonConstants.ACCOUNT_API_ENDPOINT 
                        + "/ACC-000001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(CommonConstants.IDEMPOTENCY_KEY_HEADER, TEST_IDEMPOTENCY_KEY)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value("ACC-000001"))
                .andExpect(jsonPath("$.accountName").value("Updated Account"));
    }

    @Test
    void testDeleteAccount_Success() throws Exception {
        // Arrange
        AccountEntity account = AccountEntity.builder()
                .accountId("ACC-000001")
                .accountName("Test Account")
                .email("test@example.com")
                .build();
        // Don't call initializeDefaults() manually - let @PrePersist handle it
        accountRepository.save(account);

        // Act & Assert
        mockMvc.perform(delete(CommonConstants.API_V1_PREFIX + CommonConstants.ACCOUNT_API_ENDPOINT 
                        + "/ACC-000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value("ACC-000001"))
                .andExpect(jsonPath("$.deleted").value(true));

        // Verify deleted from database
        assertEquals(0L, accountRepository.count());
    }

    @Test
    void testCreateAccount_Idempotency() throws Exception {
        // Arrange
        AccountCreateRequestDTO request = AccountCreateRequestDTO.builder()
                .accountName("Test Account")
                .email("test@example.com")
                .build();

        // Act - First request
        String response1 = mockMvc.perform(post(CommonConstants.API_V1_PREFIX + CommonConstants.ACCOUNT_API_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(CommonConstants.IDEMPOTENCY_KEY_HEADER, TEST_IDEMPOTENCY_KEY)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Act - Second request with same idempotency key
        String response2 = mockMvc.perform(post(CommonConstants.API_V1_PREFIX + CommonConstants.ACCOUNT_API_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(CommonConstants.IDEMPOTENCY_KEY_HEADER, TEST_IDEMPOTENCY_KEY)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Assert - Responses should be identical
        assertEquals(response1, response2);
        
        // Verify only one account was created
        assertEquals(1L, accountRepository.count());
    }
}

