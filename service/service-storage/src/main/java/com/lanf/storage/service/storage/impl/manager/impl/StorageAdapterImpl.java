package com.lanf.storage.service.storage.impl.manager.impl;


import com.lanf.common.utils.BigDecimalUtil;
import com.lanf.storage.model.bo.CalculatePurchaseOrderItemMoneyBO;
import com.lanf.storage.model.bo.CalculatePurchaseOrderMoneyBO;
import com.lanf.storage.model.dto.CalculatePurchaseOrderItemMoneyDTO;
import com.lanf.storage.model.dto.CalculatePurchaseOrderMoneyDTO;
import com.lanf.storage.service.storage.impl.manager.StorageAdapter;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class StorageAdapterImpl implements StorageAdapter {

    /**
     * 计算采购单各项费用
     *
     * @param calculatePurchaseOrderMoney
     * @return
     */
    @Override
    public CalculatePurchaseOrderMoneyBO calculatePurchaseOrderMoney(CalculatePurchaseOrderMoneyDTO calculatePurchaseOrderMoney) {

        CalculatePurchaseOrderMoneyBO calculatePurchaseOrderMoneyBO = new CalculatePurchaseOrderMoneyBO();
        List<CalculatePurchaseOrderItemMoneyBO> calculatePurchaseOrderItemMoney = new ArrayList<>();
        calculatePurchaseOrderMoneyBO.setCalculatePurchaseOrderItemMoney(calculatePurchaseOrderItemMoney);
        /**
         *
         * 计算个商品的总金额
         */
        BigDecimal allItemTotalMoney = new BigDecimal(0);
        List<CalculatePurchaseOrderItemMoneyDTO> purchaseOrderItemMoneyList = calculatePurchaseOrderMoney.getPurchaseOrderItemMoneyList();
        for (CalculatePurchaseOrderItemMoneyDTO purchaseOrderItemMoney : purchaseOrderItemMoneyList) {
            //计算商品项目总金额
            BigDecimal totalMoney = calculateItemTotalMoney(purchaseOrderItemMoney);
            allItemTotalMoney = BigDecimalUtil.add(allItemTotalMoney, totalMoney);
            //
            CalculatePurchaseOrderItemMoneyBO calculatePurchaseOrderItemMoneyBO = new CalculatePurchaseOrderItemMoneyBO();
            calculatePurchaseOrderItemMoneyBO.setSkuCode(purchaseOrderItemMoney.getSkuCode());
            calculatePurchaseOrderItemMoneyBO.setItemTotalMoney(totalMoney);
            calculatePurchaseOrderItemMoney.add(calculatePurchaseOrderItemMoneyBO);
        }

        /**
         * 计算总金额
         */
        //总计金额
        BigDecimal totalMoney = new BigDecimal(0);

        //运费+其他费用+商品项目总金额
        totalMoney = BigDecimalUtil.add(totalMoney, calculatePurchaseOrderMoney.getFreight()).
                add(calculatePurchaseOrderMoney.getOtherFreight()).add(allItemTotalMoney);
        //赋值
        calculatePurchaseOrderMoneyBO.setPurchaseOrderTotalMoney(totalMoney);
        calculatePurchaseOrderMoneyBO.setAllItemTotalMoney(allItemTotalMoney);

        return calculatePurchaseOrderMoneyBO;
    }

    @Override
    public BigDecimal calculateItemTotalMoney(CalculatePurchaseOrderItemMoneyDTO purchaseOrderItemMoney) {


        //数量x销售单价
        BigDecimal totalMoney = BigDecimalUtil.multiply(new BigDecimal(purchaseOrderItemMoney.getQuantity()),
                purchaseOrderItemMoney.getSalesUnitPrice());

        return totalMoney;
    }


}
