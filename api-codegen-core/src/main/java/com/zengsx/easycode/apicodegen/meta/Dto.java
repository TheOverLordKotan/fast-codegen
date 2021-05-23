package com.zengsx.easycode.apicodegen.meta;

import com.zengsx.easycode.apicodegen.constants.SwaggerConstants;
import com.zengsx.easycode.apicodegen.meta.action.AbstractMeta;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.util.ObjectUtils;

/**
 * @ClassName: Dto
 * @Description: dto定义
 * @Author: Mr.Zeng
 * @Date: 2021-04-23 17:35
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class Dto extends AbstractMeta {

    /**
     * 定义的名称
     */
    private String name;
    /**
     * 描述
     */
    private String description;

    /**
     * 属性
     */
    private List<Field> fields = new ArrayList<>();

    @Override
    protected void processExternalImport() {
        fields.stream()
                .map(Field::getExternalImports)
                .flatMap(List::stream)
                .forEach(this::addExternalImport);
    }

    /**
     * definition 属性
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class Field extends AbstractMeta {

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

        public String value() {
            if (ObjectUtils.isEmpty(value)) {
                return "";
            }
            String newValue = "String".equals(type) ? "\"" + value + "\"" : value;
            newValue = " = " + newValue;
            if ("Double".equals(type)) {
                newValue = newValue + "D";
            }
            if ("Long".equals(type)) {
                newValue = newValue + "L";
            }
            return newValue;
        }

        public boolean requiredValid() {
            return type.contains("List") || type.contains(SwaggerConstants.DTO_SUFFIX);
        }

    }

}
