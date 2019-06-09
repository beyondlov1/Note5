package com.beyond.note5.bean;

import java.util.Date;

public interface Tracable extends Element{
    Date getCreateTime();

    void setCreateTime(Date createTime);

    Date getLastModifyTime();

    void setLastModifyTime(Date lastModifyTime);

    Integer getVersion();

    void setVersion(Integer version);
}
