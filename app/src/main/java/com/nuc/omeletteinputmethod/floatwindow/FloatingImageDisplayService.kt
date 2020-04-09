package com.nuc.omeletteinputmethod.floatwindow

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast

import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.nuc.omeletteinputmethod.R
import com.nuc.omeletteinputmethod.adapters.FloatShortInputAdapter
import com.nuc.omeletteinputmethod.adapters.SettingShortInputAdapter
import com.nuc.omeletteinputmethod.entityclass.AppInfomationEntity
import com.nuc.omeletteinputmethod.entityclass.FloatShortInputEntity
import com.nuc.omeletteinputmethod.floatwindow.view.FloatWindowLayout
import com.nuc.omeletteinputmethod.floatwindow.view.PathMenu
import com.nuc.omeletteinputmethod.floatwindow.view.StateMenu
import com.nuc.omeletteinputmethod.floatwindow.view.niv.NiceImageView

import java.util.ArrayList

/**
 * Created by dongzhong on 2018/5/30.
 */

class FloatingImageDisplayService : Service() {

    private val TAG = "fzy FloatingImageDisplayService"
    private var layoutParams: WindowManager.LayoutParams? = null

    private var displayView: View? = null
    private var zhankai: View? = null
    internal lateinit var centerImage: NiceImageView

    private var images: IntArray? = null
    private val imageIndex = 0

    private var changeImageHandler: Handler? = null
    internal lateinit var layoutInflater: LayoutInflater

    private val changeImageCallback = Handler.Callback { msg ->
        //            if (msg.what == 0) {
        //                imageIndex++;
        //                if (imageIndex >= 5) {
        //                    imageIndex = 0;
        //                }
        //                if (displayView != null) {
        //                    ((ImageView) displayView.findViewById(R.id.image_display_imageview)).setImageResource(images[imageIndex]);
        //                }
        //                changeImageHandler.sendEmptyMessageDelayed(0, 2000);
        //            }
        if (msg.what == 1) {
            clickDo()
        }
        if (msg.what == 2) {
            try {
                windowManager!!.removeView(zhankai)
                windowManager!!.addView(displayView, layoutParams)
            } catch (e: Exception) {

            }

        }
        if (msg.what == 3) {
            try {
                windowManager!!.removeView(zhankai)
                layoutParams!!.width = WindowManager.LayoutParams.WRAP_CONTENT
                layoutParams!!.height = WindowManager.LayoutParams.WRAP_CONTENT
                displayView!!.findViewById<View>(R.id.id_float_shortinput_parent_LL).visibility = View.VISIBLE
                displayView!!.findViewById<View>(R.id.id_float_shortinput_close_IV).setOnClickListener { displayView!!.findViewById<View>(R.id.id_float_shortinput_parent_LL).visibility = View.GONE }
                //                    WindowManager.LayoutParams ls = layoutParams;
                //                    ls.x = ls.x + 120;
                //                    ls.y = ls.y + 10;
                //                    ls.width = WindowManager.LayoutParams.WRAP_CONTENT;
                //                    ls.height = WindowManager.LayoutParams.WRAP_CONTENT;
                //                    View testv = layoutInflater.inflate(R.layout.float_shortinput_layout, null);
                //                    windowManager.addView(testv,ls);
                val layoutManager = LinearLayoutManager(this@FloatingImageDisplayService)
                val mRecyclerView = displayView!!.findViewById<View>(R.id.id_float_shortinput_list_RV) as RecyclerView
                //调整RecyclerView的排列方向
                layoutManager.orientation = LinearLayoutManager.VERTICAL
                mRecyclerView.layoutManager = layoutManager
                val floatShortInputEntities = ArrayList<FloatShortInputEntity>()
                for (i in 0..19) {
                    floatShortInputEntities.add(FloatShortInputEntity(i, "test$i", "com.nuc.omeletteinputmethod"))
                }
                mRecyclerView.adapter = FloatShortInputAdapter(floatShortInputEntities, this@FloatingImageDisplayService)
                windowManager!!.addView(displayView, layoutParams)
            } catch (e: Exception) {

            }

        }
        false
    }

    internal var imageView1: ImageView? = null
    internal var imageView2: ImageView? = null
    internal var imageView3: ImageView? = null
    internal var imageView4: ImageView? = null
    internal lateinit var floatWindowLayout: FloatWindowLayout
    internal lateinit var openIngOnTouchListener: OpenIngOnTouchListener

    override fun onCreate() {
        super.onCreate()
        isStarted = true
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        layoutParams = WindowManager.LayoutParams()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams!!.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            layoutParams!!.type = WindowManager.LayoutParams.TYPE_PHONE
        }
        layoutParams!!.format = PixelFormat.RGBA_8888
        layoutParams!!.gravity = Gravity.LEFT or Gravity.CENTER
        layoutParams!!.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        layoutParams!!.width = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams!!.height = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams!!.x = 0
        layoutParams!!.y = 300


        images = intArrayOf(R.drawable.jiandan, R.drawable.jiandan, R.drawable.jiandan, R.drawable.jiandan, R.drawable.jiandan)

        changeImageHandler = Handler(this.mainLooper, changeImageCallback)

        val layoutInflater = LayoutInflater.from(this)
        zhankai = layoutInflater.inflate(R.layout.click_show_bar_enum, null)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            showFloatingWindow()
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        return super.onStartCommand(intent, flags, startId)
    }

    @SuppressLint("LongLogTag")
    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun showFloatingWindow() {
        if (Settings.canDrawOverlays(this)) {
            layoutInflater = LayoutInflater.from(this)
            displayView = layoutInflater.inflate(R.layout.image_display, null)
            displayView!!.setOnTouchListener(FloatingOnTouchListener())
            centerImage = displayView!!.findViewById(R.id.image_display_imageview)
            centerImage.maxWidth = 120
            //            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            //            imageView.setImageResource(images[imageIndex]);
            Log.i(TAG, "showFloatingWindow: windowManager.hashCode() " + windowManager!!.hashCode())
            windowManager!!.addView(displayView, layoutParams)
            Log.i(TAG, "showFloatingWindow: windowManager.hashCode() " + windowManager!!.hashCode())
            changeImageHandler!!.sendEmptyMessageDelayed(0, 2000)
        }
    }


    fun getItems(context: Context): ArrayList<AppInfomationEntity> {
        val pckMan = context.packageManager
        val appInfomationEntities = ArrayList<AppInfomationEntity>()
        //        ArrayList<HashMap<String, Object>> items = new ArrayList<HashMap<String, Object>>();
        var id = 0
        val packageInfo = pckMan.getInstalledPackages(0)

        for (pInfo in packageInfo) {
            appInfomationEntities.add(AppInfomationEntity(id++, pInfo.applicationInfo.loadLabel(pckMan).toString(), pInfo.packageName, pInfo.versionName, pInfo.applicationInfo.loadIcon(pckMan)))
        }
        return appInfomationEntities
    }

    /**
     * 点击悬浮图后的事件
     */
    private fun clickDo() {
        openIngOnTouchListener = OpenIngOnTouchListener()
        layoutParams!!.height = 350
        layoutParams!!.width = 300
        //        zhankai.setMinimumHeight(600);
        //        zhankai.setMinimumWidth(600);
        zhankai!!.setOnTouchListener(openIngOnTouchListener)
        try {
            windowManager!!.removeView(displayView)
            windowManager!!.addView(zhankai, layoutParams)
        } catch (e: Exception) {

        }

        floatWindowLayout = zhankai!!.findViewById<View>(R.id.item_layout) as FloatWindowLayout
        floatWindowLayout.switchState(true, PathMenu.CENTER, this, StateMenu.CENTER)

        zhankai!!.findViewById<View>(R.id.bar_image_1)
        zhankai!!.findViewById<View>(R.id.bar_image_2)
        zhankai!!.findViewById<View>(R.id.bar_image_3).setOnTouchListener(openIngOnTouchListener)
        zhankai!!.findViewById<View>(R.id.bar_image_4)
    }

    fun sendMessageToHandler(p: Int) {
        changeImageHandler!!.sendEmptyMessage(p)
    }

    /**
     * 点击悬浮图后的事件
     */
    private fun clickDo2() {

        //        layoutParams.height = 120;
        //        layoutParams.width = 120;
        //        zhankai.setMinimumHeight(600);
        //        zhankai.setMinimumWidth(600);

        //        floatWindowLayout.switchState(true, PathMenu.CENTER ,this,StateMenu.CENTER);
        //        changeImageHandler.sendEmptyMessageAtTime(2,30000);

    }


    private inner class FloatingOnTouchListener : View.OnTouchListener {
        private var x: Int = 0
        private var y: Int = 0
        private var isClick = false
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isClick = true
                    x = event.rawX.toInt()
                    y = event.rawY.toInt()
                }
                MotionEvent.ACTION_MOVE -> {

                    val nowX = event.rawX.toInt()
                    val nowY = event.rawY.toInt()
                    val movedX = nowX - x
                    val movedY = nowY - y
                    if (movedX > 1 || movedY > 1) {
                        isClick = false
                    }
                    x = nowX
                    y = nowY
                    layoutParams!!.x = layoutParams!!.x + movedX
                    layoutParams!!.y = layoutParams!!.y + movedY
                    windowManager!!.updateViewLayout(view, layoutParams)
                    if (layoutParams!!.x < 120) {
                        layoutParams!!.width = layoutParams!!.x
                        if (layoutParams!!.x < 40) {
                            layoutParams!!.width = 40
                        }
                    } else if (layoutParams!!.width < 120) {
                        layoutParams!!.width = WindowManager.LayoutParams.WRAP_CONTENT
                    }
                }
                else -> if (isClick) {
                    isClick = false
                    //                        clickDo();
                    changeImageHandler!!.sendEmptyMessage(1)
                }
            }
            return false
        }
    }


    inner class OpenIngOnTouchListener : View.OnTouchListener {

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> doByView(v)
            }
            return false
        }

        fun doByView(view: View) {
            when (view.id) {
                R.id.bar_image_3 -> {
                    Toast.makeText(this@FloatingImageDisplayService, "你点击了快捷输入",
                            Toast.LENGTH_LONG).show()
                    floatWindowLayout.switchState(true, PathMenu.CENTER,
                            this@FloatingImageDisplayService, StateMenu.SHORT_INPUT)
                    layoutParams!!.height = 120
                    layoutParams!!.width = 120
                    centerImage.setImageResource(R.drawable.ic_shortcutinput)
                }
                R.id.bar_image_center -> {
                }
                else -> {
                }
            }//                    clickDo2();
        }
    }

    companion object {
        var isStarted = false
        private var windowManager: WindowManager? = null
    }
}
