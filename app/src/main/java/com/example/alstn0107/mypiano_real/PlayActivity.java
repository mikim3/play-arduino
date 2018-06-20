package com.example.alstn0107.mypiano_real;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.LinearLayout;

import static com.example.alstn0107.mypiano_real.R.layout.activity_play;

public class PlayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_play);
        MyApplication myApp = (MyApplication) getApplication();

        Intent intent = getIntent();
        final String type = intent.getStringExtra("type");
        myApp.setType(type);

        ImageView ivPlayType = (ImageView) findViewById(R.id.iv_play_type);//다른화면들의 이미지를 불러오기 위한 소스 레이아웃중에 찾아보면 iv_play_type이라는 아이디를 가진 View가 있다
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.backgroundmaker);//배경값

        switch (type) {
            case "piano":
                setTitle("피아노");
                ivPlayType.setImageResource(R.drawable.pianoimage);
                break;
            case "drum":
                setTitle("드럼");
                ivPlayType.setImageResource(R.drawable.drum);

                break;
            case "guitarE":
                setTitle("일렉트릭기타");
                ivPlayType.setImageResource(R.drawable.egguitar);
                break;
            case "guitar":
                setTitle("기타");
                ivPlayType.setImageResource(R.drawable.acuguitar);
                break;
        }
    }
}
