package com.example.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.account.constants.CommonConstants;
import com.example.account.dto.account.request.AccountCreateRequestDTO;
import com.example.account.dto.account.request.AccountListRequestDTO;
import com.example.account.dto.account.request.AccountUpdateRequestDTO;
import com.example.account.dto.account.response.*;
import com.example.account.exception.AccountNotFoundException;
import com.example.account.i18n.Translator;
import com.example.account.service.IdempotencyService;
import com.example.account.service.account.IAccountOrchestrationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import jakarta.servlet.http.HttpServletRequest;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AccountController.
 * Tests i18n validation error message assertions.
 */
@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    private static final String TEST_ACCOUNT_ID = "ACC-000001";
    private static final String TEST_IDEMPOTENCY_KEY = "test-idempotency-key-123";

    @Mock
    private IAccountOrchestrationService accountService;

    @Mock
    private IdempotencyService idempotencyService;

    @InjectMocks
    private AccountController accountController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private MockedStatic<Translator> mockedTranslator;

    @BeforeEach
    void setUp() {
        mockedTranslator = mockStatic(Translator.class);
        // Mock Translator to return actual messages for validation errors
        mockedTranslator.when(() -> Translator.getMessage(eq("error.validation.failed")))
                .thenReturn("Validation failed");
        mockedTranslator.when(() -> Translator.getMessage(eq("error.account.notFound"), any(Object[].class)))
                .thenAnswer(invocation -> "Account not found with ID: " + ((Object[])invocation.getArgument(1))[0]);
        mockedTranslator.when(() -> Translator.resolveMessage(any(org.springframework.context.MessageSourceResolvable.class)))
                .thenAnswer(invocation -> {
                    org.springframework.context.MessageSourceResolvable resolvable = invocation.getArgument(0);
                    String[] codes = resolvable.getCodes();
                    if (codes != null && codes.length > 0) {
                        // Check all codes, not just the first one
                        for (String code : codes) {
                            // Return a mock message based on the code
                            if (code.contains("NotBlank") || (code.contains("accountName") && code.contains("NotBlank"))) {
                                return "Account Name is required";
                            } else if (code.contains("Email") || (code.contains("email") && code.contains("Email"))) {
                                Object[] args = resolvable.getArguments();
                                String rejectedValue = args != null && args.length > 0 ? String.valueOf(args[0]) : "invalid-email";
                                return "Email " + rejectedValue + " must be valid";
                            } else if (code.contains("ValidCountryCode") || (code.contains("countryCode") && code.contains("ValidCountryCode"))) {
                                Object[] args = resolvable.getArguments();
                                String rejectedValue = args != null && args.length > 0 ? String.valueOf(args[0]) : "XX";
                                return "Country Code " + rejectedValue + " must be a valid 3-letter ISO 3166-1 alpha-3 code (e.g., USA, CAN, GBR)";
                            } else if (code.contains("Size") && code.contains("website")) {
                                return "Website must not exceed 255 characters";
                            } else if (code.contains("Min") && code.contains("pageNumber")) {
                                return "Page number 0 must be at least 1";
                            }
                        }
                    }
                    // Fallback: check default message
                    String defaultMsg = resolvable.getDefaultMessage();
                    if (defaultMsg != null && defaultMsg.contains("Account Name")) {
                        return "Account Name is required";
                    }
                    return "Validation error";
                });

        mockMvc = MockMvcBuilders.standaloneSetup(accountController)
                .setControllerAdvice(new com.example.account.exception.GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() {
        if (mockedTranslator != null) {
            mockedTranslator.close();
        }
    }

    @Test
    void testCreateAccount_Success() throws Exception {
        // Arrange
        AccountCreateRequestDTO request = createValidAccountCreateRequest();
        AccountCreateResponseDTO response = AccountCreateResponseDTO.builder()
                .accountId(TEST_ACCOUNT_ID)
                .accountName("Test Account")
                .status("PENDING")
                .build();

        when(idempotencyService.getCachedResponse(anyString(), any(HttpMethod.class), 
                anyString(), eq(AccountCreateResponseDTO.class))).thenReturn(null);
        when(accountService.createAccount(any(AccountCreateRequestDTO.class))).thenReturn(response);
        doNothing().when(idempotencyService).storeResponse(anyString(), any(HttpMethod.class), 
                anyString(), any(ResponseEntity.class));

        // Act & Assert
        mockMvc.perform(post(CommonConstants.API_V1_PREFIX + CommonConstants.ACCOUNT_API_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(CommonConstants.IDEMPOTENCY_KEY_HEADER, TEST_IDEMPOTENCY_KEY)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").value(TEST_ACCOUNT_ID))
                .andExpect(jsonPath("$.accountName").value("Test Account"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(accountService).createAccount(any(AccountCreateRequestDTO.class));
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
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.accountName").exists())
                .andExpect(jsonPath("$.errors.accountName").value(org.hamcrest.Matchers.containsString("Account Name")));

        verify(accountService, never()).createAccount(any(AccountCreateRequestDTO.class));
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
                .andExpect(jsonPath("$.errors.email").value(org.hamcrest.Matchers.containsString("Email")));

        verify(accountService, never()).createAccount(any(AccountCreateRequestDTO.class));
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
                .andExpect(jsonPath("$.errors.countryCode").value(org.hamcrest.Matchers.containsString("Country Code")));

        verify(accountService, never()).createAccount(any(AccountCreateRequestDTO.class));
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
                .andExpect(jsonPath("$.errors.website").value(org.hamcrest.Matchers.containsString("Website")));

        verify(accountService, never()).createAccount(any(AccountCreateRequestDTO.class));
    }

    @Test
    void testCreateAccount_Idempotency_CachedResponse() throws Exception {
        // Arrange
        AccountCreateRequestDTO request = createValidAccountCreateRequest();
        AccountCreateResponseDTO cachedResponse = AccountCreateResponseDTO.builder()
                .accountId(TEST_ACCOUNT_ID)
                .accountName("Test Account")
                .status("PENDING")
                .build();

        ResponseEntity<AccountCreateResponseDTO> cachedEntity = ResponseEntity.status(HttpStatus.CREATED)
                .body(cachedResponse);

        when(idempotencyService.getCachedResponse(eq(TEST_IDEMPOTENCY_KEY), eq(HttpMethod.POST), 
                anyString(), eq(AccountCreateResponseDTO.class))).thenReturn(cachedEntity);

        // Act & Assert
        mockMvc.perform(post(CommonConstants.API_V1_PREFIX + CommonConstants.ACCOUNT_API_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(CommonConstants.IDEMPOTENCY_KEY_HEADER, TEST_IDEMPOTENCY_KEY)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").value(TEST_ACCOUNT_ID));

        verify(accountService, never()).createAccount(any(AccountCreateRequestDTO.class));
    }

    @Test
    void testGetAccount_Success() throws Exception {
        // Arrange
        AccountDetailsResponseDTO response = AccountDetailsResponseDTO.builder()
                .accountId(TEST_ACCOUNT_ID)
                .accountName("Test Account")
                .email("test@example.com")
                .build();

        when(accountService.getAccount(TEST_ACCOUNT_ID)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get(CommonConstants.API_V1_PREFIX + CommonConstants.ACCOUNT_API_ENDPOINT 
                        + "/" + TEST_ACCOUNT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(TEST_ACCOUNT_ID))
                .andExpect(jsonPath("$.accountName").value("Test Account"));

        verify(accountService).getAccount(TEST_ACCOUNT_ID);
    }

    @Test
    void testGetAccount_NotFound() throws Exception {
        // Arrange
        when(accountService.getAccount(TEST_ACCOUNT_ID))
                .thenThrow(new AccountNotFoundException(TEST_ACCOUNT_ID));

        mockedTranslator.when(() -> Translator.getMessage(eq("error.account.notFound"), any(Object[].class)))
                .thenReturn("Account not found with ID: " + TEST_ACCOUNT_ID);

        // Act & Assert
        mockMvc.perform(get(CommonConstants.API_V1_PREFIX + CommonConstants.ACCOUNT_API_ENDPOINT 
                        + "/" + TEST_ACCOUNT_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Account not found")));

        verify(accountService).getAccount(TEST_ACCOUNT_ID);
    }

    @Test
    void testGetAccounts_WithFilters() throws Exception {
        // Arrange
        AccountListResponseDTO response = AccountListResponseDTO.builder()
                .accounts(java.util.Collections.emptyList())
                .pageNumber(1)
                .pageSize(25)
                .totalItems(0L)
                .totalPages(0)
                .build();

        when(accountService.getAccounts(any(AccountListRequestDTO.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get(CommonConstants.API_V1_PREFIX + CommonConstants.ACCOUNT_API_ENDPOINT)
                        .param("accountName", "Test")
                        .param("pageNumber", "1")
                        .param("pageSize", "25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageNumber").value(1))
                .andExpect(jsonPath("$.pageSize").value(25));

        verify(accountService).getAccounts(any(AccountListRequestDTO.class));
    }

    @Test
    void testGetAccounts_ValidationError_InvalidPageNumber() throws Exception {
        // Arrange
        // Act & Assert
        mockMvc.perform(get(CommonConstants.API_V1_PREFIX + CommonConstants.ACCOUNT_API_ENDPOINT)
                        .param("pageNumber", "0")  // Invalid: must be >= 1
                        .param("pageSize", "25"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.pageNumber").exists())
                .andExpect(jsonPath("$.errors.pageNumber").value(org.hamcrest.Matchers.containsString("Page number")));

        verify(accountService, never()).getAccounts(any(AccountListRequestDTO.class));
    }

    @Test
    void testUpdateAccount_Success() throws Exception {
        // Arrange
        AccountUpdateRequestDTO request = AccountUpdateRequestDTO.builder()
                .accountName("Updated Account")
                .email("updated@example.com")
                .build();

        AccountUpdateResponseDTO response = AccountUpdateResponseDTO.builder()
                .accountId(TEST_ACCOUNT_ID)
                .accountName("Updated Account")
                .status("ACTIVE")
                .build();

        when(idempotencyService.getCachedResponse(anyString(), any(HttpMethod.class), 
                anyString(), eq(AccountUpdateResponseDTO.class))).thenReturn(null);
        when(accountService.updateAccount(eq(TEST_ACCOUNT_ID), any(AccountUpdateRequestDTO.class)))
                .thenReturn(response);
        doNothing().when(idempotencyService).storeResponse(anyString(), any(HttpMethod.class), 
                anyString(), any(ResponseEntity.class));

        // Act & Assert
        mockMvc.perform(put(CommonConstants.API_V1_PREFIX + CommonConstants.ACCOUNT_API_ENDPOINT 
                        + "/" + TEST_ACCOUNT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(CommonConstants.IDEMPOTENCY_KEY_HEADER, TEST_IDEMPOTENCY_KEY)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(TEST_ACCOUNT_ID))
                .andExpect(jsonPath("$.accountName").value("Updated Account"));

        verify(accountService).updateAccount(eq(TEST_ACCOUNT_ID), any(AccountUpdateRequestDTO.class));
    }

    @Test
    void testDeleteAccount_Success() throws Exception {
        // Arrange
        AccountDeleteResponseDTO response = AccountDeleteResponseDTO.builder()
                .accountId(TEST_ACCOUNT_ID)
                .message("Account deleted successfully")
                .deleted(true)
                .build();

        when(accountService.deleteAccount(TEST_ACCOUNT_ID)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(delete(CommonConstants.API_V1_PREFIX + CommonConstants.ACCOUNT_API_ENDPOINT 
                        + "/" + TEST_ACCOUNT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(TEST_ACCOUNT_ID))
                .andExpect(jsonPath("$.deleted").value(true));

        verify(accountService).deleteAccount(TEST_ACCOUNT_ID);
    }

    @Test
    void testPatchAccount_Success() throws Exception {
        // Arrange
        AccountUpdateRequestDTO request = AccountUpdateRequestDTO.builder()
                .accountName("Patched Account")
                .email("patched@example.com")
                .build();

        AccountUpdateResponseDTO response = AccountUpdateResponseDTO.builder()
                .accountId(TEST_ACCOUNT_ID)
                .accountName("Patched Account")
                .status("ACTIVE")
                .build();

        when(idempotencyService.getCachedResponse(anyString(), any(HttpMethod.class), 
                anyString(), eq(AccountUpdateResponseDTO.class))).thenReturn(null);
        when(accountService.patchAccount(eq(TEST_ACCOUNT_ID), any(AccountUpdateRequestDTO.class)))
                .thenReturn(response);
        doNothing().when(idempotencyService).storeResponse(anyString(), any(HttpMethod.class), 
                anyString(), any(ResponseEntity.class));

        // Act & Assert
        mockMvc.perform(patch(CommonConstants.API_V1_PREFIX + CommonConstants.ACCOUNT_API_ENDPOINT 
                        + "/" + TEST_ACCOUNT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(CommonConstants.IDEMPOTENCY_KEY_HEADER, TEST_IDEMPOTENCY_KEY)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(TEST_ACCOUNT_ID))
                .andExpect(jsonPath("$.accountName").value("Patched Account"));

        verify(accountService).patchAccount(eq(TEST_ACCOUNT_ID), any(AccountUpdateRequestDTO.class));
    }

    @Test
    void testPatchAccount_Idempotency_CachedResponse() throws Exception {
        // Arrange
        AccountUpdateRequestDTO request = AccountUpdateRequestDTO.builder()
                .accountName("Patched Account")
                .build();

        AccountUpdateResponseDTO cachedResponse = AccountUpdateResponseDTO.builder()
                .accountId(TEST_ACCOUNT_ID)
                .accountName("Patched Account")
                .status("ACTIVE")
                .build();

        ResponseEntity<AccountUpdateResponseDTO> cachedEntity = ResponseEntity.ok(cachedResponse);

        when(idempotencyService.getCachedResponse(eq(TEST_IDEMPOTENCY_KEY), eq(HttpMethod.PATCH), 
                anyString(), eq(AccountUpdateResponseDTO.class))).thenReturn(cachedEntity);

        // Act & Assert
        mockMvc.perform(patch(CommonConstants.API_V1_PREFIX + CommonConstants.ACCOUNT_API_ENDPOINT 
                        + "/" + TEST_ACCOUNT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(CommonConstants.IDEMPOTENCY_KEY_HEADER, TEST_IDEMPOTENCY_KEY)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(TEST_ACCOUNT_ID));

        verify(accountService, never()).patchAccount(anyString(), any(AccountUpdateRequestDTO.class));
    }

    @Test
    void testUpdateAccount_Idempotency_CachedResponse() throws Exception {
        // Arrange
        AccountUpdateRequestDTO request = AccountUpdateRequestDTO.builder()
                .accountName("Updated Account")
                .build();

        AccountUpdateResponseDTO cachedResponse = AccountUpdateResponseDTO.builder()
                .accountId(TEST_ACCOUNT_ID)
                .accountName("Updated Account")
                .status("ACTIVE")
                .build();

        ResponseEntity<AccountUpdateResponseDTO> cachedEntity = ResponseEntity.ok(cachedResponse);

        when(idempotencyService.getCachedResponse(eq(TEST_IDEMPOTENCY_KEY), eq(HttpMethod.PUT), 
                anyString(), eq(AccountUpdateResponseDTO.class))).thenReturn(cachedEntity);

        // Act & Assert
        mockMvc.perform(put(CommonConstants.API_V1_PREFIX + CommonConstants.ACCOUNT_API_ENDPOINT 
                        + "/" + TEST_ACCOUNT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(CommonConstants.IDEMPOTENCY_KEY_HEADER, TEST_IDEMPOTENCY_KEY)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(TEST_ACCOUNT_ID));

        verify(accountService, never()).updateAccount(anyString(), any(AccountUpdateRequestDTO.class));
    }

    private AccountCreateRequestDTO createValidAccountCreateRequest() {
        return AccountCreateRequestDTO.builder()
                .accountName("Test Account")
                .email("test@example.com")
                .countryCode("USA")
                .currencyCode("USD")
                .build();
    }
}

