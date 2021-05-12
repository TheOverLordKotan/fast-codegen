package com.zengsx.easycode.sqlcodegen;

import com.zengsx.easycode.sqlcodegen.config.GlobalConfig;
import com.zengsx.easycode.sqlcodegen.config.MybatisPlusConfig;
import com.zengsx.easycode.sqlcodegen.config.MybatisPlusConfig.LogicDeleteColumn;
import java.util.Collections;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @ClassName: EntityGenerateTest
 * @Description: TODO
 * @Author: Mr.Zeng
 * @Date: 2021-04-28 15:06
 */
public class EntityGenerateTest {

    @Test
    @Ignore
    public void generateLighthouseEntity() {

        GlobalConfig config = new GlobalConfig();
        config.setDbName("mysql");
        config.setOrmName("mybatis-plus");
        config.setEntityPackageName("entities");
        config.setEntitySuffix("DO");
        config.setSqlFilePath("src/test/resources/db/TableSchema.sql");
        config.setSrcJavaPath("src/test/resources");
        config.setBasePackage("com.xiaojukeji.dichat.lighthouse");
        MybatisPlusConfig mybatisPlusConfig = new MybatisPlusConfig();
        LogicDeleteColumn logicDeleteColumn = new LogicDeleteColumn();
        logicDeleteColumn.setColumnName("deleted_at");
        logicDeleteColumn.setDeletedValue("now()");
        logicDeleteColumn.setNotDeletedValue("1000-01-01 00:00:00");
        mybatisPlusConfig.setLogicDelCols(Collections.singletonList(logicDeleteColumn));
        config.setMybatisPlusConfig(mybatisPlusConfig);
        new SqlCodegenRunner().start(config);
    }

}
