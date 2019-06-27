package com.beyond.note5.sync.model.impl;

import com.alibaba.fastjson.JSONObject;
import com.beyond.note5.sync.model.LMTSharedSource;
import com.beyond.note5.sync.webdav.client.SardineDavClient;
import com.beyond.note5.utils.OkWebDavUtil;
import com.thegrizzlylabs.sardineandroid.DavResource;

import org.apache.commons.lang3.time.DateUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Deprecated
public class SelfDavSharedLMT implements LMTSharedSource {

    private SardineDavClient client;

    private String dirUrl;

    public SelfDavSharedLMT(SardineDavClient client, String dirUrl) {
        this.client = client;
        this.dirUrl = dirUrl;
    }

    @Override
    public Date get() throws IOException {
        List<DavResource> davResources = client.listAllFileResource(dirUrl);
        if (davResources.isEmpty()){
            return new Date(0);
        }
        Collections.sort(davResources, new Comparator<DavResource>() {
            @Override
            public int compare(DavResource o1, DavResource o2) {
                if (o1.getModified().before(o2.getModified())){
                    return -1;
                }else if (DateUtils.isSameInstant(o1.getModified(),o2.getModified())){
                    return 0;
                }else {
                    return 1;
                }
            }
        });
        String rootUrl = OkWebDavUtil.getRootUrl(dirUrl);
        String json = client.get(rootUrl + davResources.get(davResources.size() - 1).getPath());
        JSONObject jsonObject = JSONObject.parseObject(json);
        return jsonObject.getDate("lastModifyTime");
    }

    @Override
    public void set(Date date) throws IOException {
        // do nothing
    }
}
