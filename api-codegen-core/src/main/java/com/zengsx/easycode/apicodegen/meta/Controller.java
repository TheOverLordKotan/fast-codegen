package com.zengsx.easycode.apicodegen.meta;

import java.util.List;
import lombok.Data;

/**
 * @ClassName: ControllerMeta
 * @Description: TODO
 * @Author: Mr.Zeng
 * @Date: 2021-04-19 15:15
 */
@Data
public class Controller  {

    /**
     * controller 名称
     */
    private String name;
    /**
     * service名称
     */
    private String serviceName;
    /**
     * feignClient对象名称
     */
    private String feignClientName;
    /**
     * 描述
     */
    private String description;
    /**
     * 基础路径
     */
    private String basePath;
    /**
     * 接收类型
     */
    private List<String> consumes;

    /**
     * 输出类型
     */
    private List<String> produces;

    /**
     * 当前类包含的 请求方法
     */
    private List<HandlerMethod> handlerMethods;

}
