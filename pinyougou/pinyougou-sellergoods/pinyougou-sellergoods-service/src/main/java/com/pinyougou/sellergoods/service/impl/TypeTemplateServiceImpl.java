package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.SpecificationOptionMapper;
import com.pinyougou.mapper.TypeTemplateMapper;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.pojo.TbTypeTemplate;
import com.pinyougou.sellergoods.service.TypeTemplateService;
import com.pinyougou.service.impl.BaseServiceImpl;
import com.pinyougou.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.Map;

@Service(interfaceClass = TypeTemplateService.class)
public class TypeTemplateServiceImpl extends BaseServiceImpl<TbTypeTemplate> implements TypeTemplateService {

    @Autowired
    private TypeTemplateMapper typeTemplateMapper;

    @Autowired
    private SpecificationOptionMapper specificationOptionMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public PageResult search(Integer page, Integer rows, TbTypeTemplate typeTemplate) {
        PageHelper.startPage(page, rows);

        Example example = new Example(TbTypeTemplate.class);
        Example.Criteria criteria = example.createCriteria();
        if(!StringUtils.isEmpty(typeTemplate.getName())){
            criteria.andLike("name", "%" + typeTemplate.getName() + "%");
        }

        List<TbTypeTemplate> list = typeTemplateMapper.selectByExample(example);
        PageInfo<TbTypeTemplate> pageInfo = new PageInfo<>(list);

        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public List<Map> findSpecList(Long id) {
        //1、根据分类模板id查询分类模板（获取该模板对应的规格列表）
        TbTypeTemplate typeTemplate = findOne(id);
        //2、遍历每一个规格，根据规格id查询该规格对应的规格选项列表
        //使用fastJson将一个json格式字符串转换为一个java对象；参数1：要转换的字符串，参数2：转换后集合中的对象类型
        List<Map> specList = JSONArray.parseArray(typeTemplate.getSpecIds(), Map.class);
        if (specList != null && specList.size() > 0) {
            for (Map map : specList) {
                //根据规格id查询该规格对应的规格选项列表
                TbSpecificationOption param = new TbSpecificationOption();
                param.setSpecId(Long.parseLong(map.get("id")+""));

                List<TbSpecificationOption> options = specificationOptionMapper.select(param);

                map.put("options", options);
            }
        }

        //3、返回
        return specList;
    }

    @Override
    public void updateTypeTemplateToRedis() {
        //1、获取分类模板列表
        List<TbTypeTemplate> typeTemplateList = findAll();
        //2、缓存品牌和规格选项列表
        for (TbTypeTemplate typeTemplate : typeTemplateList) {
            //缓存品牌
            List<Map> typeTemplateBrandList = JSONArray.parseArray(typeTemplate.getBrandIds(), Map.class);
            redisTemplate.boundHashOps("brandList").put(typeTemplate.getId(), typeTemplateBrandList);

            //缓存规格选项
            List<Map> typeTemplateSpecList = findSpecList(typeTemplate.getId());
            redisTemplate.boundHashOps("specList").put(typeTemplate.getId(), typeTemplateSpecList);
        }
        System.out.println("缓存分类模板（品牌、规格选项列表）完成。");
    }
}
