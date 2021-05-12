package com.zengsx.easycode.apicodegen.resolver.impl;

import com.zengsx.easycode.apicodegen.constants.HandlerMethodParamTag;
import com.zengsx.easycode.apicodegen.constants.SwaggerConstants;
import com.zengsx.easycode.apicodegen.meta.ApiResolveResult;
import com.zengsx.easycode.apicodegen.meta.Controller;
import com.zengsx.easycode.apicodegen.meta.Dto;
import com.zengsx.easycode.apicodegen.meta.Dto.DtoFieldMeta;
import com.zengsx.easycode.apicodegen.meta.HandlerMethod;
import com.zengsx.easycode.apicodegen.meta.HandlerMethodParam;
import com.zengsx.easycode.apicodegen.meta.HandlerMethodReturn;
import com.zengsx.easycode.apicodegen.resolver.IApiResolver;
import com.zengsx.easycode.apicodegen.util.SwaggerUtils;
import io.swagger.models.ArrayModel;
import io.swagger.models.Contact;
import io.swagger.models.Info;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.RefModel;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.DateTimeProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.UntypedProperty;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
            // 检测对应请求是否有tag分类
            path.getOperations().forEach(op -> {
                if (CollectionUtils.isEmpty(op.getTags())) {
                    throw new RuntimeException("当前path存在请求定义没有tag，无法分类controller," + url);
                }
                if (op.getTags().size() > 1) {
                    throw new RuntimeException("当前path存在请求定义对应了多个tag，无法分类controller," + url);
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
                // request mapping  http method
                hMethodMeta.setRequestType(opType.name());
                // request mapping method name
                hMethodMeta.setMethodName(op.getOperationId());
                // request mapping summary
                hMethodMeta.setSummary(op.getSummary());
                // request mapping description
                hMethodMeta.setDescription(op.getDescription());
                // 处理 consumes produces ，包括默认的
                hMethodMeta.setConsumes(Optional.ofNullable(op.getConsumes()).orElse(controller.getConsumes()));
                hMethodMeta.setProduces(Optional.ofNullable(op.getProduces()).orElse(controller.getProduces()));

                // parse handlerMethod params
                List<HandlerMethodParam> handlerMethodParamMetaParams = getHandlerMethodParams(
                        op.getOperationId(),
                        op.getParameters(),
                        dtos);

                // setting handlerMethod params
                hMethodMeta.setHandlerMethodParams(handlerMethodParamMetaParams);
                // setting handlerMethod return def
                hMethodMeta.setHandlerMethodReturn(getHandlerMethodReturn(op));
                // 收集 handlerMethod
                controller.getHandlerMethods().add(hMethodMeta);
            });

        });
        ApiResolveResult resolveResult = new ApiResolveResult();
        resolveResult.setControllers(new ArrayList<>(controllerMetaMap.values()));
        resolveResult.setDtos(dtos);
        // 代码所属者
        String author = Optional.ofNullable(swagger.getInfo())
                .map(Info::getContact)
                .map(Contact::getName)
                .orElse("codegen");
        resolveResult.getDtos().forEach(dto -> dto.setAuthor(author));
        resolveResult.getControllers().forEach(controller -> controller.setAuthor(author));
        return resolveResult;
    }

    /**
     * 解析当前swagger定义的dto
     *
     * @param swagger swagger文档对象
     * @return 解析出来的dto定义
     */
    private List<Dto> getDefinitions(Swagger swagger) {
        List<Dto> dtos = new ArrayList<>(16);
        // 定义的公共 dto
        swagger.getDefinitions().forEach((definitionName, model) -> {
            ModelImpl modelImpl = (ModelImpl) model;
            if (!SwaggerConstants.TYPE_OBJECT.equals(modelImpl.getType())) {
                throw new RuntimeException("definition只处理 type=object 的定义");
            }
            Dto dto = new Dto();
            dto.setName(SwaggerUtils.getClassNameFromDefinitionName(definitionName));
            dto.setDescription(modelImpl.getDescription());
            Optional.ofNullable(modelImpl.getProperties()).ifPresent(properties->{
                dto.setProperties(
                        properties.entrySet().stream()
                                .map(entry -> {
                                    Property property = entry.getValue();
                                    DtoFieldMeta dtoFieldMeta = new DtoFieldMeta();
                                    dtoFieldMeta.setName(entry.getKey());
                                    dtoFieldMeta.setDescription(property.getDescription());
                                    dtoFieldMeta.setRequired(property.getRequired());
                                    if (property instanceof ArrayProperty) {
                                        // array 暂不支持默认值
                                        Property itemType = ((ArrayProperty) property).getItems();
                                        String subType;
                                        if (itemType instanceof RefProperty) {
                                            RefProperty refProperty = (RefProperty) itemType;
                                            subType = SwaggerUtils.getClassNameFromRefPath(refProperty.getOriginalRef());
                                        } else if (itemType instanceof ArrayProperty) {
                                            throw new RuntimeException("目前只支持一级List,不支持多级");
                                        } else if (itemType instanceof ObjectProperty) {
                                            throw new RuntimeException("请单独定义对象，并通过 $ref 引用");
                                        } else {
                                            subType = SwaggerUtils
                                                    .swaggerTypeToJavaType(itemType.getType(), itemType.getFormat());
                                        }
                                        dtoFieldMeta.setType(String.format("List<%s>", subType));
                                    } else if (property instanceof RefProperty) {
                                        RefProperty refProperty = (RefProperty) property;
                                        dtoFieldMeta.setType(
                                                SwaggerUtils.getClassNameFromRefPath(refProperty.getOriginalRef()));
                                    } else if (property instanceof DateProperty) {
                                        dtoFieldMeta.setType("Date");
                                    } else if (property instanceof DateTimeProperty) {
                                        throw new RuntimeException("暂不支持的属性类型");
                                    } else if (entry.getValue() instanceof ObjectProperty) {
                                        throw new RuntimeException("暂不支持的属性类型");
                                    } else if (entry.getValue() instanceof MapProperty) {
                                        throw new RuntimeException("暂不支持的属性类型");
                                    } else if (property instanceof UntypedProperty) {
                                        log.info("DTO:{},存在未定义类型字段:{}", definitionName, entry.getKey());
                                    } else {
                                        dtoFieldMeta.setType(SwaggerUtils
                                                .swaggerTypeToJavaType(property.getType(), property.getFormat()));
                                        // 获取默认值
                                        dtoFieldMeta.setValue(SwaggerUtils.getPropertyDefaultValue(property));
                                    }
                                    return dtoFieldMeta;
                                })
                                .collect(Collectors.toList())
                );
            });
            dtos.add(dto);
        });
        return dtos;
    }

    /**
     * 获取当前swagger的controller定义
     *
     * @param swagger swagger文档对象
     * @return controller定义
     */
    private Map<String, Controller> getControllerMap(Swagger swagger) {
        Map<String, Controller> controllerMetaMap = new HashMap<>(swagger.getTags().size());

        // controller定义
        swagger.getTags().forEach(tag -> {
            Controller controller = new Controller();
            controller.setName(SwaggerUtils.wrapControllerClassName(tag.getName()));
            controller.setServiceName(SwaggerUtils.wrapControllerServiceClassName(tag.getName()));
            controller.setDescription(tag.getDescription());
            controller.setBasePath(swagger.getBasePath());
            controller.setConsumes(Optional.ofNullable(swagger.getConsumes()).orElse(Collections.emptyList()));
            controller.setProduces(Optional.ofNullable(swagger.getProduces()).orElse(Collections.emptyList()));
            controller.setHandlerMethods(new ArrayList<>(16));
            controllerMetaMap.put(tag.getName(), controller);
        });
        return controllerMetaMap;
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
            dto.setProperties(queryParameters.stream()
                    .map(o -> {
                        DtoFieldMeta dtoFieldMeta = new DtoFieldMeta();
                        dtoFieldMeta.setRequired(o.getRequired());
                        dtoFieldMeta.setName(o.getName());
                        // 默认值
                        dtoFieldMeta.setValue(
                                Optional.ofNullable(o.getDefaultValue())
                                        .map(Object::toString)
                                        .orElse(null)
                        );
                        dtoFieldMeta.setDescription(o.getDescription());
                        if (SwaggerConstants.TYPE_ARRAY.equals(o.getType())) {
                            if (null == o.getItems()) {
                                throw new RuntimeException("QueryParam array类型参数应该具备子类型!");
                            }
                            if (o.getItems() instanceof RefProperty) {
                                throw new RuntimeException("QueryParam 暂不支持 List<$ref> ");
                            }
                            String type = o.getItems().getType();
                            String format = o.getItems().getFormat();
                            dtoFieldMeta.setType(SwaggerUtils.swaggerTypeToJavaType(type, format));
                        } else {
                            dtoFieldMeta.setType(SwaggerUtils.swaggerTypeToJavaType(o.getType(), o.getFormat()));
                        }
                        return dtoFieldMeta;
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
        } else if (model instanceof ModelImpl) {
            // 基础类型
            ModelImpl modelImpl = (ModelImpl) model;
            handlerMethodReturn
                    .setType(SwaggerUtils.swaggerTypeToJavaType(modelImpl.getType(), modelImpl.getFormat()));
        } else if (model instanceof RefModel) {
            RefModel refModel = (RefModel) model;
            handlerMethodReturn.setType(SwaggerUtils.getClassNameFromRefPath(refModel.getOriginalRef()));
        } else if (model instanceof ArrayModel) {
            Property property = ((ArrayModel) model).getItems();
            String subType;
            if (property instanceof RefProperty) {
                subType = SwaggerUtils.getClassNameFromRefPath(((RefProperty) property).getOriginalRef());
            } else {
                subType = SwaggerUtils.swaggerTypeToJavaType(property.getType(), property.getFormat());
            }
            handlerMethodReturn.setType(String.format("List<%s>", subType));
        } else {
            throw new RuntimeException("resp返回值只支持 $ref | type | List<$ref> |List<type> ");
        }
        handlerMethodReturn.setDescription(resp.getDescription());
        return handlerMethodReturn;
    }


}
