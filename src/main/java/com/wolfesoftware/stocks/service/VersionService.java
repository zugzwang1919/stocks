package com.wolfesoftware.stocks.service;

import com.wolfesoftware.stocks.exception.IllegalActionException;
import com.wolfesoftware.stocks.model.Option;
import com.wolfesoftware.stocks.model.Stock;
import com.wolfesoftware.stocks.model.VersionResponse;
import com.wolfesoftware.stocks.repository.OptionRepository;
import com.wolfesoftware.stocks.repository.UserBasedRepository;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
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
