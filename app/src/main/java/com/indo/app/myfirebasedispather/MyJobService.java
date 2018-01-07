package com.indo.app.myfirebasedispather;

import android.app.NotificationManager;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONObject;

import java.text.DecimalFormat;

import cz.msebera.android.httpclient.Header;

/**
 * Created by indo on 07/01/18.
 */

public class MyJobService extends com.firebase.jobdispatcher.JobService{

    public  static  final String TAG = MyJobService.class.getSimpleName();

    final  String APP_ID = "c963ea1922f176014be1b3b4edadd5a0";
    public static  String EXTRA_CITY ="Jakarta";

    @Override
    public boolean onStartJob(JobParameters job) {
        getCurrentWeather(job);

        //return true ketika ingin dijalankan proses dengan thread yang berbeda, misalnya asynctask
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false;
    }
    private void getCurrentWeather(final JobParameters job){
        String city = job.getExtras().getString(EXTRA_CITY);

        AsyncHttpClient  client = new AsyncHttpClient();
        String url = "http://api.openweathermap.org/data/2.5/weather?q"+city+"&appid="+APP_ID;
        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                Log.d(TAG, result);
                try {
                    JSONObject  responseObject = new JSONObject(result);
                    String currentWeather = responseObject.getJSONArray("weather").getJSONObject(0).getString("main");
                    String description = responseObject.getJSONArray("weather").getJSONObject(0).getString("description");
                    double tempInKelvin = responseObject.getJSONObject("main").getDouble("temp");

                    double tempInCelcius = tempInKelvin -273;
                    String temprature = new DecimalFormat("##.##").format(tempInCelcius);
                    String title = "Current Weatheer";

                    String message = currentWeather + ", "+description+"with "+ temprature+ "celcius";

                    int notifId = 100;

                    showNotification(getApplicationContext(), title, message, notifId);
                    //Ketika proses selesai, maka perlu dipanggil jobfinished dengan parameter false
                    jobFinished(job, false);
                }catch ( Exception e){
                    //ketika terjadi error maka, jobFinished diset dengan parameter true, yang artnya job perlu di reschedule
                    jobFinished(job, true);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                //Ketika proses gagal, maka JobFinished diset dengan  parameter true. Yang artinya job perlu di reschedule
                jobFinished(job, true);
            }
        });
    }

    private void showNotification(Context context, String title, String message, int notifId) {
        NotificationManager notificationManagerCompact = (NotificationManager)  context.getSystemService(Context.NOTIFICATION_SERVICE);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle(title)
                .setContentText(message)
                .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                .setVibrate(new long[]{1000, 1000, 1000, 1000})
                .setSound(alarmSound);
        notificationManagerCompact.notify(notifId, builder.build());
    }
}
