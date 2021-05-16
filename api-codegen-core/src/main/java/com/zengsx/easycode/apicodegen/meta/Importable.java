package com.zengsx.easycode.apicodegen.meta;

import java.util.Collections;
import java.util.List;

/**
 * @InterfaceName: Importable
 * @Description: TODO
 * @Author: Mr.Zeng
 * @Date: 2021-05-16 22:52
 */
public interface Importable {

    /**
     * 返回需要导入的依赖
     *
     * @return 需要导入的依赖
     */
    public default List<String> imports() {
        return Collections.emptyList();
    }
}
