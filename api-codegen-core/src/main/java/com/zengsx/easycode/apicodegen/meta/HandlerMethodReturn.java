package com.zengsx.easycode.apicodegen.meta;

import com.zengsx.easycode.apicodegen.meta.action.AbstractMeta;
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

}
