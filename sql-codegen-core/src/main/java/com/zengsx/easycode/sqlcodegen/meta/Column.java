package com.zengsx.easycode.sqlcodegen.meta;

import lombok.Data;

/**
 * @ClassName: Column
 * @Description: TODO
 * @Author: Mr.Zeng
 * @Date: 2021-04-17 20:49
 */
@Data
public class Column {

    private Boolean isPrimaryKey;

    private Boolean isAutoIncrement;

    private String fieldName;

    private String columnName;

    private String comment;

    private String dbType;

    private String dbTypeDesc;

    private String javaType;

    private Boolean isAutoFillWhenInsert;

    private Boolean isAutoFillWhenUpdate;

    private Boolean isAutoFillWhenInsertOrUpdate;

    private Boolean isLogicalDeleteField;

    /**
     * @return java type name
     */
    public String getSimpleJavaType() {
        return javaType.contains(".") ? javaType.substring(javaType.lastIndexOf(".") + 1) : javaType;
    }

    /**
     * @return import
     */
    public String getImports() {
        return javaType.contains(".") ? javaType : null;
    }

}
