/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wolfesoftware.stocks.controller;


import com.wolfesoftware.stocks.model.VersionResponse;
import com.wolfesoftware.stocks.service.VersionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
public class VersionController {

    @Resource
    VersionService versionService;


    @GetMapping(value = "/version")
    public VersionResponse getBuildInfo() {
        return versionService.buildVersionResponse();
    }


}
