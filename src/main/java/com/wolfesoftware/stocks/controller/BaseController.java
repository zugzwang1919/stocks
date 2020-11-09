package com.wolfesoftware.stocks.controller;

import com.wolfesoftware.stocks.model.UserBasedPersistentEntity;
import com.wolfesoftware.stocks.service.UserBasedService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public abstract class BaseController<T extends UserBasedPersistentEntity> {

    protected abstract UserBasedService<T> getService();



    // DELETE
    @DeleteMapping("/{id}")
    public void deleteStockTransaction(@PathVariable("id") Long id) {
        getService().deleteById(id);
    }

    @DeleteMapping("")
    public void deleteList(@RequestParam List<Long> ids) {
        getService().deleteList(ids);
    }

}
