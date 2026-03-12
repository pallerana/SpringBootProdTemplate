package com.example.account.service.account.impl;

import com.example.account.dto.account.request.AccountListRequestDTO;
import com.example.account.dto.account.response.AccountDetailsResponseDTO;
import com.example.account.dto.account.response.AccountListResponseDTO;
import com.example.account.exception.AccountNotFoundException;
import com.example.account.mapper.AccountMapper;
import com.example.account.model.account.AccountEntity;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountSpecifications;
import com.example.account.service.account.IAccountRetrievalService;
import com.example.account.util.DtoConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for account retrieval operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountRetrievalService implements IAccountRetrievalService {
    
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper = AccountMapper.INSTANCE;
    
    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public AccountDetailsResponseDTO getAccount(String accountId) {
        log.debug("Retrieving account with ID: {}", accountId);
        
        AccountEntity accountEntity = accountRepository.findByAccountId(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        
        return accountMapper.toDetailsDTO(accountEntity);
    }
    
    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public AccountListResponseDTO getAccounts(AccountListRequestDTO request) {
        log.debug("Retrieving accounts with filters");
        
        // Build Pageable
        int pageNumber = (request.getPageNumber() != null && request.getPageNumber() > 0) 
                ? request.getPageNumber() - 1 : 0;
        int pageSize = (request.getPageSize() != null && request.getPageSize() > 0) 
                ? request.getPageSize() : 25;
        
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.ASC, "id"));
        
        // Build dynamic JPA Specification from filters
        Page<AccountEntity> accountPage = accountRepository.findAll(
                AccountSpecifications.withFilters(request),
                pageable
        );
        
        // Convert to DTOs using MapStruct and DtoConverter
        List<AccountDetailsResponseDTO> accounts = DtoConverter.convertList(
                accountPage.getContent(),
                accountMapper::toDetailsDTO
        );
        
        return AccountListResponseDTO.builder()
                .accounts(accounts)
                .pageNumber(pageNumber + 1)
                .pageSize(pageSize)
                .totalItems(accountPage.getTotalElements())
                .totalPages(accountPage.getTotalPages())
                .build();
    }
}

