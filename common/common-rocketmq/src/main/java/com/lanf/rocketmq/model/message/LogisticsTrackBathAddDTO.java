package com.lanf.rocketmq.model.message;

import com.lanf.messagemanager.client.base.BaseMessage;
import lombok.Data;

import java.util.List;

@Data
public class LogisticsTrackBathAddDTO extends BaseMessage {

    private List<LogisticsTrackAddDTO> addDTOList;

}
