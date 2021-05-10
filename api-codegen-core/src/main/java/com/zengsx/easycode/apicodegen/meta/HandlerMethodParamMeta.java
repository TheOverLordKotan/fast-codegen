package com.zengsx.easycode.apicodegen.meta;

import com.zengsx.easycode.apicodegen.constants.ParamTag;
import lombok.Data;

/**
 * @ClassName: HandlerMethodParamMeta
 * @Description: TODO
 * @Author: Mr.Zeng
 * @Date: 2021-04-23 11:22
 */
@Data
public class HandlerMethodParamMeta {

    /**
     * 分类{@link ParamTag}
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
     * 对标 @NotNull
     */
    private boolean required;
}
