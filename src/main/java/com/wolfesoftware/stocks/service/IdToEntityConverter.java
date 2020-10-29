package com.wolfesoftware.stocks.service;

import com.wolfesoftware.stocks.exception.NotFoundException;
import com.wolfesoftware.stocks.model.UserBasedPersistentEntity;
import com.wolfesoftware.stocks.repository.UserBasedRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class IdToEntityConverter<T extends UserBasedPersistentEntity> {
    public List<T> convertFromIdsToEntities(List<Long> ids, UserBasedRepository<T> userBasedRepository, String nameOfUserBasedEntity) {
        List<T> entities = ids.stream().map(id -> {
            Optional<T> optionalEntity = userBasedRepository.retrieveById(id);
            if (optionalEntity.isEmpty())
                throw new NotFoundException("The requested " + nameOfUserBasedEntity + " with an ID of " + id + " could not be found.");
            return optionalEntity.get();
        }).collect(Collectors.toList());
        return entities;
    }
}
