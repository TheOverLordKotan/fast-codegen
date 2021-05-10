package com.zengsx.easycode.sqlcodegen.config;

import java.util.HashSet;
import java.util.Set;
import lombok.Data;

/**
 * @ClassName: MybatisInputConfig
 * @Description: TODO
 * @Author: Mr.Zeng
 * @Date: 2021-04-17 21:38
 */
@Data
public class MybatisPlusConfig {

    /**
     * 逻辑删除字段field，不指定默认 del
     */
    private String logicDelColumnName = "del";

    private Set<String> autoInsertFields = new HashSet<>();

    private Set<String> autoUpdateFields = new HashSet<>();

    private Set<String> autoInsertOrUpdateFields = new HashSet<>();

    private Boolean enableOutputMapper = false;

    private String mapperInterfacePackage = "mappers";

    private String mapperXmlDirName = "mapper";

}
