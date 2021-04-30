package com.wolfesoftware.stocks.controller;

import com.wolfesoftware.stocks.model.Profile;
import com.wolfesoftware.stocks.model.User;
import com.wolfesoftware.stocks.service.ProfileService;
import com.wolfesoftware.stocks.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    @Resource
    ProfileService profileService;

    @GetMapping(path = "")
    Profile retrieveTheOneAndOnlyProfile() {
        return profileService.buildProfileOfCurrentUser();
    }



}
