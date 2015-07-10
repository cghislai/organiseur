/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseurilesdepaix.service;

import com.cghislai.organiseurilesdepaix.domain.CampaignDay;
import com.cghislai.organiseurilesdepaix.domain.CampaignDay_;
import com.cghislai.organiseurilesdepaix.service.search.CampaignDaySearch;
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
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 *
 * @author cghislai
 */
@Stateless
public class CampaignDatesService implements Serializable {

    @PersistenceContext
    private EntityManager entityManager;

    public List<CampaignDay> findAllDays() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<CampaignDay> query = builder.createQuery(CampaignDay.class);
        Root<CampaignDay> root = query.from(CampaignDay.class);

        Path<Date> datePath = root.get(CampaignDay_.date);
        Order dateOrder = builder.asc(datePath);

        query.select(root);
        query.orderBy(dateOrder);

        TypedQuery<CampaignDay> typedQuery = entityManager.createQuery(query);
        return typedQuery.getResultList();
    }

    public Long countCampaignDays(CampaignDaySearch search) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<CampaignDay> root = query.from(CampaignDay.class);

        Expression<Long> dayCount = builder.count(root);

        List<Predicate> predicates = new ArrayList<>();
        Date date = search.getDate();
        if (date != null) {
            Path<Date> datePath = root.get(CampaignDay_.date);
            Predicate pred = builder.equal(datePath, date);
            predicates.add(pred);
        }

        query.select(dayCount);
        query.where(predicates.toArray(new Predicate[0]));

        TypedQuery<Long> typedQuery = entityManager.createQuery(query);
        return typedQuery.getSingleResult();
    }
    public CampaignDay getCampaignDays(CampaignDaySearch search) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<CampaignDay> query = builder.createQuery(CampaignDay.class);
        Root<CampaignDay> root = query.from(CampaignDay.class);

        List<Predicate> predicates = new ArrayList<>();
        Date date = search.getDate();
        if (date != null) {
            Path<Date> datePath = root.get(CampaignDay_.date);
            Predicate pred = builder.equal(datePath, date);
            predicates.add(pred);
        }

        query.select(root);
        query.where(predicates.toArray(new Predicate[0]));

        TypedQuery<CampaignDay> typedQuery = entityManager.createQuery(query);
        return typedQuery.getSingleResult();
    }

    public CampaignDay saveCampaignDay(CampaignDay campaignDay) {
        CampaignDay managedCampaignDay = entityManager.merge(campaignDay);
        return managedCampaignDay;
    }

    public void removeCampaignDay(CampaignDay campaignDay) {
        CampaignDay managedCampaignDay = entityManager.merge(campaignDay);
        entityManager.remove(managedCampaignDay);
    }
}
