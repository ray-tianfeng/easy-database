package com.easy.database.entity;

import com.easy.database.annotation.ColumnInfo;
import com.easy.database.annotation.Entity;
import com.easy.database.annotation.PrimaryKey;

/**
 * 表信息几率
 * Time: 2019/10/8 0008
 * Author: zoulong
 */
@Entity(tableName = "easy_table_record")
public class TableRecord extends IEntity{
    @PrimaryKey(name = "id")
    private int id;
    @ColumnInfo(name = "table_class_name")
    private String tableClassName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTableClassName() {
        return tableClassName;
    }

    public void setTableClassName(String tableClassName) {
        this.tableClassName = tableClassName;
    }
}
