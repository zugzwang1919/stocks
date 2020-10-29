package com.wolfesoftware.stocks.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wolfesoftware.stocks.common.BridgeToSpringBean;
import com.wolfesoftware.stocks.exception.NotFoundException;
import com.wolfesoftware.stocks.repository.RepositoryUtil;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;


@MappedSuperclass
@Component
public abstract class UserBasedPersistentEntity extends PersistentEntity {

    @ManyToOne
    @JsonIgnore
    protected User            user;

    private static final long serialVersionUID = 1L;


    // Working Methods

    @PrePersist
    public void prePersist() {
        if (user != null) {
            throw new NotFoundException("Create attempted on a User Based Entity that has already been created.");
        }

        // Set the user to the current user when this object is inserted into the database for the first time
        user = RepositoryUtil.getCurrentUser();

        super.prePersist();
    }

    @PostLoad
    // NOTE:  This is really the second line of defense.  For all Retrievals, the Repository layer should
    // NOTE:  always insert a "WHERE userId = " clause.  If the error is ever seen, there is likely a bug
    // NOTE:  in the repository layer also.
    public void postLoad() {

        User restrictingUser = RepositoryUtil.getRestrictingUser();

        // Make sure that the caller has the right to inspect this entity that we're about to return
        if (restrictingUser != null && !restrictingUser.equals(this.user))
            throw new NotFoundException("postLoad() Exception: Retrieve attempted on a User Based Entity that is not owned by the caller.");

    }

    @PreUpdate
    // NOTE:  The individual repository layers must guarantee that the UPDATE is valid - notably, the user
    // NOTE:  has not changed the update date or the create date.
    public void preUpdate() {
        super.preUpdate();
    }


    @PreRemove
    public void preRemove() {
        User currentUser = RepositoryUtil.getCurrentUser();

        // Make sure that the caller owns this entity that we're about to delete
        if (!currentUser.equals(this.user))
            throw new NotFoundException("preRemove() Exception: Delete attempted on a User Based Entity that is not owned by the caller.");

    }


    // Getters and Setters
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserBasedPersistentEntity)) return false;
        if (!super.equals(o)) return false;
        UserBasedPersistentEntity that = (UserBasedPersistentEntity) o;
        return getUser().equals(that.getUser());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getUser());
    }
}
