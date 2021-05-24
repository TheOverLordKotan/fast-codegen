package com.zengsx.easycode.apicodegen.meta;

import com.zengsx.easycode.apicodegen.constants.HandlerMethodParamTag;
import com.zengsx.easycode.apicodegen.meta.action.AbstractMeta;
import java.util.Optional;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @ClassName: HandlerMethodParam
 * @Description: TODO
 * @Author: Mr.Zeng
 * @Date: 2021-04-23 11:22
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class HandlerMethodParam extends AbstractMeta {

    /**
     * 分类{@link HandlerMethodParamTag}
     */
    private int tag;

    /**
     * 类型
     */
    private String type;
    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    @Override
    protected void processExternalImport() {
        // 注解的import，内部使用
        getValidateAnnotations().forEach(validateAnnotation -> {
            Optional.ofNullable(validateAnnotation.getAnnotationImports())
                    .ifPresent(imports -> imports.forEach(this::addExternalImport));
        });
    }
}
