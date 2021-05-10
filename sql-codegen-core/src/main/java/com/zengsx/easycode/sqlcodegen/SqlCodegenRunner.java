package com.zengsx.easycode.sqlcodegen;

import com.zengsx.easycode.codegen.utils.EnumUtils;
import com.zengsx.easycode.sqlcodegen.config.GlobalConfig;
import com.zengsx.easycode.sqlcodegen.core.meta2entity.IMetaToOrmProcessor;
import com.zengsx.easycode.sqlcodegen.core.sql2meta.ISqlToMetaProcessor;
import com.zengsx.easycode.sqlcodegen.enumuration.DB;
import com.zengsx.easycode.sqlcodegen.enumuration.ORM;
import com.zengsx.easycode.sqlcodegen.meta.Table;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;

/**
 * @ClassName: SqlCodegenRunner
 * @Description: sql代码生成
 * @Author: Mr.Zeng
 * @Date: 2021-04-28 13:37
 */
public class SqlCodegenRunner {

    /**
     * 开始生成代码
     *
     * @param config 全局配置
     */
    public void start(GlobalConfig config) {
        DB db = EnumUtils.getEnum(DB.class, config.getDbName());
        ORM orm = EnumUtils.getEnum(ORM.class, config.getOrmName());
        String sql = getCreateSqlContent(config.getSqlFilePath());
        ISqlToMetaProcessor<?> sqlToMetaProcessor = db.getProcessorCreator().get();
        sqlToMetaProcessor.setConfig(config);
        List<Table> tables = sqlToMetaProcessor.convertTable(sql);
        IMetaToOrmProcessor<?> metaToEntityProcessor = orm.getProcessorCreator().get();
        metaToEntityProcessor.setConfig(config);
        metaToEntityProcessor.generate(tables);
    }

    /**
     * 获取sql文件内容
     *
     * @param sqlFilePath sql文件路径
     * @return sql建表语句
     */
    @SneakyThrows
    public String getCreateSqlContent(String sqlFilePath) {
        File sqlFile = new File(sqlFilePath);
        System.out.println("sqlFilePath:" + sqlFile.getAbsolutePath());
        return IOUtils.toString(new FileInputStream(sqlFile), StandardCharsets.UTF_8);
    }

}
