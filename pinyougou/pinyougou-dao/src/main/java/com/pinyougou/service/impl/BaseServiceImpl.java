package com.pinyougou.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.service.BaseService;
import com.pinyougou.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.common.Mapper;

import java.io.Serializable;
import java.util.List;

public abstract class BaseServiceImpl<T> implements BaseService<T> {

    //使用了spring 4.x+版本之后可以使用泛型依赖注入
    @Autowired
    private Mapper<T> mapper;

    @Override
    public T findOne(Serializable id) {
        return mapper.selectByPrimaryKey(id);
    }

    @Override
    public List<T> findAll() {
        return mapper.selectAll();
    }

    @Override
    public List<T> findByWhere(T t) {
        return mapper.select(t);
    }

    @Override
    public PageResult findPage(Integer page, Integer rows) {
        //设置分页
        PageHelper.startPage(page, rows);
        List<T> list = mapper.selectAll();

        //转换为分页信息对象
        PageInfo<T> pageInfo = new PageInfo<>(list);

        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public PageResult findPage(Integer page, Integer rows, T t) {
        //设置分页
        PageHelper.startPage(page, rows);
        List<T> list = mapper.select(t);

        //转换为分页信息对象
        PageInfo<T> pageInfo = new PageInfo<>(list);

        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public void add(T t) {
        //选择性新增：如果对象中的属性是不null的话，则才会插入对应的字段;如：insert into tablename values(name);
        mapper.insertSelective(t);
    }

    @Override
    public void update(T t) {
        mapper.updateByPrimaryKeySelective(t);
    }

    @Override
    public void deleteByIds(Serializable[] ids) {
        if (ids != null && ids.length > 0) {
            for (Serializable id : ids) {
                mapper.deleteByPrimaryKey(id);
            }
        }
    }
}
