package com.ping.gmall.manage.mapper;

import com.ping.gmall.bean.SpuSaleAttrValue;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SpuSaleAttrValueMapper extends Mapper<SpuSaleAttrValue> {

    List<SpuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId);
}
