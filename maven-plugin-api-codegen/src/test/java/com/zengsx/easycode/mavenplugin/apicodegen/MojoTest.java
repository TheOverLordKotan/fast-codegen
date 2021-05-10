package com.zengsx.easycode.mavenplugin.apicodegen;

import java.io.File;
import lombok.SneakyThrows;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;


public class MojoTest {

    @Rule
    public MojoRule rule = new MojoRule() {

    };

    @SneakyThrows
    @Test
    @Ignore
    public void mojoTest() throws Exception {
        File projectFile = new File("src/test/resources/test-project-one");
        Mojo mojo = rule.lookupConfiguredMojo(projectFile, "ApiCodegenMojo");
        mojo.execute();
    }


    @SneakyThrows
    @Test
    @Ignore
    public void generateApiFile() throws Exception {
        File lighthousePom = new File("src/test/resources/lighthouse");
        Mojo mojo = rule.lookupConfiguredMojo(lighthousePom, "ApiCodegenMojo");
        mojo.execute();
    }


}
