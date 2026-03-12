package com.example.account.dto.account.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Account details response DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDetailsResponseDTO {
    private String accountId;
    private String accountName;
    private String email;
    private String website;
    private String country;
    private String countryCode;
    private String currency;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String zipcode;
    private String status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}

