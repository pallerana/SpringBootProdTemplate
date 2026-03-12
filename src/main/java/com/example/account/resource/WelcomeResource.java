package com.example.account.resource;

import com.example.account.constants.CommonConstants;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WelcomeResource {

    @RequestMapping(CommonConstants.WELCOME_API_ENDPOINT)
    public String index() {
        return "Welcome to the account-service project!";
    }
}