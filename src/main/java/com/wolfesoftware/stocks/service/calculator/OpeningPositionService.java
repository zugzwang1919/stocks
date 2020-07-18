package com.wolfesoftware.stocks.service.calculator;

import com.wolfesoftware.stocks.model.StockTransaction;
import com.wolfesoftware.stocks.model.calculator.OpeningPosition;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class OpeningPositionService extends PositionService {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(OpeningPositionService.class);




    public OpeningPosition buildOpeningPosition(List<StockTransaction> stockTransactions, LocalDate beginDate, LocalDate endDate) {

        List<StockTransaction> clonedList = new ArrayList<>(stockTransactions);
        Collections.sort(clonedList, new StockTransaction.StockTransactionComparator(StockTransaction.SortBy.DATE));
        LocalDate accumulationDate = null;
        boolean positionEstablished = false;
        OpeningPosition position = null;
        for( StockTransaction stockTransaction: clonedList) {
            if (position == null) {
                position = new OpeningPosition(stockTransaction.getStock(), stockTransaction.getDate());
                position.setContainsOlderTransactions(false);
            }

            if (stockTransaction.getDate().isBefore(beginDate)) {
                addStockTransactionSizeToPosition(beginDate, position, stockTransaction);
                position.setDate(beginDate);
                position.setContainsOlderTransactions(true);
                positionEstablished = true;
            }
            else if(!positionEstablished && !stockTransaction.getDate().isAfter(endDate)) {
                if (accumulationDate == null)
                    accumulationDate = stockTransaction.getDate();
                if(stockTransaction.getDate().equals(accumulationDate))
                    addStockTransactionSizeToPosition(accumulationDate, position, stockTransaction);
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
            calculateValue(position);

        logger.debug("Opening PositionService: " + position);
        return position;
    }
}
