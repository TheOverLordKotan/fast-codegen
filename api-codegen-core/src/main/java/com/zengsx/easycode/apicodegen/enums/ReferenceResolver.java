package com.zengsx.easycode.apicodegen.enums;

import com.zengsx.easycode.apicodegen.constants.SwaggerConstants;

/**
 * @EnumName: ReferenceResolver
 * @Description: TODO
 * @Author: Mr.Zeng
 * @Date: 2021-05-19 23:56
 */
public enum ReferenceResolver {


    DEFINITION(SwaggerConstants.DEFINITION_REFERENCE_PREFIX);


    private final String refPrefix;

    ReferenceResolver(String refPrefix) {
        this.refPrefix = refPrefix;
    }

    public static ReferenceResolver getInstance(String refPath) {
        return null;
    }

    public String getReferenceType(String refPath) {

    }

    public String getReferenceImport(String refPath) {

    }

}
