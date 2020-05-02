package com.nuc.omeletteinputmethod.DBoperation;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.TextView;

import com.nuc.omeletteinputmethod.entityclass.CandidatesEntity;
import com.nuc.omeletteinputmethod.entityclass.FloatShortInputEntity;
import com.nuc.omeletteinputmethod.entityclass.SinograFromDB;
import com.nuc.omeletteinputmethod.kernel.OmeletteIME;

import java.util.ArrayList;
import java.util.List;

public class DBManage {
    public static String mydbName="myandroid.db";//数据库的名字
    private static String DATABASE_PATH="/data/data/com.nuc.omeletteinputmethod/databases/";//数据库在手机里的路径
    private Context context;
    public DBManage(Context context){
        if(myDBHelper == null){
            myDBHelper = SQLiteDatabase.openOrCreateDatabase(
                    DATABASE_PATH+mydbName, null);
        }
        this.context = context;
    }

    public SQLiteDatabase getMyDBHelper(){
        return myDBHelper;
    }
    private SQLiteDatabase myDBHelper = null;

    public ArrayList<FloatShortInputEntity> getDataByPackageName(String packagename ){
        ArrayList<FloatShortInputEntity> arrayList = new ArrayList<>();
        Cursor cursor=null;
        try {
            // ORDER BY allcishu DESC
            cursor = myDBHelper.query("shortInput", new String[]{"packagename,info,cishu,id"}, "packagename = ?", new String[]{packagename}, null, null, "cishu DESC");
            Log.e("数据库返回信息", "getSinogramByPinyin: 获取返回成功");
        }catch (Exception e){
            Log.e("数据库返回信息", "getSinogramByPinyin: ",e );
            return null;
        }Log.i("数据库返回信息", "getSinogramByPinyin: 成功返回");
        if (cursor.getCount() == 0){
            Log.i("数据库返回信息", "返回信息为空");
            return null;
        }
        try {
            cursor.moveToFirst();

            while (!cursor.isLast()){
                arrayList.add(
                        new FloatShortInputEntity(cursor.getInt(cursor.getColumnIndex("id"))
                                ,cursor.getString(cursor.getColumnIndex("info"))
                                ,cursor.getString(cursor.getColumnIndex("packagename"))
                                ,cursor.getInt(cursor.getColumnIndex("cishu"))
                        )
                        );
                cursor.moveToNext();
            }

            cursor.close();
        }catch (Exception e){
            Log.e("数据库返回信息错误", "getSinogramByPinyin: ", e );
        }

        return arrayList;
    }
}
