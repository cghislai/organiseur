/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseurilesdepaix.util;

import com.cghislai.organiseurilesdepaix.domain.util.Pagination;
import java.util.List;
import java.util.Map;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

/**
 *
 * @author cghislai
 */
public abstract class MyLazyDataModel<T> extends LazyDataModel<T> {

    @Override
    public List<T> load(int first, int pageSize, List<SortMeta> multiSortMeta, Map<String, Object> filters) {
        return simpleLoad(new Pagination(first, pageSize));
    }

    @Override
    public List<T> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        return simpleLoad(new Pagination(first, pageSize));
    }

    protected abstract List<T> simpleLoad(Pagination pagination);
}
