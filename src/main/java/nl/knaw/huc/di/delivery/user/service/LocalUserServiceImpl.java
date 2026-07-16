package nl.knaw.huc.di.delivery.user.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import nl.knaw.huc.di.delivery.user.dao.UserRepository;
import nl.knaw.huc.di.delivery.user.entity.User;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Represents the service of the user package.
 * Use for local development
 */
@Service
public class LocalUserServiceImpl implements UserDetailsService {

    private final Logger logger = LoggerFactory.getLogger(LocalUserServiceImpl.class);

    /**
     * user DAO, do not autowire unless userServiceDetails bean removed
     */
    private final UserRepository userRepository;
    public LocalUserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void init() {
        logger.info("Default users for developing and testing. Do not use for production.");
        logger.info("Username and password is identical. Also the username is equal to the group the user is in.");
        for (User user : userRepository.findAll()) {
            logger.info("Found user: " + user);
        }
    }

    /**
     * Load a user by its user name.
     *
     * @param username The name/e-mail of the user.
     * @return The UserDetails of this user (User object) if successful.
     * @throws UsernameNotFoundException Thrown when the user was not found.
     * @throws DataAccessException       Thrown when there was a problem accessing the database.
     */
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
        Optional<User> ou = userRepository.findByUsername(username);
        User localUser = ou.orElseThrow(() -> new UsernameNotFoundException(username + " is no valid user"));

        // Clone the authorities from the local user. We need to strip off the ROLE_ prefix.
        String[] roles = localUser.getAuthorities().stream()
                .map(grant -> grant.getAuthority().substring(5))
                .toArray(String[]::new);

        // return a Clone object with the roles. The roles method will in turn add its own ROLE_ prefix.
        return org.springframework.security.core.userdetails.User.withDefaultPasswordEncoder()
                .username(localUser.getUsername())
                .password(localUser.getUsername())
                .roles(roles).build();

    }
}
