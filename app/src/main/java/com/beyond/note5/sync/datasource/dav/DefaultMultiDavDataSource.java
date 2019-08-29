package com.beyond.note5.sync.datasource.dav;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.beyond.note5.bean.Tracable;
import com.beyond.note5.sync.datasource.MultiDataSource;
import com.beyond.note5.sync.datasource.entity.SyncStamp;
import com.beyond.note5.sync.utils.SyncUtils;
import com.beyond.note5.sync.webdav.client.PrefixDavFilter;
import com.beyond.note5.utils.OkWebDavUtil;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static com.beyond.note5.MyApplication.DAV_STAMP_DIR;

/**
 * @author: beyond
 * @date: 2019/8/9
 */

public class DefaultMultiDavDataSource<T extends Tracable> extends DefaultPointDavDataSource<T> implements MultiDataSource<T> {

    private String chosenKey;

    private Map<String, SyncStamp> syncStamps = new LinkedHashMap<>();

    protected ExecutorService executorService;

    public DefaultMultiDavDataSource(DavDataSourceProperty property, Class<T> clazz, ExecutorService executorService) {
        super(property, clazz);
        this.executorService = executorService;
    }


    @Override
    public void setChosenKey(String key) {
        chosenKey = key;
    }

    @Override
    public String getChosenKey() {
        return chosenKey;
    }

    @Override
    public void initLastSyncStamps() throws IOException {
        String prefix = property.getBaseSyncStampFilePrefix();
        List<String> urls = getClient().listAllFileUrl(
                OkWebDavUtil.concat(getServer(), clazz.getSimpleName().toUpperCase(), DAV_STAMP_DIR),
                new PrefixDavFilter(prefix));
        if (urls.isEmpty()){
            syncStamps.clear();
            chosenKey = null;
        }
        SyncUtils.blockExecute(executorService,
                new SyncUtils.ParamCallable<String, SyncStamp>() {
                    @Override
                    public SyncStamp call(String url) throws Exception {
                        return decode(getClient().get(url));
                    }
                }, new SyncUtils.Handler<String, SyncStamp>() {
                    @Override
                    public void handle(String param, SyncStamp result) throws IOException {
                        String key = StringUtils.substring(param, param.indexOf(prefix)+11,
                                param.lastIndexOf("."));
                        key = SyncUtils.base64Decode(key);
                        syncStamps.put(key,result);
                    }
                }, null, urls);
    }

    @Override
    public Map<String, SyncStamp> getSyncStampsCache() {
        return syncStamps;
    }

    @Override
    public void setSyncStampsCache(Map<String, SyncStamp> syncStamps) {
        this.syncStamps = syncStamps;
    }

    private SyncStamp decode(String target) {
        if (target == null) {
            return null;
        }
        try {
            return JSONObject.parseObject(target, SyncStamp.class);
        } catch (JSONException e) {
            return null;
        }
    }
}
