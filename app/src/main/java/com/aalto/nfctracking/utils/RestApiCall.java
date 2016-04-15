package com.aalto.nfctracking.utils;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class RestApiCall {
    private static String BIM_SERVER_URL = "http://10.100.45.232:8080/json";
    public static String BIM_AUTH_TOKEN;


    public static void postJsonObjectReqest(final Activity activity, JSONObject param, Response.Listener successListener){
        Log.i("DNES", "RestApiCall PostRequest");

        JSONObject reqParam = new JSONObject();
        try {
            reqParam.put("token", BIM_AUTH_TOKEN);
            reqParam.put("request", param);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjRequest = new JsonObjectRequest
                (Request.Method.POST, BIM_SERVER_URL, reqParam, successListener, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("DNES", "Error: " + error.toString());
                        if(error instanceof NoConnectionError){
                            Toast.makeText(activity, "No Internet Connection!", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
        Volley.newRequestQueue(activity).add(jsonObjRequest);
    }
}
