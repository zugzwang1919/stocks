package com.wolfesoftware.stocks.controller;

import com.wolfesoftware.stocks.model.UserBasedPersistentEntity;
import com.wolfesoftware.stocks.service.UserBasedService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public abstract class BaseController<T extends UserBasedPersistentEntity> {

    protected abstract UserBasedService<T> getService();

    @DeleteMapping("")
    public void deleteList(@RequestParam List<Long> ids) {
        ids.stream().forEach(id -> getService().deleteById(id));
    }

}
