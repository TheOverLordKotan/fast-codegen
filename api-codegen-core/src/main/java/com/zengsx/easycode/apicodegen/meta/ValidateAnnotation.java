package com.zengsx.easycode.apicodegen.meta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.Data;

/**
 * @ClassName: ValidateAnnotation
 * @Description: TODO
 * @Author: Mr.Zeng
 * @Date: 2021-05-12 23:14
 */
@Data
public class ValidateAnnotation {

    /**
     * 注解名称
     */
    private String annotationName;

    /**
     * 注解import
     */
    private String annotationImport;

    /**
     * 注解属性
     */
    private List<ValidateAnnotationProperty> properties;

    /**
     * 添加属性
     *
     * @param name  属性名
     * @param value 属性值
     */
    public void addProperty(String name, Object value) {
        Objects.requireNonNull(name, "name must not be bull");
        Objects.requireNonNull(value, "value  must not be bull");
        ValidateAnnotationProperty property = new ValidateAnnotationProperty();
        property.setName(name);
        property.setValue(String.valueOf(value));
        properties = Optional.ofNullable(properties).orElse(new ArrayList<>());
        properties.add(property);
    }

    @Data
    static class ValidateAnnotationProperty {

        private String name;

        private String value;

        private boolean enabledQuotes;

        // todo valueImport

    }


}
