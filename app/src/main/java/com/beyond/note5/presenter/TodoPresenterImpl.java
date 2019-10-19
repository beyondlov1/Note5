package com.beyond.note5.presenter;

import android.support.annotation.Nullable;

import com.beyond.note5.bean.Todo;
import com.beyond.note5.event.AddTodoAllSuccessEvent;
import com.beyond.note5.event.UpdateTodoAllSuccessEvent;
import com.beyond.note5.event.todo.AddTodoSuccessEvent;
import com.beyond.note5.event.todo.DeleteTodoSuccessEvent;
import com.beyond.note5.event.todo.UpdateTodoSuccessEvent;
import com.beyond.note5.model.TodoModel;
import com.beyond.note5.model.TodoModelImpl;
import com.beyond.note5.utils.TimeNLPUtil;
import com.beyond.note5.view.TodoView;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public class TodoPresenterImpl implements TodoPresenter {
    private TodoView todoView;
    private TodoModel todoModel;

    public TodoPresenterImpl(@Nullable TodoView todoView) {
        this.todoView = todoView;
        this.todoModel = TodoModelImpl.getSingletonInstance();
    }

    @Override
    public void add(Todo todo) {
        try {
            fillContentWithoutTime(todo);
            todoModel.add(todo);
            this.addSuccess(todo);
        } catch (Exception e) {
            e.printStackTrace();
            this.addFail(todo);
        }
    }

    @Override
    public void addSuccess(Todo todo) {
        if (todoView == null) {
            return;
        }
        EventBus.getDefault().post(new AddTodoSuccessEvent(todo));
        todoView.onAddSuccess(todo);
    }

    @Override
    public void addFail(Todo todo) {
        if (todoView == null) {
            return;
        }
        todoView.onAddFail(todo);
    }

    @Override
    public void update(Todo todo) {
        try {
            todo.setVersion((todo.getVersion() == null?0:todo.getVersion())+1);
            fillContentWithoutTime(todo);
            todoModel.update(todo);
            this.updateSuccess(todo);
        } catch (Exception e) {
            e.printStackTrace();
            this.updateFail(todo);
        }
    }

    @Override
    public void updateSuccess(Todo todo) {
        if (todoView == null) {
            return;
        }
        EventBus.getDefault().post(new UpdateTodoSuccessEvent(todo));
        todoView.onUpdateSuccess(todo);
    }

    @Override
    public void updateFail(Todo todo) {
        if (todoView == null) {
            return;
        }
        todoView.onUpdateFail(todo);
    }

    @Override
    public void addAllForSync(List<Todo> addList, String[] oppositeKeys) {
        try {
            todoModel.addAll(addList);
            addAllSuccess(addList);
        }catch (Exception e){
            e.printStackTrace();
            addAllFail(e);
        }
    }

    @Override
    public void addAllForSync(List<Todo> addList, String source) {
        try {
            todoModel.addAll(addList,source);
            addAllSuccess(addList);
        }catch (Exception e){
            e.printStackTrace();
            addAllFail(e);
        }
    }

    private void addAllSuccess(List<Todo> addList) {
        if (todoView == null) {
            return;
        }
        EventBus.getDefault().post(new AddTodoAllSuccessEvent(addList));
        todoView.onAddAllSuccess(addList);
    }

    private void addAllFail(Exception e) {
        if (todoView == null) {
            return;
        }
        todoView.onAddAllFail(e);
    }

    @Override
    public void updateAllForSync(List<Todo> updateList, String[] oppositeKeys) {
        try {
            todoModel.updateAll(updateList);
            updateAllSuccess(updateList);
        }catch (Exception e){
            e.printStackTrace();
            updateAllFail(e);
        }
    }

    @Override
    public void updateAllForSync(List<Todo> updateList, String source) {
        try {
            todoModel.updateAll(updateList,source);
            updateAllSuccess(updateList);
        }catch (Exception e){
            e.printStackTrace();
            updateAllFail(e);
        }
    }

    private void updateAllSuccess(List<Todo> updateList) {
        if (todoView == null) {
            return;
        }
        EventBus.getDefault().post(new UpdateTodoAllSuccessEvent(updateList));
        todoView.onUpdateAllSuccess(updateList);
    }

    private void updateAllFail(Exception e) {
        if (todoView == null) {
            return;
        }
        todoView.onUpdateAllFail(e);
    }

    @Override
    public void deleteLogic(Todo todo) {
        try {
            todo.setVersion((todo.getVersion() == null?0:todo.getVersion())+1);
            todoModel.deleteLogic(todo);
            this.deleteSuccess(todo);
        } catch (Exception e) {
            e.printStackTrace();
            this.deleteFail(todo);
        }
    }

    @Override
    public void updatePriority(Todo todo) {
        try {
            todo.setVersion((todo.getVersion() == null?0:todo.getVersion())+1);
            todoModel.update(todo);
            this.updatePrioritySuccess(todo);
        } catch (Exception e) {
            e.printStackTrace();
            this.updatePriorityFail(todo);
        }
    }

    @Override
    public void updatePrioritySuccess(Todo document) {
        if (todoView == null) {
            return;
        }
        todoView.onUpdatePrioritySuccess(document);
    }

    @Override
    public void updatePriorityFail(Todo document) {
        if (todoView == null) {
            return;
        }
        todoView.onUpdatePriorityFail(document);
    }

    @Override
    public void delete(Todo todo) {
        try {
            todoModel.delete(todo);
            this.deleteSuccess(todo);
        } catch (Exception e) {
            e.printStackTrace();
            this.deleteFail(todo);
        }
    }

    @Override
    public void deleteSuccess(Todo todo) {
        if (todoView == null) {
            return;
        }
        EventBus.getDefault().post(new DeleteTodoSuccessEvent(todo));
        todoView.onDeleteSuccess(todo);
    }

    @Override
    public void deleteFail(Todo todo) {
        if (todoView == null) {
            return;
        }
        todoView.onDeleteFail(todo);
    }

    @Override
    public List<Todo> selectAllInAll() {
        return todoModel.findAllInAll();
    }

    @Override
    public Todo selectById(String id) {
        return todoModel.findById(id);
    }

    @Override
    public void findAll() {
        try {
            List<Todo> allTodo = todoModel.findAll();
            this.findAllSuccess(allTodo);
        } catch (Exception e) {
            e.printStackTrace();
            this.findAllFail();
        }
    }

    @Override
    public void findAllSuccess(List<Todo> allTodo) {
        if (todoView == null) {
            return;
        }
        todoView.onFindAllSuccess(allTodo);
    }

    @Override
    public void findAllFail() {
        if (todoView == null) {
            return;
        }
        todoView.onFindAllFail();
    }

    @Override
    public List<Todo> selectAllAfterLastModifyTime(Date date) {
        return todoModel.findAllAfterLastModifyTime(date);
    }

    @Override
    public List<Todo> selectByIds(Collection<String> ids) {
        return todoModel.findByIds(ids);
    }

    @Override
    public void deleteReminder(Todo todo) {
        try {
            todoModel.deleteReminder(todo);
            this.onDeleteReminderSuccess(todo);
        } catch (Exception e) {
            e.printStackTrace();
            this.onDeleteReminderFail(todo);
        }
    }

    @Override
    public void onDeleteReminderSuccess(Todo todo) {
        if (todoView == null) {
            return;
        }
        todoView.onDeleteReminderSuccess(todo);
    }

    @Override
    public void onDeleteReminderFail(Todo todo) {
        if (todoView == null) {
            return;
        }
        todoView.onDeleteReminderFail(todo);
    }

    /**
     * 计算无时间内容
     * 要不要改成异步， 看情况吧
     * @param todo 待办
     */
    private void fillContentWithoutTime(Todo todo) {
        String contentWithoutTime = StringUtils.trim(TimeNLPUtil.getOriginExpressionWithoutTime(StringUtils.trim(todo.getContent())));
        if (StringUtils.isBlank(contentWithoutTime)) {
            contentWithoutTime = StringUtils.trim(todo.getContent());
        }
        todo.setContentWithoutTime(contentWithoutTime);
    }


    @Override
    public void search(String searchKey) {
        try {
            List<Todo> todos = todoModel.searchByContent(searchKey);
            searchSuccess(todos);
        } catch (Exception e) {
            e.printStackTrace();
            this.searchFail(e);
        }
    }

    private void searchSuccess(List<Todo> notes) {
        if (todoView!=null){
            todoView.onSearchSuccess(notes);
        }
    }

    private void searchFail(Exception e) {
        if (todoView!=null){
            todoView.onSearchFail(e);
        }
    }
}
