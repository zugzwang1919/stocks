package com.wolfesoftware.stocks.repository;

import com.wolfesoftware.stocks.model.Authority;
import com.wolfesoftware.stocks.repository.cloaked.LowLevelAuthorityRepository;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class AuthorityRepository {

    @Resource
    LowLevelAuthorityRepository lowLevelAuthorityRepository;

    public Authority createAuthority(Authority authority) {
        return lowLevelAuthorityRepository.save(authority);
    }

}
