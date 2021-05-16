package com.zengsx.easycode.apicodegen.util;

import com.zengsx.easycode.apicodegen.constants.SwaggerConstants;
import com.zengsx.easycode.common.utils.ClassUtils;
import com.zengsx.easycode.common.utils.FormatUtils;
import io.swagger.models.Swagger;
import io.swagger.models.properties.Property;
import io.swagger.parser.SwaggerParser;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @ClassName: SwaggerUtils
 * @Description: TODO
 * @Author: Mr.Zeng
 * @Date: 2021-04-19 18:34
 */
public class SwaggerUtils {

    public static Swagger parseSwagger(String content) {
        return new SwaggerParser().parse(content);
    }

    /**
     * 类型映射
     */
    private final static Map<String, String> TYPE_MAPPINGS = new HashMap<String, String>() {{
        put("string", "String");
        put("integer", "Integer");
        put("integer@int32", "Integer");
        put("integer@int64", "Long");
        put("number", "Float");
        put("number@float", "Float");
        put("number@double", "Double");
        put("boolean", "Boolean");
    }};


    /**
     * 类型转换器  swagger to java type
     *
     * @param type   swagger 类型
     * @param format swagger类型格式化
     * @return java type name
     */
    public static String swaggerTypeToJavaType(String type, String format) {
        String key = type + Optional.ofNullable(format).map(o -> "@" + o).orElse("");
        if (TYPE_MAPPINGS.containsKey(key)) {
            return TYPE_MAPPINGS.get(key);
        }
        throw new RuntimeException(String.format("Unknown or UnSupport Type:%s,Format:%s", type, format));
    }

    public static String getClassNameFromHandlerMethodName(String handlerMethodName) {
        return FormatUtils.lowerCamelToUpperCamel(handlerMethodName) + SwaggerConstants.QUEUE_PARAM_DTO_SUFFIX;
    }

    public static String getClassNameFromRefPath(String refPath) {
        return getClassNameFromDefinitionName(refPath.replace("#/definitions/", ""));
    }

    public static String getClassNameFromDefinitionName(String definitionName) {
        return definitionName + SwaggerConstants.DTO_SUFFIX;
    }

    public static String wrapControllerClassName(String name) {
        return name + SwaggerConstants.CONTROLLER_SUFFIX;
    }

    public static String wrapControllerServiceClassName(String name) {
        return "I" + name + "AutogenService";
    }

    public static String wrapFeignClientClassName(String name) {
        return "I" + name + "FeignClient";
    }
    /**
     * 获取swagger属性默认值
     *
     * @param property swagger属性
     * @return 默认值
     */
    public static String getPropertyDefaultValue(Property property) {
        return Optional.ofNullable(ClassUtils.getValue(property, SwaggerConstants.DEFAULT_VALUE_FIELD))
                .map(Object::toString)
                .orElse(null);
    }

}
