package com.zengsx.easycode.mavenplugin.apicodegen;

import com.zengsx.easycode.apicodegen.ApiCodegenRunner;
import com.zengsx.easycode.apicodegen.config.GlobalConfig;
import java.io.File;
import java.util.Optional;
import lombok.SneakyThrows;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;


/**
 * @ClassName: ApiCodegenMojo
 * @Description: api生成器
 * @Author: Mr.Zeng
 * @Date: 2021-04-17 16:49
 */
@Mojo(name = "ApiCodegenMojo", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class ApiCodegenMojo extends AbstractMojo {

    /**
     * session
     */
    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession mavenSession;
    /**
     * src路径
     */
    @Parameter(defaultValue = "${project.build.sourceDirectory}", readonly = true)
    private String srcDir;
    /**
     * resource路径
     */
    @Parameter(defaultValue = "${project.build.resources[0].directory}", readonly = true)
    private String resourceDir;

    @Parameter(required = true)
    private GlobalConfig config;

    @SneakyThrows
    @Override
    public void execute() throws MojoExecutionException {
        String defaultApiDirPath = resourceDir + File.separator + "api";
        config.setApiDefineDirPath(Optional.ofNullable(config.getApiDefineDirPath()).orElse(defaultApiDirPath));
        config.setSrcJavaPath(Optional.ofNullable(config.getSrcJavaPath()).orElse(srcDir));
        new ApiCodegenRunner().start(config);
    }
}