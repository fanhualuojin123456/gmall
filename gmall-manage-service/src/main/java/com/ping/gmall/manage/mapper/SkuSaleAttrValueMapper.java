package com.ping.gmall.manage.mapper;

import com.ping.gmall.bean.SkuSaleAttrValue;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SkuSaleAttrValueMapper extends Mapper<SkuSaleAttrValue>{

    List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId);
}
