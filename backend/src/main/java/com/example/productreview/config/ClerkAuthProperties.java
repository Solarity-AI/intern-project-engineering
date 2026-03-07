package com.example.productreview.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "clerk.auth")
public class ClerkAuthProperties {

    private boolean enabled = true;
    private String verificationKey = "";
    private List<String> authorizedParties = new ArrayList<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getVerificationKey() {
        return verificationKey;
    }

    public void setVerificationKey(String verificationKey) {
        this.verificationKey = verificationKey;
    }

    public List<String> getAuthorizedParties() {
        return authorizedParties;
    }

    public void setAuthorizedParties(List<String> authorizedParties) {
        this.authorizedParties = authorizedParties;
    }
}
