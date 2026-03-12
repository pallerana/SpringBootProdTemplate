package com.example.account.service.account.impl;

import com.example.account.dto.account.request.AccountCreateRequestDTO;
import com.example.account.dto.account.response.AccountCreateResponseDTO;
import com.example.account.enums.AccountStatusEnum;
import com.example.account.model.account.AccountEntity;
import com.example.account.repository.AccountRepository;
import com.example.account.validation.AccountValidator;
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
 * Unit tests for CreateAccountService.
 */
@ExtendWith(MockitoExtension.class)
class CreateAccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountValidator accountValidator;

    @InjectMocks
    private CreateAccountService createAccountService;

    @Test
    void testCreateAccount_Success() {
        // Arrange
        AccountCreateRequestDTO request = AccountCreateRequestDTO.builder()
                .accountName("Test Account")
                .email("test@example.com")
                .countryCode("USA")
                .currencyCode("USD")
                .build();

        AccountEntity savedEntity = AccountEntity.builder()
                .accountId("ACC-1234567890123")
                .accountName("Test Account")
                .email("test@example.com")
                .countryCode("USA")
                .currency("USD")
                .status(AccountStatusEnum.PENDING.getValue())
                .build();
        savedEntity.setId(1L);

        when(accountRepository.save(any(AccountEntity.class))).thenReturn(savedEntity);
        doNothing().when(accountValidator).validateCreateAccount(any(AccountCreateRequestDTO.class));

        // Act
        AccountCreateResponseDTO result = createAccountService.createAccount(request);

        // Assert
        assertNotNull(result);
        assertEquals("ACC-1234567890123", result.getAccountId());
        assertEquals("Test Account", result.getAccountName());
        assertEquals(AccountStatusEnum.PENDING.getValue(), result.getStatus());
        
        verify(accountValidator).validateCreateAccount(request);
        verify(accountRepository).save(any(AccountEntity.class));
    }

    @Test
    void testCreateAccount_WithAllFields() {
        // Arrange
        AccountCreateRequestDTO request = AccountCreateRequestDTO.builder()
                .accountName("Test Account")
                .email("test@example.com")
                .countryCode("USA")
                .currencyCode("USD")
                .website("https://www.example.com")
                .country("United States")
                .addressLine1("123 Main St")
                .addressLine2("Suite 100")
                .city("New York")
                .state("NY")
                .zipcode("10001")
                .build();

        AccountEntity savedEntity = AccountEntity.builder()
                .accountId("ACC-1234567890123")
                .accountName("Test Account")
                .email("test@example.com")
                .countryCode("USA")
                .currency("USD")
                .website("https://www.example.com")
                .country("United States")
                .addressLine1("123 Main St")
                .addressLine2("Suite 100")
                .city("New York")
                .state("NY")
                .zipcode("10001")
                .status(AccountStatusEnum.PENDING.getValue())
                .build();
        savedEntity.setId(1L);

        when(accountRepository.save(any(AccountEntity.class))).thenReturn(savedEntity);
        doNothing().when(accountValidator).validateCreateAccount(any(AccountCreateRequestDTO.class));

        // Act
        AccountCreateResponseDTO result = createAccountService.createAccount(request);

        // Assert
        assertNotNull(result);
        assertEquals("ACC-1234567890123", result.getAccountId());
        verify(accountRepository).save(any(AccountEntity.class));
    }

    @Test
    void testCreateAccount_WithNullOptionalFields() {
        // Arrange
        AccountCreateRequestDTO request = AccountCreateRequestDTO.builder()
                .accountName("Test Account")
                .email("test@example.com")
                .build();

        AccountEntity savedEntity = AccountEntity.builder()
                .accountId("ACC-1234567890123")
                .accountName("Test Account")
                .email("test@example.com")
                .status(AccountStatusEnum.PENDING.getValue())
                .build();
        savedEntity.setId(1L);

        when(accountRepository.save(any(AccountEntity.class))).thenReturn(savedEntity);
        doNothing().when(accountValidator).validateCreateAccount(any(AccountCreateRequestDTO.class));

        // Act
        AccountCreateResponseDTO result = createAccountService.createAccount(request);

        // Assert
        assertNotNull(result);
        verify(accountRepository).save(any(AccountEntity.class));
    }
}

