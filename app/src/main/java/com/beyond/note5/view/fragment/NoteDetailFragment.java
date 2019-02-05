package com.beyond.note5.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;

import com.beyond.note5.R;

/**
 * Created by beyond on 2019/2/1.
 */

public class NoteDetailFragment extends AbstractDocumentDetailFragment {

    private ViewGroup displayContainer;
    private WebView displayWebView;
    private Button modifyButton;
    private ViewGroup modifyContainer;
    private EditText titleEditText;
    private EditText contentEditText;
    private Button modifyConfirmButton;
    private Button modifyCancelButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_note_detail, container, false);
        initView(viewGroup);
        initData();
        initEvent();
        return viewGroup;
    }

    void initView(ViewGroup viewGroup) {
        displayContainer = viewGroup.findViewById(R.id.fragment_document_display_container);
        displayWebView = viewGroup.findViewById(R.id.fragment_document_display_web);
        modifyButton = viewGroup.findViewById(R.id.fragment_document_modify_button);
        modifyContainer = viewGroup.findViewById(R.id.fragment_document_modify_container);
        titleEditText = viewGroup.findViewById(R.id.fragment_document_modify_title);
        contentEditText = viewGroup.findViewById(R.id.fragment_document_modify_content);
        modifyConfirmButton = viewGroup.findViewById(R.id.fragment_document_modify_confirm);
        modifyCancelButton = viewGroup.findViewById(R.id.fragment_document_modify_cancel);
    }

    private void initData() {
        displayWebView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                //return false 由webView加载url，return true 由应用代码处理url
                return false;
            }
        });
        displayWebView.loadData("<h2>dafdfadfad</h2>", "text/html","UTF-8");
    }

    private void initEvent() {
        modifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modifyContainer.setVisibility(View.VISIBLE);
                v.setVisibility(View.GONE);
            }
        });
        modifyConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modifyContainer.setVisibility(View.GONE);
                modifyButton.setVisibility(View.VISIBLE);
            }
        });

    }

}
