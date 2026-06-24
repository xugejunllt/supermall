package com.lanf.dynamicsrrefresh.core.listener;

import com.lanf.dynamicsrrefresh.core.handle.EventHandle;

public interface NacosListener {

    String getDataId();

    String getGroup();

    EventHandle getEventHandle();
}
