package com.zengsx.easycode.apicodegen.meta;

import com.zengsx.easycode.apicodegen.meta.action.AbstractMeta;
import java.util.Optional;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @ClassName: HandlerMethodReturnMeta
 * @Description: TODO
 * @Author: Mr.Zeng
 * @Date: 2021-04-23 17:34
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class HandlerMethodReturn extends AbstractMeta {

    /**
     * 类型
     */
    private String type;

    /**
     * 描述
     */
    private String description;

    public boolean hasReturn() {
        return !"void".equals(type);
    }

    @Override
    protected void processExternalImport() {
        // 注解的import，内部使用
        getValidateAnnotations().forEach(validateAnnotation -> {
            Optional.ofNullable(validateAnnotation.getAnnotationImports())
                    .ifPresent(imports -> imports.forEach(this::addExternalImport));
        });
    }
}
