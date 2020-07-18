package com.wolfesoftware.stocks.common;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

public class LocalDateUtil {

    private static final List<DateTimeFormatter> listOfValidDateTimeFormatters = new ArrayList<>();

    static {
        DateTimeFormatter dtf1 = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern("M/d/yyyy");
        DateTimeFormatter dtf3 = DateTimeFormatter.ofPattern("MM/dd/yy");
        DateTimeFormatter dtf4 = DateTimeFormatter.ofPattern("M/d/yy");
        listOfValidDateTimeFormatters.add(dtf1);
        listOfValidDateTimeFormatters.add(dtf2);
        listOfValidDateTimeFormatters.add(dtf3);
        listOfValidDateTimeFormatters.add(dtf4);
    }

    public static LocalDate lastDayOfMonth (LocalDate inputDate ) {
        LocalDate returnedDate = inputDate.with(TemporalAdjusters.lastDayOfMonth());
        return returnedDate;
    }

    public static LocalDate fridayOfWeek (LocalDate inputDate) {
        LocalDate returnedDate = inputDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));
        return returnedDate;
    }

    public static LocalDate lastDayOfMonthOrPreviousMonth( LocalDate inputDate ) {
        LocalDate returnedDate;
        if (inputDate.equals(lastDayOfMonth(inputDate))) {
            returnedDate = inputDate;
        }
        else {
            LocalDate tempDate = inputDate.with(TemporalAdjusters.firstDayOfMonth());
            LocalDate secondTempDate = tempDate.minusMonths(1);
            returnedDate = secondTempDate.with(TemporalAdjusters.lastDayOfMonth());
        }
        return returnedDate;
    }

    public static LocalDate lastDayOfQuarter( LocalDate inputDate ) {
        int lastMonthInQuarter = ((inputDate.getMonthValue()-1)/3+1)*3;
        LocalDate returnedDate = LocalDate.of(inputDate.getYear(), Month.of(lastMonthInQuarter),1).with(TemporalAdjusters.lastDayOfMonth());
        return returnedDate;
    }

    public static LocalDate lastDayOfYearRelative( LocalDate inputDate, int index) {
        LocalDate returnedDate = LocalDate.of(inputDate.getYear()+index, Month.DECEMBER, 31);
        return returnedDate;
    }

    public static LocalDate sameDayDifferentYear( LocalDate inputDate, int index) {
        LocalDate returnedDate = LocalDate.of(inputDate.getYear()+index, inputDate.getMonth(), inputDate.getDayOfMonth());
        return returnedDate;
    }

    /*
    This calculation is non-inclusive.  Returns a positive number when laterDate
    is actually later than earlierDate.
    */
    public static long daysBetween(LocalDate earlierDate, LocalDate laterDate) {
        Duration duration = Duration.between(earlierDate.atStartOfDay(), laterDate.atStartOfDay());
        long durationInSeconds = duration.get(ChronoUnit.SECONDS);
        long durationInDays = durationInSeconds/(24*60*60);
        return durationInDays;

    }

    public static String formatMMDDYYYY(LocalDate inputDate) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        return inputDate.format(dtf);
    }

    public static LocalDate createLocalDateAllowingAVarietyOfFormats(String string) {
        LocalDate result = null;
        for (DateTimeFormatter dtf : listOfValidDateTimeFormatters) {
            try {
                result = LocalDate.parse(string, dtf);
                return result;
            }
            catch (DateTimeParseException dtpe) {
                // Do nothing just try another DateTimeFormatter
            }
        }
        return result;
    }

}
