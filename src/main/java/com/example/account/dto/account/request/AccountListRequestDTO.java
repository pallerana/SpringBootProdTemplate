package com.example.account.dto.account.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.example.account.validation.ValidCountryCode;
import com.example.account.validation.ValidCurrencyCode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * Account list request DTO with filtering and pagination.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountListRequestDTO {

    @Size(min = 1, max = 255, message = "{account.name.size}")
    private String accountName;

    @Size(min = 1, max = 100, message = "{account.name.size}")
    private String accountId;

    @ValidCurrencyCode
    private String currency;

    @ValidCountryCode
    private String countryCode;

    @Size(max = 100, message = "{account.city.size}")
    private String city;

    @Size(max = 50, message = "{account.state.size}")
    private String state;

    @Size(max = 20, message = "{account.zipcode.size}")
    private String zipcode;

    private String status;

    @Min(value = 1, message = "{account.pageNumber.min}")
    @lombok.Builder.Default
    private Integer pageNumber = 1;

    @Min(value = 1, message = "{account.pageSize.min}")
    @lombok.Builder.Default
    private Integer pageSize = 25;
}

