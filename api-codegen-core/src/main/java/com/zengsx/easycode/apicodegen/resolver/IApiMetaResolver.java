package com.zengsx.easycode.apicodegen.resolver;

import com.zengsx.easycode.apicodegen.meta.ApiMetaResolveResult;

/**
 * @InterfaceName: IMetaResolver
 * @Description: TODO
 * @Author: Mr.Zeng
 * @Date: 2021-04-21 19:58
 */
public interface IApiMetaResolver<T> {

    /**
     * 给定对象解析得到元数据
     *
     * @param t 给定对象
     * @return 元数据
     */
    ApiMetaResolveResult resolve(T t);

}
