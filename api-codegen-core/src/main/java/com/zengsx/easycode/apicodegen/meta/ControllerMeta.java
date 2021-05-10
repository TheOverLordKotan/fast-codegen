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
public class ControllerMeta {

    /**
     * controller 名称
     */
    private String name;
    /**
     * service名称
     */
    private String serviceName;
    /**
     * 作者
     */
    private String author;
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
     * 当前类需要导入的依赖
     */
    private List<String> dtoImports;
    /**
     * 当前类包含的 请求方法
     */
    private List<HandlerMethodMeta> handlerMethodMetas;

}
