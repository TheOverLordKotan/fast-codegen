package com.zengsx.easycode.apicodegen;


import static com.zengsx.easycode.common.utils.VelocityUtils.render;

import com.zengsx.easycode.apicodegen.config.GlobalConfig;
import com.zengsx.easycode.apicodegen.meta.DtoMeta;
import com.zengsx.easycode.apicodegen.meta.ApiMetaResolveResult;
import com.zengsx.easycode.apicodegen.resolver.impl.SwaggerApiMetaResolver;
import com.zengsx.easycode.apicodegen.util.SwaggerUtils;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * @ClassName: ApiCodegenRunner
 * @Description: TODO
 * @Author: Mr.Zeng
 * @Date: 2021-05-06 22:41
 */
public class ApiCodegenRunner {


    /**
     * 开始执行api代码生成
     *
     * @param config 全局配置
     */
    public void start(GlobalConfig config) {
        File[] swaggerFiles = scanSwaggerFiles(config.getApiDefineDirPath());
        List<ApiMetaResolveResult> resolverApiMetaResolveResults = Arrays.stream(swaggerFiles)
                .map(this::parseSwaggerFile)
                .collect(Collectors.toList());
        // 检查重名dto
        checkDuplicatedNameDto(resolverApiMetaResolveResults);
        // 生成文件
        resolverApiMetaResolveResults.forEach(apiMetaResolveResult -> swaggerToFile(config, apiMetaResolveResult));
    }

    /**
     * 扫描swagger文件
     *
     * @param swaggerApiDirPath swagger文件目录
     * @return swagger files
     */
    @SneakyThrows
    public File[] scanSwaggerFiles(String swaggerApiDirPath) {
        File apiResourceDir = new File(swaggerApiDirPath);
        FileUtils.forceMkdir(apiResourceDir);
        File[] swaggerFiles = apiResourceDir.listFiles((file, name) -> name.endsWith(".yaml") || name.endsWith(".yml"));
        Objects.requireNonNull(swaggerFiles, "没有找到swagger定义文档");
        return swaggerFiles;
    }

    /**
     * 解析swagger文件
     *
     * @param swaggerFile swagger file
     * @return 解析结果
     */
    @SneakyThrows
    public ApiMetaResolveResult parseSwaggerFile(File swaggerFile) {
        String content = IOUtils.toString(new FileInputStream(swaggerFile), StandardCharsets.UTF_8);
        return new SwaggerApiMetaResolver().resolve(SwaggerUtils.parseSwagger(content));
    }


    /**
     * 检查重名dto
     *
     * @param resolverApiMetaResolveResults swagger parse results
     */
    public void checkDuplicatedNameDto(List<ApiMetaResolveResult> resolverApiMetaResolveResults) {
        // 检查重名dto
        resolverApiMetaResolveResults.stream()
                .flatMap(o -> o.getDtoMetas().stream())
                .map(DtoMeta::getName)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Entry::getKey)
                .findFirst().ifPresent(sameName -> {
            throw new RuntimeException(String.format("存在同名DTO:%s，会导致文件覆盖,请检查!", sameName));
        });
    }

    /**
     * swagger to file
     *
     * @param config 全局配置
     * @param apiMetaResolveResult 解析结果
     */
    public void swaggerToFile(GlobalConfig config, ApiMetaResolveResult apiMetaResolveResult) {
        // 生成controller文件
        apiMetaResolveResult.getControllerMetas().forEach(controllerMeta -> {
            Map<String, Object> params = new HashMap<>(8);
            params.put("controllerMeta", controllerMeta);
            params.put("config", config);
            File file = new File(config.getControllerPackagePath() + controllerMeta.getName() + ".java");
            render("template/Controller.vm", params, file);
        });
        // 生成service 接口文件
        apiMetaResolveResult.getControllerMetas().forEach(controllerMeta -> {
            Map<String, Object> params = new HashMap<>(8);
            params.put("controllerMeta", controllerMeta);
            params.put("config", config);
            File file = new File(
                    config.getServicePackagePath() + controllerMeta.getServiceName() + ".java");
            render("template/IService.vm", params, file);
        });
        // 生成 dto文件
        apiMetaResolveResult.getDtoMetas().forEach(dtoMeta -> {
            Map<String, Object> params = new HashMap<>(8);
            params.put("definitionMeta", dtoMeta);
            params.put("config", config);
            File file = new File(config.getDtoPackagePath() + dtoMeta.getName() + ".java");
            render("template/Dto.vm", params, file);
        });
    }

}
