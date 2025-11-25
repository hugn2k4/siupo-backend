package com.siupo.restaurant.security.oauth2;

import com.siupo.restaurant.model.User;
import com.siupo.restaurant.service.authentication.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final AuthenticationService authenticationService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        try {
            processOAuth2User(oAuth2User);
            return oAuth2User;
        } catch (Exception ex) {
            log.error("Error processing OAuth2 user", ex);
            throw new OAuth2AuthenticationException(ex.getMessage());
        }
    }

    private void processOAuth2User(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        log.info("Processing OAuth2 user - Email: {}, Name: {}", email, name);

        // Use AuthenticationService to handle user creation/retrieval
        User user = authenticationService.processOAuth2User(email, name, picture);
        
        if (user.getCreatedAt().plusSeconds(5).isAfter(java.time.LocalDateTime.now())) {
            log.info("New user registered via OAuth2: {}", email);
        } else {
            log.info("Existing user logged in via OAuth2: {}", email);
        }
    }
}
