package com.example.account.service.account.impl;

import com.example.account.dto.account.request.AccountCreateRequestDTO;
import com.example.account.dto.account.request.AccountListRequestDTO;
import com.example.account.dto.account.request.AccountUpdateRequestDTO;
import com.example.account.dto.account.response.*;
import com.example.account.service.account.IAccountDeleteService;
import com.example.account.service.account.IAccountRetrievalService;
import com.example.account.service.account.IAccountUpdateService;
import com.example.account.service.account.ICreateAccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AccountOrchestrationService.
 */
@ExtendWith(MockitoExtension.class)
class AccountOrchestrationServiceTest {

    private static final String TEST_ACCOUNT_ID = "ACC-000001";

    @Mock
    private ICreateAccountService createAccountService;

    @Mock
    private IAccountRetrievalService accountRetrievalService;

    @Mock
    private IAccountUpdateService accountUpdateService;

    @Mock
    private IAccountDeleteService accountDeleteService;

    @InjectMocks
    private AccountOrchestrationService accountOrchestrationService;

    @Test
    void testCreateAccount() {
        // Arrange
        AccountCreateRequestDTO request = AccountCreateRequestDTO.builder()
                .accountName("Test Account")
                .email("test@example.com")
                .build();

        AccountCreateResponseDTO response = AccountCreateResponseDTO.builder()
                .accountId(TEST_ACCOUNT_ID)
                .accountName("Test Account")
                .status("PENDING")
                .build();

        when(createAccountService.createAccount(any(AccountCreateRequestDTO.class))).thenReturn(response);

        // Act
        AccountCreateResponseDTO result = accountOrchestrationService.createAccount(request);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_ACCOUNT_ID, result.getAccountId());
        verify(createAccountService).createAccount(request);
    }

    @Test
    void testUpdateAccount() {
        // Arrange
        AccountUpdateRequestDTO request = AccountUpdateRequestDTO.builder()
                .accountName("Updated Account")
                .build();

        AccountUpdateResponseDTO response = AccountUpdateResponseDTO.builder()
                .accountId(TEST_ACCOUNT_ID)
                .accountName("Updated Account")
                .status("ACTIVE")
                .build();

        when(accountUpdateService.updateAccount(eq(TEST_ACCOUNT_ID), any(AccountUpdateRequestDTO.class)))
                .thenReturn(response);

        // Act
        AccountUpdateResponseDTO result = accountOrchestrationService.updateAccount(TEST_ACCOUNT_ID, request);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_ACCOUNT_ID, result.getAccountId());
        verify(accountUpdateService).updateAccount(TEST_ACCOUNT_ID, request);
    }

    @Test
    void testGetAccount() {
        // Arrange
        AccountDetailsResponseDTO response = AccountDetailsResponseDTO.builder()
                .accountId(TEST_ACCOUNT_ID)
                .accountName("Test Account")
                .build();

        when(accountRetrievalService.getAccount(TEST_ACCOUNT_ID)).thenReturn(response);

        // Act
        AccountDetailsResponseDTO result = accountOrchestrationService.getAccount(TEST_ACCOUNT_ID);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_ACCOUNT_ID, result.getAccountId());
        verify(accountRetrievalService).getAccount(TEST_ACCOUNT_ID);
    }

    @Test
    void testGetAccounts() {
        // Arrange
        AccountListRequestDTO request = AccountListRequestDTO.builder()
                .pageNumber(1)
                .pageSize(25)
                .build();

        AccountListResponseDTO response = AccountListResponseDTO.builder()
                .accounts(java.util.Collections.emptyList())
                .pageNumber(1)
                .pageSize(25)
                .totalItems(0L)
                .totalPages(0)
                .build();

        when(accountRetrievalService.getAccounts(any(AccountListRequestDTO.class))).thenReturn(response);

        // Act
        AccountListResponseDTO result = accountOrchestrationService.getAccounts(request);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getPageNumber());
        verify(accountRetrievalService).getAccounts(request);
    }

    @Test
    void testPatchAccount() {
        // Arrange
        AccountUpdateRequestDTO request = AccountUpdateRequestDTO.builder()
                .accountName("Patched Account")
                .build();

        AccountUpdateResponseDTO response = AccountUpdateResponseDTO.builder()
                .accountId(TEST_ACCOUNT_ID)
                .accountName("Patched Account")
                .status("ACTIVE")
                .build();

        when(accountUpdateService.patchAccount(eq(TEST_ACCOUNT_ID), any(AccountUpdateRequestDTO.class)))
                .thenReturn(response);

        // Act
        AccountUpdateResponseDTO result = accountOrchestrationService.patchAccount(TEST_ACCOUNT_ID, request);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_ACCOUNT_ID, result.getAccountId());
        verify(accountUpdateService).patchAccount(TEST_ACCOUNT_ID, request);
    }

    @Test
    void testDeleteAccount() {
        // Arrange
        AccountDeleteResponseDTO response = AccountDeleteResponseDTO.builder()
                .accountId(TEST_ACCOUNT_ID)
                .message("Account deleted successfully")
                .deleted(true)
                .build();

        when(accountDeleteService.deleteAccount(TEST_ACCOUNT_ID)).thenReturn(response);

        // Act
        AccountDeleteResponseDTO result = accountOrchestrationService.deleteAccount(TEST_ACCOUNT_ID);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_ACCOUNT_ID, result.getAccountId());
        assertTrue(result.isDeleted());
        verify(accountDeleteService).deleteAccount(TEST_ACCOUNT_ID);
    }
}




