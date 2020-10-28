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

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@Component
public class RepositoryUtil {

    @Resource
    UserRepository userRepository;

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
        // FIXME:  I BELIEVE that we should not go to the database for this. We should keep
        // FIXME:  everything required to build a user in the "authentication" token
        Optional<User> ou = userRepository.findUserByUserName(userName);
        if (ou.isEmpty()) {
            throw new IllegalActionException("No Current User could be identified.");
        } else {
            return ou.get();
        }
    }


}
