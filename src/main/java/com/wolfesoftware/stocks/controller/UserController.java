package com.wolfesoftware.stocks.controller;

import com.wolfesoftware.stocks.model.User;
import com.wolfesoftware.stocks.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping(path = "")
    User create(@RequestBody User user) {
        return userService.createUser(user.getUsername(), user.getPassword(), user.getEmailaddress());
    }


    @DeleteMapping(path = "/{id}")
    void delete(@PathVariable("id") Long id) {
        userService.deleteUser(id);
    }

}
