package com.ping.gmall.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.ping.gmall.bean.BaseAttrInfo;
import com.ping.gmall.bean.BaseAttrValue;
import com.ping.gmall.bean.SkuLsParams;
import com.ping.gmall.bean.SkuLsResult;
import com.ping.gmall.service.ListService;
import com.ping.gmall.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Controller
public class ListController {

    @Reference
    private ListService listService;

    @Reference
    private ManageService manageService;

    //http://list.gmall.com/list.html?keyword=手机
    //http://list.gmall.com/list.html?catalog3Id=61

    @RequestMapping("list.html")
    public String getList(SkuLsParams skuLsParams, HttpServletRequest request){

        // 设置每页显示的条数
        skuLsParams.setPageSize(2);

        SkuLsResult skuLsResult = listService.search(skuLsParams);
        if (skuLsResult == null ){
            return "list";
        }

        // 从结果中取出平台属性值列表
        List<String> attrValueIdList = skuLsResult.getAttrValueIdList();
        List<BaseAttrInfo> attrList = null;
        try {
            attrList = manageService.getAttrList(attrValueIdList);
        } catch (Exception e) {
            e.printStackTrace();
            return "list";
        }


        // 已选的属性值列表\
        List<BaseAttrValue> baseAttrValueList = new ArrayList<>();

        String urlParam = makeUrlParam(skuLsParams);
        // 使用迭代器
        for (Iterator<BaseAttrInfo> iterator = attrList.iterator(); iterator.hasNext(); ) {
            BaseAttrInfo baseAttrInfo =  iterator.next();
            // 获取平台属性值集合对象
            List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
            // 循环当前attrValueList
            for (BaseAttrValue baseAttrValue : attrValueList) {
                // 获取skuLsParams.getValueId() 循环对比
                if (skuLsParams.getValueId() != null && skuLsParams.getValueId().length >0){
                    for (String valueId : skuLsParams.getValueId()) {
                        //选中的属性值 和 查询结果的属性值
                        if (valueId.equals(baseAttrValue.getId())){
                            // 如果平台属性值id 相同，则将数据移除！
                            iterator.remove();
                            // 构造面包屑列表
                            BaseAttrValue baseAttrValueSelected = new BaseAttrValue();
                            baseAttrValueSelected.setValueName(baseAttrInfo.getAttrName()+ ":" + baseAttrValue.getValueName());
                            // 去除重复数据
                            String makeUrlParam = makeUrlParam(skuLsParams, valueId);
                            baseAttrValueSelected.setUrlParam(makeUrlParam);
                            baseAttrValueList.add(baseAttrValueSelected);

                        }
                    }
                }
            }
        }

        // 保存面包屑清单
        request.setAttribute("baseAttrValueList",baseAttrValueList);
        request.setAttribute("keyword",skuLsParams.getKeyword());
        request.setAttribute("urlParam",urlParam);

        request.setAttribute("attrList",attrList);
        // 获取sku属性值列表
        request.setAttribute("skuLsInfoList",skuLsResult.getSkuLsInfoList());

        request.setAttribute("pageNo",skuLsParams.getPageNo());
        request.setAttribute("totalPages",skuLsResult.getTotalPages());

        return "list";
    }
    public String makeUrlParam(SkuLsParams skuLsParams,String ... excludeValueIds){
        String urlParam = "";
        if (skuLsParams.getKeyword()!=null){
            if (urlParam.length() > 0){
                urlParam += "&";
            }
            urlParam += "keyword=" + skuLsParams.getKeyword();
        }
        if (skuLsParams.getCatalog3Id()!=null){
            if (urlParam.length() > 0){
               urlParam += "&";
            }
            urlParam += "catalog3Id=" + skuLsParams.getCatalog3Id();
        }
        if (skuLsParams.getValueId()!=null && skuLsParams.getValueId().length > 0){
            for (int i = 0; i < skuLsParams.getValueId().length; i++) {
                String valueId = skuLsParams.getValueId()[i];

                if(excludeValueIds!=null && excludeValueIds.length > 0){
                    String excludeValueId = excludeValueIds[0];
                    if (excludeValueId.equals(valueId)){
                        continue;

                    }


                }
                if (urlParam.length() > 0){
                    urlParam += "&";
                }
                urlParam += "valueId=" + valueId;
            }
        }
        return urlParam;
    }
}
