package com.wangx.dggbind;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.example.BindView;
import com.wangx.dggapi.DggBind;
import com.wangx.dggapi.IProxy;

public class MainActivity extends AppCompatActivity {


    @BindView(R.id.tv)
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DggBind.bind(this);


        textView.setText("Hello DggBind222");
    }
}
