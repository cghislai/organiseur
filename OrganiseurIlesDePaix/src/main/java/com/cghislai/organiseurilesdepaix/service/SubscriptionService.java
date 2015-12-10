/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseurilesdepaix.service;

import com.cghislai.organiseurilesdepaix.domain.CampaignDay;
import com.cghislai.organiseurilesdepaix.domain.Location;
import com.cghislai.organiseurilesdepaix.domain.LocationTimeSlot;
import com.cghislai.organiseurilesdepaix.domain.LocationTimeSlot_;
import com.cghislai.organiseurilesdepaix.domain.Subscription;
import com.cghislai.organiseurilesdepaix.domain.Subscription_;
import com.cghislai.organiseurilesdepaix.domain.User;
import com.cghislai.organiseurilesdepaix.domain.util.Pagination;
import com.cghislai.organiseurilesdepaix.service.search.SubscriptionSearch;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 *
 * @author cghislai
 */
@Stateless
public class SubscriptionService implements Serializable {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Subscription> searchSubscriptions(SubscriptionSearch search, Pagination pagination) {

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Subscription> query = builder.createQuery(Subscription.class);
        Root<Subscription> subscriptionRoot = query.from(Subscription.class);

        Join<Subscription, LocationTimeSlot> timeSlotJoin = subscriptionRoot.join(Subscription_.locationTimeSlot);

        List<Predicate> predicates = applySubscriptionSearch(search, timeSlotJoin, builder, subscriptionRoot);

        Predicate[] predicatesArray = predicates.toArray(new Predicate[0]);

        query.select(subscriptionRoot);
        query.where(predicatesArray);

        TypedQuery<Subscription> typedQuery = entityManager.createQuery(query);
        if (pagination != null) {
            typedQuery.setFirstResult(pagination.getFirstIndex());
            typedQuery.setMaxResults(pagination.getPageSize());
        }
        List<Subscription> subscriptions = typedQuery.getResultList();
        return subscriptions;
    }

    public Long countSubscriptions(SubscriptionSearch search) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Subscription> subscriptionRoot = query.from(Subscription.class);

        Join<Subscription, LocationTimeSlot> timeSlotJoin = subscriptionRoot.join(Subscription_.locationTimeSlot);

        List<Predicate> predicates = applySubscriptionSearch(search, timeSlotJoin, builder, subscriptionRoot);

        Predicate[] predicatesArray = predicates.toArray(new Predicate[0]);

        Expression<Long> subscriptionCount = builder.count(subscriptionRoot);
        query.select(subscriptionCount);
        query.where(predicatesArray);

        TypedQuery<Long> typedQuery = entityManager.createQuery(query);
        Long singleResult = typedQuery.getSingleResult();
        return singleResult;
    }

    private List<Predicate> applySubscriptionSearch(SubscriptionSearch search, Join<Subscription, LocationTimeSlot> timeSlotJoin, CriteriaBuilder builder, Root<Subscription> subscriptionRoot) {
        List<Predicate> predicates = new ArrayList<>();
        Location location = search.getLocation();
        if (location != null) {
            Path<Location> loccationPath = timeSlotJoin.get(LocationTimeSlot_.location);
            Predicate locationPredicate = builder.equal(loccationPath, location);
            predicates.add(locationPredicate);
        }
        Long startHour = search.getStartHour();
        if (startHour != null) {
            Path<Long> startTimePath = timeSlotJoin.get(LocationTimeSlot_.startTime);
            Predicate startHourPredicate = builder.equal(startTimePath, startHour);
            predicates.add(startHourPredicate);
        }
        User user = search.getUser();
        if (user != null) {
            Path<User> userPath = subscriptionRoot.get(Subscription_.user);
            Predicate userPreicate = builder.equal(userPath, user);
            predicates.add(userPreicate);
        }
        CampaignDay campaignDay = search.getCampaignDay();
        if (campaignDay != null) {
            Path<CampaignDay> campaignSayPath = timeSlotJoin.get(LocationTimeSlot_.campaignDay);
            Predicate campaignDayPredicate = builder.equal(campaignSayPath, campaignDay);
            predicates.add(campaignDayPredicate);
        }
        return predicates;
    }

    public Subscription saveSubscription(Subscription subscription) {
        Subscription mergedSubscription = entityManager.merge(subscription);
        return mergedSubscription;
    }

    public void deleteSubscription(Subscription subscription) {
        Subscription mergedSubscription = entityManager.merge(subscription);
        entityManager.remove(mergedSubscription);
    }
}
