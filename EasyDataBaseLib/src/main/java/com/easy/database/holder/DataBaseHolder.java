package com.easy.database.holder;

import android.database.sqlite.SQLiteDatabase;

import com.easy.database.entity.IEntity;
import com.easy.database.entity.TableStorage;

/**
 * SQLiteDataBaseHolder已经提供了常用的方法，如果不能满足业务需求，继承DataBaseHolder编写业务需求的方法
 * Time: 2019/8/19 0019
 * Author: zoulong
 */

public abstract class DataBaseHolder<T extends IEntity> {
    private SQLiteDatabase sqlDataBase;
    private TableStorage tableStorage;
    protected void init(SQLiteDatabase sqlDataBase, TableStorage tableStorage){
        this.sqlDataBase = sqlDataBase;
        this.tableStorage = tableStorage;
    }

    public SQLiteDatabase getDateBase(){
        return sqlDataBase;
    }

    public TableStorage getTableStorage(){
        return tableStorage;
    }
}
