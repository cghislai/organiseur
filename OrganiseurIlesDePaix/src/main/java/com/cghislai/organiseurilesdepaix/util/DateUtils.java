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
}
