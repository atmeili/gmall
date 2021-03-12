package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author mqx
 * @date 2021-2-2 09:14:33
 */
@Api(tags = "品牌的数据接口")
@RestController
@RequestMapping("admin/product/baseTrademark")
public class BaseTrademarkController {

    //  需要注入服务层
    @Autowired
    private BaseTrademarkService baseTrademarkService;

    // http://api.gmall.com/admin/product/baseTrademark/{page}/{limit}
    @GetMapping("{page}/{limit}")
    public Result getPageList(@PathVariable Long page,
                              @PathVariable Long limit){

        //  创建一个Page 对象 ,需要将 page,limit 放入进去
        Page<BaseTrademark> baseTrademarkPage = new Page<>(page,limit);

        IPage<BaseTrademark> baseTrademarkIPage = baseTrademarkService.getPage(baseTrademarkPage);

        //  返回到result.data 中
        return Result.ok(baseTrademarkIPage);
    }

    //  http://api.gmall.com/admin/product/baseTrademark/save
    //  保存传递的参数Json 对象。需要使用@RequestBody 将其转换为java 对象
    @PostMapping("save")
    public Result save(@RequestBody BaseTrademark baseTrademark){
        //  保存方法
        baseTrademarkService.save(baseTrademark);
        return Result.ok();
    }

    //  http://api.gmall.com/admin/product/baseTrademark/update
    //  传递的参数Json 对象。需要使用@RequestBody 将其转换为java 对象
    @PutMapping("update")
    public Result update(@RequestBody BaseTrademark baseTrademark){
        //  修改方法
        baseTrademarkService.updateById(baseTrademark);
        return Result.ok();
    }

    //  http://api.gmall.com/admin/product/baseTrademark/remove/{id}
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id){
        //  删除方法
        baseTrademarkService.removeById(id);
        return Result.ok();
    }

    //  http://api.gmall.com/admin/product/baseTrademark/get/{id}
    @GetMapping("get/{id}")
    public Result getBaseTradeMarkById(@PathVariable Long id){
        //  调用查询方法
        BaseTrademark baseTrademark = baseTrademarkService.getById(id);

        return Result.ok(baseTrademark);
    }

    //  http://localhost/admin/product/baseTrademark/getTrademarkList
    @GetMapping("getTrademarkList")
    public Result getTrademarkList(){
        // 查询品牌列表
        return Result.ok(baseTrademarkService.list(null));
    }

    //  根据名称查询数据
    @GetMapping("findBaseTrademarkByKeyword/{keyword}")
    public Result findBaseTrademarkByKeyword(@PathVariable String keyword){
        //  调用服务层方法
        return Result.ok(baseTrademarkService.findBaseTrademarkByKeyword(keyword));
    }

    @PostMapping("inner/findBaseTrademarkByTrademarkIdList")
    public List<BaseTrademark> findBaseTrademarkByTrademarkIdList(@RequestBody List<Long> trademarkIdList){

        return baseTrademarkService.findBaseTrademarkByTrademarkIdList(trademarkIdList);
    }
}
