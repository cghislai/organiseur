/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseurilesdepaix.service;

import com.cghislai.organiseurilesdepaix.domain.User;
import com.cghislai.organiseurilesdepaix.domain.User_;
import com.cghislai.organiseurilesdepaix.domain.util.Pagination;
import com.cghislai.organiseurilesdepaix.service.search.UserSearch;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
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
public class UserService {

    @PersistenceContext
    private EntityManager entityManager;

    public User searchUser(UserSearch userSearch) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = builder.createQuery(User.class);
        Root<User> root = query.from(User.class);

        List<Predicate> predicates = applyUserSearch(userSearch, root, builder);

        query.select(root);
        query.where(predicates.toArray(new Predicate[0]));

        TypedQuery<User> typedQuery = entityManager.createQuery(query);
        return getSingleResult(typedQuery);
    }

    public List<User> searchUsers(UserSearch userSearch) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = builder.createQuery(User.class);
        Root<User> root = query.from(User.class);

        List<Predicate> predicates = applyUserSearch(userSearch, root, builder);

        query.select(root);
        query.where(predicates.toArray(new Predicate[0]));

        TypedQuery<User> typedQuery = entityManager.createQuery(query);
        Pagination pagination = userSearch.getPagination();
        if (pagination != null) {
            typedQuery.setFirstResult(pagination.getFirstIndex());
            typedQuery.setMaxResults(pagination.getPageSize());
        }
        return typedQuery.getResultList();
    }

    public Long countUsers(UserSearch userSearch) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<User> root = query.from(User.class);

        List<Predicate> predicates = applyUserSearch(userSearch, root, builder);
        Expression<Long> userCount = builder.count(root);

        query.select(userCount);
        query.where(predicates.toArray(new Predicate[0]));

        TypedQuery<Long> typedQuery = entityManager.createQuery(query);
        return typedQuery.getSingleResult();
    }

    private List<Predicate> applyUserSearch(UserSearch userSearch, Root<User> root, CriteriaBuilder builder) {
        List<Predicate> predicates = new ArrayList<>();
        Boolean admin = userSearch.getAdmin();
        if (admin != null) {
            Path<Boolean> adminPath = root.get(User_.admin);
            Predicate adminPred = builder.equal(adminPath, admin);
            predicates.add(adminPred);
        }
        String mail = userSearch.getMail();
        if (mail != null) {
            Path<String> mailPath = root.get(User_.email);
            Predicate pred = builder.equal(mailPath, mail);
            predicates.add(pred);
        }
        String name = userSearch.getLogin();
        if (name != null) {
            Path<String> namePath = root.get(User_.userName);
            Predicate pred = builder.equal(namePath, name);
            predicates.add(pred);
        }
        String nameLike = userSearch.getNameLike();
        if (nameLike != null) {
            Path<String> namePath = root.get(User_.humanName);
            final String nameExpression = "%" + nameLike + "%";
            Predicate pred = builder.like(namePath, nameExpression);
            predicates.add(pred);
        }
        String nameOrMail = userSearch.getLoginOrMail();
        if (nameOrMail != null) {
            Path<String> namePath = root.get(User_.userName);
            Path<String> mailPath = root.get(User_.email);
            Predicate namePred = builder.equal(namePath, nameOrMail);
            Predicate mailPred = builder.equal(mailPath, nameOrMail);
            Predicate pred = builder.or(namePred, mailPred);
            predicates.add(pred);
        }
        Long userId = userSearch.getUserId();
        if (userId != null) {
            Path<Long> idPath = root.get(User_.id);
            Predicate pred = builder.equal(idPath, userId);
            predicates.add(pred);
        }
        return predicates;
    }

    private User getSingleResult(TypedQuery<User> query) {
        try {
            return query.getSingleResult();
        } catch (NonUniqueResultException | NoResultException exception) {
            return null;
        }
    }

    public User saveUser(User user) {
        User managedUser = entityManager.merge(user);
        return managedUser;
    }

    public void removeUser(User user) {
        User managedUser = entityManager.merge(user);
        entityManager.remove(managedUser);
    }
}
