package com.example.account.dto.account.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.example.account.validation.ValidCountryCode;
import com.example.account.validation.ValidCurrencyCode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * Account update request DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountUpdateRequestDTO {

    @Size(min = 1, max = 255, message = "{account.name.size}")
    private String accountName;

    @Email(message = "{account.email.email}")
    @Size(min = 5, max = 100, message = "{account.email.size}")
    private String email;

    @ValidCountryCode
    private String countryCode;

    @ValidCurrencyCode
    private String currencyCode;

    @Size(max = 255, message = "{account.website.size}")
    private String website;

    @Size(max = 100, message = "{account.country.size}")
    private String country;

    @Size(max = 255, message = "{account.addressLine1.size}")
    private String addressLine1;

    @Size(max = 255, message = "{account.addressLine2.size}")
    private String addressLine2;

    @Size(max = 100, message = "{account.city.size}")
    private String city;

    @Size(max = 50, message = "{account.state.size}")
    private String state;

    @Size(max = 20, message = "{account.zipcode.size}")
    private String zipcode;

    private String status;
}

