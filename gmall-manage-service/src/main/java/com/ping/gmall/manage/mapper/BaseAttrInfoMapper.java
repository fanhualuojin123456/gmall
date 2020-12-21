package com.ping.gmall.manage.mapper;

import com.ping.gmall.bean.BaseAttrInfo;

import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BaseAttrInfoMapper extends Mapper<BaseAttrInfo> {
    List<BaseAttrInfo> getBaseAttrInfoListByCatalog3Id(String catalog3Id);

    List<BaseAttrInfo> selectbaseAttrListByIds(@Param("valueIds") String valueIds);
}
