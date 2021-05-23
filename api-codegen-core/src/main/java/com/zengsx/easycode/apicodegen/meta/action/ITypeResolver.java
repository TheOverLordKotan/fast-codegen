package com.zengsx.easycode.apicodegen.meta.action;

import java.util.Optional;

/**
 * @InterfaceName: ITypeResolver
 * @Description: TODO
 * @Author: Mr.Zeng
 * @Date: 2021-05-23 17:02
 */
public interface ITypeResolver {

    default String getType() {
        String[] split = Optional.ofNullable(getImport()).orElse("").split("\\.");
        return split.length > 0 ? split[split.length - 1] : null;
    }

    String getImport();

}
