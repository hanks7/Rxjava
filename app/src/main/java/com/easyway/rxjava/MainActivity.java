package com.easyway.rxjava;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Ulog.i("TAG", "MainActivity");

        //使用Observable.create()创建被观察者
        Observable<String> observable1 = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onNext("Hello");
                subscriber.onNext("Wrold");
                subscriber.onCompleted();
            }
        });


        //订阅
        observable1.subscribe(new Observer<String>() {

            @Override
            public void onCompleted() {
                Ulog.i("TAG", "Completed");
            }

            @Override
            public void onError(Throwable e) {
                Ulog.i("TAG", "Error");
            }

            @Override
            public void onNext(String s) {
                Ulog.i("TAG", s);
            }
        });

        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        final File file = new File(path, "/bbbb.apk");
        final String url = "http://172.16.1.132:8080/jdbc/bbbb.apk";

        downloadFile(file, url,new Subscriber<String>() {
            @Override
            public void onCompleted() {
                //接收到事件源的onCompleted后的操作
                //一般是取消downloading dialog的操作
                Ulog.i("下载完成");
            }
            @Override
            public void onError(Throwable e) {
                //接收事件源的错误信息操作
                Ulog.i(e.toString());
            }
            @Override
            public void onNext(String s) {
                // 接受实时的下载进度
                Ulog.i(s);
            }
        });

    }

    /**
     * 使用rxjava okhttp下载文件
     * @param file
     * @param url
     * @param subscriber
     */
    private void downloadFile(final File file, final String url, Subscriber subscriber) {
        Observable<String> downloadObservable = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                InputStream inputStream = null;
                OutputStream outputStream = null;
                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url(url)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        inputStream = response.body().byteStream();
                        long length = response.body().contentLength();
                        //将文件下载到file路径下
                        outputStream = new FileOutputStream(file);
                        byte data[] = new byte[1024];
                        subscriber.onNext("0%");
                        long total = 0;
                        long totalPre;
                        int count;
                        while ((count = inputStream.read(data)) != -1) {
                            totalPre=total;
                            total += count;
                            if((total * 100 / length)>(totalPre * 100 / length)){//当下载进度大于1% 才显示
                                // 返回当前实时进度
                                subscriber.onNext(String.valueOf(total * 100 / length) + "%");
                            }
                            outputStream.write(data, 0, count);
                        }
                        outputStream.flush();
                        outputStream.close();
                        inputStream.close();
                    }
                } catch (IOException e) {
                    //告诉订阅者错误信息
                    subscriber.onError(e);
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                        }
                    }
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                        }
                    }
                }
                //告诉订阅者请求数据结束
                subscriber.onCompleted();

            }
        });

        downloadObservable.subscribeOn(Schedulers.io())
                .onBackpressureBuffer()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }
}
