package com.wolfesoftware.stocks.repository;

import com.wolfesoftware.stocks.common.BridgeToSpringBean;
import com.wolfesoftware.stocks.exception.NotFoundException;
import com.wolfesoftware.stocks.model.Portfolio;
import com.wolfesoftware.stocks.repository.cloaked.UserBasedRepositoryForPortfolios;
import com.wolfesoftware.stocks.service.PortfolioService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Optional;

@Repository
public class PortfolioRepository extends UserBasedRepository<Portfolio> {
    @Resource
    UserBasedRepositoryForPortfolios userBasedRepositoryForPortfolios;


    // Configure this class to be a subclass of  UserBasedRepository
    public PortfolioRepository(){
        super(Portfolio.class);
    }
    @Override
    protected JpaRepository<Portfolio, Long> getUserBasedPersistEntityRepository() {
        return userBasedRepositoryForPortfolios;
    }



    // UPDATE
    // NOTE: For anyone using this as a model, UPDATE is the only thing that is not handled well, by the
    // NOTE: persistent objects.  This layer must ensure that the caller has not manipulated things like the "user" and
    // NOTE: the "createdate".
    public Portfolio updatePortfolio(Long id, String portfolioName) {

        // Can't just AutoWire the Portfolio Service as it will create a circular reference
        PortfolioService portfolioService = BridgeToSpringBean.getBean(PortfolioService.class);
        Portfolio intermediatePortfolio = portfolioService.retrieveById(id);

        // Update the portfolio with new info (leaving alone stuff like User, createdate, updatedate)
        intermediatePortfolio.setPortfolioName(portfolioName);

        // Send it to the database
        return userBasedRepositoryForPortfolios.save(intermediatePortfolio);
    }




}
