package com.beyond.note5.module;

import com.beyond.note5.presenter.DocumentCompositePresenter;
import com.beyond.note5.presenter.DocumentCompositePresenterImpl;
import com.beyond.note5.view.DocumentCompositeView;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class DocumentCompositeModule {
    private DocumentCompositeView documentCompositeView;

    @Inject
    public DocumentCompositeModule(DocumentCompositeView documentCompositeView){
        this.documentCompositeView = documentCompositeView;
    }

    @Singleton
    @Provides
    DocumentCompositePresenter provideDocumentPresenterComposite(){
        return new DocumentCompositePresenterImpl(documentCompositeView);
    }
}
