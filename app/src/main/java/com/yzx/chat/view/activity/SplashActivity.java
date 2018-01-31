package com.yzx.chat.view.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.contract.SplashContract;
import com.yzx.chat.presenter.SplashPresenter;


public class SplashActivity extends BaseCompatActivity<SplashContract.Presenter> implements SplashContract.View {

    private static int PERMISSIONS_REQUEST_CODE = 1;

    public static final String INTENT_EXTRA_LOGGED = "isAlreadyLogged";

    @Override
    protected int getLayoutID() {
        return 0;
    }

    @Override
    protected void init() {

    }

    @Override
    protected void setup() {
        requestPermissionsInCompatMode(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
    }


    @Override
    public void onRequestPermissionsSuccess(int requestCode) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            mPresenter.init(getIntent().getBooleanExtra(INTENT_EXTRA_LOGGED,false));
        }
    }

    @Override
    public SplashContract.Presenter getPresenter() {
        return new SplashPresenter();
    }

    @Override
    public void startLoginActivity() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    public void startHomeActivity() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void error(String error) {

    }
}
