package com.wolfesoftware.stocks.repository;

import com.wolfesoftware.stocks.common.BridgeToSpringBean;
import com.wolfesoftware.stocks.model.Portfolio;
import com.wolfesoftware.stocks.model.User;
import com.wolfesoftware.stocks.repository.cloaked.UserBasedRepositoryForPortfolios;
import com.wolfesoftware.stocks.service.PortfolioService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PortfolioRepository extends UserBasedRepository<Portfolio> {
    @Resource
    UserBasedRepositoryForPortfolios userBasedRepositoryForPortfolios;

    @Resource
    RepositoryUtil repositoryUtil;

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


    public List<Portfolio> retrievePortfolios(List<Long> portfolioIds) {
        // If we get a null or empty list, just return an empty list
        // Hibernate does not seem to like null/empty values
        if (portfolioIds == null || portfolioIds.isEmpty()) {
            return new ArrayList<>();
        }
        User currentUser = repositoryUtil.getCurrentUser();
        return userBasedRepositoryForPortfolios.findByUserAndIdIn(currentUser, portfolioIds);
    }

}
