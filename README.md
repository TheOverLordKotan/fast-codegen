# fast-codegen
代码生成器
### api-codegen-core

主要包含了通过api定义生成Controller IService DTO的逻辑，而开发中需要做的就是实现IService业务逻辑。

### sql-codegen-core

主要包含了通过sql生成orm对象的逻辑。

已实现  mysql ~> mybatis-plus entity 的逻辑



以上只是核心逻辑的实现，maven-plugin的项目是插件的封装，可以通过mvn生命周期命令生成代码
