/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseurilesdepaix.service;

import com.cghislai.organiseurilesdepaix.domain.Location;
import com.cghislai.organiseurilesdepaix.domain.Location_;
import com.cghislai.organiseurilesdepaix.domain.util.Pagination;
import com.cghislai.organiseurilesdepaix.service.search.LocationSearch;
import java.util.ArrayList;
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
public class LocationService {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Location> findLocations(LocationSearch locationSearch) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Location> query = builder.createQuery(Location.class);
        Root<Location> root = query.from(Location.class);

        List<Predicate> predicates = applyLocationSearch(locationSearch, root, builder);
        query.select(root);
        query.where(predicates.toArray(new Predicate[0]));

        TypedQuery<Location> typedQuery = entityManager.createQuery(query);
        final Pagination pagination = locationSearch.getPagination();
        if (pagination != null) {
            typedQuery.setFirstResult(pagination.getFirstIndex());
            typedQuery.setMaxResults(pagination.getPageSize());
        }
        return typedQuery.getResultList();
    }

    public Long countLocation(LocationSearch locationSearch) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Location> root = query.from(Location.class);

        List<Predicate> predicates = applyLocationSearch(locationSearch, root, builder);
        Expression<Long> count = builder.count(root);
        query.select(count);
        query.where(predicates.toArray(new Predicate[0]));
        
        
        TypedQuery<Long> typedQuery = entityManager.createQuery(query);
        return typedQuery.getSingleResult();
    }

    private List<Predicate> applyLocationSearch(LocationSearch locationSearch, Root<Location> root, CriteriaBuilder builder) {
        List<Predicate> predicates = new ArrayList<>();
        String cityLike = locationSearch.getCityLike();
        if (cityLike != null) {
            Path<String> cityPath = root.get(Location_.city);
            String cityExpression = "%" + cityLike + "%";
            Predicate pred = builder.like(cityPath, cityExpression);
            predicates.add(pred);
        }
        String nameLike = locationSearch.getNameLike();
        if (nameLike != null) {
            Path<String> namePath = root.get(Location_.name);
            String nameExpression = "%" + nameLike + "%";
            Predicate pred = builder.like(namePath, nameExpression);
            predicates.add(pred);
        }
        String postalCode = locationSearch.getPostalCode();
        if (postalCode != null) {
            Path<String> postalCodePath = root.get(Location_.postalCode);
            Predicate pred = builder.equal(postalCodePath, postalCode);
            predicates.add(pred);
        }
        return predicates;
    }

    public Location saveLocation(Location location) {
        Location managedLocation = entityManager.merge(location);
        return managedLocation;
    }

    public void removeLocation(Location location) {
        entityManager.remove(location);
    }
}
