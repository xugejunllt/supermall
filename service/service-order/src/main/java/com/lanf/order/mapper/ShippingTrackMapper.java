package com.lanf.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lanf.order.model.entity.ShippingTrackDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 物流轨迹 Mapper 接口
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-17
 */
public interface ShippingTrackMapper extends BaseMapper<ShippingTrackDO> {
    /**
     * INSERT IGNORE INTO 是 MySQL 语法，遇到唯一索引冲突时自动跳过，不会抛异常
     *
     */
    @Insert("<script>" +
            "INSERT IGNORE INTO shipping_track " +
            "(id, flow_no, order_id, user_id, status, base_track_status, advanced_track_status, finish_time, finish_content, tenant_id, create_time, update_time, is_deleted, create_by, update_by) " +
            "VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.id}, #{item.flowNo}, #{item.orderId}, #{item.userId}, #{item.status}, #{item.baseTrackStatus}, #{item.advancedTrackStatus}, #{item.finishTime}, #{item.finishContent}, #{item.tenantId}, #{item.createTime}, #{item.updateTime}, #{item.isDeleted}, #{item.createBy}, #{item.updateBy})" +
            "</foreach>" +
            "</script>")
    void insertIgnoreBatch(@Param("list") List<ShippingTrackDO> list);
}
