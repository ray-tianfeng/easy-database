package com.zl.easy.easydatabase.table;

import com.easy.database.annotation.ColumnInfo;
import com.easy.database.annotation.Entity;
import com.easy.database.annotation.PrimaryKey;
import com.easy.database.entity.IEntity;

/**
 * 升级测试table
 * Time: 2019/10/11 0011
 * Author: zoulong
 */
@Entity(tableName = "test_upgrade")
public class UpgradeTable extends IEntity {
    @PrimaryKey(name = "test_id")
    private int id;

    @ColumnInfo(name = "des")
    private String des;

    public UpgradeTable() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }
}
