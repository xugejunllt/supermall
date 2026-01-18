package com.lanf.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.CodeGenerateUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.order.mapper.MainOrderMapper;
import com.lanf.order.model.dto.CreateOrderDTO2;
import com.lanf.order.model.dto.OrderDTO;
import com.lanf.order.model.dto.OrderItemDTO;
import com.lanf.order.model.entity.MainOrderDO;
import com.lanf.order.model.entity.OrderDO;
import com.lanf.order.model.entity.OrderItemDO;
import com.lanf.order.model.entity.PromiseOrderDO;
import com.lanf.order.model.vo.CreateOrderVO;
import com.lanf.order.service.IMainOrderService;
import com.lanf.order.service.IOrderItemService;
import com.lanf.order.service.IOrderService;
import com.lanf.order.service.IPromiseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 主订单 服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-13
 */
@Service
public class MainOrderServiceImpl extends ServiceImpl<MainOrderMapper, MainOrderDO> implements IMainOrderService {


    @Autowired
    private IOrderItemService orderItemService;
    @Autowired
    private IOrderService orderService;
    @Autowired
    private IPromiseOrderService promiseOrderService;


    @Override
    public CreateOrderVO createOrder(CreateOrderDTO2 dto) {

        String orderNumber = dto.getOrderNumber();
        MainOrderDO orderDO = this.lambdaQuery().eq(MainOrderDO::getOrderNumber, orderNumber).one();
        if (orderDO != null) {
            throw new BizException("重复下单");
        }

        MainOrderDO mainOrderDO = new MainOrderDO();
        mainOrderDO.setOrderNumber(dto.getOrderNumber());
        mainOrderDO.setId(dto.getMainOrderId());
        //
        List<OrderDTO> orderDTOList = dto.getOrderDTOList();
        List<OrderDO> orderDOList = BeanCopyUtils.copyBeanList(orderDTOList, OrderDO.class);
        List<OrderItemDO> orderItemDOList = new ArrayList<>();
        List<PromiseOrderDO> promiseOrderDOList = new ArrayList<>();
        for (OrderDO a : orderDOList) {
           // a.setCreateTimeFormat(DateUtils.format(new Date(),DateUtils.DATE));
            a.setStatus(0);
            a.setOrderNumber(CodeGenerateUtils.generateOrderNumber());
            //

        }
        orderDTOList.forEach(a -> {
            List<OrderItemDTO> orderItemDTOList = a.getOrderItemDTOList();
            Long id = a.getId();
            List<OrderItemDO> itemDOList = BeanCopyUtils.copyBeanList(orderItemDTOList, OrderItemDO.class);
            itemDOList.forEach(b -> {
                b.setOrderId(id);
            });
            orderItemDOList.addAll(itemDOList);
        });
        this.save(mainOrderDO);
        orderService.saveBatch(orderDOList);
        orderItemService.saveBatch(orderItemDOList);

        //构建返回信息
        List<Long> orderIdList = orderDOList.stream().map(BaseEntity::getId).collect(Collectors.toList());
        CreateOrderVO createOrderVO = new CreateOrderVO();
        createOrderVO.setMainOrderId(dto.getMainOrderId());
        createOrderVO.setOrderId(orderDOList.get(0).getId());
        createOrderVO.setOrderIdList(orderIdList);
        return createOrderVO;
    }


}
