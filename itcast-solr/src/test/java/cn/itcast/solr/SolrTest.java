package cn.itcast.solr;

import com.pinyougou.pojo.TbItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.ScoredPage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.Date;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/applicationContext-solr.xml")
public class SolrTest {

    @Autowired
    private SolrTemplate solrTemplate;

    /**
     * 新增或者更新solr数据
     */
    @Test
    public void testAddOrUpdate(){
        TbItem item = new TbItem();
        item.setId(7049459L);
        item.setTitle("222 黑鲨游戏手机 8GB+128GB 极夜黑 液冷更快 全网通4G 双卡双待");
        item.setPrice(new BigDecimal(3499));
        item.setUpdateTime(new Date());
        item.setImage("https://item.jd.com/7049459.html");
        item.setNum(123);
        item.setStatus("1");

        solrTemplate.saveBean(item);
        solrTemplate.commit();
    }


    /**
     * 根据主键删除
     */
    @Test
    public void testDeleteById(){
        solrTemplate.deleteById("7049459");
        solrTemplate.commit();
    }

    /**
     * 根据条件删除
     */
    @Test
    public void testDeleteByQuery(){

        //删除所有
        SimpleQuery query = new SimpleQuery("*:*");

        solrTemplate.delete(query);
        solrTemplate.commit();
    }

    //查询
    @Test
    public void testQueryForPage(){
        SimpleQuery simpleQuery = new SimpleQuery();

        //查询的条件名称需要在solr的schema.xml文件中配置过才可以的;contains对查询的关键字不再分词
        Criteria criteria = new Criteria("item_title").contains("手机");
        simpleQuery.addCriteria(criteria);

        //分页信息
        simpleQuery.setOffset(0);//查询的起始索引号，默认为0
        simpleQuery.setRows(5);//页大小，默认10

        //查询
        ScoredPage<TbItem> scoredPage = solrTemplate.queryForPage(simpleQuery, TbItem.class);

        //显示查询信息
        showPage(scoredPage);
    }

    //多条件查询
    @Test
    public void testMultiQueryForPage(){
        SimpleQuery simpleQuery = new SimpleQuery();

        //查询的条件名称需要在solr的schema.xml文件中配置过才可以的;contains对查询的关键字不再分词
        Criteria criteria = new Criteria("item_title").contains("手机");
        simpleQuery.addCriteria(criteria);

        Criteria criteria2 = new Criteria("item_price").greaterThanEqual(5000);
        simpleQuery.addCriteria(criteria2);

        //分页信息
        //simpleQuery.setOffset(0);//查询的起始索引号，默认为0
        //simpleQuery.setRows(5);//页大小，默认10

        //查询
        ScoredPage<TbItem> scoredPage = solrTemplate.queryForPage(simpleQuery, TbItem.class);

        //显示查询信息
        showPage(scoredPage);
    }

    private void showPage(ScoredPage<TbItem> scoredPage) {
        System.out.println("当前页号为：" + scoredPage.getNumber());//从0开始
        System.out.println("页大小为：" + scoredPage.getSize());
        System.out.println("总记录数为：" + scoredPage.getTotalElements());
        System.out.println("总页数数为：" + scoredPage.getTotalPages());

        //遍历列表
        for (TbItem item : scoredPage.getContent()) {
            System.out.println(item);
        }

    }

}
