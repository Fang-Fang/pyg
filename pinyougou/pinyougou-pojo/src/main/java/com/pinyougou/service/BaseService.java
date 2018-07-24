package com.pinyougou.service;

import com.pinyougou.vo.PageResult;

import java.io.Serializable;
import java.util.List;

public interface BaseService<T> {
    //根据id查询
    T findOne(Serializable id);

    //查询全部
    List<T> findAll();

    //根据条件查询
    List<T> findByWhere(T t);

    /**
     * 分页查询
     * @param page 页号
     * @param rows 页大小
     * @return 分页对象
     */
    PageResult findPage(Integer page, Integer rows);

    /**
     * 分页查询
     * @param page 页号
     * @param rows 页大小
     * @param t 查询条件
     * @return 分页对象
     */
    PageResult findPage(Integer page, Integer rows, T t);

    //新增
    void add(T t);

    //更新
    void update(T t);

    //批量删除
    void deleteByIds(Serializable[] ids);
}
