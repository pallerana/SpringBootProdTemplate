package com.example.account.constants;

/**
 * Account module database field constants.
 * Extends BaseDbFieldConstants with account-specific fields.
 */
public final class AccountDbFieldConstants extends BaseDbFieldConstants {

    private AccountDbFieldConstants() {
        // Utility class
    }

    // Account-specific fields
    public static final String ACCOUNT_ID_COLUMN_NAME = "account_id";
    public static final String ACCOUNT_NAME_COLUMN_NAME = "account_name";
    public static final String EMAIL_COLUMN_NAME = "email";
    public static final String WEBSITE_COLUMN_NAME = "website";
    public static final String COUNTRY_COLUMN_NAME = "country";
    public static final String COUNTRY_CODE_COLUMN_NAME = "country_code";
    public static final String CURRENCY_COLUMN_NAME = "currency";
    
    // Address fields
    public static final String ADDRESS_LINE1_COLUMN_NAME = "address_line1";
    public static final String ADDRESS_LINE2_COLUMN_NAME = "address_line2";
    public static final String ADDRESS_CITY_COLUMN_NAME = "city";
    public static final String ADDRESS_STATE_COLUMN_NAME = "state";
    public static final String ADDRESS_ZIPCODE_COLUMN_NAME = "zipcode";
}

