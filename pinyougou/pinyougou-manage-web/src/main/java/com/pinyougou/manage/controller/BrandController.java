package com.pinyougou.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import com.pinyougou.vo.PageResult;
import com.pinyougou.vo.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping("/brand")
@RestController //是一个组合注解，@Controller @ResponseBody 对该类所有方法生效
public class BrandController {

    //获取服务层服务器提供的服务对象
    @Reference
    private BrandService brandService;

    @GetMapping("/testPage")
    //@ResponseBody
    public List<TbBrand> testPage(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                  @RequestParam(value = "rows", defaultValue = "10")Integer rows){
        //return brandService.testPage(page, rows);
        return (List<TbBrand>) brandService.findPage(page, rows).getRows();
    }

    @GetMapping("/findAll")
    //@ResponseBody
    public List<TbBrand> findAll(){
        //return brandService.queryAll();
        return brandService.findAll();
    }

    /**
     * 分页查询品牌列表
     * @param page 页号
     * @param rows 页大小
     * @return
     */
    @GetMapping("/findPage")
    //@ResponseBody
    public PageResult findPage(@RequestParam(value = "page", defaultValue = "1") Integer page,
                               @RequestParam(value = "rows", defaultValue = "10")Integer rows){
        return brandService.findPage(page, rows);
    }


    /**
     * 保存品牌
     * @param brand 品牌
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result add(@RequestBody TbBrand brand){
        try {
            brandService.add(brand);
            return Result.ok("新增成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("新增失败");
    }

    /**
     * 根据id查询品牌
     * @param id 品牌id
     * @return 品牌
     */
    @GetMapping("/findOne")
    public TbBrand findOne(Long id){
        return brandService.findOne(id);
    }

    /**
     * 更新品牌
     * @param brand 品牌
     * @return 操作结果
     */
    @PostMapping("/update")
    public Result update(@RequestBody TbBrand brand){
        try {
            brandService.update(brand);
            return Result.ok("修改成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("修改失败");
    }

    /**
     * 批量删除
     * @param ids id集合
     * @return 操作结果
     */
    @GetMapping("/delete")
    public Result delete(Long[] ids){
        try {
            if (ids != null && ids.length > 0) {
                brandService.deleteByIds(ids);
            }
            return Result.ok("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("删除失败");
    }

    /**
     * 条件分页查询品牌列表
     * @param page 页号
     * @param rows 页大小
     * @param brand 查询条件对象
     * @return 分页对象
     */
    @PostMapping("/search")
    public PageResult search(@RequestParam(value = "page", defaultValue = "1") Integer page,
                               @RequestParam(value = "rows", defaultValue = "10")Integer rows,
                               @RequestBody TbBrand brand){
        return brandService.search(page, rows, brand);
    }

    /**
     * 查询符合结构的所有品牌列表；结构如：[{id:'1',text:'联想'},{id:'2',text:'华为'}]
     * @return 品牌列表集合
     */
    @GetMapping("/selectOptionList")
    public List<Map<String, Object>> selectOptionList(){
        return brandService.selectOptionList();
    }

}
