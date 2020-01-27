package org.socialhistoryservices.delivery.user.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Intercepts traffic before it is passed to a view, then adding the current user details.
 */
public class SecurityToViewInterceptor extends HandlerInterceptorAdapter {

    /**
     * Expose the current user to the views.
     */
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView model) {
        if (model != null) {
            model.addObject("_sec", new UserExposer());
        }
    }

    /**
     * Replacement for JspTaglibs , which is not working in unit tests.
     */
    public static class UserExposer {
        private Authentication auth;

        /**
         * Constructor.
         */
        public UserExposer() {
            auth = SecurityContextHolder.getContext().getAuthentication();
        }

        /**
         * Get whether the user is anonymous.
         *
         * @return Whether the user is anonymous.
         */
        public boolean isAnonymous() {
            return (auth instanceof AnonymousAuthenticationToken);
        }

        /**
         * Get the UserDetails object.
         *
         * @return The user details object.
         */
        public UserDetails getPrincipal() {
            return (UserDetails) auth.getPrincipal();
        }

        /**
         * Checks if the provided roles are not granted (AND)
         *
         * @param roleStr A comma separated string of roles.
         * @return True iff all roles in roleStr are NOT granted upon the user.
         */
        public boolean ifNotGranted(String roleStr) {
            String[] roles = roleStr.split(",");
            for (String r : roles) {
                for (GrantedAuthority ga : auth.getAuthorities()) {
                    if (ga.getAuthority().equals(r)) {
                        return false;
                    }
                }
            }
            return true;
        }

        /**
         * Checks if the provided roles are all granted (AND)
         *
         * @param roleStr A comma separated string of roles.
         * @return True iff all roles in roleStr are granted upon the user.
         */
        public boolean ifAllGranted(String roleStr) {
            String[] roles = roleStr.split(",");

            for (String r : roles) {
                boolean has = false;
                for (GrantedAuthority ga : auth.getAuthorities()) {
                    if (ga.getAuthority().equals(r)) {
                        has = true;
                    }
                }
                if (!has) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Checks if the provided roles are granted (OR)
         *
         * @param roleStr A comma separated string of roles.
         * @return True iff any of the roles in roleStr are granted upon the user.
         */
        public boolean ifAnyGranted(String roleStr) {
            return !ifNotGranted(roleStr);
        }
    }
}
