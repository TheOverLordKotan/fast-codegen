package com.zengsx.easycode.apicodegen.config;

import java.io.File;
import java.util.Optional;

/**
 * @ClassName: PathConfig
 * @Description: TODO
 * @Author: Mr.Zeng
 * @Date: 2021-05-13 17:44
 */
public class PathConfig {

    private final GlobalConfig config;

    public PathConfig(GlobalConfig config) {
        this.config = config;
    }

    /**
     * @return controller生成路径
     */
    public String getControllerPackagePath() {
        return getBasePackagePath() + config.getControllerPackageName() + File.separator;
    }

    /**
     * @return service生成路径
     */
    public String getServicePackagePath() {
        return getBasePackagePath() +  config.getServicePackageName() + File.separator;
    }

    /**
     * @return dto生成路径
     */
    public String getDtoPackagePath() {
        return getBasePackagePath() +  config.getDtoPackageName() + File.separator;
    }

    /**
     * @return feignClient生成路径
     */
    public String getFeignClientPackagePath() {
        return getBasePackagePath() +  config.getFeignClientPackageName() + File.separator;
    }

    /**
     * @return Application所在路径
     */
    public String getBasePackagePath() {
        return  config.getSrcJavaPath()
                + File.separator
                + Optional.ofNullable( config.getBasePackage())
                .orElse("")
                .replace(".", File.separator)
                + File.separator;
    }
}
