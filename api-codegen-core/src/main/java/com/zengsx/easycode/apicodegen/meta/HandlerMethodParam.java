package com.zengsx.easycode.apicodegen.meta;

import com.zengsx.easycode.apicodegen.constants.HandlerMethodParamTag;
import java.util.List;
import lombok.Data;

/**
 * @ClassName: HandlerMethodParamMeta
 * @Description: TODO
 * @Author: Mr.Zeng
 * @Date: 2021-04-23 11:22
 */
@Data
public class HandlerMethodParam implements Importable {

    /**
     * 分类{@link HandlerMethodParamTag}
     */
    private int tag;

    /**
     * 类型
     */
    private String type;
    /**
     * 短类型
     */
    private String shortType;

    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 对标 @NotNull
     */
    private boolean required;

    /**
     * 校验注解
     */
    private List<ValidateAnnotation> validateAnnotations;
}
