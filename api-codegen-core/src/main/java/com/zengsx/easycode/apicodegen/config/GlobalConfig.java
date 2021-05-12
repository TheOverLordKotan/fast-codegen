package com.zengsx.easycode.apicodegen.config;

import java.io.File;
import java.util.Optional;
import lombok.Data;

/**
 * @ClassName: GlobalConfig
 * @Description: TODO
 * @Author: Mr.Zeng
 * @Date: 2021-04-25 00:16
 */
@Data
public class GlobalConfig {

    /**
     * api定义类型,默认swagger
     */
    private String apiDefineType = "Swagger";
    /**
     * swagger文档目录
     */
    private String apiDefineDirPath;
    /**
     * 项目src目录的路径(默认可以从pom取，如果要生成在其他地方可自定义)
     */
    private String srcJavaPath;
    /**
     * 项目root包(Application类所在包路径)
     */
    private String basePackage;
    /**
     * controller包名
     */
    private String controllerPackageName = "controllers";
    /**
     * service包名
     */
    private String servicePackageName = "services";
    /**
     * dto包名
     */
    private String dtoPackageName = "dtos";

    /**
     * @return controller生成路径
     */
    public String getControllerPackagePath() {
        return getBasePackagePath() + controllerPackageName + File.separator;
    }

    /**
     * @return service生成路径
     */
    public String getServicePackagePath() {
        return getBasePackagePath() + servicePackageName + File.separator;
    }

    /**
     * @return dto生成路径
     */
    public String getDtoPackagePath() {
        return getBasePackagePath() + dtoPackageName + File.separator;
    }

    /**
     * @return Application所在路径
     */
    public String getBasePackagePath() {
        return srcJavaPath
                + File.separator
                + Optional.ofNullable(basePackage)
                .orElse("")
                .replace(".", File.separator)
                + File.separator;
    }

}
