package com.semantive.commons;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @author Jacek Lewandowski
 */
public class DateTimeUtils {
    public final static int[] TIME_SET = new int[]{Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND};

    public final static int[] DATE_SET = new int[]{Calendar.ERA, Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH};

    /**
     * Oblicza ile roznice w miesiacach pomiedzy dayWhenCalc i birthDate. Wykorzysytwane do sprawdzenia wieku zwierzecia w dniu uboju
     *
     * @param birthDate
     * @param dayWhenCalc
     * @return roznica w miesiacach pomiedzy dayWhenCalc i birthDate
     */
    public static int calcMonthAgeInDate(Date birthDate, Date dayWhenCalc) {
        Calendar curCal = Calendar.getInstance();
        if (dayWhenCalc != null) {
            curCal.setTime(dayWhenCalc);
        }

        Calendar birthCal = Calendar.getInstance();
        birthCal.setTime(birthDate);

        return curCal.get(Calendar.YEAR) * 12 + curCal.get(Calendar.MONTH) - (birthCal.get(Calendar.YEAR) * 12 + birthCal.get(Calendar.MONTH));
    }

    public static int calcMonthAge(Date birthDate) {
        return calcMonthAgeInDate(birthDate, null);
    }

    /**
     * Zeruje czas w dacie.
     */
    public static Date clearTime(Date dateTime) {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(dateTime);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * Ustawia date na koniec tego dnia
     */
    public static Date setEndOfDayDate(Date dateTime) {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(dateTime);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 99);
        return calendar.getTime();
    }

    /**
     * Wylicza dokladny wiek w miesiacach, od urodzenia do teraz
     *
     * @param birthDate data narodzin
     * @return wiek podany w double
     */
    public static double calcAccurateMonthAge(Date birthDate) {
        Calendar cur = GregorianCalendar.getInstance();
        double curTime = (double) cur.get(Calendar.YEAR) * 12d + (double) cur.get(Calendar.MONTH) + (double) (cur.get(Calendar.DAY_OF_MONTH) - 1) / (double) cur.getActualMaximum(Calendar.DAY_OF_MONTH);
        Calendar birth = GregorianCalendar.getInstance();
        birth.setTime(birthDate);
        double birthTime = (double) birth.get(Calendar.YEAR) * 12d + (double) birth.get(Calendar.MONTH) + (double) (birth.get(Calendar.DAY_OF_MONTH) - 1) / (double) birth.getActualMaximum(Calendar.DAY_OF_MONTH);
        return curTime - birthTime;
    }

    /**
     * Metoda podmienia podane pola z Calendar w dacie baseDate na pola z sourceDate.
     *
     * @param baseDate   data w której mają być podmienione pola
     * @param sourceDate data z której mają być wzięte nowe wartości pól
     * @param fields     pola - Calendar.xxx
     * @return data baseDate z podmienionymi polami
     */
    public static Date replaceDateFields(Date baseDate, Date sourceDate, int... fields) {
        Calendar base = Calendar.getInstance();
        base.setTime(baseDate);

        Calendar source = Calendar.getInstance();
        source.setTime(sourceDate);

        for (int field : fields) {
            base.set(field, source.get(field));
        }

        return base.getTime();
    }


}
