/*
 * Copyright (c) JForum Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms,
 * with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the
 * following  disclaimer.
 * 2)  Redistributions in binary form must reproduce the
 * above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 * 3) Neither the name of "Rafael Steil" nor
 * the names of its contributors may be used to endorse
 * or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT
 * HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE
 *
 * The JForum Project
 * http://www.jforum.net
 * 19.08.2006 21:50:05
 */
package net.jforum.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import net.jforum.JForumExecutionContext;
import net.jforum.entities.Group;
import net.jforum.exceptions.DatabaseException;

import com.igearbook.entities.PaginationData;
import com.igearbook.entities.PaginationParams;

/**
 * General utility methods to close statements and resultsets
 * 
 * @author Serge Maslyukov
 * @version $Id: DbUtils.java,v 1.3 2006/09/24 16:10:14 rafaelsteil Exp $
 */
public class DbUtils {
    public static void close(ResultSet rs, Statement st) {
        if (rs != null) {
            try {
                rs.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (st != null) {
            try {
                st.clearWarnings();
                st.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void close(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void close(Statement st) {
        if (st != null) {
            try {
                st.clearWarnings();
                st.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static String makeString(Group[] groups) {
        StringBuffer sb = new StringBuffer();

        for (Group group : groups) {
            sb.append(group.getId()).append(",");
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        } else {
            // We suppose there is no "negative" id
            sb.append("-1");
        }

        return sb.toString();
    }

    public static String makeString(int[] ids) {
        StringBuffer sb = new StringBuffer();

        for (int id : ids) {
            sb.append(id).append(",");
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        } else {
            // We suppose there is no "negative" id
            sb.append("-1");
        }

        return sb.toString();
    }

    public static String makeString(List<Group> groups) {
        StringBuffer sb = new StringBuffer();

        for (Group group : groups) {
            sb.append(group.getId()).append(",");
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        } else {
            // We suppose there is no "negative" id
            sb.append("-1");
        }

        return sb.toString();
    }

    public static int getTotalRecords(String sql) {
        int count = 0;
        PreparedStatement statement = null;
        ResultSet rs = null;
        int from = sql.toLowerCase().indexOf("from");
        int order = sql.toLowerCase().indexOf("order by");
        String newSql = (order > 0) ? "select count(*) " + sql.substring(from, order) : "select count(*) " + sql.substring(from);
        try {
            statement = JForumExecutionContext.getConnection().prepareStatement(newSql);
            rs = statement.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
            return count;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(rs, statement);
        }
    }

    public static <T> PaginationData<T> preparePagination(String sql, PaginationParams params) {
        PaginationData<T> data = new PaginationData<T>();
        BigDecimal totalRecords = BigDecimal.ZERO;
        PreparedStatement statement = null;
        ResultSet rs = null;
        int from = sql.toLowerCase().indexOf("from");
        int order = sql.toLowerCase().indexOf("order by");
        String newSql = (order > 0) ? "select count(*) " + sql.substring(from, order) : "select count(*) " + sql.substring(from);
        try {
            statement = JForumExecutionContext.getConnection().prepareStatement(newSql);
            rs = statement.executeQuery();
            if (rs.next()) {
                totalRecords = BigDecimal.valueOf(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(rs, statement);
        }
        BigDecimal recordsPerPage = BigDecimal.valueOf(params.getRecordsPerPage());
        int totalPages = totalRecords.divide(recordsPerPage, RoundingMode.CEILING).intValue();
        int currentPage = BigDecimal.valueOf(params.getStart() + 1).divide(recordsPerPage, RoundingMode.CEILING).intValue();
        data.setTotalRecords(totalRecords.intValue());
        data.setRecordsPerPage(recordsPerPage.intValue());
        data.setCurrentPage(currentPage);
        data.setTotalPages(totalPages);
        return data;
    }
}
