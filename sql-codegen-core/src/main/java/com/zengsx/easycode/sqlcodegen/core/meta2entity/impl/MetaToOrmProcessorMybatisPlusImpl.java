package com.zengsx.easycode.sqlcodegen.core.meta2entity.impl;

import com.zengsx.easycode.codegen.utils.VelocityUtils;
import com.zengsx.easycode.sqlcodegen.config.GlobalConfig;
import com.zengsx.easycode.sqlcodegen.core.meta2entity.IMetaToOrmProcessor;
import com.zengsx.easycode.sqlcodegen.core.meta2entity.context.MybatisPlusContext;
import com.zengsx.easycode.sqlcodegen.meta.Table;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @ClassName: MetaToEntityProcessorMybatisPlusImpl
 * @Description: 生成MybatisPlus文件
 * @Author: Mr.Zeng
 * @Date: 2021-04-27 22:37
 */
public class MetaToOrmProcessorMybatisPlusImpl implements IMetaToOrmProcessor<MybatisPlusContext> {

    private GlobalConfig config = null;

    @Override
    public void setConfig(GlobalConfig config) {
        this.config = config;
    }

    @Override
    public void setContext(MybatisPlusContext mybatisPlusContext) {

    }

    @Override
    public void generate(List<Table> tables) {
        tables.forEach(table -> {
            processLogicalDeleteField(table);
            processAutoFillFiled(table);
            Map<String, Object> params = new HashMap<>(8);
            params.put("entity", table);
            params.put("config", config);
            File file = new File(config.getEntityDirPath() + table.getClassName() + ".java");
            VelocityUtils.render("template" + File.separator + "mybatis_plus_entity.vm", params, file);
        });
    }

    /**
     * 处理逻辑删除字段
     *
     * @param table 表信息
     */
    private void processLogicalDeleteField(Table table) {
        table.getColumns().forEach(column -> {
            if (column.getColumnName().equals(config.getMybatisPlusConfig().getLogicDelColumnName())) {
                column.setIsLogicalDeleteField(true);
            }
        });
    }

    /**
     * 处理自动填充字段
     *
     * @param table 表信息
     */
    public void processAutoFillFiled(Table table) {
        Optional.ofNullable(config).map(GlobalConfig::getMybatisPlusConfig).ifPresent(mybatisConfig -> {
            String tableName = table.getTableName();
            table.getColumns().forEach(column -> {
                String colName = column.getColumnName();
                String uniqueColName = tableName + "@" + column.getColumnName();
                if (mybatisConfig.getAutoInsertFields().contains(colName)
                        || mybatisConfig.getAutoInsertFields().contains(uniqueColName)) {
                    column.setIsAutoFillWhenInsert(true);
                } else if (mybatisConfig.getAutoUpdateFields().contains(colName)
                        || mybatisConfig.getAutoUpdateFields().contains(uniqueColName)) {
                    column.setIsAutoFillWhenUpdate(true);
                } else if (mybatisConfig.getAutoInsertOrUpdateFields().contains(colName)
                        || mybatisConfig.getAutoInsertOrUpdateFields().contains(uniqueColName)) {
                    column.setIsAutoFillWhenInsertOrUpdate(true);
                }
            });
        });
    }
}
