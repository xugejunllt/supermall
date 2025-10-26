package com.lanf.rocketmq.util;

import com.lanf.rocketmq.model.message.LogisticsTrackAddDTO;
import com.lanf.rocketmq.model.message.LogisticsTrackBathAddDTO;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MessageBuildAdapter {

    public  static LogisticsTrackBathAddDTO buildLogisticsTrackAddDTO(Long bizOrderId,String finishContent,Integer status){

        LogisticsTrackBathAddDTO logisticsTrackBathAddDTO = new LogisticsTrackBathAddDTO();
        List<LogisticsTrackAddDTO> addDTOList = new ArrayList<>();
        Date date = new Date();
        LogisticsTrackAddDTO logisticsTrackAddDTO = new LogisticsTrackAddDTO();
        logisticsTrackAddDTO.setOrderId(bizOrderId);
        logisticsTrackAddDTO.setStatus(status);
        logisticsTrackAddDTO.setFinishTime(date);
        logisticsTrackAddDTO.setFinishContent(finishContent);
        addDTOList.add(logisticsTrackAddDTO);
        logisticsTrackBathAddDTO.setAddDTOList(addDTOList);

        return  logisticsTrackBathAddDTO;
    }
    public  static LogisticsTrackBathAddDTO buildLogisticsTrackAddDTO(List<Long> bizOrderId,String finishContent,Integer status){

        LogisticsTrackBathAddDTO logisticsTrackBathAddDTO = new LogisticsTrackBathAddDTO();
        List<LogisticsTrackAddDTO> addDTOList = new ArrayList<>();
        Date date = new Date();
        bizOrderId.forEach(a->{
            LogisticsTrackAddDTO logisticsTrackAddDTO = new LogisticsTrackAddDTO();
            logisticsTrackAddDTO.setOrderId(a);
            logisticsTrackAddDTO.setStatus(status);
            logisticsTrackAddDTO.setFinishTime(date);
            logisticsTrackAddDTO.setFinishContent(finishContent);
            addDTOList.add(logisticsTrackAddDTO);

        });
        logisticsTrackBathAddDTO.setAddDTOList(addDTOList);
        return  logisticsTrackBathAddDTO;
    }
}
