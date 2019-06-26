package com.beyond.note5.sync.model.impl;

import com.beyond.note5.sync.webdav.CommonTest;
import com.beyond.note5.sync.webdav.client.SardineDavClient;

import org.junit.Test;

import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.*;

public class SelfLSTDavModelTest {

    @Test
    public void getLastSyncTime() throws IOException {
        SelfLSTDavModel selfLSTDavModel = new SelfLSTDavModel((SardineDavClient) CommonTest.getClient(),CommonTest.getRootUrl());
        Date lastSyncTime = selfLSTDavModel.getLastSyncTime();
        System.out.println(lastSyncTime);
    }
}