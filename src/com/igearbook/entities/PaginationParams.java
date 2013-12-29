package com.igearbook.entities;

import java.util.Map;

public class PaginationParams {
    private int start;
    private int recordsPerPage = 12;
    private Map<String, Object> webParams;
    private Map<String, Object> queryParams;

    public int getStart() {
        return start;
    }

    public void setStart(final int start) {
        this.start = start;
    }

    public int getRecordsPerPage() {
        return recordsPerPage;
    }

    public void setRecordsPerPage(final int recordsPerPage) {
        this.recordsPerPage = recordsPerPage;
    }

    public Map<String, Object> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(final Map<String, Object> queryParams) {
        this.queryParams = queryParams;
    }

    public Map<String, Object> getWebParams() {
        return webParams;
    }

    public void setWebParams(final Map<String, Object> webParams) {
        this.webParams = webParams;
    }

}
