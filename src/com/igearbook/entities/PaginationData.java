package com.igearbook.entities;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class PaginationData<T> implements Serializable {
    private static final long serialVersionUID = -3847321538691386074L;
    private int totalRecords;

    private int recordsPerPage;

    private int totalPages;

    private int currentPage;

    private Map<String, String> webParams;

    public List<T> list;

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(final int totalRecords) {
        this.totalRecords = totalRecords;
    }

    public int getRecordsPerPage() {
        return recordsPerPage;
    }

    public void setRecordsPerPage(final int recordsPerPage) {
        this.recordsPerPage = recordsPerPage;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(final int currentPage) {
        this.currentPage = currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(final int totalPages) {
        this.totalPages = totalPages;
    }

    public Map<String, String> getWebParams() {
        return webParams;
    }

    public void setWebParams(final Map<String, String> webParams) {
        this.webParams = webParams;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(final List<T> list) {
        this.list = list;
    }

}