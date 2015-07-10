/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseurilesdepaix.util;

import static com.cghislai.organiseurilesdepaix.domain.CampaignDay_.date;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author cghislai
 */
public class DateUtils {

    public static Date dayOnly(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date dayOnlyDate = calendar.getTime();
        return dayOnlyDate;
    }

    public static Date timeOnly(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DATE, 0);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.YEAR, 0);
        Date dayOnlyDate = calendar.getTime();
        return dayOnlyDate;
    }

    public static Date timeAndDay(Date timeDate, Date dayDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dayDate);
        Calendar timeCalendar = Calendar.getInstance();
        timeCalendar.setTime(timeDate);
        int hour = timeCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = timeCalendar.get(Calendar.MINUTE);
        int second = timeCalendar.get(Calendar.SECOND);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        Date dayTimeDate = calendar.getTime();
        return dayTimeDate;
    }

    public static Date addMinutes(Date date, int minutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, minutes);
        Date newDate = calendar.getTime();
        return newDate;
    }
}
