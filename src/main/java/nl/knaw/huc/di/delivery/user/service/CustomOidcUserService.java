package nl.knaw.huc.di.delivery.user.service;

import jakarta.annotation.PostConstruct;
import nl.knaw.huc.di.delivery.user.dao.UserRepository;
import nl.knaw.huc.di.delivery.user.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CustomOidcUserService extends OidcUserService {

    private final Logger logger = LoggerFactory.getLogger(CustomOidcUserService.class);

    /**
     * user DAO, do not autowire unless userServiceDetails bean removed
     */
    private final UserRepository userRepository;

    public CustomOidcUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        String email = oidcUser.getAttribute("email");
        String sub = oidcUser.getAttribute("sub");
        // Zoek de gebruiker in de lokale database of maak een nieuwe aan
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    assert email != null;
                    String username = email.split("@", 2)[0];
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setSub(sub);
                    newUser.setUsername(username);
                    return userRepository.save(newUser);
                });

        // Fuse delivery authorities with the provider authorities (if any).
        final List<SimpleGrantedAuthority> deliveryAuthorities = user.getAuthorities().stream()
                .map(role -> new SimpleGrantedAuthority(role.getAuthority()))
                .toList();

        Set<GrantedAuthority> combinedAuthorities = new HashSet<>(oidcUser.getAuthorities());
        combinedAuthorities.addAll(deliveryAuthorities);

        // Create the user again from the attributes from the original provider.
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        return new DefaultOidcUser(
                combinedAuthorities,
                oidcUser.getIdToken(),
                oidcUser.getUserInfo(),
                userNameAttributeName
        );
    }

    @PostConstruct
    /*
     * Remove all users from the database if they are not linked to a group.
     */
    private void deleteUsersWithoutGroups() {
        logger.info("Delete users without groups");
        userRepository.deleteUsersWithoutGroups();
    }
}