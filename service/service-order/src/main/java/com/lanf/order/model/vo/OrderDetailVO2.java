package com.lanf.order.model.vo;

import com.lanf.logistics.model.vo.LogisticsTrackVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class OrderDetailVO2 implements Serializable {

    /**
     * 订单信息
     */
    //订单编号
    private String orderNumber;
    //下单时间
    private Date createTime;
    /**
     *订单状态
     * 0:待付款, 1:待出库 2：已出库 3：已发货，4：已完成，5：已关闭 6.已取消
     *
     */
    private Integer status;
    //订单状态名称
    private String statusName;
    //收货人
    private String consignee;
    //收货人联系电话
    private String phone;
    //收货地址
    private String takeAddress;
    //订单商品信息
    private List<OrderItemDetailVO> orderItemDetailVOList;
    /**
     * 支付信息
     */
    //订单金额
    private BigDecimal orderMoney;
    //支付金额
    private BigDecimal payMoney;
    //优惠金额
    private BigDecimal discountMoney;
    //优惠方式
    private Integer discountType;
    private String discountTypeName;

    //支付类型 0支付宝 1微信 2银联
    private Integer payType;
    private String payTypeName;
    //用户支付完成时间
    private Date payFinishTime;
    /**
     * 快递信息
     */
    //物流公司
    private String expressCompany;
    //快递单号
    private String expressNumber;
    //发货人
    private String fromConsignee;
    //发货人联系电话
    private String fromPhone;
    //发货地址
    private String fromAddress;
    /**
     * 订单轨迹
     */
    private List<LogisticsTrackVO> logisticsTrackVOList;

}
