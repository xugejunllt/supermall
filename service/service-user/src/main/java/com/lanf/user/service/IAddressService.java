package com.lanf.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.user.model.dto.AddAddressDTO;
import com.lanf.user.model.dto.SetDefaultAddressDTO;
import com.lanf.user.model.entity.AddressDO;
import com.lanf.user.model.vo.AddressListVO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-10
 */
public interface IAddressService extends IService<AddressDO> {

    void  addAddress(AddAddressDTO dto);

    AddressDO getDefaultAddress();


    List<AddressListVO> addressListQuery();

    void  setDefaultAddress(SetDefaultAddressDTO dto );


}
