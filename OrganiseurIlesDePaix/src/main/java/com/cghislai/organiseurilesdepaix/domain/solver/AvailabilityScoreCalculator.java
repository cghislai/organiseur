/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseurilesdepaix.domain.solver;

import com.cghislai.organiseurilesdepaix.domain.Location;
import com.cghislai.organiseurilesdepaix.domain.User;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.impl.score.director.easy.EasyScoreCalculator;

/**
 *
 * @author cghislai
 */
public class AvailabilityScoreCalculator implements EasyScoreCalculator<CampaignDaySolution> {

    @Override
    public HardMediumSoftScore calculateScore(CampaignDaySolution solution) {
        List<CampaignSolutionEvent> events = solution.getEvents();

        int hardScore = 0;
        for (TimeSlot timeSlot : solution.getTimeSlots()) {
            // Some user at two locations
            List<User> users = events.stream()
                    .filter(e -> e.getTimeSlot() == timeSlot)
                    .map(e -> e.getUser())
                    .distinct()
                    .collect(Collectors.toList());
            for (User user : users) {
                long count = events.stream()
                        .filter(e -> e.getTimeSlot() == timeSlot)
                        .filter(e -> e.getUser() == user)
                        .map(e -> e.getLocation())
                        .distinct()
                        .count();
                if (count > 1) {
                    hardScore -= count;
                }
            }
        }

        // Event w/o locations
        int mediumScore = 0;
        long count = events.stream()
                .map(e -> e.getLocation())
                .filter(l -> l == null)
                .count();
        mediumScore -= count;

        int softScore = 0;
        List<Location> locations = solution.getLocations();
        for (Location location : locations) {
            for (TimeSlot timeSlot : solution.getTimeSlots()) {
                long totalPersons = events.stream()
                        .filter(e -> e.getLocation() == location)
                        .filter(e -> e.getTimeSlot() == timeSlot)
                        .mapToInt(e -> e.getPersonAmount())
                        .sum();
                long expected = location.getRequriedPersonAmount();
                long diff = expected - totalPersons;
                if (diff > 0) {
                    softScore -= diff;
                }
            }
        }

        // Try to keep user at a single location
        List<User> users = events.stream()
                .map(e -> e.getUser())
                .distinct()
                .collect(Collectors.toList());
        for (User user : users) {
            long userLocationCount = events.stream()
                    .filter(e -> e.getUser() == user)
                    .map(e -> e.getLocation())
                    .filter(l -> l != null)
                    .distinct()
                    .count();
            if (userLocationCount > 1) {
                softScore -= userLocationCount * 3;
            }
        }
//        LOG.info(events.size() + " events for hard " + hardScore + " soft " + softScore);
        return HardMediumSoftScore.valueOf(hardScore, mediumScore, softScore);
    }
    private static final Logger LOG = Logger.getLogger(AvailabilityScoreCalculator.class.getName());

}
