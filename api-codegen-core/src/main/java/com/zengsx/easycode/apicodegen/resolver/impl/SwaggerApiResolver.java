package com.zengsx.easycode.apicodegen.resolver.impl;

import static com.zengsx.easycode.apicodegen.util.SwaggerUtils.getClassNameFromDefinitionName;
import static com.zengsx.easycode.apicodegen.util.SwaggerUtils.getClassNameFromRefPath;
import static com.zengsx.easycode.apicodegen.util.SwaggerUtils.getPropertyDefaultValue;

import com.zengsx.easycode.apicodegen.constants.HandlerMethodParamTag;
import com.zengsx.easycode.apicodegen.constants.SwaggerConstants;
import com.zengsx.easycode.apicodegen.enums.TypeMapping;
import com.zengsx.easycode.apicodegen.meta.ApiResolveResult;
import com.zengsx.easycode.apicodegen.meta.Controller;
import com.zengsx.easycode.apicodegen.meta.Dto;
import com.zengsx.easycode.apicodegen.meta.Dto.Field;
import com.zengsx.easycode.apicodegen.meta.HandlerMethod;
import com.zengsx.easycode.apicodegen.meta.HandlerMethodParam;
import com.zengsx.easycode.apicodegen.meta.HandlerMethodReturn;
import com.zengsx.easycode.apicodegen.resolver.IApiResolver;
import com.zengsx.easycode.apicodegen.util.SwaggerUtils;
import com.zengsx.easycode.apicodegen.util.SwaggerVendorExtensionsUtil;
import com.zengsx.easycode.apicodegen.util.TypeResolver;
import com.zengsx.easycode.apicodegen.util.ValidateAnnotationUtils;
import io.swagger.models.ArrayModel;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.RefModel;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

/**
 * @ClassName: SwaggerMetaResolver
 * @Description: TODO
 * @Author: Mr.Zeng
 * @Date: 2021-04-21 20:00
 */
@Slf4j
public class SwaggerApiResolver implements IApiResolver<Swagger> {

    @Override
    public ApiResolveResult resolve(Swagger swagger) {
        // 解析需要生成的dto对象
        List<Dto> dtos = getDefinitions(swagger);
        // 需要生成的controller对象
        Map<String, Controller> controllerMetaMap = getControllerMap(swagger);
        // request mapping 解析
        swagger.getPaths().forEach((url, path) -> {
            // 处理 每个path，每个path包含 get post delete patch put
            log.info("当前正在处理url:{}", url);
            // tag检测
            path.getOperations().forEach(op -> {
                if (CollectionUtils.isEmpty(op.getTags())) {
                    throw new RuntimeException("当前path存在请求定义没有tag，无法分类controller," + url);
                }
                if (op.getTags().size() > 1) {
                    throw new RuntimeException("当前path存在请求定义对应了多个tag，无法分类controller," + url);
                }
                String tag = op.getTags().get(0);
                if (!controllerMetaMap.containsKey(tag)) {
                    throw new RuntimeException(String.format("当前path:%s,绑定了一个未定义的tag:%s", url, tag));
                }
            });
            // 当前path的自定义扩展参数
            Map<String, Object> extParams = path.getVendorExtensions();
            // 处理请求定义
            path.getOperationMap().forEach((opType, op) -> {
                log.info("当前正在处理url:{},type:{}", url, opType.name());
                // 获取对应的controllerMeta
                Controller controller = controllerMetaMap.get(op.getTags().get(0));
                SwaggerVendorExtensionsUtil
                        .getImports(extParams)
                        .forEach(controller::addExternalImport);
                // 定义当前请求
                HandlerMethod handlerMethod = new HandlerMethod();
                handlerMethod.setUrl(url);
                handlerMethod.setRequestType(opType.name());
                handlerMethod.setMethodName(op.getOperationId());
                handlerMethod.setSummary(op.getSummary());
                handlerMethod.setDescription(op.getDescription());
                handlerMethod.setConsumes(Optional.ofNullable(op.getConsumes()).orElse(controller.getConsumes()));
                handlerMethod.setProduces(Optional.ofNullable(op.getProduces()).orElse(controller.getProduces()));
                // setting handlerMethod params
                handlerMethod.setHandlerMethodParams(getHandlerMethodParams(
                        op.getOperationId(),
                        op.getParameters(),
                        dtos));
                // setting handlerMethod return def
                handlerMethod.setHandlerMethodReturn(getHandlerMethodReturn(op));

                SwaggerVendorExtensionsUtil
                        .getValidateAnnotations(extParams)
                        .forEach(handlerMethod::addValidateAnnotation);

                SwaggerVendorExtensionsUtil
                        .getValidateAnnotations(op.getVendorExtensions())
                        .forEach(handlerMethod::addValidateAnnotation);

                SwaggerVendorExtensionsUtil
                        .getImports(op.getVendorExtensions())
                        .forEach(handlerMethod::addExternalImport);

                // 收集 handlerMethod
                controller.getHandlerMethods().add(handlerMethod);
            });

        });
        ApiResolveResult resolveResult = new ApiResolveResult();
        resolveResult.setControllers(new ArrayList<>(controllerMetaMap.values()));
        resolveResult.setDtos(dtos);
        return resolveResult;
    }

    /**
     * 解析当前swagger定义的dto
     *
     * @param swagger swagger文档对象
     * @return 解析出来的dto定义
     */
    private List<Dto> getDefinitions(Swagger swagger) {
        return Optional.ofNullable(swagger).map(Swagger::getDefinitions)
                .orElse(Collections.emptyMap()).entrySet().stream().map(o -> {
                    String definitionName = o.getKey();
                    ModelImpl modelImpl = (ModelImpl) o.getValue();
                    // definition 只处理 type=object 的定义!
                    if (!SwaggerConstants.TYPE_OBJECT.equalsIgnoreCase(modelImpl.getType())) {
                        log.error("definition 只处理 type=object 的定义，已跳过当前定义:{}!", definitionName);
                        return null;
                    }
                    Dto dto = new Dto();
                    dto.setName(getClassNameFromDefinitionName(definitionName));
                    dto.setDescription(modelImpl.getDescription());
                    dto.addValidateAnnotation(ValidateAnnotationUtils.data());
                    dto.addValidateAnnotation(ValidateAnnotationUtils.jsonInclude());
                    SwaggerVendorExtensionsUtil
                            .getImports(modelImpl.getVendorExtensions())
                            .forEach(dto::addExternalImport);
                    SwaggerVendorExtensionsUtil
                            .getValidateAnnotations(modelImpl.getVendorExtensions())
                            .forEach(dto::addValidateAnnotation);
                    Optional.ofNullable(modelImpl.getProperties()).ifPresent(properties -> dto.setFields(
                            properties.entrySet()
                                    .stream()
                                    .map(entry -> propertyConvertDtoField(entry.getKey(), entry.getValue()))
                                    .collect(Collectors.toList())
                    ));
                    return dto;
                }).filter(Objects::nonNull).collect(Collectors.toList());
    }


    /**
     * definition property 转换为 dto field
     *
     * @param fieldName 字段名
     * @param property  属性实体
     * @return dto field 对象
     */
    private Field propertyConvertDtoField(String fieldName, Property property) {
        Field field = new Field();
        field.setName(fieldName);
        field.setDescription(property.getDescription());
        // 默认拿了 default 值,特殊类型下面在处理格式
        field.setValue(getPropertyDefaultValue(property));
        // 需要导入的class，需要指定  x-Import: xx.xx.xx,yy.yy.yy,zz.zz.zz
        SwaggerVendorExtensionsUtil
                .getImports(property.getVendorExtensions())
                .forEach(field::addExternalImport);
        SwaggerVendorExtensionsUtil
                .getValidateAnnotations(property.getVendorExtensions())
                .forEach(field::addValidateAnnotation);

        if (property.getRequired()) {
            field.addValidateAnnotation(ValidateAnnotationUtils.notNull());
        }

        if (property instanceof ArrayProperty) {
            // array 暂不支持默认值
            Field childField = propertyConvertDtoField("childField", ((ArrayProperty) property).getItems());
            field.setType(String.format("%s<%s>", List.class.getSimpleName(), childField.getType()));
            field.addExternalImport(List.class.getName());

            field.addExternalImport(childField.getExternalImports().toArray(new String[]{}));

        } else if (property instanceof RefProperty) {

            RefProperty refProperty = (RefProperty) property;
            String className = getClassNameFromRefPath(refProperty.getOriginalRef());
            field.setType(className);

        } else if (SwaggerConstants.TYPE_OBJECT.equalsIgnoreCase(property.getType())) {
            String xFormat = SwaggerVendorExtensionsUtil.getXFormat(property.getVendorExtensions());
            if (null == xFormat) {
                // 没有配置 x-Type,则对应 java.lang.Object
                field.setType(Object.class.getSimpleName());
            } else if (Map.class.getSimpleName().equalsIgnoreCase(xFormat)) {
                // x-Type:Map ,则对应 java.util.Map
                String mapKeyType = Optional.ofNullable(property.getVendorExtensions().get("x-MapKeyType"))
                        .map(Object::toString)
                        .orElseThrow(() -> new RuntimeException("没有找到key对应的类型,fieldName:" + fieldName));
                String mapValueType = Optional.ofNullable(property.getVendorExtensions().get("x-MapValueType"))
                        .map(Object::toString)
                        .orElseThrow(() -> new RuntimeException("没有找到value对应的类型,fieldName:" + fieldName));
                field.setType(String.format(
                        "%s<%s,%s>",
                        Map.class.getSimpleName(),
                        TypeResolver.getType(mapKeyType),
                        TypeResolver.getType(mapValueType)
                ));
                field.addExternalImport(Map.class.getName());
            } else {
                // x-Type: xx ,则对应 xx
                field.setType(property.getFormat());
            }
        } else {
            TypeMapping mapping = TypeMapping.parse(property.getType(), property.getFormat());
            field.setType(mapping.getType());
            Optional.ofNullable(mapping.getExternalImport()).ifPresent(field::addExternalImport);
        }
        return field;
    }

    /**
     * 获取当前swagger的controller定义
     *
     * @param swagger swagger文档对象
     * @return controller定义
     */
    private Map<String, Controller> getControllerMap(Swagger swagger) {
        return Optional.ofNullable(swagger.getTags())
//                .orElseThrow(() -> new RuntimeException("tags字段不可为空,若之定义了DTO,如下配置即可:  tags; []"))
                .orElse(Collections.emptyList())
                .stream().collect(Collectors.toMap(Tag::getName, tag -> {
                    Controller controller = new Controller();
                    controller.setName(SwaggerUtils.wrapControllerClassName(tag.getName()));
                    controller.setServiceName(SwaggerUtils.wrapControllerServiceClassName(tag.getName()));
                    controller.setFeignClientName(SwaggerUtils.wrapFeignClientClassName(tag.getName()));
                    controller.setDescription(tag.getDescription());
                    controller.setBasePath(swagger.getBasePath());
                    controller.setConsumes(Optional.ofNullable(swagger.getConsumes()).orElse(Collections.emptyList()));
                    controller.setProduces(Optional.ofNullable(swagger.getProduces()).orElse(Collections.emptyList()));
                    controller.setHandlerMethods(new ArrayList<>(16));
                    return controller;
                }));
    }

    /**
     * 获取方法参数
     *
     * @param opName 方法名称
     * @param params 当前op参数
     * @return 方法参数列表
     */
    private List<HandlerMethodParam> getHandlerMethodParams(String opName, List<Parameter> params, List<Dto> dtos) {
        // 所有参数分三类，path直接存放，query参数合并生成对象，body参数也直接存放
        List<Parameter> parameters = Optional.ofNullable(params).orElse(Collections.emptyList());
        List<HandlerMethodParam> handlerMethodParams = parameters.stream()
                .filter(o -> !(o instanceof QueryParameter))
                .map(parameter -> {
                    HandlerMethodParam handlerMethodParam = new HandlerMethodParam();
                    handlerMethodParam.setName(parameter.getName());
                    handlerMethodParam.setDescription(parameter.getDescription());
                    if (parameter.getRequired()) {
                        handlerMethodParam.addValidateAnnotation(ValidateAnnotationUtils.notNull());
                    }
                    if (parameter instanceof PathParameter) {
                        PathParameter pathParameter = (PathParameter) parameter;
                        TypeMapping mapping = TypeMapping.parse(pathParameter.getType(), pathParameter.getFormat());
                        // 只支持基本类型，直接获取type就行
                        handlerMethodParam.setType(mapping.getType());
                        Optional.ofNullable(mapping.getExternalImport())
                                .ifPresent(handlerMethodParam::addExternalImport);
                        handlerMethodParam.setTag(HandlerMethodParamTag.PATH);
                    } else if (parameter instanceof BodyParameter) {
                        BodyParameter bodyParameter = ((BodyParameter) parameter);
                        String typeName = Optional.ofNullable(bodyParameter.getSchema())
                                .map(Model::getReference)
                                // ref 转换 className
                                .map(SwaggerUtils::getClassNameFromRefPath)
                                .orElseThrow(() -> new RuntimeException("body类型参数只支持ref引用"));
                        handlerMethodParam.setType(typeName);
                        handlerMethodParam.setTag(HandlerMethodParamTag.BODY);
                    } else {
                        throw new RuntimeException("目前只能处理 query path body 三类参数");
                    }
                    return handlerMethodParam;
                }).collect(Collectors.toList());
        List<QueryParameter> queryParameters = parameters.stream()
                .filter(o -> o instanceof QueryParameter)
                .map(o -> (QueryParameter) o)
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(queryParameters)) {
            HandlerMethodParam handlerMethodParam = new HandlerMethodParam();
            handlerMethodParam.setTag(HandlerMethodParamTag.QUERY);
            handlerMethodParam.setName("queryParams");
            handlerMethodParam.setDescription("query参数,详情参考dto定义");
            handlerMethodParam.setType(SwaggerUtils.getClassNameFromHandlerMethodName(opName));
            // 追加到definition定义列表中
            handlerMethodParams.add(handlerMethodParam);
            Dto dto = new Dto();
            dto.setName(SwaggerUtils.getClassNameFromHandlerMethodName(opName));
            dto.setDescription(opName + "方法查询参数");
            dto.setFields(queryParameters.stream().map(queryParameter -> {
                Field field = new Field();
                if (queryParameter.getRequired()) {
                    field.addValidateAnnotation(ValidateAnnotationUtils.notNull());
                }
                field.setName(queryParameter.getName());
                // 默认值
                field.setValue(Optional.ofNullable(queryParameter.getDefaultValue())
                        .map(Object::toString)
                        .orElse(null));
                field.setDescription(queryParameter.getDescription());
                SwaggerVendorExtensionsUtil.getValidateAnnotations(queryParameter.getVendorExtensions())
                        .forEach(handlerMethodParam::addValidateAnnotation);
                SwaggerVendorExtensionsUtil.getImports(queryParameter.getVendorExtensions())
                        .forEach(handlerMethodParam::addExternalImport);
                if (SwaggerConstants.TYPE_ARRAY.equals(queryParameter.getType())) {
                    if (null == queryParameter.getItems()) {
                        throw new RuntimeException("QueryParam array类型参数应该具备子类型!");
                    }
                    if (queryParameter.getItems() instanceof RefProperty) {
                        throw new RuntimeException("QueryParam 暂不支持 List<$ref> ");
                    }
                    String type = queryParameter.getItems().getType();
                    String format = queryParameter.getItems().getFormat();
                    TypeMapping mapping = TypeMapping.parse(type, format);
                    field.setType(String.format("%s<%s>", List.class.getSimpleName(), mapping.getType()));
                    Optional.ofNullable(mapping.getExternalImport()).ifPresent(field::addExternalImport);
                    field.addExternalImport(List.class.getName());
                } else {
                    TypeMapping mapping = TypeMapping
                            .parse(queryParameter.getType(), queryParameter.getFormat());
                    field.setType(mapping.getType());
                    Optional.ofNullable(mapping.getExternalImport()).ifPresent(field::addExternalImport);
                }
                return field;
            }).collect(Collectors.toList()));

            dtos.add(dto);
        }
        return handlerMethodParams;
    }

    /**
     * 请求方法返回对象(@Perfect)
     *
     * @param op 请求方法定义
     * @return return对象
     */
    private HandlerMethodReturn getHandlerMethodReturn(Operation op) {
        Response resp = op.getResponses().values()
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("接口定义需要有一个唯一的返回声明!"));
        HandlerMethodReturn handlerMethodReturn = new HandlerMethodReturn();
        handlerMethodReturn.setDescription(resp.getDescription());
        Model model = resp.getResponseSchema();
        // 需要导入的class，需要指定  x-Import: xx.xx.xx,yy.yy.yy,zz.zz.zz
        SwaggerVendorExtensionsUtil
                .getImports(resp.getVendorExtensions())
                .forEach(handlerMethodReturn::addExternalImport);
        if (null == model) {
            handlerMethodReturn.setType("void");
        } else if (model instanceof RefModel) {
            RefModel refModel = (RefModel) model;
            handlerMethodReturn.setType(getClassNameFromRefPath(refModel.getOriginalRef()));
        } else if (model instanceof ArrayModel) {
            Property childProperty = ((ArrayModel) model).getItems();
            String childType;
            if (childProperty instanceof RefProperty) {
                childType = getClassNameFromRefPath(((RefProperty) childProperty).getOriginalRef());
            } else {
                TypeMapping mapping = TypeMapping.parse(childProperty.getType(), childProperty.getFormat());
                childType = mapping.getType();
                Optional.ofNullable(mapping.getExternalImport()).ifPresent(handlerMethodReturn::addExternalImport);
            }
            handlerMethodReturn.setType(String.format("%s<%s>", List.class.getSimpleName(), childType));
            handlerMethodReturn.addExternalImport(List.class.getName());
        } else if (model instanceof ModelImpl) {
            // 基础类型
            ModelImpl modelImpl = (ModelImpl) model;
            if (SwaggerConstants.TYPE_OBJECT.equalsIgnoreCase(modelImpl.getType())) {
                String xFormat = SwaggerVendorExtensionsUtil.getXFormat(modelImpl.getVendorExtensions());
                if (null == xFormat) {
                    // 没有配置 x-Type,则对应 java.lang.Object
                    handlerMethodReturn.setType(Object.class.getSimpleName());
                } else if (Map.class.getSimpleName().equalsIgnoreCase(xFormat)) {
                    // x-Type:Map ,则对应 java.util.Map
                    String mapKeyType = Optional.ofNullable(modelImpl.getVendorExtensions().get("x-MapKeyType"))
                            .map(Object::toString)
                            .orElseThrow(() -> new RuntimeException(
                                    "没有找到returnType对应的 x-MapKeyType 属性,OperationId:" + op.getOperationId()
                            ));
                    String mapValueType = Optional.ofNullable(modelImpl.getVendorExtensions().get("x-MapValueType"))
                            .map(Object::toString)
                            .orElseThrow(() -> new RuntimeException(
                                    "没有找到returnType对应的 x-MapValueType 属性,OperationId:" + op.getOperationId()
                            ));
                    handlerMethodReturn.setType(String.format(
                            "%s<%s,%s>",
                            Map.class.getSimpleName(),
                            TypeResolver.getType(mapKeyType),
                            TypeResolver.getType(mapValueType)
                    ));
                    handlerMethodReturn.addExternalImport(Map.class.getName());
                } else {
                    // x-Type: xx ,则对应 xx
                    handlerMethodReturn.setType(modelImpl.getFormat());
                }
            } else {
                TypeMapping mapping = TypeMapping.parse(modelImpl.getType(), modelImpl.getFormat());
                handlerMethodReturn.setType(mapping.getType());
                Optional.ofNullable(mapping.getExternalImport()).ifPresent(handlerMethodReturn::addExternalImport);
            }
        } else {
            throw new RuntimeException("resp返回值只支持 $ref | type | List<$ref> |List<type> ");
        }
        return handlerMethodReturn;
    }


}
