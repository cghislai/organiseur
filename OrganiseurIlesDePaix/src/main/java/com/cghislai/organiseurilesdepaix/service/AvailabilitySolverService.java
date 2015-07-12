/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseurilesdepaix.service;

import com.cghislai.organiseurilesdepaix.domain.Availability;
import com.cghislai.organiseurilesdepaix.domain.Availability_;
import com.cghislai.organiseurilesdepaix.domain.CampaignDay;
import com.cghislai.organiseurilesdepaix.domain.CampaignEvent;
import com.cghislai.organiseurilesdepaix.domain.Location;
import com.cghislai.organiseurilesdepaix.domain.User;
import com.cghislai.organiseurilesdepaix.service.search.AvailabilitySearch;
import com.cghislai.organiseurilesdepaix.service.search.SolverInfo;
import com.cghislai.organiseurilesdepaix.service.search.SolverUserInfo;
import com.cghislai.organiseurilesdepaix.util.DateUtils;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

/**
 *
 * @author cghislai
 */
@Stateless
public class AvailabilitySolverService {

    private static final Logger LOG = Logger.getLogger(AvailabilitySolverService.class.getName());

    @PersistenceContext
    private EntityManager entitymanager;
    @EJB
    private AvailabilityService availabilityService;

    @Asynchronous
    public Future<List<CampaignEvent>> resolve(SolverInfo solverPreference) {
        List<CampaignEvent> campaignEvents = new ArrayList<>();
        for (CampaignDay campaignDay : solverPreference.getCampaignDays()) {
            List<CampaignEvent> newEvents = resolveDay(campaignDay, solverPreference);
            campaignEvents.addAll(newEvents);
        }
        return new AsyncResult<>(campaignEvents);
    }

    private List<CampaignEvent> resolveDay(CampaignDay campaignDay, SolverInfo solverPreference) {
        List<Location> locations = solverPreference.getLocations();
        List<User> users = findUsersWithAvailabilitesForday(campaignDay);
        // Sort availabilities per user
        Map<User, SolverUserInfo> userInfos = fetchUserInfos(users, campaignDay, locations);

        // Traverse the time through the day
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(campaignDay.getDate());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        int startDate = calendar.get(Calendar.DATE);
        final List<CampaignEvent> dayCampaignEvents = new ArrayList<>();
        final DateFormat timeFormat = new SimpleDateFormat("HH:mm");

        TimeLoop:
        while (calendar.get(Calendar.HOUR_OF_DAY) < 24
                && calendar.get(Calendar.DATE) == startDate) {
            final Date calendarTime = DateUtils.timeOnly(calendar.getTime());
            LOG.info("At " + timeFormat.format(calendarTime));
            // Sort location according to requiredAmount
            Collections.sort(locations, new Comparator<Location>() {

                @Override
                public int compare(Location o1, Location o2) {
                    int availableAmount1 = calcAvailableAmount(calendarTime, o1, dayCampaignEvents);
                    Integer required1 = o1.getRequriedPersonAmount();
                    int missing1 = required1 - availableAmount1;
                    int availableAmount2 = calcAvailableAmount(calendarTime, o2, dayCampaignEvents);
                    Integer required2 = o2.getRequriedPersonAmount();
                    int missing2 = required2 - availableAmount2;

                    return missing2 - missing1;
                }
            });

            // While there are still availabilities
            int availabilitiesCount = countAvailabilitiesForTime(calendarTime, userInfos);
            while (availabilitiesCount > 0) {
                LOG.info(" ..." + availabilitiesCount + " availabilities remaining");
                // Assign the one with the greatest amount to each location
                for (Location location : locations) {
                    int availableAmount = calcAvailableAmount(calendarTime, location, dayCampaignEvents);
                    Integer required = location.getRequriedPersonAmount();
                    int missingAmount = required - availableAmount;
                    if (missingAmount <= 0) {
                        continue;
                    }
                    LOG.info("  - " + location.getName() + " misses " + missingAmount);

                    // find availabilities for that time
                    List<Availability> availabilitiesForTime = fetchAvailabilitiesForTime(calendarTime, location, userInfos);
                    if (availabilitiesForTime.isEmpty()) {
                        continue;
                    }
                    LOG.info("    " + availabilitiesForTime.size() + " availabilities");
                    // Sort them by user amount
                    Collections.sort(availabilitiesForTime, new Comparator<Availability>() {

                        @Override
                        public int compare(Availability o1, Availability o2) {
                            Integer person1 = o1.getPersonAmount();
                            Integer person2 = o2.getPersonAmount();
                            return person1.compareTo(person2);
                        }
                    });
                    // Assign the first one
                    Availability availability = availabilitiesForTime.get(0);
                    LOG.info("    Assigning " + printAvilability(availability));

                    CampaignEvent campaignEvent = new CampaignEvent();
                    campaignEvent.setCampaignDay(campaignDay);
                    campaignEvent.setLocation(location);
                    campaignEvent.setStartTime(availability.getStartTime());
                    campaignEvent.setEndTime(availability.getEndTime());
                    campaignEvent.setPersonAmount(availability.getPersonAmount());
                    campaignEvent.setUser(availability.getUser());
                    dayCampaignEvents.add(campaignEvent);

                    SolverUserInfo userInfo = userInfos.get(availability.getUser());
                    removeOverlappingEvents(campaignEvent, userInfo);

                }
                availabilitiesCount = countAvailabilitiesForTime(calendarTime, userInfos);
            }
            calendar.add(Calendar.MINUTE, 30);
        }

        return dayCampaignEvents;
    }

    private String printAvilability(Availability availability) {
        StringBuilder builder = new StringBuilder();
        builder.append(availability.getUser().getUserName());
        builder.append(" (");
        builder.append(availability.getPersonAmount());
        builder.append(") at ");
        builder.append(availability.getLocation().getName());
        builder.append(", ");
        DateFormat timeFormat = new SimpleDateFormat("HH:mm");
        String startTimeString = timeFormat.format(availability.getStartTime());
        String endTimeString = timeFormat.format(availability.getEndTime());
        builder.append(startTimeString);
        builder.append(" -> ");
        builder.append(endTimeString);
        return builder.toString();
    }

    private Map<User, SolverUserInfo> fetchUserInfos(List<User> users, CampaignDay campaignDay, List<Location> locations) {
        Map<User, SolverUserInfo> userInfos = new HashMap<>();
        for (User user : users) {
            SolverUserInfo userInfo = new SolverUserInfo();
            userInfo.setCampaignDay(campaignDay);
            userInfo.setUser(user);
            Map<Location, List<Availability>> sortedAvailabilities = new HashMap<>();
            Map<Location, Integer> personAmountMap = new HashMap<>();

            AvailabilitySearch availabilitySearch = new AvailabilitySearch();
            availabilitySearch.setCampaignDay(campaignDay);
            availabilitySearch.setUser(user);
            for (Location location : locations) {
                availabilitySearch.setLocation(location);
                List<Availability> availabilities = availabilityService.findAvailabilities(availabilitySearch);
                if (availabilities.isEmpty()) {
                    continue;
                }
                Collections.sort(availabilities, new Comparator<Availability>() {

                    @Override
                    public int compare(Availability o1, Availability o2) {
                        return o1.getStartTime().compareTo(o2.getStartTime());
                    }
                });
                sortedAvailabilities.put(location, availabilities);
                Integer personAmount = 0;
                for (Availability availability : availabilities) {
                    if (personAmount < availability.getPersonAmount()) {
                        personAmount = availability.getPersonAmount();
                    }
                }
                personAmountMap.put(location, personAmount);
            }
            userInfo.setAvailabilitiesPerLocation(sortedAvailabilities);
            userInfo.setPersonAmountPerLocation(personAmountMap);
            userInfos.put(user, userInfo);
        }
        return userInfos;
    }

    private List<User> findUsersWithAvailabilitesForday(CampaignDay campaignDay) {
        CriteriaBuilder builder = entitymanager.getCriteriaBuilder();
        CriteriaQuery<User> query = builder.createQuery(User.class);
        Root<Availability> root = query.from(Availability.class);

        Path<User> userPath = root.get(Availability_.user);
        Path<CampaignDay> dayPath = root.get(Availability_.campaignDay);

        query.select(userPath);
        query.distinct(true);
        query.where(builder.equal(dayPath, campaignDay));

        TypedQuery<User> typedQuery = entitymanager.createQuery(query);
        return typedQuery.getResultList();
    }

    private int calcAvailableAmount(Date calendarTime, Location location, List<CampaignEvent> campaignEvents) {
        int available = 0;
        for (CampaignEvent campaignEvent : campaignEvents) {
            Location eventLocation = campaignEvent.getLocation();
            if (eventLocation != location) {
                continue;
            }
            Date startTime = campaignEvent.getStartTime();
            startTime = DateUtils.timeOnly(startTime);
            Date endTime = campaignEvent.getEndTime();
            endTime = DateUtils.timeOnly(endTime);
            if (startTime.after(calendarTime) || endTime.before(calendarTime)) {
                continue; // TODO: break ?
            }
            Integer personAmount = campaignEvent.getPersonAmount();
            available += personAmount;
        }
        return available;
    }

    private void removeOverlappingEvents(CampaignEvent campaignEvent, SolverUserInfo userInfo) {
        Date startTime = campaignEvent.getStartTime();
        Date endTime = campaignEvent.getEndTime();

        Map<Location, List<Availability>> availabilitiesPerLocation = userInfo.getAvailabilitiesPerLocation();
        Iterator<Location> keyIterator = availabilitiesPerLocation.keySet().iterator();
        while (keyIterator.hasNext()) {
            Location nextKey = keyIterator.next();
            List<Availability> availabilities = availabilitiesPerLocation.get(nextKey);
            Iterator<Availability> availabilitiesIterator = availabilities.iterator();
            while (availabilitiesIterator.hasNext()) {
                Availability availability = availabilitiesIterator.next();
                Date availabilityStartTime = availability.getStartTime();
                Date availabilityEndTime = availability.getEndTime();
                if (availabilityStartTime.after(endTime)
                        || availabilityEndTime.before(startTime)) {
                    continue;
                }
                // Overlapping
                LOG.info("        removing overlapping" + printAvilability(availability));
                availabilitiesIterator.remove();
            }
            if (availabilities.isEmpty()) {
                keyIterator.remove();
            }
        }
    }

    private List<Availability> fetchAvailabilitiesForTime(Date calendarTime, Location location, Map<User, SolverUserInfo> userInfos) {
        List<Availability> availabilities = new ArrayList<>();
        for (SolverUserInfo userInfo : userInfos.values()) {
            List<Availability> userAvailabilities = userInfo.getAvailabilitiesPerLocation().get(location);
            if (userAvailabilities == null) {
                continue;
            }
            for (Availability availability : userAvailabilities) {
                Date startTime = availability.getStartTime();
                startTime = DateUtils.timeOnly(startTime);
                if (startTime.after(calendarTime)) {
                    continue;
                }
                Date endTime = availability.getEndTime();
                endTime = DateUtils.timeOnly(endTime);
                if (endTime.before(calendarTime)) {
                    continue;
                }
                availabilities.add(availability);
            }
        }
        return availabilities;
    }

    private int countAvailabilitiesForTime(Date calendarTime, Map<User, SolverUserInfo> userInfos) {
        int count = 0;
        for (SolverUserInfo userInfo : userInfos.values()) {
            for (Location location : userInfo.getAvailabilitiesPerLocation().keySet()) {
                List<Availability> userAvailabilities = userInfo.getAvailabilitiesPerLocation().get(location);
                for (Availability availability : userAvailabilities) {
                    Date startTime = availability.getStartTime();
                    startTime = DateUtils.timeOnly(startTime);
                    if (startTime.after(calendarTime)) {
                        continue;
                    }
                    Date endTime = availability.getEndTime();
                    endTime = DateUtils.timeOnly(endTime);
                    if (endTime.before(calendarTime)) {
                        continue;
                    }
                    count++;
                }
            }
        }
        return count;
    }
}
