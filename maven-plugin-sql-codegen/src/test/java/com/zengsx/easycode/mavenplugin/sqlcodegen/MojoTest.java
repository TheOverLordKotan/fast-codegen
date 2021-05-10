package com.zengsx.easycode.mavenplugin.sqlcodegen;

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

    public File getTestProjectFile() {
        return new File("src/test/resources/lighthouse");
    }

    @SneakyThrows
    @Test
    @Ignore
    public void generateEntityFile() throws Exception {
        File projectFile = getTestProjectFile();
        Mojo mojo = rule.lookupConfiguredMojo(projectFile, "SqlToEntityConvertor");
        mojo.execute();
    }


}
