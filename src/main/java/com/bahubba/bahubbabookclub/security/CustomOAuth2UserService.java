package com.bahubba.bahubbabookclub.security;

import com.bahubba.bahubbabookclub.exception.OAuth2ProviderException;
import com.bahubba.bahubbabookclub.model.entity.User;
import com.bahubba.bahubbabookclub.model.enums.OAuth2Provider;
import com.bahubba.bahubbabookclub.repository.UserRepo;
import com.bahubba.bahubbabookclub.security.user.OAuth2UserInfo;
import com.bahubba.bahubbabookclub.security.user.OAuth2UserInfoFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepo userRepo;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        try {
            return verifyOAuth2User(userRequest, oAuth2User);
        } catch (AuthenticationException e) {
            throw e; // TODO - handle this
        }
    }

    private OAuth2User verifyOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(userRequest.getClientRegistration().getRegistrationId(), oAuth2User.getAttributes());
        if(StringUtils.hasLength(oAuth2UserInfo.getEmail())) {
            throw new OAuth2ProviderException("Email not found from OAuth2 provider");
        }

        Optional<User> userOptional = userRepo.findByEmail(oAuth2UserInfo.getEmail());
        User user;
        if(userOptional.isPresent()) {
            user = userOptional.get();

            // TODO - If the user hasn't used the OAuth2 provider to login before, update the user
            if(!user.getProvider().equals(OAuth2Provider.valueOf(userRequest.getClientRegistration().getRegistrationId()))) {
                throw new OAuth2AuthenticationProcessingException("Looks like you're signed up with " +
                    user.getProvider() + " account. Please use your " + user.getProvider() +
                    " account to login.");
            }
            user = updateExistingUser(user, oAuth2UserInfo);
        } else {
            user = registerNewUser(userRequest, oAuth2UserInfo);
        }

        return UserPrincipal.create(user, oAuth2User.getAttributes());
    }
}
