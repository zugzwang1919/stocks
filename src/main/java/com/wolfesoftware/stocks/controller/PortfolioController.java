package com.wolfesoftware.stocks.controller;

import com.wolfesoftware.stocks.model.Portfolio;
import com.wolfesoftware.stocks.model.Stock;
import com.wolfesoftware.stocks.service.PortfolioService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/portfolio")
public class PortfolioController extends BaseController<Portfolio>{

    @Resource
    PortfolioService portfolioService;

    // CREATE
    @PostMapping("")
    public Portfolio createPortfolio(@RequestParam(name = "portfolioName") String portfolioName) {
        return portfolioService.create(portfolioName);
    }

    // RETRIEVE - Handled by BaseController

    // UPDATE
    @PostMapping("/{id}")
    public Portfolio updatePortfolio(@PathVariable("id") Long id, @RequestParam(name = "portfolioName") String portfolioName) {
        return portfolioService.updatePortfolio(id, portfolioName);
    }

    // DELETE - Handled by BaseController


    // OTHER FUNCTIONALITY - Get a list of all stocks with transactions or stock option transactions in the list of portfolios
    @GetMapping("/tickers")
    public List<Stock> retrieveStocksUsedInPortfolios(@RequestParam(required = false) List<Long> portfolioIds) {
        return portfolioService.retrieveStocksUsedInPortfolios(portfolioIds);
    }


    // Used by BaseController
    protected PortfolioService getService() {
        return portfolioService;
    }}
