package com.zengsx.easycode.apicodegen.meta;

import com.zengsx.easycode.apicodegen.holders.DataHolder;
import lombok.Data;

/**
 * @ClassName: HandlerMethodReturn
 * @Description: TODO
 * @Author: Mr.Zeng
 * @Date: 2021-04-23 17:34
 */
@Data
public class HandlerMethodReturn {

    /**
     * 类型
     */
    private String type;

    /**
     * 描述
     */
    private String description;

    /**
     * @return 是否有返回值
     */
    public boolean hasReturn() {
        return !void.class.getSimpleName().equals(type);
    }

    /**
     * import holder
     */
    private final DataHolder<String> externalImportHolder = new DataHolder<>();
    /**
     * annotation holder
     */
    private final DataHolder<ValidateAnnotation> validateAnnotationHolder = new DataHolder<>();

}
