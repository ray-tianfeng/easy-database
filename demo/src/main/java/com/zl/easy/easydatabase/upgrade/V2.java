package com.zl.easy.easydatabase.upgrade;

import android.database.sqlite.SQLiteDatabase;

import com.easy.database.LogUtils;
import com.easy.database.entity.TableStorage;
import com.easy.database.holder.DataBaseUpgradeHolder;
import com.easy.database.holder.SQLiteDatabaseHolder;
import com.zl.easy.easydatabase.table.UpgradeTable;

import java.util.HashMap;

/**
 * version 1->2升级处理
 * Time: 2019/10/10 0010
 * Author: zoulong
 */
public class V2 extends DataBaseUpgradeHolder.AbstractSQLiteDefaultUpgrade {
    @Override
    public boolean isApplyUpgrade(int currentVersion) {
        //当先版本是否可以升级
        return 0 < currentVersion && currentVersion < 2;
    }

    @Override
    public boolean upgrade(SQLiteDatabase sqLiteDatabase, HashMap<Class, TableStorage> tableStorageMap) {
//        return super.upgrade(sqLiteDatabase, tableStorageMap);//默认实现方法1.备份数据数据；2.删除所有表；3.创建新表；4.还原备份数据
        TableStorage tableStorage = tableStorageMap.get(UpgradeTable.class);
        String execSql = tableStorage.getCreateTableSql();
        LogUtils.i("execSql:"+execSql);
        //添加一张表
        sqLiteDatabase.execSQL(execSql);
        return true;
    }

    @Override
    public int getUpgradedVersion() {
        //当次升级后数据库版本
        return 2;
    }
}
