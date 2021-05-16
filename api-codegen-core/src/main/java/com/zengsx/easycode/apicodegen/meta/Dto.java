package com.zengsx.easycode.apicodegen.meta;

import com.zengsx.easycode.apicodegen.constants.SwaggerConstants;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.util.ObjectUtils;

/**
 * @ClassName: DtoMeta
 * @Description: dto定义
 * @Author: Mr.Zeng
 * @Date: 2021-04-23 17:35
 */
@Data
public class Dto {

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
    private List<DtoField> properties = new ArrayList<>();


    /**
     * definition 属性
     */
    @Data
    public static class DtoField {

        /**
         * 类型
         */
        private String type;
        /**
         * 短类型
         */
        private String shortType;
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
         * 对标 @NotNull
         */
        private boolean required;

        /**
         * 校验注解
         */
        private List<ValidateAnnotation> validateAnnotations;

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
