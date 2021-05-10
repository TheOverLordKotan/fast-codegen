package com.zengsx.easycode.sqlcodegen;

import com.zengsx.easycode.sqlcodegen.config.GlobalConfig;
import com.zengsx.easycode.sqlcodegen.config.MybatisPlusConfig;
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
//        config.setDbName("mysql");
//        config.setOrmName("mybatis-plus");
//        config.setEntityPackageName("entities");
//        config.setEntitySuffix("DO");
        config.setSqlFilePath("src/test/resources/db/TableSchema.sql");
        config.setSrcJavaPath("src/test/resources");
        config.setBasePackage("com.xiaojukeji.dichat.lighthouse");

        config.setMybatisPlusConfig(new MybatisPlusConfig());
        new SqlCodegenRunner().start(config);
    }

}
