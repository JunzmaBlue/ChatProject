package com.yzx.chat.mvp.contract;

import android.content.Context;

import com.amap.api.services.core.PoiItem;
import com.yzx.chat.base.BasePresenter;
import com.yzx.chat.base.BaseView;

import java.util.List;

/**
 * Created by YZX on 2018年03月01日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */


public class LocationMapActivityContract {
    public interface View extends BaseView<Presenter> {
        Context getContext();

        void showNewCurrentLocation(List<PoiItem> poiItemList);

        void showMoreCurrentLocation(List<PoiItem> poiItemList, boolean hasMore);

        void showNewSearchLocation(List<PoiItem> poiItemList);

        void showMoreSearchLocation(List<PoiItem> poiItemList, boolean hasMore);

        void showError(String error);
    }


    public interface Presenter extends BasePresenter<View> {

        void searchCurrentLocation(double latitude, double longitude);

        void searchCurrentMoreLocation(double latitude, double longitude);

        void searchPOIByKeyword(String keyword);

        void searchMorePOIByKeyword(String keyword);
    }
}
