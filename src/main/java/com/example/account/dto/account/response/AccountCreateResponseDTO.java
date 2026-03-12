package com.example.account.dto.account.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Account creation response DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountCreateResponseDTO {
    private String accountId;
    private String accountName;
    private String status;
}

