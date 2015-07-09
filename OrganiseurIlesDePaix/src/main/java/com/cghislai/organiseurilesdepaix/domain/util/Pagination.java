/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseurilesdepaix.domain.util;

import java.io.Serializable;

/**
 *
 * @author cghislai
 */
public class Pagination implements Serializable {

    private int firstIndex;
    private int pageSize;

    public Pagination(int firstIndex, int pageSize) {
        this.firstIndex = firstIndex;
        this.pageSize = pageSize;
    }

    public Pagination() {
    }

    public int getFirstIndex() {
        return firstIndex;
    }

    public void setFirstIndex(int firstIndex) {
        this.firstIndex = firstIndex;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

}
