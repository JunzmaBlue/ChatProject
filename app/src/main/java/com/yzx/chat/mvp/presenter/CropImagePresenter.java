package com.yzx.chat.mvp.presenter;

import android.graphics.Bitmap;
import android.os.Handler;
import android.text.TextUtils;

import com.yzx.chat.R;
import com.yzx.chat.mvp.contract.CropImageContract;
import com.yzx.chat.network.api.user.UploadAvatarBean;
import com.yzx.chat.network.chat.IMClient;
import com.yzx.chat.network.chat.ResultCallback;
import com.yzx.chat.tool.DirectoryHelper;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.util.AsyncUtil;
import com.yzx.chat.util.BitmapUtil;
import com.yzx.chat.util.BackstageAsyncTask;

import java.util.UUID;

/**
 * Created by YZX on 2018年03月20日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class CropImagePresenter implements CropImageContract.Presenter {

    private CropImageContract.View mCropImageView;
    private Handler mHandler;
    private SaveAvatarToLocalTask mSaveAvatarToLocalTask;

    @Override
    public void attachView(CropImageContract.View view) {
        mCropImageView = view;
        mHandler = new Handler();
    }

    @Override
    public void detachView() {
        AsyncUtil.cancelTask(mSaveAvatarToLocalTask);
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        mCropImageView = null;
    }

    @Override
    public void uploadAvatar(Bitmap bitmap) {
        AsyncUtil.cancelTask(mSaveAvatarToLocalTask);
        mSaveAvatarToLocalTask = new SaveAvatarToLocalTask(this);
        mSaveAvatarToLocalTask.execute(bitmap);
    }

    private void saveComplete(String imagePath) {
        IMClient.getInstance().getUserManager().uploadAvatar(imagePath, new ResultCallback<UploadAvatarBean>() {
            @Override
            public void onSuccess(UploadAvatarBean result) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mCropImageView.goBack();
                    }
                });
            }

            @Override
            public void onFailure(final String error) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mCropImageView.showError(error);
                    }
                });

            }
        });
    }

    private void saveFail() {
        mCropImageView.showError(AndroidUtil.getString(R.string.CropImageActivity_SaveAvatarFail));
    }

    private static class SaveAvatarToLocalTask extends BackstageAsyncTask<CropImagePresenter, Bitmap, String> {

        SaveAvatarToLocalTask(CropImagePresenter lifeCycleDependence) {
            super(lifeCycleDependence);
        }

        @Override
        protected String doInBackground(Bitmap... params) {
            Bitmap bitmap = params[0];
            String savePath = BitmapUtil.saveBitmapToPNG(bitmap, DirectoryHelper.getUserImagePath(), UUID.randomUUID().toString());
            bitmap.recycle();
            return savePath;
        }

        @Override
        protected void onPostExecute(String result, CropImagePresenter lifeDependentObject) {
            super.onPostExecute(result, lifeDependentObject);
            if (!TextUtils.isEmpty(result)) {
                lifeDependentObject.saveComplete(result);
            } else {
                lifeDependentObject.saveFail();
            }
        }
    }
}
