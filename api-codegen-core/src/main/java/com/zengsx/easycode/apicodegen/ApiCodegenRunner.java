package com.zengsx.easycode.apicodegen;


import static com.zengsx.easycode.common.utils.VelocityUtils.render;

import com.zengsx.easycode.apicodegen.config.GlobalConfig;
import com.zengsx.easycode.apicodegen.config.PathConfig;
import com.zengsx.easycode.apicodegen.enums.GenerateType;
import com.zengsx.easycode.apicodegen.meta.ApiResolveResult;
import com.zengsx.easycode.apicodegen.meta.Dto;
import com.zengsx.easycode.apicodegen.resolver.impl.SwaggerApiResolver;
import com.zengsx.easycode.apicodegen.util.SwaggerUtils;
import com.zengsx.easycode.common.utils.EnumUtils;
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
        List<ApiResolveResult> resolverApiResolveResults = Arrays.stream(swaggerFiles)
                .map(this::parseSwaggerFile)
                .collect(Collectors.toList());
        // 检查重名dto
        checkDuplicatedNameDto(resolverApiResolveResults);

        GenerateType generateType = EnumUtils.getEnum(GenerateType.class, config.getGenerateType());
        switch (generateType) {
            case SPRING_MVC:
                // 生成文件
                resolverApiResolveResults.forEach(apiResolveResult -> {
                    generateControllerFile(config, apiResolveResult);
                    generateServiceFile(config, apiResolveResult);
                    generateDtoFile(config, apiResolveResult);
                });
                break;
            case FEIGN_CLIENT:
                // 生成文件
                resolverApiResolveResults.forEach(apiResolveResult -> {
                    generateFeignClientFile(config, apiResolveResult);
                    generateDtoFile(config, apiResolveResult);
                });
                break;
            default:
                break;
        }
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
    public ApiResolveResult parseSwaggerFile(File swaggerFile) {
        String content = IOUtils.toString(new FileInputStream(swaggerFile), StandardCharsets.UTF_8);
        return new SwaggerApiResolver().resolve(SwaggerUtils.parseSwagger(content));
    }

    /**
     * 检查重名dto
     *
     * @param resolverApiResolveResults swagger parse results
     */
    public void checkDuplicatedNameDto(List<ApiResolveResult> resolverApiResolveResults) {
        // 检查重名dto
        resolverApiResolveResults.stream()
                .flatMap(o -> o.getDtos().stream())
                .map(Dto::getName)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Entry::getKey)
                .findFirst().ifPresent(sameName -> {
            throw new RuntimeException(String.format("存在同名DTO:%s，会导致文件覆盖,请检查!", sameName));
        });
    }


    /**
     * generate  file
     *
     * @param config           全局配置
     * @param apiResolveResult 解析结果
     */
    public void generateControllerFile(GlobalConfig config, ApiResolveResult apiResolveResult) {
        PathConfig pathConfig = new PathConfig(config);
        // 生成controller文件
        apiResolveResult.getControllers().forEach(controllerMeta -> {
            Map<String, Object> params = new HashMap<>(8);
            params.put("controller", controllerMeta);
            params.put("config", config);
            File file = new File(pathConfig.getControllerPackagePath() + controllerMeta.getName() + ".java");
            render("template/Controller.vm", params, file);
        });
    }

    /**
     * generate  file
     *
     * @param config           全局配置
     * @param apiResolveResult 解析结果
     */
    public void generateServiceFile(GlobalConfig config, ApiResolveResult apiResolveResult) {
        PathConfig pathConfig = new PathConfig(config);
        // 生成service 接口文件
        apiResolveResult.getControllers().forEach(controllerMeta -> {
            Map<String, Object> params = new HashMap<>(8);
            params.put("controller", controllerMeta);
            params.put("config", config);
            File file = new File(
                    pathConfig.getServicePackagePath() + controllerMeta.getServiceName() + ".java");
            render("template/IService.vm", params, file);
        });
    }


    /**
     * generate  file
     *
     * @param config           全局配置
     * @param apiResolveResult 解析结果
     */
    public void generateDtoFile(GlobalConfig config, ApiResolveResult apiResolveResult) {
        PathConfig pathConfig = new PathConfig(config);
        // 生成 dto文件
        apiResolveResult.getDtos().forEach(dto -> {
            Map<String, Object> params = new HashMap<>(8);
            params.put("definition", dto);
            params.put("config", config);
            File file = new File(pathConfig.getDtoPackagePath() + dto.getName() + ".java");
            render("template/Dto.vm", params, file);
        });
    }

    /**
     * generate  file
     *
     * @param config           全局配置
     * @param apiResolveResult 解析结果
     */
    public void generateFeignClientFile(GlobalConfig config, ApiResolveResult apiResolveResult) {
        PathConfig pathConfig = new PathConfig(config);
        // 生成controller文件
        apiResolveResult.getControllers().forEach(controllerMeta -> {
            Map<String, Object> params = new HashMap<>(8);
            params.put("controller", controllerMeta);
            params.put("config", config);
            File file = new File(
                    pathConfig.getFeignClientPackagePath() + controllerMeta.getFeignClientName() + ".java");
            render("template/FeignClient.vm", params, file);
        });
    }

}
