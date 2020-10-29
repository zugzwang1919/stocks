package com.wolfesoftware.stocks.repository;

import com.wolfesoftware.stocks.exception.NotFoundException;
import com.wolfesoftware.stocks.exception.UnexpectedException;
import com.wolfesoftware.stocks.model.UserBasedPersistentEntity;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;


public abstract class UserBasedRepository<T extends UserBasedPersistentEntity> {

    private final Class<T> typeOfUserBasedPersistentEntity;


    protected UserBasedRepository( Class<T> typeOfUserBasedPersistentEntity ) {
        this.typeOfUserBasedPersistentEntity = typeOfUserBasedPersistentEntity;
    }


    protected abstract JpaRepository<T, Long> getUserBasedPersistEntityRepository();


    // Existence

    public boolean existsById(Long id) {
        T t = buildExampleWithUserAndId(id);
        return getUserBasedPersistEntityRepository().exists(Example.of(t));
    }


    // C is for Create

    public T create(T t) {
        return getUserBasedPersistEntityRepository().save(t);
    }



    // R is for Retrieve

    public Optional<T> retrieveById(Long id)  {
        T t = buildExampleWithUserAndId(id);
        return getUserBasedPersistEntityRepository().findOne(Example.of(t));
    }

    public List<T> retrieveAll() {
        T t = buildExampleWithUser();
        return getUserBasedPersistEntityRepository().findAll(Example.of(t));
    }


    // U is for Update and is handled elsewhere



    // D is for delete

    public void deleteById(Long id) {
        getUserBasedPersistEntityRepository().deleteById(id);
    }



    public T buildExampleWithUserAndId(Long id) {
        // NOTE:  It's worth noting that we need to check for null.  If we insert the null into an
        // NOTE:  EXAMPLE we'll search for all objects of this type
        if (id == null)
            throw new NotFoundException("The requested item with a null ID could not be found.");
        T t = buildExampleWithUser();
        t.setId(id);
        return t;
    }

    /**
     * buildExampleWithUser - When we are interacting with the JpaRepository, we frequently want to filter results with
     * an Example.  This method can be used by this class and its subclasses to guarantee that we are always returning
     * entities actually owned by the current user (or in the case of an admin or the SYSTEM user, no user is populated)
     *
     * @return an instance of T that can then be used to create an Example
     */
    public T buildExampleWithUser() {
        T t;
        try {
            // Construct a new instance of T
            t = typeOfUserBasedPersistentEntity.getConstructor().newInstance();
        }
        catch (ReflectiveOperationException e) {
            throw new UnexpectedException("An internal error occurred in the repository layer attempting to construct an object of type " +
                    typeOfUserBasedPersistentEntity.getCanonicalName() + " inside UserBasedRepository.");
        }
        // Populate the user part of t
        t.setUser(RepositoryUtil.getRestrictingUser());
        return t;
    }
}

