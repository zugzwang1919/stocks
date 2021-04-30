package com.wolfesoftware.stocks.service;

import ch.qos.logback.core.util.COWArrayList;
import com.wolfesoftware.stocks.model.Profile;
import com.wolfesoftware.stocks.model.User;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProfileService {

    @Resource
    UserService userService;

    @Resource
    OptionService optionService;

    @Resource
    OptionTransactionService optionTransactionService;

    @Resource
    StockService stockService;

    @Resource
    StockTransactionService stockTransactionService;

    public Profile buildProfileOfCurrentUser() {

        Profile profile = new Profile();

        // Fill in Username
        User user = userService.getCurrentUser();
        profile.setUserName(user.getUsername());

        List<Profile.AuthenticationSupported> authenticationsSupported = new ArrayList<>();
        if (user.getPassword() != null) {
            authenticationsSupported.add(Profile.AuthenticationSupported.ID_PW);
        }
        if (user.getGoogleid() != null) {
            authenticationsSupported.add(Profile.AuthenticationSupported.GOOGLE);
        }
        profile.setAuthenticationsSupported(authenticationsSupported);

        // Fill in Count of Stocks being tracked by User
        profile.setNumberOfStocks(stockService.count());

        // Fill in Count of Options being tracked by User
        profile.setNumberOfOptions(optionService.count());

        // Fill in Count of Stock Transactions being tracked by User
        profile.setNumberOfStockTransactions(stockTransactionService.count());

        // Fill in Count of Option Transactions being tracked by User
        profile.setNumberOfOptionTransactions(optionTransactionService.count());


        return profile;
    }


}
