package com.wolfesoftware.stocks.service.calculator;

import com.wolfesoftware.stocks.common.BridgeToSpringBean;
import com.wolfesoftware.stocks.model.Stock;
import com.wolfesoftware.stocks.model.StockTransaction;
import com.wolfesoftware.stocks.model.calculator.Position;
import com.wolfesoftware.stocks.service.StockPriceService;
import com.wolfesoftware.stocks.service.StockSplitService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Service
public class PositionService {

    @Resource
    StockSplitService stockSplitService;

    public Position createPreciseNonValuedPositionAfterOpeningPosition(Position openingPosition,
                                                                       List<StockTransaction> interveningTransactions, LocalDate preciseDate) {
        List<StockTransaction> clonedList = new ArrayList<>(interveningTransactions);
        clonedList.sort(new StockTransaction.StockTransactionComparator(StockTransaction.SortBy.DATE));
        Position newPosition = new Position(openingPosition);
        newPosition.setSize(adjustedSize(openingPosition.getStock(), openingPosition.getDate(), openingPosition.getSize(), preciseDate));
        newPosition.setDate(preciseDate);
        for( StockTransaction transaction : clonedList ) {
            if (!transaction.getDate().isAfter(preciseDate)) {
                newPosition = addStockTransactionSizeToPosition(preciseDate, newPosition, transaction);
            }
            else {
                break;
            }
        }
        return newPosition;
    }

    public Position createPreciseNonValuedPositionBetweenPositions(Position openingPosition, Position closingPosition,
                                                                   List<StockTransaction> interveningTransactions,
                                                                   LocalDate preciseDate ) {
        if (preciseDate.isBefore(openingPosition.getDate()) || preciseDate.isAfter(closingPosition.getDate()))
            throw new IllegalStateException("An unexpected date was provided while trying to create a position");
        if (openingPosition.getDate().equals(preciseDate))
            return openingPosition;
        if (closingPosition.getDate().equals(preciseDate))
            return closingPosition;
        return createPreciseNonValuedPositionAfterOpeningPosition(openingPosition, interveningTransactions, preciseDate);
    }



    protected Position addStockTransactionSizeToPosition(LocalDate date, Position position, StockTransaction transaction) {
        // Set most values in the returned position
        Position returnedPosition = position == null ? new Position(transaction.getStock(), date) : position;
        // Set size and value attributes in the returned position
        BigDecimal adjustedSize = adjustedSize(transaction.getStock(), transaction.getDate(), transaction.getTradeSize(), date);
        if (transaction.getActivity().equals(StockTransaction.Activity.BUY)) {
            returnedPosition.setSize(returnedPosition.getSize().add(adjustedSize));
            returnedPosition.setValue(returnedPosition.getValue().add(transaction.getAmount()));
        }
        else if (transaction.getActivity().equals(StockTransaction.Activity.SELL)) {
            returnedPosition.setSize(returnedPosition.getSize().subtract(adjustedSize));
            returnedPosition.setValue(returnedPosition.getValue().subtract(transaction.getAmount()));
        }
        return returnedPosition;
    }

    /*
    protected static void removeTransactionSizeFromPosition(PositionService position, StockTransaction transaction) {
        // Modify the size attribute
        // FIXME:  As I'm migrating this, it doesn't seem possible that the adjustedSize variable calculated below does not need to be used
        BigDecimal adjustedSize = adjustedSize(transaction.getStock(), transaction.getDate(), transaction.getTradeSize(), position.getDate());
        if (transaction.getActivity().equals(StockTransaction.Activity.BUY)) {
            position.size = position.size.subtract(transaction.getTradeSize());
            position.value = position.value.subtract(transaction.getAmount());
        }
        else if (transaction.getActivity().equals(StockTransaction.Activity.SELL)) {
            position.size = position.size.add(transaction.getTradeSize());
            position.value = position.value.add(transaction.getAmount());}
    }
    */

    protected BigDecimal adjustedSize(Stock stock, LocalDate originalDate, BigDecimal originalSize, LocalDate dateInQuestion) {
        BigDecimal adjustedSize = originalSize;
        BigDecimal multiplicativeFactor = stockSplitService.stockSplitFactorBetween(stock, originalDate, dateInQuestion);
        adjustedSize =  adjustedSize.multiply(multiplicativeFactor);
        return adjustedSize;
    }


    protected  void calculateValue(Position position) {
        if (position != null) {
            StockPriceService stockPriceService = BridgeToSpringBean.getBean(StockPriceService.class);
            BigDecimal price = stockPriceService.retrieveClosingPrice(position.getStock(), position.getDate());
            position.setValue( position.getSize().multiply(price, MathContext.UNLIMITED));
        }
    }

    /* Leaving this here in the event, we want to re-introduce a price cache */
    /*
    private static BigDecimal retrievePrice(EntityManager em, Date date, Stock stock, Map<Date,BigDecimal> priceCache){
        BigDecimal priceOnDate;
        if (priceCache == null) {
            priceOnDate = new StockPriceRetriever().getClosingPrice(em, stock, date);
        }
        else {
            priceOnDate = priceCache.get(date);
            if (priceOnDate == null) {
                priceOnDate = new StockPriceRetriever().getClosingPrice(em, stock, date);
                priceCache.put(date,priceOnDate);
            }
        }
        return priceOnDate;
    }
    */

}
