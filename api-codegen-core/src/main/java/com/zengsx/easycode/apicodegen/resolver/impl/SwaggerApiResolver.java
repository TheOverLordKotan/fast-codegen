package com.zengsx.easycode.apicodegen.resolver.impl;

import com.zengsx.easycode.apicodegen.constants.HandlerMethodParamTag;
import com.zengsx.easycode.apicodegen.constants.SwaggerConstants;
import com.zengsx.easycode.apicodegen.enums.Type;
import com.zengsx.easycode.apicodegen.meta.ApiResolveResult;
import com.zengsx.easycode.apicodegen.meta.Controller;
import com.zengsx.easycode.apicodegen.meta.Dto;
import com.zengsx.easycode.apicodegen.meta.Dto.Field;
import com.zengsx.easycode.apicodegen.meta.HandlerMethod;
import com.zengsx.easycode.apicodegen.meta.HandlerMethodParam;
import com.zengsx.easycode.apicodegen.meta.HandlerMethodReturn;
import com.zengsx.easycode.apicodegen.resolver.IApiResolver;
import com.zengsx.easycode.apicodegen.util.SwaggerUtils;
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
import io.swagger.models.properties.BaseIntegerProperty;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.DateTimeProperty;
import io.swagger.models.properties.DecimalProperty;
import io.swagger.models.properties.DoubleProperty;
import io.swagger.models.properties.FloatProperty;
import io.swagger.models.properties.LongProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
                // 定义当前请求
                HandlerMethod hMethodMeta = new HandlerMethod();
                hMethodMeta.setUrl(url);
                hMethodMeta.setRequestType(opType.name());
                hMethodMeta.setMethodName(op.getOperationId());
                hMethodMeta.setSummary(op.getSummary());
                hMethodMeta.setDescription(op.getDescription());
                hMethodMeta.setConsumes(Optional.ofNullable(op.getConsumes()).orElse(controller.getConsumes()));
                hMethodMeta.setProduces(Optional.ofNullable(op.getProduces()).orElse(controller.getProduces()));
                // setting handlerMethod params
                hMethodMeta.setHandlerMethodParams(getHandlerMethodParams(
                        op.getOperationId(),
                        op.getParameters(),
                        dtos));
                // setting handlerMethod return def
                hMethodMeta.setHandlerMethodReturn(getHandlerMethodReturn(op));
                // 收集 handlerMethod
                controller.getHandlerMethods().add(hMethodMeta);
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
                        throw new RuntimeException("definition 只处理 type=object 的定义!");
                    }
                    Dto dto = new Dto();
                    dto.setName(SwaggerUtils.getClassNameFromDefinitionName(definitionName));
                    dto.setDescription(modelImpl.getDescription());
                    Optional.ofNullable(modelImpl.getProperties()).ifPresent(properties -> {
                        dto.setFields(properties.entrySet().stream().map(entry -> {
                                    Property property = entry.getValue();
                                    Field field = new Field();
                                    field.setName(entry.getKey());
                                    field.setDescription(property.getDescription());
                                    field.setValue(SwaggerUtils.getPropertyDefaultValue(property));
                                    field.setValidateAnnotations(new ArrayList<>());
                                    if (property.getRequired()) {
                                        field.getValidateAnnotations().add(ValidateAnnotationUtils.required());
                                    }
                                    if (property instanceof ArrayProperty) {
                                        // array 暂不支持默认值
                                        Property itemType = ((ArrayProperty) property).getItems();
                                        String subType;
                                        if (itemType instanceof RefProperty) {
                                            RefProperty refProperty = (RefProperty) itemType;
                                            subType = SwaggerUtils
                                                    .getClassNameFromRefPath(refProperty.getOriginalRef());
                                        } else if (itemType instanceof ArrayProperty) {
                                            throw new RuntimeException("目前只支持一级List,不支持多级");
                                        } else if (itemType instanceof ObjectProperty) {
                                            throw new RuntimeException("请单独定义对象，并通过 $ref 引用");
                                        } else {
                                            subType = SwaggerUtils
                                                    .swaggerTypeToJavaType(itemType.getType(), itemType.getFormat());
                                        }
                                        field.setSsss(String.format("List<%s>", subType));
                                    } else if (property instanceof LongProperty) {
                                        field.setType(Type.LONG.getName());
                                        field.setSsss(Type.LONG.getImport());
                                    } else if (property instanceof BaseIntegerProperty) {
                                        // 包含了 int32 的情况
                                        field.setType(Type.INTEGER.getName());
                                        field.setSsss(Type.INTEGER.getImport());
                                    } else if (property instanceof BooleanProperty) {
                                        field.setType(Type.BOOLEAN.getName());
                                        field.setSsss(Type.BOOLEAN.getImport());
                                    } else if (property instanceof DateTimeProperty) {
                                        field.setType(Type.LOCAL_DATE_TIME.getName());
                                        field.setSsss(Type.LOCAL_DATE_TIME.getImport());
                                    } else if (property instanceof DoubleProperty) {
                                        field.setType(Type.DOUBLE.getName());
                                        field.setSsss(Type.DOUBLE.getImport());
                                    } else if (property instanceof FloatProperty) {
                                        field.setType(Type.FLOAT.getName());
                                        field.setSsss(Type.FLOAT.getImport());
                                    } else if (property instanceof DecimalProperty) {
                                        field.setType(Type.DECIMAL.getName());
                                        field.setSsss(Type.DECIMAL.getImport());
                                    }
//                                    else if (property instanceof FileProperty) {
//
//                                    }
                                    else if (property instanceof RefProperty) {
                                        RefProperty refProperty = (RefProperty) property;
                                        field.setSsss(
                                                SwaggerUtils.getClassNameFromRefPath(refProperty.getOriginalRef()));
                                    } else if (property instanceof StringProperty) {
                                        field.setType(Type.STRING.getName());
                                        field.setSsss(Type.STRING.getImport());
                                    } else {

                                    }
                                    return field;
                                }).collect(Collectors.toList())
                        );
                    });
                    return dto;
                }).collect(Collectors.toList());
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
                    handlerMethodParam.setRequired(parameter.getRequired());
                    if (parameter instanceof PathParameter) {
                        PathParameter pathParameter = (PathParameter) parameter;
                        // 只支持基本类型，直接获取type就行
                        handlerMethodParam.setType(
                                SwaggerUtils.swaggerTypeToJavaType(pathParameter.getType(), pathParameter.getFormat())
                        );
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
            dto.setFields(queryParameters.stream()
                    .map(o -> {
                        Field field = new Field();
                        field.setRequired(o.getRequired());
                        field.setName(o.getName());
                        // 默认值
                        field.setValue(
                                Optional.ofNullable(o.getDefaultValue())
                                        .map(Object::toString)
                                        .orElse(null)
                        );
                        field.setDescription(o.getDescription());
                        if (SwaggerConstants.TYPE_ARRAY.equals(o.getType())) {
                            if (null == o.getItems()) {
                                throw new RuntimeException("QueryParam array类型参数应该具备子类型!");
                            }
                            if (o.getItems() instanceof RefProperty) {
                                throw new RuntimeException("QueryParam 暂不支持 List<$ref> ");
                            }
                            String type = o.getItems().getType();
                            String format = o.getItems().getFormat();
                            field.setSsss(SwaggerUtils.swaggerTypeToJavaType(type, format));
                        } else {
                            field.setSsss(SwaggerUtils.swaggerTypeToJavaType(o.getType(), o.getFormat()));
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
        Model model = resp.getResponseSchema();
        if (null == model) {
            handlerMethodReturn.setType("void");
            handlerMethodReturn.setCtype("void");
        } else if (model instanceof ModelImpl) {
            // 基础类型
            ModelImpl modelImpl = (ModelImpl) model;
            handlerMethodReturn
                    .setCtype(SwaggerUtils.swaggerTypeToJavaType(modelImpl.getType(), modelImpl.getFormat()));
        } else if (model instanceof RefModel) {
            RefModel refModel = (RefModel) model;
            handlerMethodReturn.setCtype(SwaggerUtils.getClassNameFromRefPath(refModel.getOriginalRef()));
        } else if (model instanceof ArrayModel) {
            Property property = ((ArrayModel) model).getItems();
            String subType;
            if (property instanceof RefProperty) {
                subType = SwaggerUtils.getClassNameFromRefPath(((RefProperty) property).getOriginalRef());
            } else {
                subType = SwaggerUtils.swaggerTypeToJavaType(property.getType(), property.getFormat());
            }
            handlerMethodReturn.setCtype(String.format("List<%s>", subType));
        } else {
            throw new RuntimeException("resp返回值只支持 $ref | type | List<$ref> |List<type> ");
        }
        handlerMethodReturn.setDescription(resp.getDescription());
        return handlerMethodReturn;
    }


}
