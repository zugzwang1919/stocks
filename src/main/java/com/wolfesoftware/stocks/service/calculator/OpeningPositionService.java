package com.wolfesoftware.stocks.service.calculator;

import com.wolfesoftware.stocks.model.StockSplitCache;
import com.wolfesoftware.stocks.model.StockTransaction;
import com.wolfesoftware.stocks.model.calculator.OpeningPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class OpeningPositionService extends PositionService {

    private static final Logger logger = LoggerFactory.getLogger(OpeningPositionService.class);
    private static final DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE;



    public OpeningPosition buildOpeningPosition(List<StockTransaction> stockTransactions, StockSplitCache stockSplitCache, LocalDate beginDate, LocalDate endDate) {

        String debugString = "Creating Opening Position on " + dtf.format(beginDate);
        logger.debug("STARTING - " + debugString);
        StopWatch stopWatch = new StopWatch(debugString);
        stopWatch.start();
        List<StockTransaction> clonedList = new ArrayList<>(stockTransactions);
        clonedList.sort( new StockTransaction.StockTransactionComparator(StockTransaction.SortBy.DATE));
        LocalDate accumulationDate = null;
        boolean positionEstablished = false;
        OpeningPosition position = null;
        for( StockTransaction stockTransaction: clonedList) {
            if (position == null) {
                position = new OpeningPosition(stockTransaction.getStock(), stockTransaction.getDate());
                position.setContainsOlderTransactions(false);
            }

            if (stockTransaction.getDate().isBefore(beginDate)) {
                addStockTransactionSizeToPosition(beginDate, position, stockTransaction, stockSplitCache);
                position.setDate(beginDate);
                position.setContainsOlderTransactions(true);
                positionEstablished = true;
            }
            else if(!positionEstablished && !stockTransaction.getDate().isAfter(endDate)) {
                if (accumulationDate == null)
                    accumulationDate = stockTransaction.getDate();
                if(stockTransaction.getDate().equals(accumulationDate))
                    addStockTransactionSizeToPosition(accumulationDate, position, stockTransaction, stockSplitCache);
            }
            // If we ever detect that the size of the opening position is zero (there has been a buy and sell of the same amount),
            // null everything out and act like a position was never established in the first place
            if (position.getSize().compareTo(BigDecimal.ZERO) == 0) {
                position = null;
                accumulationDate = null;
                positionEstablished = false;
            }
        }


        // Calculate the value of the position if there is one.
        if (position != null && position.containsOlderTransactions())
            calculateValue(position, stockSplitCache);

        stopWatch.stop();
        logger.debug(stopWatch.prettyPrint());
        return position;
    }
}
