package com.ping.gmall.service;

import com.ping.gmall.bean.SkuLsInfo;
import com.ping.gmall.bean.SkuLsParams;
import com.ping.gmall.bean.SkuLsResult;

public interface ListService {
    public void saveSkuInfo(SkuLsInfo skuLsInfo);


    public SkuLsResult search(SkuLsParams skuLsParams);

    public void incrHotScore(String skuId);
}
