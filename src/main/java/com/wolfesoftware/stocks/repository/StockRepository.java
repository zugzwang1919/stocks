package com.wolfesoftware.stocks.repository;

import com.wolfesoftware.stocks.exception.NotFoundException;
import com.wolfesoftware.stocks.model.Stock;
import com.wolfesoftware.stocks.model.User;
import com.wolfesoftware.stocks.repository.cloaked.UserBasedRepositoryForStocks;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@Repository
public class StockRepository extends UserBasedRepository<Stock>{
    @Resource
    UserBasedRepositoryForStocks userBasedRepositoryForStocks;


    // Configure this class to be a subclass of  UserBasedRepository
    public StockRepository() {
        super(Stock.class);
    }
    protected JpaRepository<Stock, Long> getUserBasedPersistEntityRepository() {
        return userBasedRepositoryForStocks;
    }

    // CREATE


    // RETRIEVE - NOTE: retrieveById() and retrieveAll() are handled by UserBasedRepository

    public List<Stock> retrieveAllBenchmarks() {
        // Get the current user
        User currentUser = RepositoryUtil.getCurrentUser();
        return userBasedRepositoryForStocks.findAllBenchmarksByUser(currentUser);
    }


    // EXISTENCE

    public boolean existsByTicker(String ticker) {
        // NOTE:  It's worth noting that we need to check for null.  If we insert the null into an
        // NOTE:  EXAMPLE we'll search for all objects of this type
        if (ticker == null)
            return false;
        Stock s = buildExampleWithUser();
        s.setTicker(ticker);
        return userBasedRepositoryForStocks.exists(Example.of(s));
    }


    // UPDATE
    // NOTE: For anyone using this as a model, UPDATE is the only thing that is not handled well, by the
    // NOTE: persistent objects.  This layer must ensure that the caller has not manipulated things like the "user" and
    // NOTE: the "createdate".
    // NOTE:
    public Stock updateStock(Long id, String name, boolean benchmark) {

        // If the stock does not exist OR does not belong to the 'current user', just stop
        Optional<Stock> possibleStockInDB = retrieveById(id);
        if (possibleStockInDB.isEmpty())
            throw new NotFoundException("The requested stock could not be found.");

        Stock intermediateStock = possibleStockInDB.get();

        // Update the stock with new info (leaving alone stuff like User, createdate, updatedate)
        intermediateStock.setName(name);
        intermediateStock.setBenchmark(benchmark);

        // Send it to the database
        return userBasedRepositoryForStocks.save(intermediateStock);
    }

    // DELETE

    @Override
    public void deleteListOfIds(List<Long> ids) {
        User currentUser = RepositoryUtil.getCurrentUser();
        userBasedRepositoryForStocks.deleteByUserAndIdIn(currentUser, ids);
    }
}
