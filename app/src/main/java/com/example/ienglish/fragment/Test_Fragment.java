package com.example.ienglish.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.ienglish.Bean.Words;
import com.example.ienglish.EssayAdapter;
import com.example.ienglish.My_api_Application;
import com.example.ienglish.Play_Content;
import com.example.ienglish.R;
import com.example.ienglish.UserDB.UserDb;
import com.example.ienglish.ieng_main_activity;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Test_Fragment extends Fragment implements ieng_main_activity.Send_Words{

    private EditText et_s;
    private TextView tv_res;
    private Button btn_test;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_test, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        InitView(view);
    }


    public void InitView(View view){
        et_s = view.findViewById(R.id.et_s);
        tv_res = view.findViewById(R.id.tv_res);
        btn_test = view.findViewById(R.id.btn_test);
        btn_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                query(et_s.getText().toString());
            }
        });
    }

    private void query(String s) {
        String url = "http://192.168.43.20:5000/translate?s="+s;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Volley",response.toString());
                        try {
                            String res = response.getString("trans_res");
                            tv_res.setText(res.substring(0, res.length() - 5));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Volley",error.toString());
                    }
                }

        );

        My_api_Application.addRequest(jsonObjectRequest,"Test_Fragment");

    }


    @Override
    public void sendPron(String uk, String us, String name) {

    }

    @Override
    public void getResult(Words words) {

    }

    @Override
    public void getResultInChines(String sentence, String trans_res) {

    }

}
