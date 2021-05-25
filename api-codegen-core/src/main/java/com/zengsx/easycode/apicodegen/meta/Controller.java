package com.zengsx.easycode.apicodegen.meta;

import com.zengsx.easycode.apicodegen.holders.DataHolder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;

/**
 * @ClassName: Controller
 * @Description: TODO
 * @Author: Mr.Zeng
 * @Date: 2021-04-19 15:15
 */
@Data
public class Controller implements Importable {

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
     * 当前类包含的 请求方法
     */
    private List<HandlerMethod> handlerMethods;

    /**
     * import holder
     */
    private final DataHolder<String> externalImportHolder = new DataHolder<>();
    /**
     * annotation holder
     */
    private final DataHolder<ValidateAnnotation> validateAnnotationHolder = new DataHolder<>();


    @Override
    public List<String> getExternalImports() {
        List<String> externalImports = new ArrayList<>(externalImportHolder.get());
        handlerMethods.forEach(handlerMethod -> externalImports.addAll(handlerMethod.getExternalImports()));
        validateAnnotationHolder.get()
                .forEach(annotation -> externalImports.addAll(annotation.getExternalImportHolder().get()));
        return externalImports.stream().distinct().collect(Collectors.toList());
    }
}
