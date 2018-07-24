package com.pinyougou.sellergoods.service;

import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.service.BaseService;
import com.pinyougou.vo.PageResult;
import com.pinyougou.vo.Specification;

import java.util.List;
import java.util.Map;

public interface SpecificationService extends BaseService<TbSpecification> {

    PageResult search(Integer page, Integer rows, TbSpecification specification);

    /**
     * 同时新增规格及其选项
     * @param specification 规格及其选项
     */
    void add(Specification specification);

    /**
     * 根据根据规格id查询该规格及其规格选项列表
     * @param id 规格id
     * @return 规格及其规格选项列表
     */
    Specification findOne(Long id);

    /**
     * 同时修改规格及其选项
     * @param specification 规格及其选项
     */
    void update(Specification specification);

    /**
     * 查询符合结构的所有规格列表；结构如：[{id:'1',text:'联想'},{id:'2',text:'华为'}]
     * @return 规格列表集合
     */
    List<Map<String,Object>> selectOptionList();
}