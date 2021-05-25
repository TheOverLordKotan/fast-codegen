package com.zengsx.easycode.apicodegen.meta;

import com.zengsx.easycode.apicodegen.constants.HandlerMethodParamTag;
import com.zengsx.easycode.apicodegen.holders.DataHolder;
import lombok.Data;

/**
 * @ClassName: HandlerMethodParam
 * @Description: TODO
 * @Author: Mr.Zeng
 * @Date: 2021-04-23 11:22
 */
@Data
public class HandlerMethodParam {

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

    /**
     * import holder
     */
    private final DataHolder<String> externalImportHolder = new DataHolder<>();
    /**
     * annotation holder
     */
    private final DataHolder<ValidateAnnotation> validateAnnotationHolder = new DataHolder<>();

}
