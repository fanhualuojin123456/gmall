package com.ping.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.ping.gmall.bean.SkuInfo;
import com.ping.gmall.bean.SkuSaleAttrValue;
import com.ping.gmall.bean.SpuSaleAttr;
import com.ping.gmall.bean.SpuSaleAttrValue;
import com.ping.gmall.config.LoginRequire;
import com.ping.gmall.service.ListService;
import com.ping.gmall.service.ManageService;
import org.junit.Test;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;


@Controller
public class ItemController {

    @Reference
    private ManageService manageService;

    @Reference
    private ListService listService;

    //localhost:8084/42.html
    @LoginRequire(autoRedirect = true)
    @RequestMapping("{skuId}.html")
    public String skuInfoPage(@PathVariable String skuId, HttpServletRequest request){
        // 根据skuId 获取数据
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);

        request.setAttribute("skuInfo",skuInfo);
        // 查询销售属性，销售属性值集合 spuId，skuId
        List<SpuSaleAttr> spuSaleAttrList = manageService.getSpuSaleAttrListCheckBySku(skuInfo);
        request.setAttribute("spuSaleAttrList",spuSaleAttrList);

        // getSpuId获取销售属性值
        List<SkuSaleAttrValue> skuSaleAttrValueList = manageService.getSkuSaleAttrValueListBySpu(skuInfo.getSpuId());

        String key = "";
        HashMap<String, Object> map = new HashMap<>();
        // 普通循环
        for (int i = 0; i < skuSaleAttrValueList.size(); i++) {
             //单个属性
            SkuSaleAttrValue skuSaleAttrValue = skuSaleAttrValueList.get(i);
            if (key.length() > 0){
                key += "|";

            }
            key += skuSaleAttrValue.getSaleAttrValueId();
            if ((i + 1) == skuSaleAttrValueList.size() || !skuSaleAttrValue.getSkuId().equals(skuSaleAttrValueList.get(i+1).getSkuId())){
                map.put(key,skuSaleAttrValue.getSkuId());
                key = "";
            }
        }
        // 将map 转换为json 字符串
        String valuesSkuJson = JSON.toJSONString(map);
        request.setAttribute("valuesSkuJson",valuesSkuJson);


        listService.incrHotScore(skuId);


        return "item";
    }



}
