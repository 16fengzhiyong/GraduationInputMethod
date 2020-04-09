package com.nuc.omeletteinputmethod.DBoperation

import android.app.Activity
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.os.Bundle

import com.nuc.omeletteinputmethod.R

import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class CopyDB : Activity() {
    internal var alpha = 255
    internal var b = 0

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //判断数据库是否存在
        val dbExist = checkDataBase()
        if (dbExist) {

        } else {//不存在就把raw里的数据库写入手机
            try {
                copyDataBase()
            } catch (e: IOException) {
                throw Error("Error copying database")
            }

        }

        Thread(Runnable {
            initApp() //初始化程序

            while (b < 2) {
                try {
                    if (b == 0) {
                        Thread.sleep(20)
                        b = 1
                    } else {
                        Thread.sleep(50)
                    }
                    updateApp()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

            }
        }).start()


    }

    fun updateApp() {
        alpha -= 5
        if (alpha <= 0) {
            b = 2
            if (true) {
                try {
                    //Intent in = new Intent(Loggin.this,MealActivityGroup.class);
                    // CopyDB.this.startActivity(in);
                    this@CopyDB.finish()
                } catch (e: Exception) {

                }

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

        val `is` = this@CopyDB.resources.openRawResource(R.raw.myandroid)//得到数据库文件的数据流
        val buffer = ByteArray(8192)
        var count = 0
        try {
            count = `is`.read(buffer)
            while (count> 0) {
                os!!.write(buffer, 0, count)
                os.flush()
                count = `is`.read(buffer)
            }
        } catch (e: IOException) {

        }

        try {
            `is`.close()
            os!!.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    /**
     * 初始化，这里是起始页的没有用
     */
    fun initApp() {}

    companion object {
        var dbName = "dinner.db"//数据库的名字
        private val DATABASE_PATH = "/data/data/com.nuc.omeletteinputmethod/databases/"//数据库在手机里的路径
    }
}