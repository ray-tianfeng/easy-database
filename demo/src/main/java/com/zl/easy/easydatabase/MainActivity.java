package com.zl.easy.easydatabase;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.easy.database.DataBaseConfig;
import com.easy.database.holder.SQLiteDatabaseHolder;
import com.zl.easy.easydatabase.holder.TestTableHolder;
import com.zl.easy.easydatabase.table.TestTable;
import com.zl.easy.easydatabase.table.UpgradeTable;
import com.zl.easy.easydatabase.upgrade.V2;

import java.util.ArrayList;

public class MainActivity extends Activity {
    private TextView result;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        result = findViewById(R.id.tv_result);
    }

    public void onClick(View view){
        int viewId = view.getId();
        switch (viewId){
            case R.id.bt_init:
                DataBaseConfig dataBaseConfig = new DataBaseConfig.Builder()
                        .dataBaseName("test.db")
                        .version(2)//数据库版本
                        .registerTable(TestTable.class, UpgradeTable.class)//注册表，可以添加多个以“，”隔开
                        .upgradeGradients(new V2())//数据库升级，可以添加多个升级计划以“，”隔开
                        .build();
                SQLiteDatabaseHolder.getInstance().init(this, dataBaseConfig);
                break;
            case R.id.bt_add:
                TestTable testTable = new TestTable();
                testTable.setTestString("this is text");
                testTable.setTestFloat(1.0f);
                testTable.setTestDouble(2.0);
                testTable.setTestBoolean(true);
                int id = SQLiteDatabaseHolder.getInstance().insert(testTable);
                if(id > 0) result.setText("添加数据成功，id："+id);
                else result.setText("添加数据失败");
                break;
            case R.id.bt_delete:
                ArrayList<TestTable> datas = SQLiteDatabaseHolder.getInstance().queryAllByClassType(TestTable.class);
                if(datas != null && datas.size() > 0){
                    TestTable data = datas.get(0);
                    boolean deleteResult = SQLiteDatabaseHolder.getInstance().deleteByPrimaryId(data.getId(), TestTable.class);
                    result.setText("删除id为"+data.getId()+"的数据"+(deleteResult ? "成功":"失败"));
                }else{
                    result.setText("删除失败，数据库为空");
                }
                break;
            case R.id.bt_update:
                ArrayList<TestTable> datas2 = SQLiteDatabaseHolder.getInstance().queryAllByClassType(TestTable.class);
                if(datas2 != null && datas2.size() > 0){
                    TestTable data = datas2.get(0);
                    data.setTestString("数据已经被更新");
                    boolean deleteResult = SQLiteDatabaseHolder.getInstance().updateByPrimaryId(data);
                    result.setText("更新id为"+data.getId()+"的数据"+(deleteResult ? "成功":"失败"));
                }else{
                    result.setText("更新失败，数据库为空");
                }
                break;
            case R.id.bt_get_all:
                ArrayList<TestTable> datas3 = SQLiteDatabaseHolder.getInstance().queryAllByClassType(TestTable.class);
                result.setText("所有数据：" + JSON.toJSONString(datas3));
                break;
            case R.id.bt_text_upgrade:
                UpgradeTable upgradeTable = new UpgradeTable();
                upgradeTable.setDes("this is upgrade table des");
                int insertId = SQLiteDatabaseHolder.getInstance().insert(upgradeTable);
                if(insertId > 0) result.setText("更新表添加数据成功，id："+insertId);
                else result.setText("更新表添加数据失败");
                break;
            case R.id.bt_self_holder:
                TestTableHolder testTableHolder = new TestTableHolder();
                SQLiteDatabaseHolder.getInstance().decorateHolder(testTableHolder);
                testTableHolder.delete();
                result.setText("执行成功");
                break;
        }
    }
}
