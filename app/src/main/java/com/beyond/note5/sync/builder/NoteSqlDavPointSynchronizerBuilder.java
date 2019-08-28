package com.beyond.note5.sync.builder;

import com.beyond.note5.bean.Account;
import com.beyond.note5.bean.Note;
import com.beyond.note5.sync.datasource.DataSource;
import com.beyond.note5.sync.datasource.dav.DavDataSourceProperty;
import com.beyond.note5.sync.datasource.dav.NotePointDavDataSource;
import com.beyond.note5.sync.datasource.sql.NoteSqlDataSourceWrap;
import com.beyond.note5.utils.OkWebDavUtil;

import static com.beyond.note5.MyApplication.DAV_ROOT_DIR;

/**
 * @author: beyond
 * @date: 2019/8/3
 */

public class NoteSqlDavPointSynchronizerBuilder extends AbstractPointSynchronizerBuilder<Note> {


    public NoteSqlDavPointSynchronizerBuilder(Account account) {
        super(account);
    }

    @Override
    public DataSource<Note> getDataSource1() {
        return new NoteSqlDataSourceWrap();
    }

    /**
     private String username;
     private String password;
     private String server;
     private String lockPath;
     private String syncStampPath;
     private String dataPath;
     private boolean needExecutorService;
     private String latestSyncStampFileName;
     private String baseSyncStampFilePrefix;
     */
    @Override
    public DataSource<Note> getDataSource2() {

        DavDataSourceProperty property = new DavDataSourceProperty(
                account.getUsername(),
                account.getPassword(),
                OkWebDavUtil.concat(account.getServer(), DAV_ROOT_DIR)
        );

        boolean needExecutor =false;
        if (!account.getServer().contains("jianguoyun")){
            needExecutor = true;
        }
        property.setNeedExecutorService(needExecutor);

        return new NotePointDavDataSource(property,Note.class);
    }
}
