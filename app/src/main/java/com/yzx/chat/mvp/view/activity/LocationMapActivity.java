package com.yzx.chat.mvp.view.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.constraint.ConstraintLayout;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.UiSettings;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.yzx.chat.R;
import com.yzx.chat.base.BaseCompatActivity;
import com.yzx.chat.base.BaseRecyclerViewAdapter;
import com.yzx.chat.configure.Constants;
import com.yzx.chat.mvp.contract.LocationMapActivityContract;
import com.yzx.chat.mvp.presenter.LocationMapActivityPresenter;
import com.yzx.chat.widget.adapter.LocationAdapter;
import com.yzx.chat.widget.listener.OnRecyclerViewItemClickListener;
import com.yzx.chat.widget.view.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZX on 2018年02月28日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */

public class LocationMapActivity extends BaseCompatActivity<LocationMapActivityContract.Presenter> implements LocationMapActivityContract.View {

    public static final int RESULT_CODE = LocationMapActivity.class.hashCode();
    public static final String INTENT_EXTRA_POI = "POI";

    private static final int MODE_SEND = 0;
    private static final int MODE_SHARE = 1;

    private static final int DEFAULT_ZOOM = 15;

    private MapView mMapView;
    private SearchView mSearchView;
    private MenuItem mSendMenuItem;
    private View mSearchLocationFooterView;
    private View mCurrentLocationFooterView;
    private TextView mTvSearchLocationLoadMoreHint;
    private TextView mTvCurrentLocationLoadMoreHint;
    private TextView mTvShareLocationTitle;
    private TextView mTvShareLocationContent;
    private RecyclerView mRvSearchLocation;
    private RecyclerView mRvCurrentLocation;
    private FrameLayout mFlSearchLocationLayout;
    private FrameLayout mFlCurrentLocationLayout;
    private ProgressBar mPbSearchLocation;
    private ProgressBar mPbCurrentLocation;
    private ConstraintLayout mClShareLayout;
    private LocationAdapter mCurrentLocationAdapter;
    private LocationAdapter mSearchLocationAdapter;
    private Handler mSearchHandler;
    private AMap mAMap;
    private Marker mMapMarker;
    private SensorManager mSensorManager;
    private List<PoiItem> mCurrentLocationList;
    private List<PoiItem> mSearchLocationList;
    private LatLng mCurrentLatLng;
    private LatLng mShareLatLng;

    private int mCurrentMode;
    private boolean isPositionComplete;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_location_map;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mMapView = findViewById(R.id.LocationMapActivity_mMapView);
        mRvSearchLocation = findViewById(R.id.LocationMapActivity_mRvSearchLocation);
        mRvCurrentLocation = findViewById(R.id.LocationMapActivity_mRvCurrentLocation);
        mFlSearchLocationLayout = findViewById(R.id.LocationMapActivity_mRvSearchLocationLayout);
        mFlCurrentLocationLayout = findViewById(R.id.LocationMapActivity_mRvCurrentLocationLayout);
        mPbSearchLocation = findViewById(R.id.LocationMapActivity_mPbSearchLocation);
        mPbCurrentLocation = findViewById(R.id.LocationMapActivity_mPbCurrentLocation);
        mClShareLayout = findViewById(R.id.LocationMapActivity_mClShareLayout);
        mTvShareLocationTitle = findViewById(R.id.LocationMapActivity_mTvShareLocationTitle);
        mTvShareLocationContent = findViewById(R.id.LocationMapActivity_mTvShareLocationContent);
        mSearchLocationFooterView = getLayoutInflater().inflate(R.layout.view_load_more, (ViewGroup) getWindow().getDecorView(), false);
        mCurrentLocationFooterView = getLayoutInflater().inflate(R.layout.view_load_more, (ViewGroup) getWindow().getDecorView(), false);
        mTvSearchLocationLoadMoreHint = mSearchLocationFooterView.findViewById(R.id.LoadMoreView_mTvLoadMoreHint);
        mTvCurrentLocationLoadMoreHint = mCurrentLocationFooterView.findViewById(R.id.LoadMoreView_mTvLoadMoreHint);
        mCurrentLocationList = new ArrayList<>(32);
        mSearchLocationList = new ArrayList<>(32);
        mCurrentLocationAdapter = new LocationAdapter(mCurrentLocationList);
        mSearchLocationAdapter = new LocationAdapter(mSearchLocationList);
        mSearchHandler = new Handler();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    protected void setup(Bundle savedInstanceState) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mRvCurrentLocation.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRvCurrentLocation.setAdapter(mCurrentLocationAdapter);
        mRvCurrentLocation.setHasFixedSize(true);
        mRvCurrentLocation.addItemDecoration(new DividerItemDecoration(1, ContextCompat.getColor(this, R.color.dividerColorBlack), DividerItemDecoration.ORIENTATION_HORIZONTAL));
        mRvCurrentLocation.addOnItemTouchListener(mOnRvCurrentLocationItemClickListener);
        mCurrentLocationAdapter.setScrollToBottomListener(mOnCurrentLocationScrollToBottomListener);

        mRvSearchLocation.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRvSearchLocation.setAdapter(mSearchLocationAdapter);
        mRvSearchLocation.setRecycledViewPool(mRvCurrentLocation.getRecycledViewPool());
        mRvSearchLocation.setHasFixedSize(true);
        mRvSearchLocation.addItemDecoration(new DividerItemDecoration(1, ContextCompat.getColor(this, R.color.dividerColorBlack), DividerItemDecoration.ORIENTATION_HORIZONTAL));
        mRvSearchLocation.addOnItemTouchListener(mOnRvSearchLocationItemClickListener);
        mSearchLocationAdapter.setScrollToBottomListener(mOnSearchLocationScrollToBottomListener);

        setupMap(savedInstanceState);
    }

    private void setupMap(Bundle savedInstanceState) {
        mAMap = mMapView.getMap();

        mAMap.setMyLocationStyle(new MyLocationStyle()
                .myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW_NO_CENTER)
                .interval(Constants.LOCATION_INTERVAL)
                .myLocationIcon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_location_point)))
                .radiusFillColor(Color.TRANSPARENT)
                .strokeColor(Color.TRANSPARENT));
        mAMap.setMyLocationEnabled(true);
        mAMap.setOnMyLocationChangeListener(mOnMyLocationChangeListener);

        UiSettings uiSettings = mAMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(false);
        uiSettings.setScaleControlsEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);

        PoiItem poiItem = getIntent().getParcelableExtra(INTENT_EXTRA_POI);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.draggable(false);//可拖放性
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(getBitmapFromDrawable(this, R.drawable.ic_location_flag)));
        mMapMarker = mAMap.addMarker(markerOptions);

        if (poiItem == null) {
            mCurrentMode = MODE_SEND;
            getSupportActionBar().setTitle(R.string.LocationMapActivity_Title);
            mMapView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mMapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mMapMarker.setPositionByPixels(mMapView.getWidth() >> 1, mMapView.getHeight() >> 1);
                }
            });
            mAMap.setOnCameraChangeListener(mOnCameraChangeListener);
            mAMap.moveCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));
            mFlCurrentLocationLayout.setVisibility(View.VISIBLE);
            mClShareLayout.setVisibility(View.GONE);

        } else {
            mCurrentMode = MODE_SHARE;
            getSupportActionBar().setTitle(R.string.Location);
            LatLonPoint point = poiItem.getLatLonPoint();
            mShareLatLng = new LatLng(point.getLatitude(), point.getLongitude());
            mMapMarker.setPosition(mShareLatLng);
            mFlCurrentLocationLayout.setVisibility(View.GONE);
            mClShareLayout.setVisibility(View.VISIBLE);
            mTvShareLocationTitle.setText(poiItem.getTitle());
            mTvShareLocationContent.setText(poiItem.getSnippet());
        }


        mMapView.onCreate(savedInstanceState);
    }

    public static Bitmap getBitmapFromDrawable(Context context, @DrawableRes int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof VectorDrawable || drawable instanceof VectorDrawableCompat) {
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);

            return bitmap;
        } else {
            throw new IllegalArgumentException("unsupported drawable type");
        }
    }

    private void setupSearchView() {
        mSearchView.setQueryHint(getString(R.string.LocationMapActivity_SearchHint));
        mSearchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMapView.setVisibility(View.INVISIBLE);
                mFlCurrentLocationLayout.setVisibility(View.GONE);
                mFlSearchLocationLayout.setVisibility(View.VISIBLE);
            }
        });

        mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                mMapView.setVisibility(View.VISIBLE);
                mFlCurrentLocationLayout.setVisibility(View.VISIBLE);
                mFlSearchLocationLayout.setVisibility(View.GONE);
                return false;
            }
        });

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                mSearchHandler.removeCallbacksAndMessages(null);
                if (TextUtils.isEmpty(newText)) {
                    mPbSearchLocation.setVisibility(View.INVISIBLE);
                    showNewSearchLocation(null);
                } else {
                    mSearchHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mPbSearchLocation.setVisibility(View.VISIBLE);
                            mPresenter.searchPOIByKeyword(newText);
                        }
                    }, 400);
                }
                return false;
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
        mSensorManager.registerListener(mGyroscopeEventListener, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(mGyroscopeEventListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(mGyroscopeEventListener,mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD));
        mSensorManager.unregisterListener(mGyroscopeEventListener,mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAMap.setMyLocationEnabled(false);
        mAMap.setOnMyLocationChangeListener(null);
        mAMap.setOnCameraChangeListener(null);
        mSearchHandler.removeCallbacksAndMessages(null);
        mMapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mCurrentMode == MODE_SEND) {
            getMenuInflater().inflate(R.menu.menu_location_map, menu);
            MenuItem searchItem = menu.findItem(R.id.LocationMapMenu_Search);
            mSendMenuItem = menu.findItem(R.id.LocationMapMenu_Send);
            mSendMenuItem.setEnabled(false);
            mSearchView = (SearchView) searchItem.getActionView();
            setupSearchView();
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.LocationMapMenu_Send:
                Intent intent = new Intent();
                intent.putExtra(INTENT_EXTRA_POI, mCurrentLocationList.get(mCurrentLocationAdapter.getSelectedPosition()));
                setResult(RESULT_CODE, intent);
                finish();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mCurrentMode == MODE_SEND && !mSearchView.isIconified()) {
            closeSearch();
        } else {
            super.onBackPressed();
        }
    }

    private void closeSearch() {
        mSearchView.setQuery(null, false);
        mSearchView.setIconified(true);
    }

    private final AMap.OnMyLocationChangeListener mOnMyLocationChangeListener = new AMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(Location location) {
            if (!isPositionComplete) {
                if (mCurrentMode == MODE_SEND) {
                    mAMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM));
                } else {
                    mAMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mShareLatLng, DEFAULT_ZOOM));
                }
            }
            isPositionComplete = true;
        }
    };

    private final AMap.OnCameraChangeListener mOnCameraChangeListener = new AMap.OnCameraChangeListener() {
        @Override
        public void onCameraChange(CameraPosition cameraPosition) {
        }

        @Override
        public void onCameraChangeFinish(final CameraPosition cameraPosition) {
            if (isPositionComplete) {
                showNewCurrentLocation(null);
                mSearchHandler.removeCallbacksAndMessages(null);
                mSearchHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPbCurrentLocation.setVisibility(View.VISIBLE);
                        mCurrentLatLng = cameraPosition.target;
                        mPresenter.searchCurrentLocation(mCurrentLatLng.latitude, mCurrentLatLng.longitude);
                    }
                }, 200);
            }
        }
    };

    private final OnRecyclerViewItemClickListener mOnRvCurrentLocationItemClickListener = new OnRecyclerViewItemClickListener() {
        @Override
        public void onItemClick(int position, RecyclerView.ViewHolder viewHolder) {
            mCurrentLocationAdapter.setSelectedPosition(position);
            PoiItem poiItem = mCurrentLocationList.get(position);
            LatLonPoint point = poiItem.getLatLonPoint();
            mAMap.setOnCameraChangeListener(null);
            mAMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(point.getLatitude(), point.getLongitude()), mAMap.getCameraPosition().zoom), new AMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    mAMap.setOnCameraChangeListener(mOnCameraChangeListener);
                }

                @Override
                public void onCancel() {
                    mAMap.setOnCameraChangeListener(mOnCameraChangeListener);
                }
            });
        }
    };

    private final OnRecyclerViewItemClickListener mOnRvSearchLocationItemClickListener = new OnRecyclerViewItemClickListener() {
        @Override
        public void onItemClick(int position, RecyclerView.ViewHolder viewHolder) {
            mSearchLocationAdapter.setSelectedPosition(position);
            PoiItem poiItem = mSearchLocationList.get(position);
            LatLonPoint point = poiItem.getLatLonPoint();
            closeSearch();
            mAMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(point.getLatitude(), point.getLongitude()), mAMap.getCameraPosition().zoom));
        }
    };

    private final BaseRecyclerViewAdapter.OnScrollToBottomListener mOnSearchLocationScrollToBottomListener = new BaseRecyclerViewAdapter.OnScrollToBottomListener() {
        @Override
        public void OnScrollToBottom() {
            mPresenter.searchMorePOIByKeyword(mSearchView.getQuery().toString());
        }
    };

    private final BaseRecyclerViewAdapter.OnScrollToBottomListener mOnCurrentLocationScrollToBottomListener = new BaseRecyclerViewAdapter.OnScrollToBottomListener() {
        @Override
        public void OnScrollToBottom() {
            mPresenter.searchCurrentMoreLocation(mCurrentLatLng.latitude, mCurrentLatLng.longitude);
        }
    };


    private SensorEventListener mGyroscopeEventListener = new SensorEventListener() {
        private float[] mMagneticFieldValues;
        private float mCurrentDegrees;

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                mMagneticFieldValues = event.values;
            } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && mMagneticFieldValues != null) {
                float[] values = new float[3];
                float[] R = new float[9];
                SensorManager.getRotationMatrix(R, null, event.values, mMagneticFieldValues);
                SensorManager.getOrientation(R, values);
                float newDegrees = -(float) Math.toDegrees(values[0]);
                if (Math.abs(mCurrentDegrees - newDegrees) > 3) {//角度变化幅度大于3的时候才旋转
                    mAMap.setMyLocationRotateAngle(newDegrees);
                    mCurrentDegrees = newDegrees;
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    @Override
    public LocationMapActivityContract.Presenter getPresenter() {
        return new LocationMapActivityPresenter();
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void showNewCurrentLocation(List<PoiItem> poiItemList) {
        mPbCurrentLocation.setVisibility(View.INVISIBLE);
        mCurrentLocationAdapter.setFooterView(null);
        if (poiItemList != null) {
            int size = poiItemList.size();
            if (size >= Constants.SEARCH_LOCATION_PAGE_SIZE) {
                mTvCurrentLocationLoadMoreHint.setText(R.string.LoadMoreHint_LoadingMore);
            } else if (size == 0) {
                mTvCurrentLocationLoadMoreHint.setText(R.string.LoadMoreHint_None);
            } else {
                mTvCurrentLocationLoadMoreHint.setText(R.string.LoadMoreHint_Default);
            }
            mCurrentLocationAdapter.setFooterView(mCurrentLocationFooterView);
        }
        mCurrentLocationAdapter.notifyDataSetChanged();
        mCurrentLocationAdapter.setSelectedPosition(0);
        mCurrentLocationList.clear();
        if (poiItemList != null && poiItemList.size() > 0) {
            mCurrentLocationList.addAll(poiItemList);
            mSendMenuItem.setEnabled(true);
        } else {
            mSendMenuItem.setEnabled(false);
        }
    }

    @Override
    public void showMoreCurrentLocation(List<PoiItem> poiItemList, boolean hasMore) {
        if (hasMore) {
            mTvCurrentLocationLoadMoreHint.setText(R.string.LoadMoreHint_LoadingMore);
        } else {
            mTvCurrentLocationLoadMoreHint.setText(R.string.LoadMoreHint_NoMore);
        }
        mCurrentLocationAdapter.notifyItemRangeInsertedEx(mCurrentLocationList.size(), poiItemList.size());
        mCurrentLocationList.addAll(poiItemList);
    }

    @Override
    public void showNewSearchLocation(List<PoiItem> poiItemList) {
        mPbSearchLocation.setVisibility(View.INVISIBLE);
        mSearchLocationAdapter.setFooterView(null);
        if (poiItemList != null) {
            int size = poiItemList.size();
            if (size >= Constants.SEARCH_LOCATION_PAGE_SIZE) {
                mTvSearchLocationLoadMoreHint.setText(R.string.LoadMoreHint_LoadingMore);
            } else if (size == 0) {
                mTvSearchLocationLoadMoreHint.setText(R.string.LoadMoreHint_None);
            } else {
                mTvSearchLocationLoadMoreHint.setText(R.string.LoadMoreHint_Default);
            }
            mSearchLocationAdapter.setFooterView(mSearchLocationFooterView);
        }
        mSearchLocationAdapter.notifyDataSetChanged();
        mSearchLocationAdapter.setSelectedPosition(0);
        mSearchLocationList.clear();
        if (poiItemList != null && poiItemList.size() > 0) {
            mSearchLocationList.addAll(poiItemList);
        }
    }

    @Override
    public void showMoreSearchLocation(List<PoiItem> poiItemList, boolean hasMore) {
        if (hasMore) {
            mTvSearchLocationLoadMoreHint.setText(R.string.LoadMoreHint_LoadingMore);
        } else {
            mTvSearchLocationLoadMoreHint.setText(R.string.LoadMoreHint_NoMore);
        }
        mSearchLocationAdapter.notifyItemRangeInsertedEx(mSearchLocationList.size(), poiItemList.size());
        mSearchLocationList.addAll(poiItemList);
    }

    @Override
    public void showError(String error) {
        showLongToast(error);
    }


}
