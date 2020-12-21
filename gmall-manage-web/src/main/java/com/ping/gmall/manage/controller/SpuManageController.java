package com.ping.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.ping.gmall.bean.SpuInfo;
import com.ping.gmall.service.ManageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin
@RestController
public class SpuManageController {

//    http://localhost:8082/spuList?catalog3Id=1
    @Reference
    private ManageService manageService;

    @RequestMapping("spuList")
    public List<SpuInfo> spuList(SpuInfo spuInfo){
        return manageService.getSpuList(spuInfo);
    }
    //http://localhost:8082/saveSpuInfo
    //保存：保存到数据涉及的表？
    /**
     * spuInfo
     * spuSaleAttr
     * spuSaleAttrValue
     * spuImage
     */
    @RequestMapping("saveSpuInfo")
    public void saveSpuInfo(@RequestBody SpuInfo spuInfo){
        if (spuInfo != null){
            manageService.saveSpuInfo(spuInfo);
        }
    }


}
