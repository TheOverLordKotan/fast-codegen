package com.zengsx.easycode.apicodegen.meta;

import com.zengsx.easycode.apicodegen.constants.SwaggerConstants;
import com.zengsx.easycode.apicodegen.holders.DataHolder;
import com.zengsx.easycode.apicodegen.util.ValidateAnnotationUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Data;
import org.springframework.util.ObjectUtils;

/**
 * @ClassName: Dto
 * @Description: dto定义
 * @Author: Mr.Zeng
 * @Date: 2021-04-23 17:35
 */
@Data
public class Dto implements Importable {

    /**
     * 定义的名称
     */
    private String name;
    /**
     * 描述
     */
    private String description;

    /**
     * import holder
     */
    private final DataHolder<String> externalImportHolder = new DataHolder<>();

    /**
     * annotation holder
     */
    private final DataHolder<ValidateAnnotation> validateAnnotationHolder = new DataHolder<>();

    /**
     * 属性
     */
    private List<Field> fields = new ArrayList<>();

    @Override
    public List<String> getExternalImports() {
        List<String> externalImports = new ArrayList<>(externalImportHolder.get());
        fields.forEach(field -> {
            externalImports.addAll(field.getExternalImportHolder().get());
            field.getValidateAnnotationHolder().get()
                    .forEach(annotation -> externalImports.addAll(annotation.getExternalImportHolder().get()));
        });
        validateAnnotationHolder.get()
                .forEach(annotation -> externalImports.addAll(annotation.getExternalImportHolder().get()));
        return externalImports.stream().distinct().collect(Collectors.toList());
    }

    /**
     * definition 属性
     */
    @Data
    public static class Field {

        /**
         * 类型
         */
        private String type;
        /**
         * 属性名
         */
        private String name;
        /**
         * 值
         */
        private String value;
        /**
         * 描述
         */
        private String description;

        /**
         * import holder
         */
        private final DataHolder<String> externalImportHolder = new DataHolder<>();

        /**
         * annotation holder
         */
        private final DataHolder<ValidateAnnotation> validateAnnotationHolder = new DataHolder<>();

        /**
         * @return format 默认值
         */
        public String value() {
            if (ObjectUtils.isEmpty(value)) {
                return "";
            }
            String newValue = String.class.getSimpleName().equals(type) ? "\"" + value + "\"" : value;
            newValue = " = " + newValue;
            if (Double.class.getSimpleName().equals(type)) {
                newValue = newValue + "D";
            }
            if (Long.class.getSimpleName().equals(type)) {
                newValue = newValue + "L";
            }
            return newValue;
        }

        public void setType(String type) {
            this.type = type;
            if (type.contains(List.class.getSimpleName())
                    || type.contains(SwaggerConstants.DTO_SUFFIX)
                    || type.contains(Map.class.getSimpleName())) {
                validateAnnotationHolder.addItem(ValidateAnnotationUtils.valid());
            }
        }

    }

}
