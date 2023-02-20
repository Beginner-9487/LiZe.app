package com.example.lize_app.ui.base;

/**
 *
 */
public interface Presenter<V extends MvpView> {
    void attachView(V mvpView);

    void detachView();
}
