/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseurilesdepaix.domain.solver;

import com.cghislai.organiseurilesdepaix.domain.Availability;
import java.util.Calendar;

/**
 *
 * @author cghislai
 */
public class TimeSlot {

    private int hour;

    public static boolean availabilityMatches(TimeSlot timeSlot, Availability availability) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(availability.getStartTime());
        int startHour = calendar.get(Calendar.HOUR_OF_DAY);
        if (timeSlot.getHour() < startHour) {
            return false;
        }
        calendar.setTime(availability.getEndTime());
        int endHour = calendar.get(Calendar.HOUR_OF_DAY);
        if (timeSlot.getHour() + 1 > endHour) {
            return false;
        }
        return true;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

}
