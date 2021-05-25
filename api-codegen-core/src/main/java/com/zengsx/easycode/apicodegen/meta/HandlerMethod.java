package com.zengsx.easycode.apicodegen.meta;

import com.zengsx.easycode.apicodegen.holders.DataHolder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;

/**
 * @ClassName: HandlerMethod
 * @Description: TODO
 * @Author: Mr.Zeng
 * @Date: 2021-04-19 15:15
 */
@Data
public class HandlerMethod implements Importable {

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

    /**
     * import holder
     */
    private final DataHolder<String> externalImportHolder = new DataHolder<>();
    /**
     * annotation holder
     */
    private final DataHolder<ValidateAnnotation> validateAnnotationHolder = new DataHolder<>();

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

    @Override
    public List<String> getExternalImports() {
        List<String> externalImports = new ArrayList<>(externalImportHolder.get());
        handlerMethodParams.forEach(param -> externalImports.addAll(param.getExternalImportHolder().get()));
        externalImports.addAll(handlerMethodReturn.getExternalImportHolder().get());
        validateAnnotationHolder.get()
                .forEach(annotation -> externalImports.addAll(annotation.getExternalImportHolder().get()));
        return externalImports.stream().distinct().collect(Collectors.toList());
    }
}
