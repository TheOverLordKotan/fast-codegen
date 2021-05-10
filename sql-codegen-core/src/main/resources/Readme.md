目前版本只支持 **mysql** 到 **mybatis-plus** 的转换，其他转换后续版本支持

下面提供了配置参考示例

```xml

<plugin>
    <groupId>com.zengshangxing.easycode</groupId>
    <artifactId>entity-convertor</artifactId>
    <version>1.0-SNAPSHOT</version>
    <configuration>
        <!--                    注释了的配置都代表不是必须的-->
        <config>

            <!--                        <dbName>数据库类型（用于决定解析sql的方式），默认为: mysql</dbName>-->
            <!--                        <ormName>持久化层类型（用于决定生成哪类实体），默认为: mybatis-plus</ormName>-->
            <!--                        默认会在项目下找 resources/db/TableSchema.sql-->
            <!--                        <sqlFilePath>/xx/xx/xxx.sql</sqlFilePath>-->

            <!--                        默认为项目的 src/java/main,最后生成的entity文件则在 src/java/main/${basePackage}/entities/下 -->
            <!--                        <srcJavaPath>/xx/yy/zz</srcJavaPath>-->
            <basePackage>com.easycode.demo</basePackage>
            <!--                        <entityPackageName>实体包名,默认为 entities </entityPackageName>-->
            <!--                        <entitySuffix>实体class名称后缀,默认为 DO</entitySuffix>-->
            <mybatisPlusConfig>
                <!--                            <logicDelColumnName>逻辑删除字段，默认为 del</logicDelColumnName>-->
                <!--                            需要生成自动填充配置的字段    @TableField(value = "xxx", fill = FieldFill.INSERT)-->
                <autoInsertFields>
                    <param>created_at</param>
                </autoInsertFields>
                <!--                            需要生成自动填充配置的字段    @TableField(value = "xxx", fill = FieldFill.UPDATE)-->
                <autoUpdateFields>
                    <param>updated_at</param>
                </autoUpdateFields>
                <!--                            需要生成自动填充配置的字段    @TableField(value = "xxx", fill = FieldFill.INSERT_UPDATE)-->
                <autoInsertOrUpdateFields>

                </autoInsertOrUpdateFields>
                <!--                            下列功能目前不打算支持，考虑到文件覆盖的问题: mapper.xml和mapperInterface.java都是可能会有自定义的select方法，容易引起安全事故。。-->
                <!--                            <enableOutputMapper>是否开启生成mapperInterface.java mapper.xml,默认 false</enableOutputMapper>-->
                <!--                            <mapperInterfacePackage>mapper接口生成的包名,默认mappers</mapperInterfacePackage>-->
                <!--                            <mapperXmlDirName>mapper.xml生成的目录名（相对于resources）,默认为 mapper</mapperXmlDirName>-->
            </mybatisPlusConfig>
        </config>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>SqlToEntityConvertor</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

