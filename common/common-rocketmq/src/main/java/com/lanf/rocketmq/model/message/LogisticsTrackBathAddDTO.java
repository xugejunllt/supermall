package com.lanf.rocketmq.model.message;

import lombok.Data;

import java.util.List;

@Data
public class LogisticsTrackBathAddDTO  {

    private List<LogisticsTrackAddDTO> addDTOList;

}
