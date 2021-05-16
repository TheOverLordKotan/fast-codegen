package com.zengsx.easycode.apicodegen.meta;

import lombok.Data;

/**
 * @ClassName: HandlerMethodReturnMeta
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
     * 短类型
     */
    private String shortType;

    /**
     * 描述
     */
    private String description;

    public boolean isNotVoid() {
        return !"void".equals(type);
    }

}
