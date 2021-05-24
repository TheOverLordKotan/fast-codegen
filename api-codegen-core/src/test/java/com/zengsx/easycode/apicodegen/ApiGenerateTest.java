package com.zengsx.easycode.apicodegen;

import com.zengsx.easycode.apicodegen.config.GlobalConfig;
import com.zengsx.easycode.apicodegen.enums.GenerateType;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @ClassName: ApiGenerateTest
 * @Description: TODO
 * @Author: Mr.Zeng
 * @Date: 2021-05-10 20:03
 */
public class ApiGenerateTest {

    @Test
    @Ignore
    public void generateLighthouseApi() {
        GlobalConfig config = new GlobalConfig();
        config.setApiDefineDirPath("src/test/resources/api");
        config.setSrcJavaPath("src/test/resources/");
        config.setBasePackage("com.java.demo");

        config.setGenerateType(GenerateType.SPRING_MVC.getId());
        config.setApplicationName("FastCodegenApp");
        config.setApplicationServerPort(8080);
        new ApiCodegenRunner().start(config);
    }

}
