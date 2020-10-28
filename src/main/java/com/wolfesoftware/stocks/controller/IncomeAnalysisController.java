/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wolfesoftware.stocks.controller;


import com.wolfesoftware.stocks.model.calculator.IncomeAnalysisResponse;
import com.wolfesoftware.stocks.service.calculator.IncomeAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
public class IncomeAnalysisController {
    private static final Logger logger = LoggerFactory.getLogger(IncomeAnalysisController.class);

    @Resource
    IncomeAnalysisService incomeAnalysisService;


    @RequestMapping(value = "/income-analysis", method = RequestMethod.POST)
    public IncomeAnalysisResponse performAnalysis(@RequestParam("beginDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate beginDate,
                                                  @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                                  @RequestParam List<Long> portfolioIds, @RequestParam List<Long> stockIds,
                                                  @RequestParam Boolean includeDividends, @RequestParam Boolean includeOptions) {
        logger.debug("Inside performAnalysis() - Begin Date = {}", beginDate == null ? null : beginDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        logger.debug("Inside performAnalysis() - End Date = {}", endDate == null ? null : endDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        logger.debug("Inside performAnalysis() - portfolioIds = {}", portfolioIds);
        logger.debug("Inside performAnalysis() - stockIds = {}", stockIds);
        logger.debug("Inside performAnalysis() - includeDividends = {}", includeDividends);
        logger.debug("Inside performAnalysis() - includeOptions = {}", includeOptions);
        return incomeAnalysisService.analyze(beginDate, endDate, portfolioIds, stockIds, includeDividends, includeOptions);
    }


}
