package com.beyond.note5.sync.model.impl;

import com.beyond.note5.sync.webdav.CommonTest;
import com.beyond.note5.sync.webdav.client.SardineDavClient;

import org.junit.Test;

import java.io.IOException;
import java.util.Date;

public class SelfLSTDavModelTest {

    @Test
    public void getLastSyncTime() throws IOException {
        SelfDavSharedLMT selfLSTDavModel = new SelfDavSharedLMT((SardineDavClient) CommonTest.getClient(),CommonTest.getRootUrl());
        Date lastSyncTime = selfLSTDavModel.get();
        System.out.println(lastSyncTime);
    }
}