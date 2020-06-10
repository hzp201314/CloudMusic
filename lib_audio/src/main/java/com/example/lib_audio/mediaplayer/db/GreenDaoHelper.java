package com.example.lib_audio.mediaplayer.db;

import android.database.sqlite.SQLiteDatabase;

import com.example.lib_audio.app.AudioHelper;
import com.example.lib_audio.mediaplayer.model.AudioBean;
import com.example.lib_audio.mediaplayer.model.Favourite;

/**
 * 操作greenDao数据库帮助类
 */
public class GreenDaoHelper {
    private static final String DB_NAME="music_db";

    private static DaoMaster.DevOpenHelper mHelper;
    private static SQLiteDatabase mDb;
    //管理数据库
    private static DaoMaster mDaoMaster;
    //管理各种实体Dao,不让业务层拿到session直接去操作数据库，统一由此类提供方法
    private static DaoSession mDaoSession;

    /**
     * 设置初始化greenDao
     */
    public static void initDatabase(){
        mHelper=new DaoMaster.DevOpenHelper(AudioHelper.getContext(),DB_NAME,null);
        mDb=mHelper.getWritableDatabase();
        mDaoMaster=new DaoMaster(mDb);
        mDaoSession=mDaoMaster.newSession();
    }

    /**
     * 添加收藏
     * @param audioBean 歌曲
     */
    public static void addFavourite(AudioBean audioBean){
        FavouriteDao dao = mDaoSession.getFavouriteDao();
        Favourite favourite = new Favourite();
        favourite.setAudioId(audioBean.id);
        favourite.setAudioBean(audioBean);
        dao.insertOrReplace(favourite);
    }

    /**
     * 移除收藏
     * @param audioBean 歌曲
     */
    public static void removeFavourite(AudioBean audioBean){
        FavouriteDao dao = mDaoSession.getFavouriteDao();
        Favourite favourite = dao.queryBuilder().where(FavouriteDao.Properties.AudioId.eq(audioBean.id)).unique();
        dao.delete(favourite);
    }

    /**
     * 查找感兴趣
     * @param audioBean 歌曲
     * @return 收藏表数据
     */
    public static Favourite selectFavourite(AudioBean audioBean){
        FavouriteDao dao = mDaoSession.getFavouriteDao();
        Favourite favourite = dao.queryBuilder().where(FavouriteDao.Properties.AudioId.eq(audioBean.id)).unique();
        return favourite;
    }



}
