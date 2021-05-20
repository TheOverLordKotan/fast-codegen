package com.zengsx.easycode.apicodegen.enums;

/**
 * @EnumName: Type
 * @Description: TODO
 * @Author: Mr.Zeng
 * @Date: 2021-05-19 23:08
 */
public enum Type {

    BOOLEAN("Boolean"),
    INTEGER("Integer"),
    LONG("Long"),
    DECIMAL("java.math.BigDecimal"),
    FLOAT("Float"),
    DOUBLE("Double"),
    LOCAL_DATE("java.time.LocalDate"),
    LOCAL_TIME("java.time.LocalTime"),
    LOCAL_DATE_TIME("java.time.LocalDateTime"),
    STRING("String");


    Type(String fullTypeName) {
        this.fullTypeName = fullTypeName;
    }

    private final String fullTypeName;

    public String getImport() {
        return fullTypeName;
    }

    public String getName() {
        String[] sa = fullTypeName.split("\\.");
        return sa.length > 0 ? sa[sa.length - 1] : null;
    }
}
