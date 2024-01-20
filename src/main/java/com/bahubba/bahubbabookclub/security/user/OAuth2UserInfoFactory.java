package com.bahubba.bahubbabookclub.security.user;

import com.bahubba.bahubbabookclub.exception.OAuth2ProviderException;
import com.bahubba.bahubbabookclub.model.enums.OAuth2Provider;

import java.util.Map;

public class OAuth2UserInfoFactory {
    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if(registrationId.equalsIgnoreCase(OAuth2Provider.GOOGLE.toString())) {
            return new GoogleOAuth2UserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase(OAuth2Provider.FACEBOOK.toString())) {
            return new FacebookOAuth2UserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase(OAuth2Provider.GITHUB.toString())) {
            return new GitHubOAuth2UserInfo(attributes);
        } else {
            throw new OAuth2ProviderException("Sorry! Login with " + registrationId + " is not supported yet.");
        }
    }
}
