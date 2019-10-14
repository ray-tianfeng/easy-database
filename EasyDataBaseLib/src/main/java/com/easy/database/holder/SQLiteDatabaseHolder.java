package com.easy.database.holder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.easy.database.DataBaseConfig;
import com.easy.database.LogUtils;
import com.easy.database.annotation.ColumnInfo;
import com.easy.database.annotation.Entity;
import com.easy.database.annotation.PrimaryKey;
import com.easy.database.entity.IEntity;
import com.easy.database.entity.TableRecord;
import com.easy.database.entity.TableStorage;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * 非关系型数据库管理类
 * Time: 2019/8/19 0019
 * Author: zoulong
 */

public class SQLiteDatabaseHolder {
    private DataBaseConfig mDataBaseConfig;
    private static SQLiteDatabaseHolder mPersistentManager = null;
    private SQLiteDatabase sqlDataBase;
    private LinkedHashSet<Class<? extends IEntity>> tables = new LinkedHashSet<Class<? extends IEntity>>();
    private HashMap<Class, TableStorage> tableStorageMap = new HashMap<>();
    public SQLiteDatabaseHolder(){};
    
    public static synchronized SQLiteDatabaseHolder getInstance(){
        if(mPersistentManager == null){
            mPersistentManager = new SQLiteDatabaseHolder();
        }
        return mPersistentManager;
    }

    public void init(Context mContext, DataBaseConfig mDataBaseConfig){
        LogUtils.i("SQLiteDatabaseHolder call init statrt");
        this.mDataBaseConfig = mDataBaseConfig;
        tables.add(TableRecord.class);
        tables.addAll(Arrays.asList(mDataBaseConfig.getTables()));
        //创建SQLiteDataBase
        sqlDataBase = createSQLiteDataBase(mContext);
        //注册表信息记录
        registerTable(tables.toArray(new Class[]{}));
        //加载数据库中所有的表映射
        loadTableRecord();
        //解析注解并记录到tableStorageMap中
        parseTableAnnotation(tables.toArray(new Class[]{}));
        //检测并升级数据库
        DataBaseUpgradeHolder.get().checkAndUpgradeSQLite(tableStorageMap, mDataBaseConfig.getGradients());
        LogUtils.i("SQLiteDatabaseHolder call init finish");
    }

    //加载数据库中所有的表映射
    private void loadTableRecord(){
        if(sqlDataBase == null){
            LogUtils.e("loadTableRecord  SQLiteDatabase is null!");
            return;
        }
        try {
            ArrayList<TableRecord> tableRecords = queryAllByClassType(TableRecord.class);
            if(tableRecords == null) return;
            for(TableRecord tableRecord : tableRecords){
                Class<? extends IEntity> tableClass = (Class<? extends IEntity>) Class.forName(tableRecord.getTableClassName());
                if(tableClass != null) tables.add(tableClass);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 修饰DataBaseHolder
     * @param dataBaseHolder 继承DatabaseHolder后的实例类
     * @param <T> entity对象
     * @param <H> holder对象
     * @return 修饰后holder对象
     */
    public  <T extends IEntity, H extends DataBaseHolder<T>> H decorateHolder(H dataBaseHolder){
        ParameterizedType type = (ParameterizedType) dataBaseHolder.getClass().getGenericSuperclass();
        if(type == null) throw new RuntimeException("DataBaseHolder Generics are erased");
        Type[] args = ((ParameterizedType) type).getActualTypeArguments();
        Class genericsClassType = (Class) args[0];
        if(!tableStorageMap.containsKey(genericsClassType)){
            throw new RuntimeException("please create table user SQLiteDatabaseHolder");
        }
        dataBaseHolder.init(sqlDataBase, tableStorageMap.get(genericsClassType));
        return dataBaseHolder;
    }

    public boolean registerTable(Class<? extends IEntity> ... tables){
        if(tables == null || tables.length == 0) return true;
        for(Class entity : tables){
            if(!registerTable(entity)) return false;
        }
        return true;
    }

    public boolean  registerTable(Class<? extends IEntity> table){
        if(table == null) throw new NullPointerException("create table param is null");
        //解析注解
        parseTableAnnotation(table);
        TableStorage tableStorage = tableStorageMap.get(table);
        if(hasExitTable(tableStorage.getTableName())){
            LogUtils.i(table.getName() + " has register!");
            return true;
        }
        try {
            //创建表
            String execSql = tableStorage.getCreateTableSql();
            LogUtils.d("execSql:"+execSql);
            sqlDataBase.execSQL(execSql);
            tables.add(table);
            //记录表信息
            TableRecord tableRecord = new TableRecord();
            tableRecord.setTableClassName(table.getName());
            insert(tableRecord);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //注册表信息失败，移除表注解信息
        tableStorageMap.remove(table);
        return false;
    }

    /**
     * 删除一条数据，根据主键id
     * @param id 组件id
     * @param classType 表Entity
     * @return 是否删除成功
     */
    public boolean deleteByPrimaryId(int id, Class classType){
        try {
            TableStorage tableStorage = tableStorageMap.get(classType);
            int changeCount= sqlDataBase.delete(tableStorage.getTableName(), tableStorage.getPrimaryName() + "=" + id, null);
            return changeCount > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 插入一条数据
     * @param entity entity对象
     * @return 插入数据id
     */
    public <T extends IEntity> int insert(T entity){
        try {
            TableStorage tableStorage = tableStorageMap.get(entity.getClass());
            Map<String, Object> entityMap = entity.toMap();
            ContentValues col = new ContentValues();
            for(TableStorage.ColumnStorage columnStorage : tableStorage.getColumns()){
                if(entityMap.get(columnStorage.getVariableName()).getClass().getName().equals(Boolean.class.getName())){
                    if(!entityMap.containsKey(columnStorage.getVariableName())) continue;
                    col.put(columnStorage.getColumnName(), (entityMap.get(columnStorage.getVariableName()).toString().equals("true"))? 1 : 0);
                    continue;
                }
                col.put(columnStorage.getColumnName(), entityMap.get(columnStorage.getVariableName()).toString());
            }
            int id = (int)sqlDataBase.insert(tableStorage.getTableName(), null, col);
            return id;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public <T extends IEntity> boolean updateByPrimaryId(T entity){
        try {
            TableStorage tableStorage = tableStorageMap.get(entity.getClass());
            Map<String, Object> entityMap = entity.toMap();
            ContentValues col = new ContentValues();
            for(TableStorage.ColumnStorage columnStorage : tableStorage.getColumns()){
                if(!entityMap.containsKey(columnStorage.getVariableName())) continue;
                if(entityMap.get(columnStorage.getVariableName()).getClass().getName().equals(Boolean.class.getName())){
                    col.put(columnStorage.getColumnName(), (entityMap.get(columnStorage.getVariableName()).toString().equals("true"))? 1 : 0);
                    continue;
                }
                col.put(columnStorage.getColumnName(), entityMap.get(columnStorage.getVariableName()).toString());
            }
            String whereClause = String.format("%s=?", tableStorage.getPrimaryName());
            String[]whereArgs = {entityMap.get(tableStorage.getPrimaryVariableName()).toString()};
            return sqlDataBase.update(tableStorage.getTableName(), col, whereClause, whereArgs) > 0;
        } catch (Exception e) {
            LogUtils.e(e);
        }
        return false;
    }

    /**
     * 获取表中所有的数据
     * @param classType
     * @param <T>
     * @return
     */
    public <T extends IEntity> ArrayList<T> queryAllByClassType(Class<T> classType){
        try {
            ArrayList<T> dataList = new ArrayList<>();
            TableStorage tableStorage = tableStorageMap.get(classType);
            Cursor cr = sqlDataBase.query(tableStorage.getTableName(), null, null, null, null, null, null);
            while (cr.moveToNext()){
                T classInstance = classType.newInstance();
                for(TableStorage.ColumnStorage columnStorage : tableStorage.getColumns()){
                    Field field = classType.getDeclaredField(columnStorage.getVariableName());
                    field.setAccessible(true);
                    int columnIndex = cr.getColumnIndex(columnStorage.getColumnName());
                    if(columnIndex == -1){
                        LogUtils.e(columnStorage.getColumnName() + " Attribute not found");
                        continue;
                    }
                    if(field.getType().getName().equals(int.class.getName())){
                        field.set(classInstance, cr.getInt(columnIndex));
                    }else if(field.getType().getName().equals(String.class.getName())){
                        field.set(classInstance, cr.getString(columnIndex));
                    }else if(field.getType().getName().equals(float.class.getName())){
                        field.set(classInstance, cr.getFloat(columnIndex));
                    }else if(field.getType().getName().equals(double.class.getName())){
                        field.set(classInstance, cr.getDouble(columnIndex));
                    }else if(field.getType().getName().equals(boolean.class.getName())){
                        field.set(classInstance, cr.getInt(columnIndex) == 1);
                    }else{
                        throw new RuntimeException("Data type cannot be processed");
                    }
                }
                Field field = classType.getDeclaredField(tableStorage.getPrimaryVariableName());
                field.setAccessible(true);
                field.set(classInstance, cr.getInt(cr.getColumnIndex(tableStorage.getPrimaryName())));
                dataList.add(classInstance);
            }
            cr.close();
            return dataList;
        } catch (Exception e) {
            LogUtils.e(e);
        }
        return null;
    }

    /**
     * 解析表上的注解
     */
    private void parseTableAnnotation(Class<? extends IEntity> ... registerTable){
        for(Class tableClass : registerTable){
            TableStorage tableStorage = new TableStorage();
            Entity entity = (Entity) tableClass.getAnnotation(Entity.class);
            tableStorage.setTableName(entity.tableName());
            Field[] fields = tableClass.getDeclaredFields();
            for(Field field : fields){
                Annotation[] annotations = field.getAnnotations();
                if(annotations == null || annotations.length == 0) continue;
                if(annotations[0] instanceof ColumnInfo){
                    ColumnInfo columnInfo = (ColumnInfo) annotations[0];
                    TableStorage.ColumnStorage columnStorage = new TableStorage.ColumnStorage();
                    columnStorage.setColumnName(columnInfo.name());
                    columnStorage.setNull(columnInfo.isNull());
                    columnStorage.setSqlTypeName(field.getType());
                    columnStorage.setVariableName(field.getName());
                    tableStorage.addColumn(columnStorage);
                }else if(annotations[0] instanceof PrimaryKey){
                    tableStorage.setPrimaryName(((PrimaryKey)annotations[0]).name());
                    tableStorage.setPrimaryVariableName(field.getName());
                }
            }
            tableStorageMap.put(tableClass, tableStorage);
        }
    }

    /**
     * 判断表是否存在
     * @param tableName 表名称
     * @return true存在 false不存在
     */
    private boolean hasExitTable(String tableName){
        Cursor cr = sqlDataBase.query("sqlite_master", null, String.format("type='%s' and name = '%s'","table", tableName), null, null, null, null);
        boolean hasExit = cr.getCount() > 0;
        cr.close();
        return hasExit;
    }

    private SQLiteDatabase createSQLiteDataBase(Context mContext) {
        return new SQLiteOpenHelper(mContext, mDataBaseConfig.getDataBaseName(), null, mDataBaseConfig.getDataBaseVersion()){
            @Override
            public void onCreate(SQLiteDatabase db) {
            }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                //升级初始化在这里主要是因为在执行getWritableDatabase的时候，会将新version写到数据库中，具体实现参考源码中SQLiteOpenHelper.getDatabaseLocked
                // 所以我们这里是默认升级成功，在DataBaseUpgradeHolder中如果升级失败会通过serVersion做降级处理
                DataBaseUpgradeHolder.get().init(db, oldVersion, newVersion);
            }
        }.getWritableDatabase();
    }
}
