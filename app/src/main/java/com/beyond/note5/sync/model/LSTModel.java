package com.beyond.note5.sync.model;

import java.io.IOException;
import java.util.Date;

public interface LSTModel {
    Date getLastSyncTime() throws IOException;
    void setLastSyncTime(Date date) throws IOException;
}
