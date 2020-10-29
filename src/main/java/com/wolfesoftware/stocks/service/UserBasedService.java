package com.wolfesoftware.stocks.service;

import com.wolfesoftware.stocks.exception.NotFoundException;
import com.wolfesoftware.stocks.model.UserBasedPersistentEntity;
import com.wolfesoftware.stocks.repository.UserBasedRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public abstract class UserBasedService<T extends UserBasedPersistentEntity> {



    protected abstract UserBasedRepository<T> getRepo();
    protected abstract String getEntityNameForMessage();




    @Transactional
    public T retrieveById(Long id) {
        Optional<T> optionalT = getRepo().retrieveById(id);
        if (optionalT.isEmpty()) {
            throw new NotFoundException("The requested " +   getEntityNameForMessage() + " could not be found.");
        }
        else {
            return optionalT.get();
        }
    }

    @Transactional
    public List<T> retrieveAll() {
        return getRepo().retrieveAll();
    }



    @Transactional
    public void deleteById(Long portfolioId) {
        if (!getRepo().existsById(portfolioId))
            throw new NotFoundException("The requested " + getEntityNameForMessage() + " could not be found.");

        getRepo().deleteById(portfolioId);
    }


}
