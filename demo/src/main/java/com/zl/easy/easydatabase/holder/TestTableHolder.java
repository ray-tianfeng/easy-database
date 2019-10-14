package com.zl.easy.easydatabase.holder;
import com.easy.database.holder.DataBaseHolder;
import com.zl.easy.easydatabase.table.TestTable;

/**
 * Time: 2019/10/11 0011
 * Author: zoulong
 */
public class TestTableHolder extends DataBaseHolder<TestTable> {

    //删除主键id大于10的数据
    public void delete(){
        getDateBase().delete(getTableStorage().getTableName(), getTableStorage().getPrimaryName() + " > 10", null);
    }
}
