package com.example.lib_image_loader.app;

import android.app.Notification;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.BaseRequestOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.NotificationTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.example.lib_image_loader.R;
import com.example.lib_image_loader.image.CustomRequestListener;
import com.example.lib_image_loader.image.Utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions.withCrossFade;

/**
 * 单例类
 * 图处加载类，外界唯一调用类,支持为view,notifaication,appwidget加载图片
 */
public class ImageLoaderManager {
    private ImageLoaderManager() {

    }

    public static ImageLoaderManager getInstance() {
        return SingletonHolder.instance;
    }

    //单例
    private static class SingletonHolder {
        private static ImageLoaderManager instance = new ImageLoaderManager();
    }

    /**
     * 为notification中的id控件加载图片
     * @param context 上下文环境
     * @param remoteViews notification布局
     * @param id 要加载图片的控件id
     * @param notification notification对象
     * @param NOTIFICATION_ID notification id
     * @param url 图片url
     */
    public void displayImageForNotification(Context context, RemoteViews remoteViews, int id,
                                            Notification notification, int NOTIFICATION_ID, String url) {
        this.displayImageForTarget(context, initNotificationTarget(context, id, remoteViews, notification, NOTIFICATION_ID), url);
    }

    /**
     * 不带回调的加载
     * @param imageView
     * @param url
     */
    public void displayImageForView(ImageView imageView, String url) {
        this.displayImageForView(imageView, url, null);
    }

    /**
     * 带回调的加载图片方法
     * @param imageView
     * @param url
     * @param requestListener
     */
    public void displayImageForView(ImageView imageView, String url, CustomRequestListener requestListener) {
        Glide.with(imageView.getContext())
                .asBitmap()
                .load(url)
                .apply(initCommonRequestOption())
                .transition(withCrossFade())
                .into(imageView);
    }

    /**
     * 带回调的加载圆形图片方法
     * @param imageView
     * @param url
     */
    public void displayImageForCircle(final ImageView imageView, String url) {
        Glide.with(imageView.getContext())
                .asBitmap()
                .load(url)
                .apply(initCommonRequestOption())
                .into(new BitmapImageViewTarget(imageView) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        //构造圆形drawable
                        RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(imageView.getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        imageView.setImageDrawable(circularBitmapDrawable);
                    }
                });
    }

    /**
     * 为任意viewgroup设置背景图片
     * @param group
     * @param url
     */
    public void displayImageForViewGroup(final ViewGroup group, String url) {
        Glide.with(group.getContext())
                .asBitmap()
                .load(url)
                .apply(initCommonRequestOption())
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        final Bitmap res = resource;
                        Observable.just(resource)
                                .map(new Function<Bitmap, Drawable>() {
                                    @Override
                                    public Drawable apply(Bitmap bitmap) throws Exception {
                                        //bitmap映射成drawable
                                        Drawable drawable = new BitmapDrawable(
                                                Utils.doBlur(res, 100, true)
                                        );
                                        return drawable;
                                    }
                                })
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Consumer<Drawable>() {
                                    @Override
                                    public void accept(Drawable drawable) throws Exception {
                                        group.setBackground(drawable);
                                    }
                                });
                    }
                });
    }

    /**
     * 为非view加载图片
     * @param context
     * @param target
     * @param url
     */
    private void displayImageForTarget(Context context, Target target, String url) {
        this.displayImageForTarget(context, target, url, null);
    }

    /**
     * 为非view加载图片
     * @param context
     * @param target
     * @param url
     * @param requestListener
     */
    private void displayImageForTarget(Context context, Target target, String url, CustomRequestListener requestListener) {
        Glide.with(context)
                .asBitmap()
                .load(url)
                .apply(initCommonRequestOption())//配置
                .transition(withCrossFade())//效果
                .fitCenter()
                .listener(requestListener)
                .into(target);
    }

    /**
     * 构造初始化Notification Target
     * @param context
     * @param id
     * @param remoteViews
     * @param notification
     * @param NOTIFICATION_ID
     * @return
     */
    private NotificationTarget initNotificationTarget(Context context, int id, RemoteViews remoteViews, Notification notification, int NOTIFICATION_ID) {
        NotificationTarget notificationTarget = new NotificationTarget(context, id, remoteViews, notification, NOTIFICATION_ID);
        return notificationTarget;
    }

    /**
     * 公共配置options
     *
     * @return RequestOptions
     */
    private RequestOptions initCommonRequestOption() {
        RequestOptions options = new RequestOptions();
        options.placeholder(R.mipmap.b4y)
                .error(R.mipmap.b4y)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)//缓存策略：Glide自己决定
                .skipMemoryCache(false)//使用内存缓存
                .priority(Priority.NORMAL);//
        return options;
    }
}
