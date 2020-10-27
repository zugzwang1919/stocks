package com.wolfesoftware.stocks.repository;

import com.wolfesoftware.stocks.model.Option;
import com.wolfesoftware.stocks.model.Stock;
import com.wolfesoftware.stocks.repository.cloaked.UserBasedRepositoryForOptions;
import com.wolfesoftware.stocks.service.OptionService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;

@Repository
public class OptionRepository extends UserBasedRepository<Option> {
    @Resource
    private UserBasedRepositoryForOptions userBasedRepositoryForOptions;

    @Resource
    private OptionService optionService;

    // Configure this class to be a subclass of  UserBasedRepository
    public OptionRepository() {
        super(Option.class);
    }
    @Override
    protected JpaRepository<Option, Long> getUserBasedPersistEntityRepository() {
        return userBasedRepositoryForOptions;
    }



    // UPDATE
    // NOTE: For anyone using this as a model, UPDATE is the only thing that is not handled well, by the
    // NOTE: persistent objects.  This layer must ensure that the caller has not manipulated things like the "user" and
    // NOTE: the "createdate".
    // NOTE:

    public Option update(Long id, Option.OptionType optionType, Stock stock, BigDecimal strikePrice, LocalDate expirationDate) {

        // Get the Option being requested (The Option Service will throw NotFoundException if the event
        // of a non-existent Option or the caller does not have access)
        Option intermediateOption = optionService.retrieveById(id);

        // Update the option with new info (leaving alone stuff like User, createdate, updatedate)

        intermediateOption.setOptionType(optionType);
        intermediateOption.setStock(stock);
        intermediateOption.setStrikePrice(strikePrice);
        intermediateOption.setExpirationDate(expirationDate);

        // Send it to the database
        return userBasedRepositoryForOptions.save(intermediateOption);
    }



}
