package com.wolfesoftware.stocks.service;

import com.wolfesoftware.stocks.model.VersionResponse;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
public class VersionService {

    @Resource
    BuildProperties buildProperties;


    public VersionResponse buildVersionResponse() {

        return new VersionResponse( buildProperties.getVersion(),
                                    LocalDate.ofInstant(buildProperties.getTime(), ZoneId.systemDefault()),
                                    buildProperties.get("gitHash"),
                                    buildProperties.get("gitHashFull"));
    }


}
