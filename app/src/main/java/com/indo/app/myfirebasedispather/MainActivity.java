package com.indo.app.myfirebasedispather;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btnSetScheldure, btnCancelScheldure;
    private String DISPATCHER_TAG = "mydispatcher";
    private String CITY = "Jakarta";
    private FirebaseJobDispatcher mdispatcher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mdispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));

        btnSetScheldure = (Button) findViewById(R.id.btnsetScheldure);
        btnCancelScheldure = (Button)findViewById(R.id.btncancelScheldure);
        btnCancelScheldure.setOnClickListener(this);
        btnSetScheldure.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnsetScheldure){
            startdispatcher();
            Toast.makeText(this, "Dispatcher Created", Toast.LENGTH_LONG).show();
        }
        if (v.getId() == R.id.btncancelScheldure){
            cancelDispatcher();
            Toast.makeText(this, "Cancel Dispatcher", Toast.LENGTH_LONG).show();
        }
    }

    private void cancelDispatcher() {
        mdispatcher.cancel(DISPATCHER_TAG);
    }

    private void startdispatcher() {
        Bundle myExtraBundle = new Bundle();
    myExtraBundle.putString(MyJobService.EXTRA_CITY,CITY);
        Job myJob  = mdispatcher.newJobBuilder()
                //kelas service yang akan dipanggil
        .setService(MyJobService.class)
                //unique tag untuk identifikasi job
        .setTag(DISPATCHER_TAG)
                //one-off job
                //true job tersebut diulang, dan false job tersebut tidak hilang
        .setRecurring(true)
                //until_next_boot berartii hanya sampai next boot
                //forever berarti akan berjalan meskipun sudah reboot
        .setLifetime(Lifetime.UNTIL_NEXT_BOOT)
                //waktu trigger 0 sampai 60 detik
        .setTrigger(Trigger.executionWindow(0,60))
                //overwrite job dengann tag sama
        .setReplaceCurrent(true)
                //set waktu kapan aka dijalankan lagi jika gagal
        .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                //set kondisi dari device
        .setConstraints(
                //hanya berjalan saat ada koneksi yang unmetered
                Constraint.ON_UNMETERED_NETWORK,
                //hanya berjalan ketika device charge
                Constraint.DEVICE_CHARGING

                //berjalan saat ada koneksi internet
                //Constraint.ON_ANY_NETWORK

                //berjalan saat device dalam kondisi idle
                //Constraint.DEVICE_IDLE
        )
                .setExtras(myExtraBundle)
                .build();
        mdispatcher.mustSchedule(myJob);
    }
}
