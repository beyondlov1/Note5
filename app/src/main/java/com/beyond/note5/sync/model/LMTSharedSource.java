package com.beyond.note5.sync.model;

import java.io.IOException;
import java.util.Date;

@Deprecated
public interface LMTSharedSource extends SharedSource<Date>{
    Date get() throws IOException;
    void set(Date date) throws IOException;
}
