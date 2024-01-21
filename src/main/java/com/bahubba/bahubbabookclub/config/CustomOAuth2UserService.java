package com.bahubba.bahubbabookclub.config;

import com.bahubba.bahubbabookclub.exception.OAuth2ProviderException;
import com.bahubba.bahubbabookclub.model.entity.OAuth2Info;
import com.bahubba.bahubbabookclub.model.entity.User;
import com.bahubba.bahubbabookclub.model.enums.OAuth2Provider;
import com.bahubba.bahubbabookclub.repository.UserRepo;
import com.bahubba.bahubbabookclub.security.user.OAuth2UserInfo;
import com.bahubba.bahubbabookclub.security.user.OAuth2UserInfoFactory;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
                userRequest.getClientRegistration().getRegistrationId(), oAuth2User.getAttributes());
        if (!StringUtils.hasLength(oAuth2UserInfo.getEmail())) {
            throw new OAuth2ProviderException("Email not found from OAuth2 provider");
        }

        userRepo.findByEmail(oAuth2UserInfo.getEmail())
                .ifPresentOrElse(
                        user -> updateExistingUser(
                                user,
                                oAuth2UserInfo,
                                OAuth2Provider.valueOf(userRequest
                                        .getClientRegistration()
                                        .getRegistrationId()
                                        .toUpperCase())),
                        () -> registerNewUser(userRequest, oAuth2UserInfo));

        return oAuth2User;
    }

    private void updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo, OAuth2Provider oAuth2Provider) {
        existingUser
                .getOAuth2Info()
                .add(OAuth2Info.builder()
                        .provider(oAuth2Provider)
                        .name(oAuth2UserInfo.getName())
                        .imageURL(oAuth2UserInfo.getImageUrl())
                        .build());
        userRepo.save(existingUser);
    }

    private void registerNewUser(OAuth2UserRequest userRequest, OAuth2UserInfo oAuth2UserInfo) {
        userRepo.save(User.builder()
                .email(oAuth2UserInfo.getEmail())
                .username(oAuth2UserInfo.getEmail())
                .oAuth2Info(Set.of(OAuth2Info.builder()
                        .provider(OAuth2Provider.valueOf(userRequest
                                .getClientRegistration()
                                .getRegistrationId()
                                .toUpperCase()))
                        .name(oAuth2UserInfo.getName())
                        .imageURL(oAuth2UserInfo.getImageUrl())
                        .build()))
                .build());
    }
}
