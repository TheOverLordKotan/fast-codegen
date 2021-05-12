package com.zengsx.easycode.apicodegen.meta;

import java.util.List;
import lombok.Data;

/**
 * @ClassName: ApiMetaResolverResult
 * @Description: api元数据解析结果
 * @Author: Mr.Zeng
 * @Date: 2021-04-24 18:17
 */
@Data
public class ApiResolveResult {

    /**
     * controller对象
     */
    private List<Controller> controllers;
    /**
     * 需要生成的dto信息
     */
    private List<Dto> dtos;

}
