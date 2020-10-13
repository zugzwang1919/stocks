package com.wolfesoftware.stocks.controller;

import com.wolfesoftware.stocks.model.LoadOrUpdateResponse;
import com.wolfesoftware.stocks.model.StockDividend;
import com.wolfesoftware.stocks.model.StockPrice;
import com.wolfesoftware.stocks.service.StockDividendService;
import com.wolfesoftware.stocks.service.StockPriceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDate;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Resource
    StockPriceService stockPriceService;
    @Resource
    StockDividendService stockDividendService;

    private static Logger logger = LoggerFactory.getLogger(AdminController.class);

    @PostMapping("/reloadAllStockPrices")
    public LoadOrUpdateResponse reloadAllStockPrices() {
        logger.debug("Inside reloadAllStockPrices()");

        LocalDate beginDate = StockPrice.EARLIEST_DAILY_PRICE;
        LocalDate endDate = LocalDate.now();
        LoadOrUpdateResponse response = stockPriceService.loadOrUpdateAllStockPrices(beginDate, endDate);

        logger.debug(response.getSummary());
        return response;
    }

    @PostMapping("/reloadAllDividends")
    public LoadOrUpdateResponse reloadAllDividends() {
        logger.debug("Inside reloadAllDividends()");

        LocalDate beginDate = StockDividend.EARLIEST_STOCK_DIVIDEND_DATE;
        LocalDate endDate = LocalDate.now();
        LoadOrUpdateResponse response = stockDividendService.loadOrUpdateDividendsForAllSecurities(beginDate, endDate);

        logger.debug(response.getSummary());
        return response;
    }

}
