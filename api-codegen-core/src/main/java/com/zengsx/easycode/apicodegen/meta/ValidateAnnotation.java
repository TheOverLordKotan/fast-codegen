package com.zengsx.easycode.apicodegen.meta;

import com.zengsx.easycode.apicodegen.holders.DataHolder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Data;
import org.springframework.util.ObjectUtils;

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
     * import holder
     */
    private final DataHolder<String> externalImportHolder = new DataHolder<>();

    /**
     * 注解属性
     */
    private List<ValidateAnnotationProperty> properties;

    /**
     * 添加属性
     *
     * @param name          属性名
     * @param value         属性值
     * @param enabledQuotes 是否加引号
     */
    public void addProperty(String name, String value, boolean enabledQuotes) {
        Objects.requireNonNull(value, "value  must not be bull");
        ValidateAnnotationProperty property = new ValidateAnnotationProperty(name, value, enabledQuotes);
        properties = Optional.ofNullable(properties).orElse(new ArrayList<>());
        properties.add(property);
    }

    @Override
    public String toString() {
        if (ObjectUtils.isEmpty(properties)) {
            return "@" + annotationName;
        }
        return "@" + annotationName + "(" +
                properties.stream()
                        .map(ValidateAnnotationProperty::toString)
                        .collect(Collectors.joining(", "))
                + ")";
    }

    @Data
    static class ValidateAnnotationProperty {

        private String name;

        private String value;

        private boolean enabledQuotes;

        public ValidateAnnotationProperty(String name, String value, boolean enabledQuotes) {
            this.name = name;
            this.value = value;
            this.enabledQuotes = enabledQuotes;
        }

        // todo valueImport
        @Override
        public String toString() {
            if (name == null || "".equals(name.trim())) {
                return enabledQuotes ? "\"" + value + "\"" : value;
            }
            String format = enabledQuotes ? "%s = \"%s\"" : "%s = %s";
            return String.format(format, name, value);
        }

    }


}
