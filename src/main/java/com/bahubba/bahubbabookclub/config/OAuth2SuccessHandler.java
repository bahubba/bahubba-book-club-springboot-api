package com.bahubba.bahubbabookclub.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuth2SuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Value("${app.ui.url}")
    private String uiURL;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse rsp, Authentication auth) {
        this.setAlwaysUseDefaultTargetUrl(true);
        this.setDefaultTargetUrl(uiURL);

        try {
            super.onAuthenticationSuccess(req, rsp, auth);
        } catch(Exception e) {
            // TODO - Throw an appropriate custom exception and handle in the GlobalExceptionHandler
            e.printStackTrace();
        }
    }
}
