/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseurilesdepaix.service;

import com.cghislai.organiseurilesdepaix.domain.Availability;
import com.cghislai.organiseurilesdepaix.domain.Availability_;
import com.cghislai.organiseurilesdepaix.domain.CampaignDay;
import com.cghislai.organiseurilesdepaix.domain.Location;
import com.cghislai.organiseurilesdepaix.domain.User;
import com.cghislai.organiseurilesdepaix.domain.util.Pagination;
import com.cghislai.organiseurilesdepaix.service.search.AvailabilitySearch;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 *
 * @author cghislai
 */
@Stateless
public class AvailabilityService implements Serializable {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Availability> findAvailabilities(AvailabilitySearch availabilitySearch) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Availability> query = builder.createQuery(Availability.class);
        Root<Availability> root = query.from(Availability.class);

        List<Predicate> predicates = applyAvailabilitySearch(availabilitySearch, root, builder);

        query.select(root);
        query.where(predicates.toArray(new Predicate[0]));

        TypedQuery<Availability> typedQuery = entityManager.createQuery(query);
        Pagination pagination = availabilitySearch.getPagination();
        if (pagination != null) {
            typedQuery.setFirstResult(pagination.getFirstIndex());
            typedQuery.setMaxResults(pagination.getPageSize());
        }
        return typedQuery.getResultList();
    }

    public List<CampaignDay> findAvailabilitiesDays(AvailabilitySearch availabilitySearch) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<CampaignDay> query = builder.createQuery(CampaignDay.class);
        Root<Availability> root = query.from(Availability.class);

        List<Predicate> predicates = applyAvailabilitySearch(availabilitySearch, root, builder);

        Path<CampaignDay> campaignDayPath = root.get(Availability_.campaignDay);
        query.select(campaignDayPath);
        query.distinct(true);
        query.where(predicates.toArray(new Predicate[0]));

        TypedQuery<CampaignDay> typedQuery = entityManager.createQuery(query);
        Pagination pagination = availabilitySearch.getPagination();
        if (pagination != null) {
            typedQuery.setFirstResult(pagination.getFirstIndex());
            typedQuery.setMaxResults(pagination.getPageSize());
        }
        return typedQuery.getResultList();
    }

    public List<Location> findAvailabilitiesLocations(AvailabilitySearch availabilitySearch) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Location> query = builder.createQuery(Location.class);
        Root<Availability> root = query.from(Availability.class);

        List<Predicate> predicates = applyAvailabilitySearch(availabilitySearch, root, builder);

        Path<Location> locationPath = root.get(Availability_.location);
        query.select(locationPath);
        query.distinct(true);
        query.where(predicates.toArray(new Predicate[0]));

        TypedQuery<Location> typedQuery = entityManager.createQuery(query);
        Pagination pagination = availabilitySearch.getPagination();
        if (pagination != null) {
            typedQuery.setFirstResult(pagination.getFirstIndex());
            typedQuery.setMaxResults(pagination.getPageSize());
        }
        return typedQuery.getResultList();
    }

    public Long countAvailabilities(AvailabilitySearch availabilitySearch) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Availability> root = query.from(Availability.class);

        List<Predicate> predicates = applyAvailabilitySearch(availabilitySearch, root, builder);

        Expression<Long> count = builder.count(root);
        query.select(count);
        query.where(predicates.toArray(new Predicate[0]));

        TypedQuery<Long> typedQuery = entityManager.createQuery(query);
        return typedQuery.getSingleResult();
    }

    public Long countUsersWithAvailabilites() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Availability> root = query.from(Availability.class);

        Path<User> userPath = root.get(Availability_.user);
        Expression<Long> userCount = builder.countDistinct(userPath);

        query.select(userCount);
        query.distinct(true);

        TypedQuery<Long> typedQuery = entityManager.createQuery(query);
        return typedQuery.getSingleResult();
    }

    private List<Predicate> applyAvailabilitySearch(AvailabilitySearch availabilitySearch, Root<Availability> root, CriteriaBuilder builder) {
        List<Predicate> predicates = new ArrayList<>();
        CampaignDay campaignDay = availabilitySearch.getCampaignDay();
        if (campaignDay != null) {
            Path<CampaignDay> campaignDayPath = root.get(Availability_.campaignDay);
            Predicate pred = builder.equal(campaignDayPath, campaignDay);
            predicates.add(pred);
        }
        Date endTime = availabilitySearch.getEndTime();
        if (endTime != null) {
            Path<Date> endPath = root.get(Availability_.endTime);
            Predicate pred = builder.greaterThan(endPath, endTime);
            predicates.add(pred);
        }
        Date forTime = availabilitySearch.getForTime();
        if (forTime != null) {
            Path<Date> startTimePath = root.get(Availability_.startTime);
            Path<Date> endTimePath = root.get(Availability_.endTime);
            Predicate endGreater = builder.greaterThan(endTimePath, forTime);
            Predicate startSmaller = builder.lessThanOrEqualTo(startTimePath, forTime);
            Predicate pred = builder.and(endGreater, startSmaller);
            predicates.add(pred);
        }
        Location location = availabilitySearch.getLocation();
        if (location != null) {
            Path<Location> locationPath = root.get(Availability_.location);
            Predicate pred = builder.equal(locationPath, location);
            predicates.add(pred);
        }
        Date startTime = availabilitySearch.getStartTime();
        if (startTime != null) {
            Path<Date> startTimePath = root.get(Availability_.startTime);
            Predicate pred = builder.lessThanOrEqualTo(startTimePath, startTime);
            predicates.add(pred);
        }
        User user = availabilitySearch.getUser();
        if (user != null) {
            Path<User> userPath = root.get(Availability_.user);
            Predicate pred = builder.equal(userPath, user);
            predicates.add(pred);
        }
        return predicates;
    }

    public Availability saveAvailability(Availability availabilitiy) {
        Availability managedAvailability = entityManager.merge(availabilitiy);
        return managedAvailability;
    }

    public void removeAvailability(Availability availabilitiy) {
        Availability managedAvailability = entityManager.merge(availabilitiy);
        entityManager.remove(managedAvailability);
    }
}
