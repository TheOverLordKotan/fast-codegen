package com.zengsx.easycode.apicodegen.util;

import com.zengsx.easycode.apicodegen.meta.ValidateAnnotation;

/**
 * @ClassName: ValidateAnnotationUtils
 * @Description: TODO
 * @Author: Mr.Zeng
 * @Date: 2021-05-12 23:37
 */
public class ValidateAnnotationUtils {

    public static ValidateAnnotation required(){
        ValidateAnnotation annotation = new ValidateAnnotation();
        annotation.setAnnotationName("NotNull");

        return annotation;
    }





}
