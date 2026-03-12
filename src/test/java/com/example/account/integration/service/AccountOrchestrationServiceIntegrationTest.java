package com.example.account.integration.service;

import com.example.account.dto.account.request.AccountCreateRequestDTO;
import com.example.account.dto.account.request.AccountListRequestDTO;
import com.example.account.dto.account.request.AccountUpdateRequestDTO;
import com.example.account.dto.account.response.*;
import com.example.account.exception.AccountNotFoundException;
import com.example.account.integration.BaseIntegrationTest;
import com.example.account.model.account.AccountEntity;
import com.example.account.repository.AccountRepository;
import com.example.account.service.account.IAccountOrchestrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for AccountOrchestrationService.
 * Tests all orchestration methods with real services and database interactions.
 * No mocks are used to ensure full integration testing.
 * 
 * Target: 90% branch, method, and line coverage.
 */
@Transactional
class AccountOrchestrationServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private IAccountOrchestrationService accountOrchestrationService;

    @Autowired
    private AccountRepository accountRepository;

    private static final String TEST_ACCOUNT_NAME = "Test Account";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_COUNTRY_CODE = "USA";
    private static final String TEST_CURRENCY_CODE = "USD";
    private static final String TEST_WEBSITE = "https://www.test.com";
    private static final String TEST_COUNTRY = "United States";
    private static final String TEST_ADDRESS_LINE1 = "123 Main St";
    private static final String TEST_ADDRESS_LINE2 = "Suite 100";
    private static final String TEST_CITY = "New York";
    private static final String TEST_STATE = "NY";
    private static final String TEST_ZIPCODE = "10001";
    private static final String TEST_STATUS = "ACTIVE";

    private String createdAccountId;

    @BeforeEach
    void setUp() {
        // Clean up database before each test
        accountRepository.deleteAll();
    }

    // ========== CREATE ACCOUNT TESTS ==========

    @Test
    void testCreateAccount_Success() {
        // Arrange
        AccountCreateRequestDTO request = AccountCreateRequestDTO.builder()
                .accountName(TEST_ACCOUNT_NAME)
                .email(TEST_EMAIL)
                .countryCode(TEST_COUNTRY_CODE)
                .currencyCode(TEST_CURRENCY_CODE)
                .website(TEST_WEBSITE)
                .country(TEST_COUNTRY)
                .addressLine1(TEST_ADDRESS_LINE1)
                .addressLine2(TEST_ADDRESS_LINE2)
                .city(TEST_CITY)
                .state(TEST_STATE)
                .zipcode(TEST_ZIPCODE)
                .build();

        // Act
        AccountCreateResponseDTO response = accountOrchestrationService.createAccount(request);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getAccountId(), "Account reference ID should be generated");
        assertTrue(response.getAccountId().startsWith("ACC-"), 
                "Account reference ID should start with 'ACC-'");
        assertEquals(TEST_ACCOUNT_NAME, response.getAccountName(), 
                "Account name should match");
        assertNotNull(response.getStatus(), "Status should be set");
        assertEquals("PENDING", response.getStatus(), "Initial status should be PENDING");

        // Verify account was saved in database
        AccountEntity savedAccount = accountRepository.findByAccountId(response.getAccountId())
                .orElseThrow(() -> new AssertionError("Account should be saved in database"));
        assertEquals(TEST_ACCOUNT_NAME, savedAccount.getAccountName());
        assertEquals(TEST_EMAIL, savedAccount.getEmail());
        assertEquals(TEST_COUNTRY_CODE, savedAccount.getCountryCode());
        assertEquals(TEST_CURRENCY_CODE, savedAccount.getCurrency());
    }

    @Test
    void testCreateAccount_MinimalFields() {
        // Arrange - Only required fields
        AccountCreateRequestDTO request = AccountCreateRequestDTO.builder()
                .accountName(TEST_ACCOUNT_NAME)
                .email(TEST_EMAIL)
                .build();

        // Act
        AccountCreateResponseDTO response = accountOrchestrationService.createAccount(request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getAccountId());
        assertEquals(TEST_ACCOUNT_NAME, response.getAccountName());
    }

    @Test
    void testCreateAccount_WithNullOptionalFields() {
        // Arrange - Explicitly set optional fields to null
        AccountCreateRequestDTO request = AccountCreateRequestDTO.builder()
                .accountName(TEST_ACCOUNT_NAME)
                .email(TEST_EMAIL)
                .countryCode(null)
                .currencyCode(null)
                .website(null)
                .country(null)
                .addressLine1(null)
                .addressLine2(null)
                .city(null)
                .state(null)
                .zipcode(null)
                .build();

        // Act
        AccountCreateResponseDTO response = accountOrchestrationService.createAccount(request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getAccountId());
        
        // Verify optional fields are null in database
        AccountEntity savedAccount = accountRepository.findByAccountId(response.getAccountId())
                .orElseThrow();
        assertNull(savedAccount.getCountryCode());
        assertNull(savedAccount.getCurrency());
        assertNull(savedAccount.getWebsite());
    }

    // ========== GET ACCOUNT TESTS ==========

    @Test
    void testGetAccount_Success() {
        // Arrange - Create account first
        AccountEntity account = createTestAccount();
        createdAccountId = account.getAccountId();

        // Act
        AccountDetailsResponseDTO response = accountOrchestrationService.getAccount(createdAccountId);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(createdAccountId, response.getAccountId(), 
                "Account reference ID should match");
        assertEquals(TEST_ACCOUNT_NAME, response.getAccountName(), 
                "Account name should match");
        assertEquals(TEST_EMAIL, response.getEmail(), 
                "Email should match");
        assertEquals(TEST_COUNTRY_CODE, response.getCountryCode(), 
                "Country code should match");
        assertEquals(TEST_CURRENCY_CODE, response.getCurrency(), 
                "Currency should match");
    }

    @Test
    void testGetAccount_NotFound() {
        // Arrange
        String nonExistentId = "ACC-NONEXISTENT";

        // Act & Assert
        AccountNotFoundException exception = assertThrows(
                AccountNotFoundException.class,
                () -> accountOrchestrationService.getAccount(nonExistentId),
                "Should throw AccountNotFoundException for non-existent account"
        );
        assertTrue(exception.getMessage().contains(nonExistentId) || 
                   exception.getMessage().contains("Account"),
                "Exception message should contain account ID or 'Account'");
    }

    // ========== GET ACCOUNTS (LIST) TESTS ==========

    @Test
    void testGetAccounts_EmptyList() {
        // Arrange - No accounts in database
        AccountListRequestDTO request = AccountListRequestDTO.builder()
                .pageNumber(1)
                .pageSize(25)
                .build();

        // Act
        AccountListResponseDTO response = accountOrchestrationService.getAccounts(request);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getAccounts(), "Accounts list should not be null");
        assertEquals(0, response.getAccounts().size(), "Accounts list should be empty");
        assertEquals(1, response.getPageNumber(), "Page number should be 1");
        assertEquals(25, response.getPageSize(), "Page size should be 25");
        assertEquals(0L, response.getTotalItems(), "Total items should be 0");
        assertEquals(0, response.getTotalPages(), "Total pages should be 0");
    }

    @Test
    void testGetAccounts_WithPagination() {
        // Arrange - Create multiple accounts
        createTestAccount("Account 1", "email1@test.com", "USA", "USD");
        createTestAccount("Account 2", "email2@test.com", "CAN", "CAD");
        createTestAccount("Account 3", "email3@test.com", "GBR", "GBP");

        AccountListRequestDTO request = AccountListRequestDTO.builder()
                .pageNumber(1)
                .pageSize(2)
                .build();

        // Act
        AccountListResponseDTO response = accountOrchestrationService.getAccounts(request);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.getAccounts().size(), "Should return 2 accounts per page");
        assertEquals(1, response.getPageNumber(), "Page number should be 1");
        assertEquals(2, response.getPageSize(), "Page size should be 2");
        assertEquals(3L, response.getTotalItems(), "Total items should be 3");
        assertEquals(2, response.getTotalPages(), "Total pages should be 2");
    }

    @Test
    void testGetAccounts_WithFilters_AccountName() {
        // Arrange
        createTestAccount("Alpha Account", "alpha@test.com", "USA", "USD");
        createTestAccount("Beta Account", "beta@test.com", "CAN", "CAD");
        createTestAccount("Gamma Account", "gamma@test.com", "GBR", "GBP");

        AccountListRequestDTO request = AccountListRequestDTO.builder()
                .accountName("Alpha")
                .pageNumber(1)
                .pageSize(25)
                .build();

        // Act
        AccountListResponseDTO response = accountOrchestrationService.getAccounts(request);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getAccounts().size(), "Should return 1 matching account");
        assertTrue(response.getAccounts().get(0).getAccountName().contains("Alpha"),
                "Account name should contain 'Alpha'");
    }

    @Test
    void testGetAccounts_WithFilters_CountryCode() {
        // Arrange
        createTestAccount("Account 1", "email1@test.com", "USA", "USD");
        createTestAccount("Account 2", "email2@test.com", "CAN", "CAD");
        createTestAccount("Account 3", "email3@test.com", "USA", "USD");

        AccountListRequestDTO request = AccountListRequestDTO.builder()
                .countryCode("USA")
                .pageNumber(1)
                .pageSize(25)
                .build();

        // Act
        AccountListResponseDTO response = accountOrchestrationService.getAccounts(request);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.getAccounts().size(), "Should return 2 accounts with USA country code");
        response.getAccounts().forEach(account -> 
                assertEquals("USA", account.getCountryCode(), 
                        "All accounts should have USA country code")
        );
    }

    @Test
    void testGetAccounts_WithFilters_Currency() {
        // Arrange
        createTestAccount("Account 1", "email1@test.com", "USA", "USD");
        createTestAccount("Account 2", "email2@test.com", "CAN", "CAD");
        createTestAccount("Account 3", "email3@test.com", "GBR", "GBP");
        createTestAccount("Account 4", "email4@test.com", "USA", "USD");

        AccountListRequestDTO request = AccountListRequestDTO.builder()
                .currency("USD")
                .pageNumber(1)
                .pageSize(25)
                .build();

        // Act
        AccountListResponseDTO response = accountOrchestrationService.getAccounts(request);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.getAccounts().size(), "Should return 2 accounts with USD currency");
        response.getAccounts().forEach(account -> 
                assertEquals("USD", account.getCurrency(), 
                        "All accounts should have USD currency")
        );
    }

    @Test
    void testGetAccounts_WithFilters_City() {
        // Arrange
        AccountEntity account1 = createTestAccount("Account 1", "email1@test.com", "USA", "USD");
        account1.setCity("New York");
        accountRepository.save(account1);

        AccountEntity account2 = createTestAccount("Account 2", "email2@test.com", "CAN", "CAD");
        account2.setCity("Toronto");
        accountRepository.save(account2);

        AccountEntity account3 = createTestAccount("Account 3", "email3@test.com", "USA", "USD");
        account3.setCity("New York");
        accountRepository.save(account3);

        AccountListRequestDTO request = AccountListRequestDTO.builder()
                .city("New York")
                .pageNumber(1)
                .pageSize(25)
                .build();

        // Act
        AccountListResponseDTO response = accountOrchestrationService.getAccounts(request);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.getAccounts().size(), "Should return 2 accounts in New York");
        response.getAccounts().forEach(account -> 
                assertTrue(account.getCity().contains("New York"),
                        "All accounts should be in New York")
        );
    }

    @Test
    void testGetAccounts_WithFilters_MultipleFilters() {
        // Arrange
        AccountEntity account1 = createTestAccount("Account 1", "email1@test.com", "USA", "USD");
        account1.setCity("New York");
        account1.setState("NY");
        accountRepository.save(account1);

        AccountEntity account2 = createTestAccount("Account 2", "email2@test.com", "USA", "USD");
        account2.setCity("Los Angeles");
        account2.setState("CA");
        accountRepository.save(account2);

        AccountListRequestDTO request = AccountListRequestDTO.builder()
                .countryCode("USA")
                .currency("USD")
                .city("New York")
                .state("NY")
                .pageNumber(1)
                .pageSize(25)
                .build();

        // Act
        AccountListResponseDTO response = accountOrchestrationService.getAccounts(request);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getAccounts().size(), 
                "Should return 1 account matching all filters");
        AccountDetailsResponseDTO account = response.getAccounts().get(0);
        assertEquals("USA", account.getCountryCode());
        assertEquals("USD", account.getCurrency());
        assertTrue(account.getCity().contains("New York"));
        assertEquals("NY", account.getState());
    }

    @Test
    void testGetAccounts_DefaultPagination() {
        // Arrange - Create accounts
        createTestAccount("Account 1", "email1@test.com", "USA", "USD");
        createTestAccount("Account 2", "email2@test.com", "CAN", "CAD");

        AccountListRequestDTO request = AccountListRequestDTO.builder()
                .build(); // No pagination specified

        // Act
        AccountListResponseDTO response = accountOrchestrationService.getAccounts(request);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getPageNumber(), "Default page number should be 1");
        assertEquals(25, response.getPageSize(), "Default page size should be 25");
    }

    @Test
    void testGetAccounts_InvalidPageNumber() {
        // Arrange
        createTestAccount("Account 1", "email1@test.com", "USA", "USD");

        AccountListRequestDTO request = AccountListRequestDTO.builder()
                .pageNumber(0) // Invalid - should default to 0 internally
                .pageSize(25)
                .build();

        // Act
        AccountListResponseDTO response = accountOrchestrationService.getAccounts(request);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getPageNumber(), "Page number should be adjusted to 1");
    }

    @Test
    void testGetAccounts_InvalidPageSize() {
        // Arrange
        createTestAccount("Account 1", "email1@test.com", "USA", "USD");

        AccountListRequestDTO request = AccountListRequestDTO.builder()
                .pageNumber(1)
                .pageSize(0) // Invalid - should default to 25
                .build();

        // Act
        AccountListResponseDTO response = accountOrchestrationService.getAccounts(request);

        // Assert
        assertNotNull(response);
        assertEquals(25, response.getPageSize(), "Page size should default to 25");
    }

    // ========== UPDATE ACCOUNT TESTS ==========

    @Test
    void testUpdateAccount_Success() {
        // Arrange - Create account first
        AccountEntity account = createTestAccount();
        createdAccountId = account.getAccountId();

        AccountUpdateRequestDTO request = AccountUpdateRequestDTO.builder()
                .accountName("Updated Account Name")
                .email("updated@example.com")
                .countryCode("CAN")
                .currencyCode("CAD")
                .website("https://www.updated.com")
                .country("Canada")
                .addressLine1("456 Updated St")
                .addressLine2("Suite 200")
                .city("Toronto")
                .state("ON")
                .zipcode("M5H 2N2")
                .status("ACTIVE")
                .build();

        // Act
        AccountUpdateResponseDTO response = accountOrchestrationService.updateAccount(createdAccountId, request);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(createdAccountId, response.getAccountId(), 
                "Account reference ID should match");
        assertEquals("Updated Account Name", response.getAccountName(), 
                "Account name should be updated");
        assertEquals("ACTIVE", response.getStatus(), "Status should be updated");

        // Verify account was updated in database
        AccountEntity updatedAccount = accountRepository.findByAccountId(createdAccountId)
                .orElseThrow();
        assertEquals("Updated Account Name", updatedAccount.getAccountName());
        assertEquals("updated@example.com", updatedAccount.getEmail());
        assertEquals("CAN", updatedAccount.getCountryCode());
        assertEquals("CAD", updatedAccount.getCurrency());
        assertEquals("ACTIVE", updatedAccount.getStatus());
    }

    @Test
    void testUpdateAccount_NotFound() {
        // Arrange
        String nonExistentId = "ACC-NONEXISTENT";
        AccountUpdateRequestDTO request = AccountUpdateRequestDTO.builder()
                .accountName("Updated Name")
                .build();

        // Act & Assert
        assertThrows(
                AccountNotFoundException.class,
                () -> accountOrchestrationService.updateAccount(nonExistentId, request),
                "Should throw AccountNotFoundException for non-existent account"
        );
    }

    @Test
    void testUpdateAccount_PartialFields() {
        // Arrange - Create account first
        AccountEntity account = createTestAccount();
        createdAccountId = account.getAccountId();

        // Update only some fields
        AccountUpdateRequestDTO request = AccountUpdateRequestDTO.builder()
                .accountName("Partially Updated Name")
                .email("partial@example.com")
                // Other fields are null/blank - should keep existing values
                .build();

        // Act
        AccountUpdateResponseDTO response = accountOrchestrationService.updateAccount(createdAccountId, request);

        // Assert
        assertNotNull(response);
        assertEquals("Partially Updated Name", response.getAccountName());

        // Verify other fields remain unchanged
        AccountEntity updatedAccount = accountRepository.findByAccountId(createdAccountId)
                .orElseThrow();
        assertEquals("Partially Updated Name", updatedAccount.getAccountName());
        assertEquals("partial@example.com", updatedAccount.getEmail());
        // Country code should remain unchanged (from original)
        assertEquals(TEST_COUNTRY_CODE, updatedAccount.getCountryCode());
    }

    // ========== PATCH ACCOUNT TESTS ==========

    @Test
    void testPatchAccount_Success() {
        // Arrange - Create account first
        AccountEntity account = createTestAccount();
        createdAccountId = account.getAccountId();

        // Patch only specific fields
        AccountUpdateRequestDTO request = AccountUpdateRequestDTO.builder()
                .accountName("Patched Account Name")
                .status("ACTIVE")
                // Other fields are null - should not be updated
                .build();

        // Act
        AccountUpdateResponseDTO response = accountOrchestrationService.patchAccount(createdAccountId, request);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(createdAccountId, response.getAccountId(), 
                "Account reference ID should match");
        assertEquals("Patched Account Name", response.getAccountName(), 
                "Account name should be patched");
        assertEquals("ACTIVE", response.getStatus(), "Status should be patched");

        // Verify only specified fields were updated
        AccountEntity patchedAccount = accountRepository.findByAccountId(createdAccountId)
                .orElseThrow();
        assertEquals("Patched Account Name", patchedAccount.getAccountName());
        assertEquals("ACTIVE", patchedAccount.getStatus());
        // Email should remain unchanged
        assertEquals(TEST_EMAIL, patchedAccount.getEmail());
        // Country code should remain unchanged
        assertEquals(TEST_COUNTRY_CODE, patchedAccount.getCountryCode());
    }

    @Test
    void testPatchAccount_NotFound() {
        // Arrange
        String nonExistentId = "ACC-NONEXISTENT";
        AccountUpdateRequestDTO request = AccountUpdateRequestDTO.builder()
                .accountName("Patched Name")
                .build();

        // Act & Assert
        assertThrows(
                AccountNotFoundException.class,
                () -> accountOrchestrationService.patchAccount(nonExistentId, request),
                "Should throw AccountNotFoundException for non-existent account"
        );
    }

    @Test
    void testPatchAccount_OnlyEmail() {
        // Arrange - Create account first
        AccountEntity account = createTestAccount();
        createdAccountId = account.getAccountId();

        // Patch only email
        AccountUpdateRequestDTO request = AccountUpdateRequestDTO.builder()
                .email("patched@example.com")
                .build();

        // Act
        AccountUpdateResponseDTO response = accountOrchestrationService.patchAccount(createdAccountId, request);

        // Assert
        assertNotNull(response);
        
        // Verify only email was updated
        AccountEntity patchedAccount = accountRepository.findByAccountId(createdAccountId)
                .orElseThrow();
        assertEquals("patched@example.com", patchedAccount.getEmail());
        // Account name should remain unchanged
        assertEquals(TEST_ACCOUNT_NAME, patchedAccount.getAccountName());
    }

    @Test
    void testPatchAccount_AllFields() {
        // Arrange - Create account first
        AccountEntity account = createTestAccount();
        createdAccountId = account.getAccountId();

        // Patch all fields
        AccountUpdateRequestDTO request = AccountUpdateRequestDTO.builder()
                .accountName("Fully Patched Name")
                .email("fullypatched@example.com")
                .countryCode("GBR")
                .currencyCode("GBP")
                .website("https://www.patched.com")
                .country("United Kingdom")
                .addressLine1("789 Patched St")
                .addressLine2("Suite 300")
                .city("London")
                .state("ENG")
                .zipcode("SW1A 1AA")
                .status("INACTIVE")
                .build();

        // Act
        AccountUpdateResponseDTO response = accountOrchestrationService.patchAccount(createdAccountId, request);

        // Assert
        assertNotNull(response);
        assertEquals("Fully Patched Name", response.getAccountName());
        assertEquals("INACTIVE", response.getStatus());

        // Verify all fields were updated
        AccountEntity patchedAccount = accountRepository.findByAccountId(createdAccountId)
                .orElseThrow();
        assertEquals("Fully Patched Name", patchedAccount.getAccountName());
        assertEquals("fullypatched@example.com", patchedAccount.getEmail());
        assertEquals("GBR", patchedAccount.getCountryCode());
        assertEquals("GBP", patchedAccount.getCurrency());
        assertEquals("INACTIVE", patchedAccount.getStatus());
    }

    @Test
    void testPatchAccount_EmptyRequest() {
        // Arrange - Create account first
        AccountEntity account = createTestAccount();
        createdAccountId = account.getAccountId();
        String originalName = account.getAccountName();
        String originalEmail = account.getEmail();

        // Patch with empty request (all fields null/blank)
        AccountUpdateRequestDTO request = AccountUpdateRequestDTO.builder()
                .build();

        // Act
        AccountUpdateResponseDTO response = accountOrchestrationService.patchAccount(createdAccountId, request);

        // Assert
        assertNotNull(response);
        
        // Verify no fields were changed
        AccountEntity patchedAccount = accountRepository.findByAccountId(createdAccountId)
                .orElseThrow();
        assertEquals(originalName, patchedAccount.getAccountName());
        assertEquals(originalEmail, patchedAccount.getEmail());
    }

    // ========== DELETE ACCOUNT TESTS ==========

    @Test
    void testDeleteAccount_Success() {
        // Arrange - Create account first
        AccountEntity account = createTestAccount();
        createdAccountId = account.getAccountId();

        // Act
        AccountDeleteResponseDTO response = accountOrchestrationService.deleteAccount(createdAccountId);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(createdAccountId, response.getAccountId(), 
                "Account reference ID should match");
        assertTrue(response.isDeleted(), "Deleted flag should be true");
        assertNotNull(response.getMessage(), "Message should not be null");
        assertTrue(response.getMessage().contains("deleted") || 
                   response.getMessage().contains("success"),
                "Message should indicate successful deletion");

        // Verify account was deleted from database
        assertFalse(accountRepository.findByAccountId(createdAccountId).isPresent(),
                "Account should not exist in database after deletion");
    }

    @Test
    void testDeleteAccount_NotFound() {
        // Arrange
        String nonExistentId = "ACC-NONEXISTENT";

        // Act & Assert
        assertThrows(
                AccountNotFoundException.class,
                () -> accountOrchestrationService.deleteAccount(nonExistentId),
                "Should throw AccountNotFoundException for non-existent account"
        );
    }

    // ========== HELPER METHODS ==========

    /**
     * Creates a test account with default values.
     * 
     * @return Created AccountEntity
     */
    private AccountEntity createTestAccount() {
        return createTestAccount(TEST_ACCOUNT_NAME, TEST_EMAIL, TEST_COUNTRY_CODE, TEST_CURRENCY_CODE);
    }

    /**
     * Creates a test account with specified values.
     * 
     * @param accountName Account name
     * @param email Email
     * @param countryCode Country code
     * @param currencyCode Currency code
     * @return Created AccountEntity
     */
    private AccountEntity createTestAccount(String accountName, String email, String countryCode, String currencyCode) {
        AccountEntity account = AccountEntity.builder()
                .accountId(com.example.account.util.AccountUtil.generateAccountId())
                .accountName(accountName)
                .email(email)
                .countryCode(countryCode)
                .currency(currencyCode)
                .website(TEST_WEBSITE)
                .country(TEST_COUNTRY)
                .addressLine1(TEST_ADDRESS_LINE1)
                .addressLine2(TEST_ADDRESS_LINE2)
                .city(TEST_CITY)
                .state(TEST_STATE)
                .zipcode(TEST_ZIPCODE)
                .status(TEST_STATUS)
                .build();
        
        account.initializeDefaults();
        return accountRepository.save(account);
    }
}

