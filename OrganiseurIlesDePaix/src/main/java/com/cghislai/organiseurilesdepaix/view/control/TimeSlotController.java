/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseurilesdepaix.view.control;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

/**
 *
 * @author cghislai
 */
@Named
@ApplicationScoped
public class TimeSlotController {

    private List<Long> allSlots;

    @PostConstruct
    public void init() {
        allSlots = new ArrayList<>();
        for (long time = 10; time < 20; time += 2) {
            allSlots.add(time);
        }
    }

    public List<Long> getAllSlots() {
        return allSlots;
    }

    public String getSlotLabel(Long time) {
        String label = time + "h - " + (time + 2) + "h";
        return label;
    }

}
