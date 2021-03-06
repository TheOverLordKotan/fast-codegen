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
        // ?????????????????????dto??????
        List<Dto> dtos = getDefinitions(swagger);
        // ???????????????controller??????
        Map<String, Controller> controllerMetaMap = getControllerMap(swagger);
        List<String> globalProduces = Optional.ofNullable(swagger.getProduces()).orElse(Collections.emptyList());
        List<String> globalConsumes = Optional.ofNullable(swagger.getConsumes()).orElse(Collections.emptyList());
        // request mapping ??????
        swagger.getPaths().forEach((url, path) -> {
            // ?????? ??????path?????????path?????? get post delete patch put
            log.info("??????????????????url:{}", url);
            // tag??????
            path.getOperations().forEach(op -> {
                if (CollectionUtils.isEmpty(op.getTags())) {
                    throw new RuntimeException("??????path????????????????????????tag???????????????controller," + url);
                }
                if (op.getTags().size() > 1) {
                    throw new RuntimeException("??????path?????????????????????????????????tag???????????????controller," + url);
                }
                String tag = op.getTags().get(0);
                if (!controllerMetaMap.containsKey(tag)) {
                    throw new RuntimeException(String.format("??????path:%s,???????????????????????????tag:%s", url, tag));
                }
            });
            // ??????path????????????????????????
            Map<String, Object> extParams = path.getVendorExtensions();
            // ??????????????????
            path.getOperationMap().forEach((opType, op) -> {
                log.info("??????????????????url:{},type:{}", url, opType.name());
                // ???????????????controllerMeta
                Controller controller = controllerMetaMap.get(op.getTags().get(0));
                // ??????????????????
                HandlerMethod handlerMethod = new HandlerMethod();
                handlerMethod.setUrl(url);
                handlerMethod.setRequestType(opType.name());
                handlerMethod.setMethodName(op.getOperationId());
                handlerMethod.setSummary(op.getSummary());
                handlerMethod.setDescription(op.getDescription());
                handlerMethod.setConsumes(Optional.ofNullable(op.getConsumes()).orElse(globalConsumes));
                handlerMethod.setProduces(Optional.ofNullable(op.getProduces()).orElse(globalProduces));
                // setting handlerMethod params
                handlerMethod.setHandlerMethodParams(
                        getHandlerMethodParams(op.getOperationId(), op.getParameters(), dtos)
                );
                // setting handlerMethod return def
                handlerMethod.setHandlerMethodReturn(getHandlerMethodReturn(op));

                // ??????path????????????
                handlerMethod.getValidateAnnotationHolder().addItem(
                        SwaggerVendorExtensionsUtil.getValidateAnnotations(extParams)
                );
                // ???????????????????????????
                handlerMethod.getValidateAnnotationHolder().addItem(
                        SwaggerVendorExtensionsUtil.getValidateAnnotations(op.getVendorExtensions())
                );
                // ?????? handlerMethod
                controller.getHandlerMethods().add(handlerMethod);
            });

        });
        ApiResolveResult resolveResult = new ApiResolveResult();
        resolveResult.setControllers(new ArrayList<>(controllerMetaMap.values()));
        resolveResult.setDtos(dtos);
        return resolveResult;
    }

    /**
     * ????????????swagger?????????dto
     *
     * @param swagger swagger????????????
     * @return ???????????????dto??????
     */
    private List<Dto> getDefinitions(Swagger swagger) {
        return Optional.ofNullable(swagger).map(Swagger::getDefinitions).orElse(Collections.emptyMap())
                .entrySet()
                .stream()
                .map(o -> {
                    String definitionName = o.getKey();
                    if (!(o.getValue() instanceof ModelImpl)
                            || !SwaggerConstants.TYPE_OBJECT.equalsIgnoreCase(((ModelImpl) o.getValue()).getType())) {
                        log.warn("definition ????????? type=object ?????????????????????????????????:{}!", definitionName);
                        return null;
                    }
                    ModelImpl modelImpl = (ModelImpl) o.getValue();
                    Map<String, Property> propertyMap = modelImpl.getProperties();
                    Dto dto = new Dto();
                    dto.setName(getClassNameFromDefinitionName(definitionName));
                    dto.setDescription(modelImpl.getDescription());
                    // ????????????
                    dto.getValidateAnnotationHolder().addItem(
                            ValidateAnnotationUtils.data(),
                            ValidateAnnotationUtils.jsonInclude()
                    );
                    // ???????????????
                    dto.getValidateAnnotationHolder().addItem(
                            SwaggerVendorExtensionsUtil.getValidateAnnotations(modelImpl.getVendorExtensions())
                    );
                    // ??????
                    dto.setFields(
                            Optional.ofNullable(propertyMap)
                                    .orElse(Collections.emptyMap())
                                    .keySet()
                                    .stream()
                                    .map(key -> propertyConvertDtoField(key, propertyMap.get(key)))
                                    .collect(Collectors.toList())
                    );
                    return dto;
                }).filter(Objects::nonNull).collect(Collectors.toList());
    }


    /**
     * definition property ????????? dto field
     *
     * @param fieldName ?????????
     * @param property  ????????????
     * @return dto field ??????
     */
    private Field propertyConvertDtoField(String fieldName, Property property) {
        Field field = new Field();
        field.setName(fieldName);
        field.setDescription(property.getDescription());
        // ???????????? default ???,?????????????????????????????????
        field.setValue(getPropertyDefaultValue(property));
        // ???????????????class???????????????  x-import: xx.xx.xx,yy.yy.yy,zz.zz.zz
        field.getExternalImportHolder().addItem(
                SwaggerVendorExtensionsUtil.getImports(property.getVendorExtensions())
        );
        field.getValidateAnnotationHolder().addItem(
                SwaggerVendorExtensionsUtil.getValidateAnnotations(property.getVendorExtensions())
        );
        if (property.getRequired()) {
            field.getValidateAnnotationHolder().addItem(ValidateAnnotationUtils.notNull());
        }
        if (property instanceof ArrayProperty) {
            // array ?????????????????????
            Field childField = propertyConvertDtoField("childField", ((ArrayProperty) property).getItems());
            field.setType(String.format("%s<%s>", List.class.getSimpleName(), childField.getType()));
            field.getExternalImportHolder().addItem(List.class.getName());
            field.getExternalImportHolder().addItem(childField.getExternalImportHolder().get());
        } else if (property instanceof RefProperty) {
            RefProperty refProperty = (RefProperty) property;
            String className = getClassNameFromRefPath(refProperty.getOriginalRef());
            field.setType(className);
        } else if (SwaggerConstants.TYPE_OBJECT.equalsIgnoreCase(property.getType())) {
            String xFormat = SwaggerVendorExtensionsUtil.getXFormat(property.getVendorExtensions());
            if (null == xFormat) {
                // ???????????? x-Type,????????? java.lang.Object
                field.setType(Object.class.getSimpleName());
            } else {
                // x-Type: xx ,????????? xx
                field.setType(xFormat);
            }
        } else {
            TypeMapping mapping = TypeMapping.parse(property.getType(), property.getFormat());
            field.setType(mapping.getType());
            field.getExternalImportHolder().addItem(mapping.getExternalImport());
        }
        return field;
    }

    /**
     * ????????????swagger???controller??????
     *
     * @param swagger swagger????????????
     * @return controller??????
     */
    private Map<String, Controller> getControllerMap(Swagger swagger) {
        return Optional.ofNullable(swagger.getTags())
                .orElse(Collections.emptyList())
                .stream().collect(Collectors.toMap(Tag::getName, tag -> {
                    Controller controller = new Controller();
                    controller.setName(SwaggerUtils.wrapControllerClassName(tag.getName()));
                    controller.setServiceName(SwaggerUtils.wrapControllerServiceClassName(tag.getName()));
                    controller.setFeignClientName(SwaggerUtils.wrapFeignClientClassName(tag.getName()));
                    controller.setDescription(tag.getDescription());
                    controller.setBasePath(swagger.getBasePath());
                    controller.setHandlerMethods(new ArrayList<>(16));
                    return controller;
                }));
    }

    /**
     * ??????????????????
     *
     * @param opName ????????????
     * @param params ??????op??????
     * @return ??????????????????
     */
    private List<HandlerMethodParam> getHandlerMethodParams(String opName, List<Parameter> params, List<Dto> dtos) {
        // ????????????????????????path???????????????query???????????????????????????body?????????????????????
        List<Parameter> parameters = Optional.ofNullable(params).orElse(Collections.emptyList());
        List<HandlerMethodParam> handlerMethodParams = parameters.stream()
                .filter(o -> !(o instanceof QueryParameter))
                .map(parameter -> {
                    HandlerMethodParam handlerMethodParam = new HandlerMethodParam();
                    handlerMethodParam.setName(parameter.getName());
                    handlerMethodParam.setDescription(parameter.getDescription());
                    if (parameter.getRequired()) {
                        handlerMethodParam.getValidateAnnotationHolder().addItem(ValidateAnnotationUtils.notNull());
                    }
                    if (parameter instanceof PathParameter) {
                        PathParameter pathParameter = (PathParameter) parameter;
                        TypeMapping mapping = TypeMapping.parse(pathParameter.getType(), pathParameter.getFormat());
                        // ????????????????????????????????????type??????
                        handlerMethodParam.setType(mapping.getType());
                        handlerMethodParam.getExternalImportHolder().addItem(mapping.getExternalImport());
                        handlerMethodParam.setTag(HandlerMethodParamTag.PATH);
                    } else if (parameter instanceof BodyParameter) {
                        BodyParameter bodyParameter = ((BodyParameter) parameter);
                        Model model = bodyParameter.getSchema();
                        if (!(model instanceof RefModel)) {
                            throw new RuntimeException("body?????????????????????ref??????");
                        }
                        // TODO ??? DTO   arrayModel refModel modelImpl
                        String typeName = SwaggerUtils.getClassNameFromRefPath(model.getReference());
                        handlerMethodParam.setType(typeName);
                        handlerMethodParam.setTag(HandlerMethodParamTag.BODY);
                    } else {
                        throw new RuntimeException("?????????????????? query path body ????????????");
                    }
                    return handlerMethodParam;
                }).collect(Collectors.toList());
        List<QueryParameter> queryParameters = parameters.stream()
                .filter(o -> o instanceof QueryParameter)
                .map(o -> (QueryParameter) o)
                .collect(Collectors.toList());
        // ?????? queryParams ??? QueryParamsDTO
        if (!CollectionUtils.isEmpty(queryParameters)) {

            HandlerMethodParam handlerMethodParam = new HandlerMethodParam();
            handlerMethodParam.setTag(HandlerMethodParamTag.QUERY);
            handlerMethodParam.setName("queryParams");
            handlerMethodParam.setDescription("query??????,????????????dto??????");
            handlerMethodParam.setType(SwaggerUtils.getClassNameFromHandlerMethodName(opName));
            // ?????????definition???????????????
            handlerMethodParams.add(handlerMethodParam);

            Dto dto = new Dto();
            dto.setName(SwaggerUtils.getClassNameFromHandlerMethodName(opName));
            dto.setDescription(opName + "??????????????????");
            dto.setFields(queryParameters.stream().map(parameter -> {
                Field field = new Field();
                if (parameter.getRequired()) {
                    field.getValidateAnnotationHolder().addItem(ValidateAnnotationUtils.notNull());
                }
                field.setName(parameter.getName());
                // ?????????
                field.setValue(Optional.ofNullable(parameter.getDefaultValue())
                        .map(Object::toString)
                        .orElse(null));
                field.setDescription(parameter.getDescription());
                handlerMethodParam.getValidateAnnotationHolder().addItem(
                        SwaggerVendorExtensionsUtil.getValidateAnnotations(parameter.getVendorExtensions())
                );
                handlerMethodParam.getExternalImportHolder().addItem(
                        SwaggerVendorExtensionsUtil.getImports(parameter.getVendorExtensions())
                );
                // TODO ???????????? ref
                if (SwaggerConstants.TYPE_ARRAY.equals(parameter.getType())) {
                    if (null == parameter.getItems()) {
                        throw new RuntimeException("QueryParam array?????????????????????????????????!");
                    }
                    if (parameter.getItems() instanceof RefProperty) {
                        throw new RuntimeException("QueryParam ???????????? List<$ref> ");
                    }
                    String type = parameter.getItems().getType();
                    String format = parameter.getItems().getFormat();
                    TypeMapping mapping = TypeMapping.parse(type, format);
                    field.setType(String.format("%s<%s>", List.class.getSimpleName(), mapping.getType()));
                    field.getExternalImportHolder().addItem(mapping.getExternalImport());
                    field.getExternalImportHolder().addItem(List.class.getName());
                } else {
                    TypeMapping mapping = TypeMapping.parse(parameter.getType(), parameter.getFormat());
                    field.setType(mapping.getType());
                    field.getExternalImportHolder().addItem(mapping.getExternalImport());
                }
                return field;
            }).collect(Collectors.toList()));

            dtos.add(dto);
        }
        return handlerMethodParams;
    }

    /**
     * ????????????????????????(@Perfect)
     *
     * @param op ??????????????????
     * @return return??????
     */
    private HandlerMethodReturn getHandlerMethodReturn(Operation op) {
        Response resp = op.getResponses().values()
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("????????????????????????????????????????????????!"));
        HandlerMethodReturn handlerMethodReturn = new HandlerMethodReturn();
        handlerMethodReturn.setDescription(resp.getDescription());
        Model model = resp.getResponseSchema();
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
                handlerMethodReturn.getExternalImportHolder().addItem(mapping.getExternalImport());
            }
            handlerMethodReturn.setType(String.format("%s<%s>", List.class.getSimpleName(), childType));
            handlerMethodReturn.getExternalImportHolder().addItem(List.class.getName());
        } else if (model instanceof ModelImpl) {
            // ????????????
            ModelImpl modelImpl = (ModelImpl) model;
            if (SwaggerConstants.TYPE_OBJECT.equalsIgnoreCase(modelImpl.getType())) {
                String xFormat = SwaggerVendorExtensionsUtil.getXFormat(modelImpl.getVendorExtensions());
                if (null == xFormat) {
                    // ???????????? x-Type,????????? java.lang.Object
                    handlerMethodReturn.setType(Object.class.getSimpleName());
                } else {
                    // x-Type: xx ,????????? xx
                    handlerMethodReturn.setType(xFormat);
                    handlerMethodReturn.getExternalImportHolder().addItem(
                            SwaggerVendorExtensionsUtil.getImports(modelImpl.getVendorExtensions())
                    );
                }
            } else {
                TypeMapping mapping = TypeMapping.parse(modelImpl.getType(), modelImpl.getFormat());
                handlerMethodReturn.setType(mapping.getType());
                handlerMethodReturn.getExternalImportHolder().addItem(mapping.getExternalImport());
            }
        } else {
            throw new RuntimeException("resp?????????????????? $ref | type | List<$ref> |List<type> ");
        }
        return handlerMethodReturn;
    }


}
