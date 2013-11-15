package com.igearbook.entities;

import java.io.Serializable;
import java.util.Collection;

/**
 * Data package, usually use in the list.
 */
public class DataPackage implements Serializable {
    private static final long serialVersionUID = -3847321538691386074L;
    public int totalRecords;// 行总数

    public int recordsPerPage;// 每一页显示的行数

    public int currentPage;// 当前页

    public Collection<Serializable> datas;// 封闭好的PO的集合

    /**
     * @return Returns the datas.
     */
    public Collection<Serializable> getDatas() {
        return datas;
    }

    /**
     * @param datas
     *            The datas to set.
     */
    public void setDatas(Collection<Serializable> datas) {
        this.datas = datas;
    }

}