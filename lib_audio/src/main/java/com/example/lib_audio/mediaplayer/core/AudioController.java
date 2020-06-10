package com.example.lib_audio.mediaplayer.core;

import com.example.lib_audio.mediaplayer.db.GreenDaoHelper;
import com.example.lib_audio.mediaplayer.events.AudioCompleteEvent;
import com.example.lib_audio.mediaplayer.events.AudioErrorEvent;
import com.example.lib_audio.mediaplayer.events.AudioFavouriteEvent;
import com.example.lib_audio.mediaplayer.events.AudioPlayModeEvent;
import com.example.lib_audio.mediaplayer.exception.AudioQueueEmptyException;
import com.example.lib_audio.mediaplayer.model.AudioBean;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Random;

/**
 * 控制播放逻辑类，注意添加一些控制方法时，要考虑是否需要增加Event,来更新UI
 */
public class AudioController {
    /**
     * 播放方式
     */
    public enum PlayMode {
        /**
         * 列表循环
         */
        LOOP,
        /**
         * 随机
         */
        RANDOM,
        /**
         * 单曲循环
         */
        REPEAT
    }

    private AudioPlayer mAudioPlayer;
    //播放队列,不能为空,不设置主动抛错
    private ArrayList<AudioBean> mQueue = new ArrayList<>();
    //当前播放索引
    private int mQueueIndex = 0;
    //播放模式：默认列表循环
    private PlayMode mPlayMode = PlayMode.LOOP;

    private AudioController() {
        EventBus.getDefault().register(this);
        mAudioPlayer = new AudioPlayer();
    }

    /**
     * 添加歌曲到播放对列
     *
     * @param index 索引
     * @param bean  歌曲
     */
    private void addCustomAudio(int index, AudioBean bean) {
        checkQueueIsNull();
        mQueue.add(index, bean);
    }

    /**
     * 查询是否添加过
     *
     * @param bean 歌曲
     * @return -1：没有添加 否则添加过
     */
    private int queryAudio(AudioBean bean) {
        return mQueue.indexOf(bean);
    }

    /**
     * 加载歌曲
     */
    private void load(AudioBean bean) {
        mAudioPlayer.load(bean);
    }

    /**
     * 获取播放器当前状态
     */
    private CustomMediaPlayer.Status getStatus() {
        return mAudioPlayer.getStatus();
    }

    /**
     * 获取当前播放的歌曲
     *
     * @param index 歌曲索引
     * @return 歌曲
     */
    private AudioBean getPlaying(int index) {
        if (mQueue != null && !mQueue.isEmpty() && index >= 0 && index < mQueue.size()) {
            return mQueue.get(index);
        } else {
            throw new AudioQueueEmptyException("当前播放队列为空,请先设置播放队列.");
        }
    }

    /**
     * 上一首歌曲
     */
    private AudioBean getPreviousPlaying() {
        switch (mPlayMode) {
            case LOOP:
                mQueueIndex = (mQueueIndex + mQueue.size() - 1) % mQueue.size();
                return getPlaying(mQueueIndex);
            case RANDOM:
                mQueueIndex = new Random().nextInt(mQueue.size()) % mQueue.size();
                return getPlaying(mQueueIndex);
            case REPEAT:
                return getPlaying(mQueueIndex);
        }
        return null;
    }

    /**
     * 下一首歌曲
     */
    private AudioBean getNextPlaying() {
        switch (mPlayMode) {
            case LOOP:
                mQueueIndex = (mQueueIndex + 1) % mQueue.size();
                return getPlaying(mQueueIndex);
            case RANDOM:
                mQueueIndex = new Random().nextInt(mQueue.size()) % mQueue.size();
                return getPlaying(mQueueIndex);
            case REPEAT:
                return getPlaying(mQueueIndex);
        }
        return null;
    }

    /**
     * 判断播放队列是否为空
     */
    private void checkQueueIsNull() {
        if (mQueue == null) {
            throw new AudioQueueEmptyException("当前播放队列为空,请先设置播放队列.");
        }
    }


    //单例
    public static AudioController getInstance() {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        private static AudioController instance = new AudioController();
    }

    /**
     * 对外提供是否播放中状态
     */
    public boolean isStartState() {
        return CustomMediaPlayer.Status.STARTED == getStatus();
    }

    /**
     * 对外提供是否暂停状态
     */
    public boolean isPauseState() {
        return CustomMediaPlayer.Status.PAUSED == getStatus();
    }


    /**
     * 对外提供播放列表
     */
    public ArrayList<AudioBean> getQueue() {
        return mQueue == null ? new ArrayList<AudioBean>() : mQueue;
    }

    /**
     * 设置播放队列
     */
    public void setQueue(ArrayList<AudioBean> queue) {
        setQueue(queue,0);
    }

    /**
     * 设置播放队列 并指定当前播放索引
     *
     * @param queue      播放列表
     * @param queueIndex 播放索引
     */
    public void setQueue(ArrayList<AudioBean> queue, int queueIndex) {
        mQueue.addAll(queue);
        mQueueIndex = queueIndex;
    }

    /**
     * 队列头添加歌曲
     */
    public void addAudio(AudioBean bean) {
        this.addAudio(0, bean);
    }

    /**
     * 添加歌曲到播放列表
     *
     * @param index 索引
     * @param bean  歌曲
     */
    public void addAudio(int index, AudioBean bean) {
        checkQueueIsNull();
        int query = queryAudio(bean);
        if (query <= -1) {
            //没有添加过 ，添加歌曲
            addCustomAudio(index, bean);
            setPlayIndex(index);
        } else {
            AudioBean currentBean = getNowPlaying();
            if (!currentBean.id.equals(bean.id)) {
                //添加过且不是当前播放，播放，否则什么也不干
                setPlayIndex(query);
            }
        }
    }

    /**
     * 设置播放索引
     *
     * @param index 索引
     */
    public void setPlayIndex(int index) {
        checkQueueIsNull();
        mQueueIndex = index;
        play();
    }

    /**
     * 获取当前播放模式
     */
    public PlayMode getPlayMode() {
        return mPlayMode;
    }

    /**
     * 设置播放模式
     */
    public void setPlayMode(PlayMode playMode) {
        mPlayMode = playMode;
        //还要对外发送切换事件，更新UI
        EventBus.getDefault().post(new AudioPlayModeEvent(mPlayMode));
    }

    public int getQueueIndex(){
        return mQueueIndex;
    }

    /**
     * 添加/移除收藏
     */
    public void changeFavourite(){
        if(null!= GreenDaoHelper.selectFavourite(getNowPlaying())){
            //已收藏，移除
            GreenDaoHelper.removeFavourite(getNowPlaying());
            EventBus.getDefault().post(new AudioFavouriteEvent(false));
        }else {
            //未收藏，添加收藏
            GreenDaoHelper.addFavourite(getNowPlaying());
            EventBus.getDefault().post(new AudioFavouriteEvent(true));
        }
    }

    /**
     * 播放/暂停切换
     */
    public void playOrPause() {
        if (isStartState()) {
            pause();
        } else if (isPauseState()) {
            resume();
        }
    }

    /**
     * 播放
     * 加载当前index歌曲
     */
    public void play() {
        AudioBean bean = getPlaying(mQueueIndex);
        load(bean);
    }

    /**
     * 下一首
     * 加载next index歌曲
     */
    public void next() {
        AudioBean bean = getNextPlaying();
        load(bean);
    }

    /**
     * 加载previous index歌曲
     */
    public void previous() {
        AudioBean bean = getPreviousPlaying();
        load(bean);
    }

    /**
     * 对外提供获取当前播放时间
     */
    public int getNowPlayTime() {
        return mAudioPlayer.getCurrentPosition();
    }

    /**
     * 对外提供获取总播放时间
     */
    public int getTotalPlayTime() {
        return mAudioPlayer.getCurrentPosition();
    }

    /**
     * 对外提供的获取当前歌曲信息
     */
    public AudioBean getNowPlaying() {
        return getPlaying(mQueueIndex);
    }

    /**
     * 暂停
     */
    public void pause() {
        mAudioPlayer.pause();
    }

    /**
     * 恢复播放
     */
    public void resume() {
        mAudioPlayer.resume();
    }

    /**
     * 释放
     */
    public void release() {
        mAudioPlayer.release();
        EventBus.getDefault().unregister(this);
    }

    //插放完毕事件处理
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudioCompleteEvent(
            AudioCompleteEvent event) {
        next();
    }

    //播放出错事件处理
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudioErrorEvent(AudioErrorEvent event) {
        next();
    }


}
