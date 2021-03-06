package com.zengsx.easycode.apicodegen.util;

import com.zengsx.easycode.apicodegen.meta.ValidateAnnotation;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class SwaggerVendorExtensionsUtil {

    public static String getXFormat(Map<String, Object> vendorExtensions) {
        return Optional.ofNullable(vendorExtensions)
                .map(map -> map.get("x-format"))
                .map(Object::toString)
                .orElse(null);
    }


    public static List<String> getImports(Map<String, Object> vendorExtensions) {
        return Arrays.asList(Optional.ofNullable(vendorExtensions)
                .map(map -> map.get("x-import"))
                .map(Object::toString)
                .orElse("")
                .split(";|,|；|，"));
    }

    public static List<ValidateAnnotation> getValidateAnnotations(Map<String, Object> vendorExtensions) {
        return vendorExtensions.keySet()
                .stream()
                .filter(key -> key.startsWith("x-@") & key.length() > 3)
                .map(key -> {
                    ValidateAnnotation annotation = new ValidateAnnotation();
                    annotation.setAnnotationName(key.substring(3));
                    Object val = vendorExtensions.get(key);
                    if (val instanceof Map) {
                        Map<String, Object> data = (Map<String, Object>) val;
                        // 处理 import
                        if (data.containsKey("x-import")) {
                            annotation.getExternalImportHolder().addItem(getImports(data));
                            data.remove("x-import");
                        }
                        data.forEach((k, v) -> annotation.addProperty(k, String.valueOf(v), v instanceof String));
                    } else {
                        throw new RuntimeException("注解扩展属性（x-@***）的值一定是 {} 或 有值的map!");
                    }
                    return annotation;
                }).collect(Collectors.toList());

    }


}
