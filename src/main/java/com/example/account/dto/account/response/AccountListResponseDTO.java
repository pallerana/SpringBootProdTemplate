package com.example.account.dto.account.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Account list response DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountListResponseDTO {
    private List<AccountDetailsResponseDTO> accounts;
    private int pageNumber;
    private int pageSize;
    private long totalItems;
    private int totalPages;
}

