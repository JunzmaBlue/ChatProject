package com.yzx.chat.mvp.view.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.bean.ContactOperationBean;
import com.yzx.chat.bean.UserBean;
import com.yzx.chat.mvp.contract.StrangerProfileContract;
import com.yzx.chat.mvp.presenter.StrangerProfilePresenter;
import com.yzx.chat.network.chat.ContactManager;
import com.yzx.chat.util.AndroidUtil;
import com.yzx.chat.util.GlideUtil;
import com.yzx.chat.widget.adapter.CenterCropImagePagerAdapter;
import com.yzx.chat.widget.view.PageIndicator;
import com.yzx.chat.widget.view.ProgressDialog;

import java.util.ArrayList;

/**
 * Created by YZX on 2018年01月29日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */


public class StrangerProfileActivity extends BaseCompatActivity<StrangerProfileContract.Presenter> implements StrangerProfileContract.View {

    public static final String INTENT_EXTRA_USER = "User";
    public static final String INTENT_EXTRA_CONTENT_OPERATION = "ContactOperation";

    private EditText mEtReason;
    private ProgressDialog mProgressDialog;
    private Button mBtnConfirm;
    private ViewPager mVpBanner;
    private PageIndicator mPageIndicator;
    private TextView mTvSignature;
    private TextView mTvNickname;
    private TextView mTvLocationAndAge;
    private ImageView mIvSexIcon;
    private ImageView mIvAvatar;
    private CenterCropImagePagerAdapter mCropImagePagerAdapter;

    private ArrayList<Object> mPicUrlList;

    private UserBean mUser;
    private ContactOperationBean mContactOperationBean;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_stranger_profile;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mEtReason = findViewById(R.id.StrangerProfileActivity_mEtReason);
        mBtnConfirm = findViewById(R.id.StrangerProfileActivity_mBtnConfirm);
        mTvNickname = findViewById(R.id.StrangerProfileActivity_mTvNickname);
        mTvSignature = findViewById(R.id.StrangerProfileActivity_mTvSignature);
        mIvSexIcon = findViewById(R.id.StrangerProfileActivity_mIvSexIcon);
        mIvAvatar = findViewById(R.id.StrangerProfileActivity_mIvAvatar);
        mTvLocationAndAge = findViewById(R.id.StrangerProfileActivity_mTvLocationAndAge);
        mPageIndicator = findViewById(R.id.StrangerProfileActivity_mPageIndicator);
        mVpBanner = findViewById(R.id.StrangerProfileActivity_mVpBanner);
        mProgressDialog = new ProgressDialog(this, getString(R.string.ProgressHint_Send));
        mPicUrlList = new ArrayList<>(6);
        mCropImagePagerAdapter = new CenterCropImagePagerAdapter(mPicUrlList);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        setSystemUiMode(SYSTEM_UI_MODE_TRANSPARENT_BAR_STATUS);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            setTitle(null);
        }

        fillTestData();

        mPageIndicator.setIndicatorColorSelected(Color.WHITE);
        mPageIndicator.setIndicatorColorUnselected(ContextCompat.getColor(this, R.color.backgroundColorWhiteLight));
        mPageIndicator.setIndicatorRadius((int) AndroidUtil.dip2px(3));
        mPageIndicator.setupWithViewPager(mVpBanner);

        mVpBanner.setAdapter(mCropImagePagerAdapter);

        mBtnConfirm.setOnClickListener(mOnConfirmClickListener);


        setData();
    }

    private void fillTestData() {
        mPicUrlList.add(R.drawable.temp_image_1);
        mPicUrlList.add(R.drawable.temp_image_2);
        mPicUrlList.add(R.drawable.temp_image_3);
    }


    private void setData() {
        mUser = getIntent().getParcelableExtra(INTENT_EXTRA_USER);
        mContactOperationBean = getIntent().getParcelableExtra(INTENT_EXTRA_CONTENT_OPERATION);
        if (mUser == null && mContactOperationBean == null) {
            return;
        }

        if (mContactOperationBean != null) {
            mUser = mContactOperationBean.getUser();
            boolean isOperable = true;
            switch (mContactOperationBean.getType()) {
                case ContactManager.CONTACT_OPERATION_REQUEST_ACTIVE:
                    mBtnConfirm.setText(R.string.ContactMessageAdapter_Verifying);
                    isOperable = false;
                    break;
                case ContactManager.CONTACT_OPERATION_REJECT_ACTIVE:
                    mBtnConfirm.setText(R.string.ContactMessageAdapter_AlreadyRefused);
                    isOperable = false;
                    break;
                case ContactManager.CONTACT_OPERATION_REQUEST:
                    mBtnConfirm.setText(R.string.ContactMessageAdapter_Requesting);
                    break;
                case ContactManager.CONTACT_OPERATION_REJECT:
                    mBtnConfirm.setText(R.string.ContactMessageAdapter_Disagree);
                    isOperable = false;
                    break;
                case ContactManager.CONTACT_OPERATION_ACCEPT_ACTIVE:
                case ContactManager.CONTACT_OPERATION_DELETE:
                    mBtnConfirm.setText(R.string.ContactMessageAdapter_Added);
                    isOperable = false;
                    break;
                default:
                    finish();
                    return;
            }

            mEtReason.setEnabled(false);
            mBtnConfirm.setEnabled(isOperable);
            if(TextUtils.isEmpty(mContactOperationBean.getReason())){
                mEtReason.setText(R.string.StrangerProfileActivity_DefaultReason);
            }else {
                mEtReason.setText(mContactOperationBean.getReason());
            }
        } else {
            mBtnConfirm.setText(R.string.StrangerProfileActivity_RequestAddContact);
        }

        mTvNickname.setText(mUser.getNickname());
        mIvSexIcon.setSelected(mUser.getSex() == UserBean.SEX_WOMAN);
        StringBuilder locationAndAge = new StringBuilder();
        locationAndAge.append(mUser.getAge());
        if (!TextUtils.isEmpty(mUser.getLocation())) {
            locationAndAge.append(" · ").append(mUser.getLocation());
        }
        mTvLocationAndAge.setText(locationAndAge.toString());
        if (TextUtils.isEmpty(mUser.getSignature())) {
            mTvSignature.setText(null);
            mTvSignature.setVisibility(View.GONE);
        } else {
            mTvSignature.setText(mUser.getSignature());
            mTvSignature.setVisibility(View.VISIBLE);
        }
        GlideUtil.loadAvatarFromUrl(this, mIvAvatar, mUser.getAvatar());

    }

    private final View.OnClickListener mOnConfirmClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mContactOperationBean != null) {
                mPresenter.acceptContactRequest(mContactOperationBean);
            } else if (mUser != null) {
                mPresenter.requestContact(mUser, mEtReason.getText().toString());
            }
        }
    };

    @Override
    public StrangerProfileContract.Presenter getPresenter() {
        return new StrangerProfilePresenter();
    }

    @Override
    public void goBack() {
        finish();
    }

    @Override
    public void showError(String error) {
        showToast(error);
    }

    @Override
    public void setEnableProgressDialog(boolean isEnable) {
        if (isEnable) {
            mProgressDialog.show();
        } else {
            mProgressDialog.dismiss();
        }
    }
}
