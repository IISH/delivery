package org.socialhistoryservices.delivery.user.service;

import org.socialhistoryservices.delivery.user.entity.User;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;

import java.util.HashSet;
import java.util.Set;

/**
 * Obtains the authorities for the logged in LDAP user from the database.
 */
public class AuthoritiesPopulator extends DefaultLdapAuthoritiesPopulator {
    private UserService userService;

    /**
     * Constructor for group search scenarios. <tt>userRoleAttributes</tt> may still be set as a property.
     *
     * @param contextSource   supplies the contexts used to search for user roles.
     * @param groupSearchBase if this is an empty string the search will be performed from the root DN of the
     *                        context factory. If null, no search will be performed.
     * @param userService     the user service.
     */
    public AuthoritiesPopulator(ContextSource contextSource, String groupSearchBase, UserService userService) {
        super(contextSource, groupSearchBase);
        this.userService = userService;
    }

    /**
     * This method should be overridden if required to obtain any additional
     * roles for the given user (on top of those obtained from the standard
     * search implemented by this class).
     *
     * @param user     the context representing the user who's roles are required
     * @param username the username
     * @return the extra roles which will be merged with those returned by the group search
     */
    @Override
    public Set<GrantedAuthority> getAdditionalRoles(DirContextOperations user, String username) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        User userEntity = userService.getUserByName(username);
        if (userEntity != null) {
            authorities.addAll(userEntity.getAuthorities());
        }
        else {
            User u = new User();
            u.setUsername(username);
            userService.addUser(u);
        }
        return authorities;
    }
}
