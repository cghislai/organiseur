/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseurilesdepaix.service;

import com.cghislai.organiseurilesdepaix.domain.solver.AvailabilityScoreCalculator;
import com.cghislai.organiseurilesdepaix.domain.Availability;
import com.cghislai.organiseurilesdepaix.domain.CampaignDay;
import com.cghislai.organiseurilesdepaix.domain.solver.CampaignDaySolution;
import com.cghislai.organiseurilesdepaix.domain.CampaignEvent;
import com.cghislai.organiseurilesdepaix.domain.Location;
import com.cghislai.organiseurilesdepaix.domain.User;
import com.cghislai.organiseurilesdepaix.domain.solver.CampaignSolutionEvent;
import com.cghislai.organiseurilesdepaix.domain.solver.TimeSlot;
import com.cghislai.organiseurilesdepaix.service.search.AvailabilitySearch;
import com.cghislai.organiseurilesdepaix.service.search.LocationSearch;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.domain.ScanAnnotatedClassesConfig;
import org.optaplanner.core.config.score.definition.ScoreDefinitionType;
import org.optaplanner.core.config.score.director.ScoreDirectorFactoryConfig;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;

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
    @EJB
    private LocationService locationService;
    @EJB
    private CampaignDatesService campaignDatesService;

    @Asynchronous
    public Future<List<CampaignEvent>> resolve() {
        List<CampaignDay> campaignDays = campaignDatesService.findAllDays();
        List<CampaignEvent> campaignEvents = new ArrayList<>();
        for (CampaignDay campaignDay : campaignDays) {
            List<CampaignEvent> events = resolve(campaignDay);
            campaignEvents.addAll(events);
        }
        return new AsyncResult<>(campaignEvents);
    }

    public List<CampaignEvent> resolve(CampaignDay campaignDay) {
//        InputStream configInputStream = getClass().getClassLoader().getResourceAsStream("com/cghislai/organiseurilesdepaix/solverConfig.xml");
        SolverFactory solverFactory = SolverFactory.createEmpty();
        SolverConfig solverConfig = solverFactory.getSolverConfig();

        TerminationConfig terminationConfig = new TerminationConfig();
        terminationConfig.setSecondsSpentLimit(20L);
        solverConfig.setTerminationConfig(terminationConfig);

        ScoreDirectorFactoryConfig scoreDirectorFactoryConfig = new ScoreDirectorFactoryConfig();
        scoreDirectorFactoryConfig.setScoreDefinitionType(ScoreDefinitionType.HARD_MEDIUM_SOFT);
        scoreDirectorFactoryConfig.setEasyScoreCalculatorClass(AvailabilityScoreCalculator.class);
        scoreDirectorFactoryConfig.setInitializingScoreTrend("ANY");
        solverConfig.setScoreDirectorFactoryConfig(scoreDirectorFactoryConfig);

        ScanAnnotatedClassesConfig annotatedClassesConfig = new ScanAnnotatedClassesConfig();
        solverConfig.setScanAnnotatedClassesConfig(annotatedClassesConfig);

        Solver solver = solverFactory.buildSolver();

        CampaignDaySolution campaignDaySolution = new CampaignDaySolution();
        AvailabilitySearch availabilitySearch = new AvailabilitySearch();;
        availabilitySearch.setCampaignDay(campaignDay);
        List<Availability> availabilities = availabilityService.findAvailabilities(availabilitySearch);

        List<Location> locations = locationService.findLocations(new LocationSearch());
        campaignDaySolution.init(campaignDay, availabilities, locations);

        solver.solve(campaignDaySolution);

        CampaignDaySolution bestSolution = (CampaignDaySolution) solver.getBestSolution();
        List<CampaignSolutionEvent> events = bestSolution.getEvents();
        events = events.stream()
                .filter(e -> e.getLocation() != null)
                .collect(Collectors.toList());

        List<CampaignEvent> campaignEvents = new ArrayList<>();

        List<User> users = events.stream()
                .map(e -> e.getUser())
                .distinct()
                .collect(Collectors.toList());
        for (User user : users) {
            List<CampaignEvent> userEvents = new ArrayList<>();
            List<CampaignSolutionEvent> userSolutions = events.stream()
                    .filter(e -> e.getUser() == user)
                    .collect(Collectors.toList());

            dispatchSolutions(user, campaignDay, userSolutions, userEvents);

            campaignEvents.addAll(userEvents);
        }

        return campaignEvents;
    }

    private void dispatchSolutions(User user, CampaignDay campaignDay, List<CampaignSolutionEvent> userSolutions, List<CampaignEvent> userEvents) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(campaignDay.getDate());

        Collections.sort(userSolutions, (e1, e2) -> e1.getTimeSlot().getHour() - e2.getTimeSlot().getHour());

        CampaignEvent campaignEvent = null;
        int previousHour = 0;
        for (CampaignSolutionEvent solutionEvent : userSolutions) {
            Location location = solutionEvent.getLocation();
            int hour = solutionEvent.getTimeSlot().getHour();
            int personAmount = solutionEvent.getPersonAmount();

            if (campaignEvent == null) {
                campaignEvent = createEvent(solutionEvent, calendar, hour);
                campaignEvent.setCampaignDay(campaignDay);
                campaignEvent.setLocation(location);
                campaignEvent.setPersonAmount(personAmount);
                campaignEvent.setUser(user);
                previousHour = hour;
                continue;
            }
            if (campaignEvent.getLocation() != location
                    || campaignEvent.getPersonAmount() != personAmount
                    || previousHour + 1 != hour) {
                closeEvent(campaignEvent, calendar, hour);
                userEvents.add(campaignEvent);

                campaignEvent = createEvent(solutionEvent, calendar, hour);
                campaignEvent.setCampaignDay(campaignDay);
                campaignEvent.setLocation(location);
                campaignEvent.setPersonAmount(personAmount);
                campaignEvent.setUser(user);
                previousHour = hour;
                continue;
            }
            previousHour = hour;
        }
        if (campaignEvent != null) {
            closeEvent(campaignEvent, calendar, previousHour + 1);
            userEvents.add(campaignEvent);
        }
    }

    private CampaignEvent createEvent(CampaignSolutionEvent userSolutions, Calendar calendar, int hour) {
        CampaignEvent campaignEvent = new CampaignEvent();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        Date startTime = calendar.getTime();
        campaignEvent.setStartTime(startTime);
        return campaignEvent;
    }

    private void closeEvent(CampaignEvent campaignEvent, Calendar calendar, int hour) {
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        Date endTime = calendar.getTime();
        campaignEvent.setEndTime(endTime);
    }

}
