package com.wolfesoftware.stocks.repository;

import com.wolfesoftware.stocks.exception.IllegalActionException;
import com.wolfesoftware.stocks.model.Authority;
import com.wolfesoftware.stocks.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RepositoryUtil {


    /**
     * This should be used if the caller is absolutely expecting a 'current user'.  This method
     * throws an exception if one does not exist.
     *
     */
    @Transactional(propagation= Propagation.REQUIRES_NEW)
    public User getCurrentUser() {
        User user = inspectCurrentUser();
        if (user == null)
            throw new IllegalActionException("No Current User could be identified.");
        return user;
    }

    /**
     * This should be used if the caller is interested in knowing whether or not to provide a User in a query that
     * would prevent one caller from modifying another's data.  If this method detects that the caller is an admin OR
     * that the SYSTEM is running a null value is returned.
     *
     */
    @Transactional(propagation= Propagation.REQUIRES_NEW)
    public User getRestrictingUser() {
        User u = inspectCurrentUser();
        // If there is not a user in the current context, this is the system running & no restriction should be placed on a query
        if (u == null)
            return null;
        // If this user is an admin no restriction should be placed on a query
        List<Authority> authorities = u.getAuthorities();
        boolean admin = authorities.stream().anyMatch(a -> a.getRole().equals(Authority.Role.ROLE_ADMIN));
        if (admin)
            return null;
        return u;
    }

    private User inspectCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null)
            return null;
        Object principal = authentication.getPrincipal();
        String userName = (principal instanceof UserDetails) ?
                ((UserDetails) principal).getUsername() :
                principal.toString();

        // Create a minimal User object to be used in JOINs and Comparisons
        // NOTE: User's equals() method only relies on Id.
        // NOTE: The code above requires some basic Authority information as well
        User u = new User();
        u.setId(((Map<String, Long>)authentication.getDetails()).get("ID"));
        u.setUsername(userName);

        List<Authority> authorities = authentication.getAuthorities().stream()
                .map(ga -> {
                        Authority a = new Authority();
                        a.setRole(Authority.Role.valueOf(ga.getAuthority()));
                        return a;
                    })
                .collect(Collectors.toList());
        u.setAuthorities(authorities);
        return u;
    }


}
