package com.zengsx.easycode.sqlcodegen.core.sql2meta.impl;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLDataTypeImpl;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlCreateTableStatement;
import com.alibaba.fastjson.JSON;
import com.zengsx.easycode.codegen.utils.FormatUtils;
import com.zengsx.easycode.sqlcodegen.config.GlobalConfig;
import com.zengsx.easycode.sqlcodegen.core.sql2meta.ISqlToMetaProcessor;
import com.zengsx.easycode.sqlcodegen.core.sql2meta.context.MysqlContext;
import com.zengsx.easycode.sqlcodegen.meta.Column;
import com.zengsx.easycode.sqlcodegen.meta.Table;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.springframework.util.StringUtils;

/**
 * @ClassName: SqlToMetaProcessorMysqlImpl
 * @Description: TODO
 * @Author: Mr.Zeng
 * @Date: 2021-04-27 22:55
 */
public class SqlToMetaProcessorMysqlImpl implements ISqlToMetaProcessor<MysqlContext> {

    private GlobalConfig config = null;

    @Override
    public void setConfig(GlobalConfig config) {
        this.config = config;
    }

    @Override
    public void setContext(MysqlContext mysqlContext) {

    }

    @SneakyThrows
    public static void main(String[] args) {
        File sqlFile = new File("entity-convertor/src/main/resources/db/TableFile.sql");
        System.out.println(sqlFile.getAbsolutePath());
        String sql = IOUtils.toString(new FileInputStream(sqlFile), StandardCharsets.UTF_8);
        List<Table> tables = new SqlToMetaProcessorMysqlImpl().convertTable(sql);
        System.out.println(JSON.toJSONString(tables));
    }

    @Override
    public List<Table> convertTable(String createTableSql) {
        return SQLUtils.parseStatements(createTableSql, DbType.mysql)
                .stream().filter(o -> o instanceof MySqlCreateTableStatement)
                .map(o -> (MySqlCreateTableStatement) o)
                .map(tableStat -> {
                    Table table = new Table();
                    table.setTableName(FormatUtils.escapeQuotes(tableStat.getTableName()));
                    table.setClassName(FormatUtils.snakeToUpperCamel(table.getTableName()) + config.getEntitySuffix());
                    table.setTableComment(FormatUtils.escapeQuotes(tableStat.getComment().toString()));

                    String pkName = tableStat.getPrimaryKeyNames()
                            .stream()
                            .limit(1)
                            // 去掉 `
                            .map(FormatUtils::snakeToLowerCamel)
                            .findFirst()
                            .orElseThrow(
                                    () -> new RuntimeException(String.format("当前表不存在主键:%s", table.getTableName())));

                    if (tableStat.getPrimaryKeyNames().size() > 1) {
                        throw new RuntimeException("暂不支持联合主键");
                    }
                    table.setColumns(tableStat.getColumnDefinitions().stream().map(columnDef -> {
                        SQLDataTypeImpl dataType = (SQLDataTypeImpl) columnDef.getDataType();

                        Column column = new Column();
                        column.setColumnName(FormatUtils.escapeQuotes(columnDef.getColumnName()));

                        column.setIsPrimaryKey(pkName.equals(column.getColumnName()));
                        column.setIsAutoIncrement(columnDef.isAutoIncrement());

                        column.setFieldName(FormatUtils.snakeToLowerCamel(columnDef.getColumnName()));
                        column.setComment(FormatUtils.escapeQuotes(columnDef.getComment().toString()));
                        column.setDbType(dataType.getName());
                        column.setDbTypeDesc(dataType.toString());

                        column.setJavaType(dbTypeToJavaType(dataType.getName(), dataType.isUnsigned()));

                        return column;
                    }).collect(Collectors.toList()));
                    table.setImports(
                            table.getColumns()
                                    .stream()
                                    .map(Column::getImports)
                                    .filter(o -> !StringUtils.isEmpty(o))
                                    .distinct()
                                    .collect(Collectors.toList())
                    );
                    return table;
                }).collect(Collectors.toList());
    }


    @Override
    public String dbTypeToJavaType(String dbType, boolean isUnsigned) {
        return typeMappings.getOrDefault(isUnsigned ? dbType + "@unsigned" : dbType, dbType);
    }

    /**
     * java类型映射
     */
    private final Map<String, String> typeMappings = new HashMap<String, String>() {{
        put("varchar", "String");
        put("char", "String");

        put("tinytext", "String");
        put("mediumtext", "String");
        put("text", "String");
        put("longtext", "String");

        put("tinyblob", "byte[]");
        put("mediumblob", "byte[]");
        put("blob", "byte[]");
        put("longblob", "byte[]");

        put("integer", "Integer");
        put("integer@unsigned", "Long");
        put("mediumint", "Integer");
        put("mediumint@unsigned", "Long");
        put("tinyint", "Integer");
        put("smallint", "Integer");
        put("bigint", "Long");
        put("bigint@unsigned", "Long");

        put("float", "Float");
        put("double", "Double");
        put("decimal", "java.math.BigDecimal");

        put("boolean", "Boolean");
        put("bit", "boolean");

        put("date", "java.util.Date");
        put("time", "java.util.Date");
        put("datetime", "java.util.Date");
        put("timestamp", "java.util.Date");
    }};
}
