package com.zengsx.easycode.apicodegen.meta;

import com.zengsx.easycode.apicodegen.meta.action.AbstractMeta;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @ClassName: HandlerMethod
 * @Description: TODO
 * @Author: Mr.Zeng
 * @Date: 2021-04-19 15:15
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class HandlerMethod extends AbstractMeta {

    /**
     * 请求类型  GET POST PUT PATCH DELETE
     */
    private String requestType;

    /**
     * 对应方法名
     */
    private String methodName;

    /**
     * 接口url
     */
    private String url;

    /**
     * 接口摘要
     */
    private String summary;

    /**
     * 接口描述
     */
    private String description;

    /**
     * 接收类型
     */
    private List<String> consumes;

    /**
     * 输出类型
     */
    private List<String> produces;

    /**
     * 方法参数
     */
    List<HandlerMethodParam> handlerMethodParams;

    /**
     * 返回数据类型
     */
    private HandlerMethodReturn handlerMethodReturn;

    @Override
    protected void processExternalImport() {
        handlerMethodParams.stream()
                .map(HandlerMethodParam::getExternalImports)
                .flatMap(List::stream)
                .forEach(this::addExternalImport);
        handlerMethodReturn.getExternalImports().forEach(this::addExternalImport);
    }

    /**
     * @return 是否开启json序列化
     */
    public boolean enableResponseBody() {
        return produces.stream().anyMatch(o -> o.contains("json"));
    }

    /**
     * @return 是否开json反序列化
     */
    public boolean enableRequestBody() {
        return consumes.stream().anyMatch(o -> o.contains("json"));
    }

}
