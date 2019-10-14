package com.easy.database.entity;

import java.util.ArrayList;

/**
 * 表Column信息记录
 * Time: 2019/8/21 0021
 * Author: zoulong
 */

public class TableStorage {
    private String primaryName;
    private String primaryVariableName;
    private String tableName;
    private ArrayList<ColumnStorage> columns = new ArrayList<>();

    public void setPrimaryVariableName(String primaryVariableName) {
        this.primaryVariableName = primaryVariableName;
    }

    public void setPrimaryName(String primaryName) {
        this.primaryName = primaryName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void addColumn(ColumnStorage column) {
        this.columns.add(column);
    }

    public String getPrimaryName() {
        return primaryName;
    }

    public String getPrimaryVariableName() {
        return primaryVariableName;
    }

    public String getTableName() {
        return tableName;
    }

    public ArrayList<ColumnStorage> getColumns() {
        return columns;
    }

    public String getCreateTableSql(){
        StringBuffer sb = new StringBuffer();
        sb.append("create table if not exists ")
                .append(tableName)
                .append(" (")
                .append(primaryName)
                .append(" integer primary key autoincrement");
        for(ColumnStorage column : columns){
            sb.append(", ")
                    .append(column.columnName)
                    .append(" ")
                    .append(column.sqlTypeName);
            if(!column.isNull){
                sb.append(" ")
                        .append("not null");
            }
        }
        sb.append(")");
        return sb.toString();
    }

    public static class ColumnStorage{
        private String columnName;
        private boolean isNull;
        private String sqlTypeName;
        private String variableName;
        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        public void setNull(boolean aNull) {
            isNull = aNull;
        }

        public String getColumnName() {
            return columnName;
        }

        public boolean isNull() {
            return isNull;
        }

        public String getSqlTypeName() {
            return sqlTypeName;
        }

        public String getVariableName() {
            return variableName;
        }

        public void setSqlTypeName(Class typeClass){
            if(typeClass == null) throw new RuntimeException("class type is null");
            if(typeClass.getName().equals(int.class.getName())){
                this.sqlTypeName = SqlType.SQL_INT;
            }else if(typeClass.getName().equals(String.class.getName())){
                this.sqlTypeName = SqlType.SQL_STRING;
            }else if(typeClass.getName().equals(boolean.class.getName())){
                this.sqlTypeName = SqlType.SQL_BOOLEAN;
            }else if(typeClass.getName().equals(float.class.getName())){
                this.sqlTypeName = SqlType.SQL_FLOAT;
            }else if(typeClass.getName().equals(double.class.getName())){
                this.sqlTypeName = SqlType.SQL_DOUBLE;
            }else{
                throw new RuntimeException("Data types that cannot be processed");
            }
        }
        public void setSqlTypeName(String sqlTypeName) {
            this.sqlTypeName = sqlTypeName;
        }

        public void setVariableName(String variableName) {
            this.variableName = variableName;
        }
    }
}
