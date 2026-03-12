package com.example.account.dto.account.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Account deletion response DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDeleteResponseDTO {
    private String accountId;
    private String message;
    private boolean deleted;
}

