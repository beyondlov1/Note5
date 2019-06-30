package com.beyond.note5.model;

import com.beyond.note5.MyApplication;
import com.beyond.note5.bean.Account;
import com.beyond.note5.model.dao.AccountDao;

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

public class AccountModelImpl implements AccountModel {

    private AccountDao accountDao;

    public AccountModelImpl() {
        this.accountDao = MyApplication.getInstance().getDaoSession().getAccountDao();
    }

    @Override
    public void add(Account account) {
        List<Account> list = accountDao.queryBuilder()
                .where(AccountDao.Properties.Server.eq(account.getServer()))
                .where(AccountDao.Properties.Username.eq(account.getUsername()))
                .list();
        if (CollectionUtils.isEmpty(list)){
            accountDao.insert(account);
        }
    }

    @Override
    public void update(Account account) {
        accountDao.update(account);
    }

    @Override
    public void delete(Account account) {
        accountDao.delete(account);
    }

    @Override
    public List<Account> findAll() {
        return accountDao.loadAll();
    }

    @Override
    public List<Account> selectByServer(String server) {
        return accountDao.queryBuilder()
                .where(AccountDao.Properties.Server.eq(server))
                .list();
    }

    @Override
    public List<Account> findAllValid() {
        return accountDao.queryBuilder()
                .where(AccountDao.Properties.Valid.eq(true))
                .list();
    }
}
