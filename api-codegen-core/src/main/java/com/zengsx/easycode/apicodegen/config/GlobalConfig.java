package com.zengsx.easycode.apicodegen.config;

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
     * 生成类型（springmvc,feignClient）
     */
    private String generateType = "SpringMvc";

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
     * feignClient包名
     */
    private String feignClientPackageName = "feignclients";

    /**
     * app名称
     */
    private String applicationName;
    /**
     * app服务端口号
     */
    private Integer applicationServerPort;

}
