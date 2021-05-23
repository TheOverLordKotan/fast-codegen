package com.zengsx.easycode.apicodegen.meta.action;

import com.zengsx.easycode.apicodegen.meta.ValidateAnnotation;
import java.util.List;

/**
 * @InterfaceName: AnnotationSupport
 * @Description: TODO
 * @Author: Mr.Zeng
 * @Date: 2021-05-23 16:22
 */
public interface AnnotationSupport {

    List<ValidateAnnotation> getValidateAnnotations();

    void addValidateAnnotation(ValidateAnnotation... annotations);

}
