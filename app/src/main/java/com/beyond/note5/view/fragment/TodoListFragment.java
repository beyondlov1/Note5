package com.beyond.note5.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.beyond.note5.MyApplication;
import com.beyond.note5.R;
import com.beyond.note5.bean.Document;
import com.beyond.note5.bean.Todo;
import com.beyond.note5.dao.DaoSession;
import com.beyond.note5.dao.DocumentDao;
import com.beyond.note5.view.adapter.DocumentRecyclerViewAdapter;

import java.util.List;

/**
 * Created by beyond on 2019/1/30.
 */

public class TodoListFragment extends Fragment  {

    private List<Document> data;

    private RecyclerView todoRecyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup viewGroup=(ViewGroup) inflater.inflate(R.layout.fragment_todo_list,container,false);
        showTodoList(viewGroup);

        return viewGroup;
    }

    private void showTodoList(ViewGroup viewGroup) {
        todoRecyclerView = viewGroup.findViewById(R.id.todo_recycler_view);
        data = getData();
        DocumentRecyclerViewAdapter documentRecyclerViewAdapter = new DocumentRecyclerViewAdapter(this.getContext(),data);
        todoRecyclerView.setAdapter(documentRecyclerViewAdapter);

        //设置显示格式
        final StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        todoRecyclerView.setLayoutManager(staggeredGridLayoutManager);
    }

    private List<Document> getData() {
        DaoSession daoSession = ((MyApplication) getActivity().getApplication()).getDaoSession();
        DocumentDao documentDao = daoSession.getDocumentDao();
        return documentDao.queryBuilder().where(DocumentDao.Properties.Type.eq(Document.TODO)).orderDesc(DocumentDao.Properties.LastModifyTime).list();
    }

    public void onNoteReceived(Todo todo){
        if (getActivity() == null){
            return;
        }
        DaoSession daoSession = ((MyApplication) getActivity().getApplication()).getDaoSession();
        DocumentDao documentDao = daoSession.getDocumentDao();
        documentDao.insert(todo);
        data.add(0,todo);
        todoRecyclerView.getAdapter().notifyItemInserted(0);
    }
}
