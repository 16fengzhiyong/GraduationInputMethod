package com.nuc.omeletteinputmethod.myframent;

import android.os.Bundle;
import android.view.View;

/**
 * 避免activity刚创建的时候初始化大量资源。
 * 实现Fragment懒加载
 */

public abstract class LazyFrament extends BaseFrament {

    /**
     *  view是否初始化完毕
     */
    protected boolean isViewInitiated ;
    /**
     * 表示数据是否加载
     */
    protected boolean isDataInitiated;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        // 每次切换fragment时调用的方法
        if (isVisibleToUser) {
            prepareFetchData(true);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isViewInitiated = true;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isViewInitiated = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 判断当前fragment是否显示
        if (getUserVisibleHint()) {
            prepareFetchData(true);
        }
    }


    /***
     * @param forceUpdate 页面可见时是否强制刷新数据第一次加载强制更新
     * @return
     */
    public boolean prepareFetchData(boolean forceUpdate) {
        if ( isViewInitiated && (!isDataInitiated || forceUpdate)) {
            fetchData();
            isDataInitiated = true;
            return true;
        }
        return false;
    }

    public abstract void fetchData();
}
