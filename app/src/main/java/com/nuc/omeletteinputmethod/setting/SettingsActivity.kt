package com.nuc.omeletteinputmethod.setting

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

import com.nuc.omeletteinputmethod.CCPCustomViewPager
import com.nuc.omeletteinputmethod.R
import com.nuc.omeletteinputmethod.adapters.MainViewPagerAdapter
import com.nuc.omeletteinputmethod.floatwindow.FloatingImageDisplayService
import com.nuc.omeletteinputmethod.floatwindow.schedule.Schedule
import com.nuc.omeletteinputmethod.myframent.ScheduleFrament
import com.nuc.omeletteinputmethod.myframent.ShortInputFrament

import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.ArrayList


class SettingsActivity : FragmentActivity() {
    //    private Spinner spinner;
    private val REQUEST_CODE = 0


    private var fragmentList: ArrayList<Fragment>? = null


    lateinit internal var vp: CCPCustomViewPager
    lateinit var mMainViewPagerAdapter: MainViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_setting)
        initView()
        //verifyStoragePermissions(this);
        getPermissions()
        //判断数据库是否存在
        val dbExist = checkDataBase()
        if (dbExist) {
            Log.d("复制数据库", "onCreate: 数据库存在")
        } else {//不存在就把raw里的数据库写入手机
            try {
                copyDataBase()
            } catch (e: IOException) {
                throw Error("Error copying database")
            }

        }


    }

    /**
     * 判断数据库是否存在
     * @return false or true
     */
    fun checkDataBase(): Boolean {
        var checkDB: SQLiteDatabase? = null
        try {
            val databaseFilename = DATABASE_PATH + dbName
            checkDB = SQLiteDatabase.openDatabase(databaseFilename, null,
                    SQLiteDatabase.OPEN_READONLY)
        } catch (e: SQLiteException) {
            Log.e("复制数据库", "checkDataBase: ", e)
        }

        checkDB?.close()
        return if (checkDB != null) true else false
    }

    /**
     * 复制数据库到手机指定文件夹下
     * @throws IOException
     */
    @Throws(IOException::class)
    fun copyDataBase() {
        val databaseFilenames = DATABASE_PATH + dbName
        val dir = File(DATABASE_PATH)
        if (!dir.exists())
        //判断文件夹是否存在，不存在就新建一个
            dir.mkdir()
        var os: FileOutputStream? = null
        try {
            os = FileOutputStream(databaseFilenames)//得到数据库文件的写入流
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        val `is` = this@SettingsActivity.resources.openRawResource(R.raw.myandroid)//得到数据库文件的数据流
        val buffer = ByteArray(8192)
        var count = 0
        try {
            count = `is`.read(buffer);
            while (count > 0) {

                os!!.write(buffer, 0, count)
                os.flush()
                count = `is`.read(buffer);
            }
        } catch (e: IOException) {
            Log.e("复制数据库", "copyDataBase: ", e)
        }

        try {
            `is`.close()
            os!!.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun initView() {
        initFragments()
        vp = findViewById(R.id.vp)
        vp.setSlideEnabled(true)
        vp.offscreenPageLimit = fragmentList!!.size  //更改：总页数
        mMainViewPagerAdapter = MainViewPagerAdapter(supportFragmentManager,
                fragmentList!!)
        vp.adapter = mMainViewPagerAdapter
    }

    private fun initFragments() {
        fragmentList = ArrayList()
        val homeFra = ScheduleFrament.newInstance()//消息
        fragmentList!!.add(homeFra)
        val shortInputFrament = ShortInputFrament.newInstance()
        fragmentList!!.add(shortInputFrament)
        //        ScheduleFrament dialFra = ScheduleFrament.newInstance();//通讯录
        //        fragmentList.add(dialFra);
        // WorkbenchFragment workbenchFragment = WorkbenchFragment.newInstance();//工作台
        // fragmentList.add(workbenchFragment);
        //        ScheduleFrament myFrament = ScheduleFrament.newInstance();//我的
        //        fragmentList.add(myFrament);

    }

    fun getPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.i("弹窗", "getPermissions: ")
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "当前无权限，请授权", Toast.LENGTH_SHORT)
                startActivityForResult(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")), 0)
            } else {
                Log.i("弹窗", "getPermissions: 准备开启 FloatingImageDisplayService")
                try {
                    startService(Intent(this@SettingsActivity, FloatingImageDisplayService::class.java))

                } catch (e: Exception) {

                }

            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show()
                    Log.i("弹窗", "onActivityResult: 准备开启 FloatingImageDisplayService")
                    startService(Intent(this@SettingsActivity, FloatingImageDisplayService::class.java))
                }
            }
        }
    }

    companion object {
        var showMyselfkeyboard = false

        var dbName = "myandroid.db"//数据库的名字
        private val DATABASE_PATH = "/data/data/com.nuc.omeletteinputmethod/databases/"//数据库在手机里的路径
    }
}
