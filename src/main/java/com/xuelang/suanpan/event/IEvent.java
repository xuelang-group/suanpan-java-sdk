package com.xuelang.suanpan.event;

import com.xuelang.suanpan.common.entities.enums.EventLevel;

public interface IEvent {
    void notify(EventLevel eventLevel, String title, String message);
}
