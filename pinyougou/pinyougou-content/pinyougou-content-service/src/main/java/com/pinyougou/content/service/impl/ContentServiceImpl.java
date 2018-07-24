package com.pinyougou.content.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.ContentMapper;
import com.pinyougou.pojo.TbContent;
import com.pinyougou.content.service.ContentService;
import com.pinyougou.service.impl.BaseServiceImpl;
import com.pinyougou.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

@Service(interfaceClass = ContentService.class)
public class ContentServiceImpl extends BaseServiceImpl<TbContent> implements ContentService {

    //内容数据在redis中对应的key的名称
    private static final String REDIS_CONTENT_KEY = "content";

    @Autowired
    private ContentMapper contentMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public PageResult search(Integer page, Integer rows, TbContent content) {
        PageHelper.startPage(page, rows);

        Example example = new Example(TbContent.class);
        Example.Criteria criteria = example.createCriteria();
        /*if(!StringUtils.isEmpty(content.get***())){
            criteria.andLike("***", "%" + content.get***() + "%");
        }*/

        List<TbContent> list = contentMapper.selectByExample(example);
        PageInfo<TbContent> pageInfo = new PageInfo<>(list);

        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public List<TbContent> findContentListByCategoryId(Long categoryId) {
        List<TbContent> list = null;

        try {
            //从redis中加载数据
            list = (List<TbContent>) redisTemplate.boundHashOps(REDIS_CONTENT_KEY).get(categoryId);
            if (list != null) {
                return list;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Example example = new Example(TbContent.class);
        Example.Criteria criteria = example.createCriteria();

        //根据内容分类查询
        criteria.andEqualTo("categoryId", categoryId);
        //根据状态查询
        criteria.andEqualTo("status", "1");

        //降序排序 asc升序，desc降序
        example.orderBy("sortOrder").desc();

        list = contentMapper.selectByExample(example);

        try {
            //存入到redis
            redisTemplate.boundHashOps(REDIS_CONTENT_KEY).put(categoryId, list);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public void add(TbContent tbContent) {
        super.add(tbContent);

        //更新缓存数据
        updateContentInRedisByCategoryId(tbContent.getCategoryId());
    }

    /**
     * 删除redis中分类id对应的缓存数据
     * @param categoryId 分类id
     */
    private void updateContentInRedisByCategoryId(Long categoryId) {
        redisTemplate.boundHashOps(REDIS_CONTENT_KEY).delete(categoryId);
    }

    @Override
    public void update(TbContent tbContent) {

        //查询旧的内容
        TbContent oldContent = findOne(tbContent.getId());

        if (!tbContent.getCategoryId().equals(oldContent.getCategoryId())) {
            //如果修改过分类id，则需要更新旧分类对应的内容和新分类对应的内容
            updateContentInRedisByCategoryId(oldContent.getCategoryId());
        }

        //更新缓存数据
        updateContentInRedisByCategoryId(tbContent.getCategoryId());

        super.update(tbContent);
    }

    @Override
    public void deleteByIds(Serializable[] ids) {

        //ids 表示内容的id集合
        //先根据内容查询所有的内容列表获得每个内容的内容分类id,再对每个分类进行更新缓存
        Example example = new Example(TbContent.class);
        example.createCriteria().andIn("id", Arrays.asList(ids));

        List<TbContent> contentList = contentMapper.selectByExample(example);
        for (TbContent content : contentList) {
            updateContentInRedisByCategoryId(content.getCategoryId());
        }

        super.deleteByIds(ids);
    }
}
