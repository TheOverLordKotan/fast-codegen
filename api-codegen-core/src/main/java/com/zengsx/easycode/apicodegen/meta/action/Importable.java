package com.zengsx.easycode.apicodegen.meta.action;

import java.util.List;

/**
 * @InterfaceName: Importable
 * @Description: TODO
 * @Author: Mr.Zeng
 * @Date: 2021-05-23 16:19
 */
public interface Importable {

    List<String> getExternalImports();

    void addExternalImport(String... imports);


}
