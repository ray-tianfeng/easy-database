package com.zl.easy.easydatabase.table;

import com.easy.database.annotation.ColumnInfo;
import com.easy.database.annotation.Entity;
import com.easy.database.annotation.PrimaryKey;
import com.easy.database.entity.IEntity;

/**
 * Time: 2019/10/10 0010
 * Author: zoulong
 */
@Entity(tableName = "test_table")
public class TestTable extends IEntity {
    @PrimaryKey(name = "primary_id")
    private int id;

    @ColumnInfo(name = "test_string", isNull = false)
    private String testString;

    @ColumnInfo(name = "test_boolean")
    private boolean testBoolean;

    @ColumnInfo(name = "test_float")
    private float testFloat;

    @ColumnInfo(name = "test_double")
    private double testDouble;

    public TestTable() {//必须有一个空实现
    }

    public int getId() {
        return id;
    }

    public String getTestString() {
        return testString;
    }

    public void setTestString(String testString) {
        this.testString = testString;
    }

    public boolean isTestBoolean() {
        return testBoolean;
    }

    public void setTestBoolean(boolean testBoolean) {
        this.testBoolean = testBoolean;
    }

    public float getTestFloat() {
        return testFloat;
    }

    public void setTestFloat(float testFloat) {
        this.testFloat = testFloat;
    }

    public double getTestDouble() {
        return testDouble;
    }

    public void setTestDouble(double testDouble) {
        this.testDouble = testDouble;
    }
}
