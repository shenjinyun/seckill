package com.xxx.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xxx.seckill.pojo.Goods;
import com.xxx.seckill.vo.GoodsVo;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author joey
 * @since 2022-04-27
 */
public interface GoodsMapper extends BaseMapper<Goods> {
    /**
     * 获取秒杀商品列表
     * @return
     */
    List<GoodsVo> findGoodsVo();

    /**
     * 获取商品详情
     * @return
     */
    GoodsVo findGoodsVoByGoodsId(Long goodsId);
}
