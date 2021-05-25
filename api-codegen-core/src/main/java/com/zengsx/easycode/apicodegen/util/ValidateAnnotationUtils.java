package com.zengsx.easycode.apicodegen.util;

import com.zengsx.easycode.apicodegen.meta.ValidateAnnotation;
import java.util.Arrays;

/**
 * @ClassName: ValidateAnnotationUtils
 * @Description: TODO
 * @Author: Mr.Zeng
 * @Date: 2021-05-12 23:37
 */
public class ValidateAnnotationUtils {

    public static ValidateAnnotation notNull() {
        ValidateAnnotation annotation = new ValidateAnnotation();
        annotation.setAnnotationName("NotNull");
        annotation.getExternalImportHolder().addItem("javax.validation.constraints.NotNull");
        return annotation;
    }

    public static ValidateAnnotation data() {
        ValidateAnnotation annotation = new ValidateAnnotation();
        annotation.setAnnotationName("Data");
        annotation.getExternalImportHolder().addItem("lombok.Data");
        return annotation;
    }


    public static ValidateAnnotation jsonInclude() {
        ValidateAnnotation annotation = new ValidateAnnotation();
        annotation.setAnnotationName("JsonInclude");
        annotation.getExternalImportHolder().addItem(Arrays.asList(
                "com.fasterxml.jackson.annotation.JsonInclude",
                "com.fasterxml.jackson.annotation.JsonInclude.Include"
        ));
        annotation.addProperty(null, "Include.NON_NULL", false);
        return annotation;
    }

    public static ValidateAnnotation valid() {
        ValidateAnnotation annotation = new ValidateAnnotation();
        annotation.setAnnotationName("Valid");
        annotation.getExternalImportHolder().addItem("javax.validation.Valid");
        return annotation;
    }


}
