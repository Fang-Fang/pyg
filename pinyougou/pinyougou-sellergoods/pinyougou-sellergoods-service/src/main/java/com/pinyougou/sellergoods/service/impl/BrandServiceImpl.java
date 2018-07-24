package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.BrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import com.pinyougou.service.impl.BaseServiceImpl;
import com.pinyougou.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.Map;

//service的注解来自dubbo
@Service(interfaceClass = BrandService.class)
public class BrandServiceImpl extends BaseServiceImpl<TbBrand> implements BrandService {

    @Autowired
    private BrandMapper brandMapper;

    @Override
    public List<TbBrand> queryAll() {
        return brandMapper.queryAll();
    }

    @Override
    public List<TbBrand> testPage(Integer page, Integer rows) {
        //设置分页；只对紧接着执行的查询才生效
        PageHelper.startPage(page, rows);
        return brandMapper.selectAll();
    }

    @Override
    public PageResult search(Integer page, Integer rows, TbBrand brand) {
        //设置分页；只对紧接着执行的查询才生效
        PageHelper.startPage(page, rows);

        //创建查询对象
        Example example = new Example(TbBrand.class);

        //创建查询条件对象
        Example.Criteria criteria = example.createCriteria();

        if(!StringUtils.isEmpty(brand.getName())){//根据名字模糊查询
            //参数1：查询对象中的属性名称
            //参数2：查询的条件值
            criteria.andLike("name", "%" + brand.getName() + "%");
        }

        if(!StringUtils.isEmpty(brand.getFirstChar())){//根据名字模糊查询
            criteria.andEqualTo("firstChar", brand.getFirstChar());
        }


        //查询
        List<TbBrand> list = brandMapper.selectByExample(example);

        //转换为分页信息对象
        PageInfo<TbBrand> pageInfo = new PageInfo<>(list);

        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public List<Map<String, Object>> selectOptionList() {
        return brandMapper.selectOptionList();
    }
}
