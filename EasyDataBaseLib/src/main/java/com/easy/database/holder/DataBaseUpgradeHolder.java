package com.easy.database.holder;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.easy.database.LogUtils;
import com.easy.database.entity.IEntity;
import com.easy.database.entity.TableStorage;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 数据库升级管理器
 * 实现ISQLiteUpgrade接口对不同版本进行逐步升级
 * Time: 2019/10/8 0008
 * Author: zoulong
 */
public class DataBaseUpgradeHolder {
    private int olderVersion;
    private int newVersion;
    private SQLiteDatabase mSQLiteDatabase;
    private static DataBaseUpgradeHolder mDataBaseUpgradeHolder = null;
    public DataBaseUpgradeHolder(){};

    public static DataBaseUpgradeHolder get(){
        if(mDataBaseUpgradeHolder == null){
            mDataBaseUpgradeHolder = new DataBaseUpgradeHolder();
        }
        return mDataBaseUpgradeHolder;
    }

    public void init(SQLiteDatabase db, int olderVersion, int newVersion){
        this.mSQLiteDatabase = db;
        this.olderVersion = olderVersion;
        this.newVersion = newVersion;
    }

    public boolean checkAndUpgradeSQLite(HashMap<Class, TableStorage> tableStorageMap, ISQLiteUpgrade[] gradients){
        try {
            if(gradients == null||mSQLiteDatabase == null || olderVersion == newVersion ||
                    tableStorageMap == null || tableStorageMap.size() == 0){
                return false;
            }
            LogUtils.d(String.format("开始升级数据库，当前数据库版本：%d -> %d", olderVersion, newVersion));
            for(ISQLiteUpgrade sqLiteUpgrade : gradients){
                if(olderVersion < newVersion && sqLiteUpgrade.isApplyUpgrade(olderVersion)){
                    if(!sqLiteUpgrade.upgrade(mSQLiteDatabase, tableStorageMap)){
                        throw new RuntimeException(String.format("在升级%d -> %d时失败了", olderVersion, newVersion));
                    }
                    olderVersion = sqLiteUpgrade.getUpgradedVersion();
                }
            }
            LogUtils.d("升级完成，当前数据库版本：" + olderVersion);
            return true;
        } catch (Exception e) {
            LogUtils.e(e);
            emergency(tableStorageMap);
        }
        return false;
    }

    /**如果升级出错或者异常
     * 强制处理升级，删除所有表在重建
     * @param tableStorageMap
     */
    private void emergency(HashMap<Class, TableStorage> tableStorageMap){
        try {
            for(Class key : tableStorageMap.keySet()){
                TableStorage tableStorage = tableStorageMap.get(key);
                //删除表
                mSQLiteDatabase.execSQL("DROP TABLE IF EXISTS " + tableStorage.getTableName());
                String execSql = tableStorage.getCreateTableSql();
                //重建表
                mSQLiteDatabase.execSQL(execSql);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static interface ISQLiteUpgrade{
        //当前版本是否能用于升级
        public boolean isApplyUpgrade(int currentVersion);
        //升级
        public boolean upgrade(SQLiteDatabase sqLiteDatabase, HashMap<Class, TableStorage> tableStorageMap);
        //升级后的版本
        public int getUpgradedVersion();
    }

    public static abstract class AbstractSQLiteDefaultUpgrade implements ISQLiteUpgrade{
        @Override
        public boolean upgrade(SQLiteDatabase sqLiteDatabase, HashMap<Class, TableStorage> tableStorageMap) {
            for(Class key : tableStorageMap.keySet()){
                ArrayList<? extends IEntity> datas = SQLiteDatabaseHolder.getInstance().queryAllByClassType(key);
                TableStorage tableStorage = tableStorageMap.get(key);
                //删除表
                sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + tableStorage.getTableName());
                String execSql = tableStorage.getCreateTableSql();
                LogUtils.i("execSql:"+execSql);
                //重建表
                sqLiteDatabase.execSQL(execSql);
                if(datas != null && datas.size() > 0){
                    for(IEntity entity : datas){//将数据重新添加进去
                        SQLiteDatabaseHolder.getInstance().insert(entity);
                    }
                }
            }
            return true;
        }
    }
}
