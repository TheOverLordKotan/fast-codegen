package com.zengsx.easycode.apicodegen.meta;

import java.util.List;
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
     * 描述
     */
    private String description;

    /**
     * 外部引入
     */
    private List<String> externalImport;

    public boolean hasReturn() {
        return !"void".equals(type);
    }

}
