## SQLite数据库操作 ##
- **数据库的映射关系式通过注解来说明**
- **动态创建表**
- **梯度式版本更新**
- **自定义操作**  
    
    这是一个通过注解来说明映射关系的数据库操作库，以对象的方式操作数据库，本库还提供数据库梯度升级方案。现已封装成依赖包，也可以直接git源代码修改。
   
### 用法 ###

-  LIB配置</br>
<pre><code>allprojects {
    repositories {
        jcenter()
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
    compile fileTree(dir: 'libs', include: ['*.jar'])
    compile 'com.github.ray-tianfeng:FileDownload:v1.0.4'
}</pre></code>

- 初始化
 ```java
    DataBaseConfig dataBaseConfig = new DataBaseConfig.Builder()
                        .dataBaseName("test.db")//数据库名称
                        .version(2)//数据库版本
                        .registerTable(TableEntity.class)//注册表，可以添加多个以“，”隔开
                        .upgradeGradients(new V2())//数据库梯度升级，可以添加多个升级计划以“，”隔开
                        .build();
    SQLiteDatabaseHolder.getInstance().init(Context mContext, DataBaseConfig dataBaseConfig);
  ```
  说明：
 <table>
 <tr>
  <td>参数</td>
  <td>是否必须</td>
  <td>说明</td>
 </tr>
 <tr>
  <td>dataBaseName</td>
  <td>Y</td>
  <td>数据库名称</td>
 </tr>
 <tr>
  <td>dataBaseVersion</td>
  <td>Y</td>
  <td>数据库版本</td>
 </tr>
 <tr>
  <td>tables</td>
  <td>N</td>
  <td>数据库表映射类注册，这里的表必须继承IEntity，而且需要一个空的构造方法，可以在初始化方法里面注册，也可以使用的时候再注册</td>
 </tr>
 <tr>
  <td>gradients</td>
  <td>N</td>
  <td>数据库版本</td>
 </tr>
 </table>

- 创建表映射
   1. 注解介绍  
    Entity：注解表，并设置表名称，示例：@Entity(tableName = "test_table")  
    PrimaryKey：注解主键，并设置主键列名称，示例：@PrimaryKey(name = "primary_id")  
    ColumnInfo：注解列名称并设置名称，示例： @ColumnInfo(name = "test_float")
   2. demo示例：
 ```java
@Entity(tableName = "test_table")
public class TestTable extends IEntity {//必须实现IEntity
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
```
- 注册表
 ```java
    SQLiteDatabaseHolder.getInstance().registerTable(TableEntity);
```
说明：注册映射表

- 插入数据
```java
    SQLiteDatabaseHolder.getInstance().insert(TableEntity);
```
  说明：TableEntity为IEntity的子类
  
- 根据主键id删除数据
```java
  QLiteDatabaseHolder.getInstance().deleteByPrimaryId(id, TableEntity.class);
```
说明：  
id为主键id  
TableEntity为IEntity的子类的class

- 更新数据
```java
SQLiteDatabaseHolder.getInstance().updateByPrimaryId(TableEntity);
```
- 获取映射表全部数据
```java
SQLiteDatabaseHolder.getInstance().queryAllByClassType(TableEntity.class);
```
- 自定义操作
1. 实现DataBaseHolder&lt;T&gt; T类型为映射表实体类
2. 编写操作代码
3. 使用SQLiteDatabaseHolder装饰自定义操作类

demo示例：
```java
public class TestTableHolder extends DataBaseHolder<TestTable> {

    //删除主键id大于10的数据
    public void delete(){
        getDateBase().delete(getTableStorage().getTableName(), getTableStorage().getPrimaryName() + " > 10", null);
    }
}

TestTableHolder testTableHolder = new TestTableHolder();
SQLiteDatabaseHolder.getInstance().decorateHolder(testTableHolder);
testTableHolder.delete();
```

- 数据库版本梯度升级
1. 实现DataBaseUpgradeHolder.AbstractSQLiteDefaultUpgrade或者ISQLiteUpgrade（AbstractSQLiteDefaultUpgrade实现了默认升级逻辑，备份数据，然后删除所有表，最后在创建表还原数据）
2. 实现isApplyUpgrade，这个方法判断当先这个升级计划可以升级区间，例如：线上版本为2，升级版本为3，方法实现为1 < currentVersion && currentVersion < 3
3. 实现getUpgradedVersion 返回升级后数据库版本号
4. 实现upgrade，升级数库版本，该方法有两个参数，SQLiteDatabase数据库操作对象，tableStorageMap映射表

demo示例：
```java
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
```
**总结：**
   本库只支持非关系型数据库，支持的数据类型仅限基础数据类型，使用适用于记录大量非关系型数据，例如数据采集、账号密码。
   
   [源码地址](https://github.com/ray-tianfeng/easydatabase)
