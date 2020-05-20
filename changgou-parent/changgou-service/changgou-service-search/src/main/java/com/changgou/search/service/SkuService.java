package com.changgou.search.service;

import java.util.Map;

/**
 * @Auther: hftang
 * @Date: 2020/1/2 17:10
 * @Description:
 */
public interface SkuService {

    void importSku();

    /***
     * 搜索
     * @param searchMap
     * @return
     */
    Map search(Map<String, String> searchMap);

}
