package com.zengsx.easycode.apicodegen.meta.action;

import com.zengsx.easycode.apicodegen.meta.ValidateAnnotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @ClassName: AbstractMeta
 * @Description: TODO
 * @Author: Mr.Zeng
 * @Date: 2021-05-23 16:49
 */
public abstract class AbstractMeta implements AnnotationSupport, Importable {

    /**
     * 校验注解
     */
    private final List<ValidateAnnotation> validateAnnotations;
    /**
     * 外部引入
     */
    private final List<String> externalImports;

    public AbstractMeta() {
        validateAnnotations = new ArrayList<>();
        externalImports = new ArrayList<>();
    }

    @Override
    public final List<ValidateAnnotation> getValidateAnnotations() {
        return validateAnnotations;
    }

    @Override
    public final void addValidateAnnotation(ValidateAnnotation... annotations) {
        validateAnnotations.addAll(Arrays.asList(annotations));
    }

    @Override
    public final List<String> getExternalImports() {
        processExternalImport();
        return externalImports
                .stream()
                .filter(Objects::nonNull)
                .filter(o -> o.contains("."))
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public final void addExternalImport(String... imports) {
        externalImports.addAll(Arrays.asList(imports));
    }

    protected void processExternalImport() {
        // TODO 子类实现自己要扩展的imports填充
    }
}
