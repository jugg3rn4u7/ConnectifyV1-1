package com.team5.uta.connectifyv1;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.tagmanager.Container;
import com.team5.uta.connectifyv1.adapter.IMyCallbackInterface;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.EventListener;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Shrikant on 3/25/2015.
 */
public class HTTPWrapper extends AsyncTask<Object, JSONArray, JSONArray> {

    String req_url = null;
    Object data = null;
    String encodedData = null;
    JSONArray result = null;

    final IMyCallbackInterface callback;

    HTTPWrapper(IMyCallbackInterface callback) {
        this.callback = callback;
    }

    /*
    * param_list - type, url, data(object), class
    * ex - GET, http://example.com/index.php, "a=1&b=2"
    * */
    @Override
     protected JSONArray doInBackground(Object params[]) {

        JSONArray jsonData = null;

        try {

            if(params[0].toString().equalsIgnoreCase("GET")) {
                HTTPGet(params[1].toString(), params[2].toString());
                jsonData = receive();
            } else {
                HTTPPost(params[1].toString(), params);
                jsonData = receive(true);
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("jsonData -> receive1 : " + jsonData.toString());
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
            System.out.println("jsonData -> receive2 : " + jsonData.toString());
            return null;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            System.out.println("jsonData -> receive3 : " + jsonData.toString());
            return null;
        }

        System.out.println("jsonData -> receive : " + jsonData.toString());

        result = jsonData;

        return jsonData;
    }

    protected JSONArray onPreExecute(JSONArray jsonData){
        return jsonData;
    }

    @Override
    protected void onPostExecute(JSONArray jsonData){
        try {
            callback.onComplete(jsonData);
        } catch (JSONException e) {
            System.out.println("Fuck u1!");
            e.printStackTrace();
        }
    }

    private void HTTPGet(String link, String data) {

        req_url = link + "?" + data;
    }

    private void HTTPPost(String link, Object data[]) throws UnsupportedEncodingException {

        req_url = link;
        encodedData = "";

        for (int i = 2; i < data.length; i++) {
            String param = data[i].toString(); System.out.println("Data: "+ data[i]);
            String[] strArray = param.split("=");
            encodedData += URLEncoder.encode(strArray[0], "UTF-8") + "=" + URLEncoder.encode(strArray[1], "UTF-8") + "&";
        }

        encodedData = encodedData.substring(0, encodedData.length() - 1);
        System.out.println("encodedData: "+ encodedData);
    }

    public JSONArray receive() throws IOException, JSONException, URISyntaxException {

        URL url = new URL(req_url);
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet();
        request.setURI(url.toURI());
        HttpResponse response = client.execute(request);
        BufferedReader in = new BufferedReader
                (new InputStreamReader(response.getEntity().getContent()));

        StringBuffer sb = new StringBuffer("");
        String line="";
        while ((line = in.readLine()) != null) {
            sb.append(line);
            break;
        }
        in.close();
        JSONArray jsonArray = new JSONArray(sb.toString());
        return jsonArray;
    }

    public JSONArray receive(Boolean param) throws IOException, JSONException, URISyntaxException {

        URL obj = new URL(req_url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        Log.i("Blah", "1");
        //add request header
        con.setRequestMethod("POST");
        Log.i("Blah", "2");
        // Send post request
        con.setDoOutput(true);
        Log.i("Blah", "3");

        OutputStream ost = null;

        do {
            Log.i("Blah", "OS : 1");
            try {
                ost = con.getOutputStream();
            } catch (Exception e) {

            }
            Log.i("Blah", "OS : 2 : ->");
        } while ( ost == null);


        DataOutputStream wr = new DataOutputStream(ost);
        Log.i("Blah", "4");
        wr.writeBytes(encodedData);
        Log.i("Blah", "5");
        int responseCode = con.getResponseCode();
        Log.i("Blah", "\nSending 'POST' request to URL : " + req_url);
        Log.i("Blah", "Post parameters : " + encodedData);
        Log.i("Blah", "Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }

        JSONArray jsonArray = new JSONArray(response.toString());
        return jsonArray;
    }
}
