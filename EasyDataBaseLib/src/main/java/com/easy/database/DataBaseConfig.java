package com.easy.database;
import com.easy.database.entity.IEntity;
import com.easy.database.holder.DataBaseUpgradeHolder;

/**
 * Time: 2019/10/10 0010
 * Author: zoulong
 */
public class DataBaseConfig {
    private int dataBaseVersion;
    private String dataBaseName;
    private Class<? extends IEntity>[] tables;
    private DataBaseUpgradeHolder.ISQLiteUpgrade[] gradients;

    private DataBaseConfig(String dataBaseName, int dataBaseVersion, Class<? extends IEntity>[] tables, DataBaseUpgradeHolder.ISQLiteUpgrade[] gradients) {
        this.dataBaseName = dataBaseName;
        this.dataBaseVersion = dataBaseVersion;
        this.tables = tables;
        this.gradients = gradients;
    }

    public int getDataBaseVersion() {
        return dataBaseVersion;
    }

    public Class<? extends IEntity>[] getTables() {
        return tables;
    }

    public DataBaseUpgradeHolder.ISQLiteUpgrade[] getGradients() {
        return gradients;
    }

    public String getDataBaseName() {
        return dataBaseName;
    }

    public static class Builder{
        private int dataBaseVersion = 1;
        private Class<? extends IEntity>[] tables;
        private String dataBaseName;
        private DataBaseUpgradeHolder.ISQLiteUpgrade[] gradients;

        public Builder version(int dataBaseVersion){
            this.dataBaseVersion = dataBaseVersion;
            return this;
        }

        public Builder registerTable(Class<? extends IEntity> ... tables){
            if(tables == null) tables = new Class[0];
            this.tables = tables;
            return this;
        }

        public <T extends DataBaseUpgradeHolder.ISQLiteUpgrade> Builder upgradeGradients(T ... gradients){
            this.gradients = gradients;
            return this;
        }

        public Builder dataBaseName(String dataBaseName){
            if(dataBaseName == null || dataBaseName.length() == 0) throw new RuntimeException("database name is null");
            this.dataBaseName = dataBaseName;
            return this;
        }
        public DataBaseConfig build(){
            return new DataBaseConfig(dataBaseName, dataBaseVersion, tables, gradients);
        }
    }
}
