package com.ping.gmall.manage.mapper;

import com.ping.gmall.bean.SpuSaleAttr;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SpuSaleAttrMapper extends Mapper<SpuSaleAttr> {
    /**
     * 根据spuId 查询销售属性集合
     * 需要使用SpuSaleAttrMapper.xml 写在resources 目录下
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> selectSpuSaleAttrList(String spuId);

    /**
     * 查询销售属性集合
     * @param id
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(String id, String spuId);
}
