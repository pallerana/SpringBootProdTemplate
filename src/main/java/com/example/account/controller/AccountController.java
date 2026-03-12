package com.example.account.controller;

import com.example.account.constants.CommonConstants;
import com.example.account.dto.account.request.AccountCreateRequestDTO;
import com.example.account.dto.account.request.AccountListRequestDTO;
import com.example.account.dto.account.request.AccountUpdateRequestDTO;
import com.example.account.dto.account.response.AccountCreateResponseDTO;
import com.example.account.dto.account.response.AccountDeleteResponseDTO;
import com.example.account.dto.account.response.AccountDetailsResponseDTO;
import com.example.account.dto.account.response.AccountListResponseDTO;
import com.example.account.dto.account.response.AccountUpdateResponseDTO;
import com.example.account.service.IdempotencyService;
import com.example.account.service.account.IAccountOrchestrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * Account controller demonstrating orchestrator pattern.
 * Delegates to IAccountOrchestrationService (orchestrator) which delegates to granular services.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(CommonConstants.API_V1_PREFIX + CommonConstants.ACCOUNT_API_ENDPOINT)
@Validated
@Tag(name = "Account", description = "Account management API")
public class AccountController {

    private final IAccountOrchestrationService accountService;
    private final IdempotencyService idempotencyService;

    /**
     * Create a new account.
     * Supports idempotency via Idempotency-Key header.
     */
    @Operation(summary = "Create a new account", description = "Creates a new account with the provided details. Supports idempotency via Idempotency-Key header.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Account created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping(CommonConstants.ACCOUNT_CREATE_API_ENDPOINT)
    public ResponseEntity<AccountCreateResponseDTO> createAccount(
            @RequestHeader(value = CommonConstants.IDEMPOTENCY_KEY_HEADER, required = false) String idempotencyKey,
            @Valid @RequestBody AccountCreateRequestDTO request,
            HttpServletRequest httpRequest) {
        
        String requestPath = httpRequest.getRequestURI();
        HttpMethod requestMethod = HttpMethod.POST;
        
        // Check for cached response
        ResponseEntity<AccountCreateResponseDTO> cachedResponse = idempotencyService.getCachedResponse(
                idempotencyKey, requestMethod, requestPath, AccountCreateResponseDTO.class);
        if (cachedResponse != null) {
            return cachedResponse;
        }
        
        // Process request
        AccountCreateResponseDTO response = accountService.createAccount(request);
        ResponseEntity<AccountCreateResponseDTO> responseEntity = ResponseEntity.status(HttpStatus.CREATED).body(response);
        
        // Store response for idempotency
        idempotencyService.storeResponse(idempotencyKey, requestMethod, requestPath, responseEntity);
        
        return responseEntity;
    }
    
    /**
     * Update an existing account.
     * Supports idempotency via Idempotency-Key header.
     */
    @Operation(summary = "Update an account", description = "Updates an existing account with the provided details. Supports idempotency via Idempotency-Key header.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data or account not found")
    })
    @PutMapping(CommonConstants.ACCOUNT_UPDATE_API_ENDPOINT)
    public ResponseEntity<AccountUpdateResponseDTO> updateAccount(
            @RequestHeader(value = CommonConstants.IDEMPOTENCY_KEY_HEADER, required = false) String idempotencyKey,
            @Parameter(description = "Account ID (e.g., ACC-000001)", required = true) @PathVariable String accountId,
            @Valid @RequestBody AccountUpdateRequestDTO request,
            HttpServletRequest httpRequest) {
        
        String requestPath = httpRequest.getRequestURI();
        HttpMethod requestMethod = HttpMethod.PUT;
        
        // Check for cached response
        ResponseEntity<AccountUpdateResponseDTO> cachedResponse = idempotencyService.getCachedResponse(
                idempotencyKey, requestMethod, requestPath, AccountUpdateResponseDTO.class);
        if (cachedResponse != null) {
            return cachedResponse;
        }
        
        // Process request
        AccountUpdateResponseDTO response = accountService.updateAccount(accountId, request);
        ResponseEntity<AccountUpdateResponseDTO> responseEntity = ResponseEntity.ok(response);
        
        // Store response for idempotency
        idempotencyService.storeResponse(idempotencyKey, requestMethod, requestPath, responseEntity);
        
        return responseEntity;
    }
    
    /**
     * Get account by ID.
     */
    @Operation(summary = "Get account by ID", description = "Retrieves account details by account ID (e.g., ACC-000001)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account found"),
            @ApiResponse(responseCode = "400", description = "Account not found")
    })
    @GetMapping(CommonConstants.ACCOUNT_GET_API_ENDPOINT)
    public ResponseEntity<AccountDetailsResponseDTO> getAccount(
            @Parameter(description = "Account ID (e.g., ACC-000001)", required = true) @PathVariable String accountId) {
        AccountDetailsResponseDTO response = accountService.getAccount(accountId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get list of accounts with filtering and pagination.
     */
    @Operation(summary = "Get list of accounts", description = "Retrieves a paginated list of accounts with optional filtering")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accounts retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid filter parameters")
    })
    @GetMapping(CommonConstants.ACCOUNT_LIST_API_ENDPOINT)
    public ResponseEntity<AccountListResponseDTO> getAccounts(
            @ParameterObject @Valid AccountListRequestDTO requestDTO) {
        // Create default requestDTO if null to handle optional query parameters
        AccountListRequestDTO request = requestDTO != null ? requestDTO : AccountListRequestDTO.builder().build();
        AccountListResponseDTO response = accountService.getAccounts(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Partially update an existing account (PATCH).
     * Supports idempotency via Idempotency-Key header.
     */
    @Operation(summary = "Partially update an account", description = "Partially updates an existing account with only the provided fields. Supports idempotency via Idempotency-Key header.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data or account not found")
    })
    @PatchMapping(CommonConstants.ACCOUNT_UPDATE_API_ENDPOINT)
    public ResponseEntity<AccountUpdateResponseDTO> patchAccount(
            @RequestHeader(value = CommonConstants.IDEMPOTENCY_KEY_HEADER, required = false) String idempotencyKey,
            @Parameter(description = "Account ID (e.g., ACC-000001)", required = true) @PathVariable String accountId,
            @Valid @RequestBody AccountUpdateRequestDTO request,
            HttpServletRequest httpRequest) {
        
        String requestPath = httpRequest.getRequestURI();
        HttpMethod requestMethod = HttpMethod.PATCH;
        
        // Check for cached response
        ResponseEntity<AccountUpdateResponseDTO> cachedResponse = idempotencyService.getCachedResponse(
                idempotencyKey, requestMethod, requestPath, AccountUpdateResponseDTO.class);
        if (cachedResponse != null) {
            return cachedResponse;
        }
        
        // Process request
        AccountUpdateResponseDTO response = accountService.patchAccount(accountId, request);
        ResponseEntity<AccountUpdateResponseDTO> responseEntity = ResponseEntity.ok(response);
        
        // Store response for idempotency
        idempotencyService.storeResponse(idempotencyKey, requestMethod, requestPath, responseEntity);
        
        return responseEntity;
    }
    
    /**
     * Delete an account.
     */
    @Operation(summary = "Delete an account", description = "Deletes an account by account ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Account not found")
    })
    @DeleteMapping(CommonConstants.ACCOUNT_DELETE_API_ENDPOINT)
    public ResponseEntity<AccountDeleteResponseDTO> deleteAccount(
            @Parameter(description = "Account ID (e.g., ACC-000001)", required = true) @PathVariable String accountId) {
        AccountDeleteResponseDTO response = accountService.deleteAccount(accountId);
        return ResponseEntity.ok(response);
    }
}

