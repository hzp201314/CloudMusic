package com.example.cloudmusic.view.login.presenter;

import com.example.cloudmusic.api.MockData;
import com.example.cloudmusic.api.RequestCenter;
import com.example.cloudmusic.model.login.LoginEvent;
import com.example.cloudmusic.model.user.User;
import com.example.cloudmusic.utils.UserManager;
import com.example.cloudmusic.view.login.inter.IUserLoginPresenter;
import com.example.cloudmusic.view.login.inter.IUserLoginView;
import com.example.lib_network.listener.DisposeDataListener;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

/**
 * 登陆页面对应Presenter
 */
public class UserLoginPresenter implements IUserLoginPresenter, DisposeDataListener {
    private IUserLoginView mIView;

    public UserLoginPresenter(IUserLoginView iView) {
        mIView = iView;
    }

    @Override
    public void login(String username, String password) {
        mIView.showLoadingView();
        RequestCenter.login( this);
    }

    @Override
    public void onSuccess(Object responseObj) {
        mIView.hideLoadingView();
        User user = (User) responseObj;
        UserManager.getInstance().setUser(user);
        //发送登陆Event
        EventBus.getDefault().post(new LoginEvent());
        mIView.finishActivity();
    }

    @Override
    public void onFailure(Object responseObj) {
        mIView.hideLoadingView();
        onSuccess(new Gson().fromJson(MockData.LOGIN_DATA, User.class));
        mIView.showLoginFailedView();
    }
}
