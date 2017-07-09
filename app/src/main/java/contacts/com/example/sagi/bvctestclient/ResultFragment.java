package contacts.com.example.sagi.bvctestclient;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.InputStreamEntity;
import cz.msebera.android.httpclient.entity.StringEntity;

public class ResultFragment extends Fragment {

    private final String TAG = "MainActivity";
    private final String TOKEN_URL = "https://token.beyondverbal.com/token";
    private String BASE_URL = "";
    private final String API_KEY = "APIKEY_HERE";
    AsyncHttpClient client;
    String response;
    String access_token;
    String recordingID;
    TextView responseText;
    ProgressDialog pDialog;
    Button btnv3, btnv4;
    Snackbar snackbar;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.result_fragment, container, false);

        responseText = (TextView) v.findViewById(R.id.responseText);
        btnv4 = (Button) v.findViewById(R.id.btnv4Start);
        btnv3 = (Button) v.findViewById(R.id.btnv3Start);
        btnv3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                responseText.setText("");
                BASE_URL = "https://apiv3.beyondverbal.com/v3/";
                snackbar = Snackbar.make(view, "Testing APIV3...", Snackbar.LENGTH_SHORT);
                snackbar.getView().setBackgroundColor(ContextCompat.getColor(getView().getContext(), R.color.sagiBlue));
                snackbar.show();
                getAuth(TOKEN_URL);
            }
        });

        btnv4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                responseText.setText("");
                BASE_URL = "https://apiv4.beyondverbal.com/v3/";
                snackbar = Snackbar.make(view, "Testing APIV4...", Snackbar.LENGTH_SHORT);
                snackbar.getView().setBackgroundColor(ContextCompat.getColor(getView().getContext(), R.color.sagiBlue));
                snackbar.show();
                getAuth(TOKEN_URL);
            }
        });

        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        client = new AsyncHttpClient();
        pDialog = new ProgressDialog(getActivity());
    }

    public void getAuth(String token_url) {

        RequestParams params = new RequestParams();
        params.put("apiKey", API_KEY);
        params.put("grant_type", "client_credentials");
        client.addHeader("Content-Type", "x-www-form-urlencoded");
        client.post(token_url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String responseOK = new String(responseBody);
                try {
                    JSONObject obj = new JSONObject(responseOK);
                    access_token = "Bearer " + obj.getString("access_token");
                    try {
                        getRecordingID(access_token);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                response = responseOK;
                Log.i(TAG, responseOK);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String responseError = new String(responseBody);
                snackbar = Snackbar.make(getView(), "Error",Snackbar.LENGTH_SHORT);
                snackbar.getView().setBackgroundColor(ContextCompat.getColor(getView().getContext(), R.color.red));
                snackbar.show();
                Log.i(TAG, responseError);
            }
        });
    }

    public void getRecordingID(String auth) throws JSONException, UnsupportedEncodingException {
        JSONObject obj = new JSONObject();
        obj.put("type", "wav");
        JSONObject obj2 = new JSONObject();
        obj2.put("dataFormat", obj);
        StringEntity entity = new StringEntity(obj2.toString());
        client = new AsyncHttpClient();
        client.addHeader("Authorization", auth);
        client.post(getActivity(), BASE_URL + "recording/start", entity, "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String status = "";
                String responseOK = new String(responseBody);
                try {
                    JSONObject getID = new JSONObject(responseOK);
                    String recIDtoString = getID.getString("recordingId");
                    status = getID.getString("status");
                    pDialog.setMessage(status);
                    recordingID = recIDtoString;
                    //getFrom();
                    getAnalisys(recIDtoString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                snackbar = Snackbar.make(getView(), "Status" + " " + status,Snackbar.LENGTH_SHORT);
                snackbar.getView().setBackgroundColor(ContextCompat.getColor(getView().getContext(), R.color.green));
                snackbar.show();
                Log.i(TAG, responseOK);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String responseError = new String(responseBody);
                snackbar = Snackbar.make(getView(),responseError.toString(),Snackbar.LENGTH_SHORT);
                snackbar.getView().setBackgroundColor(ContextCompat.getColor(getView().getContext(), R.color.red));
                snackbar.show();
                Log.i(TAG, responseError);
            }
        });
    }


    public void getAnalisys(String recordingID) {

        client = new AsyncHttpClient();
        client.addHeader("Authorization", access_token);
        //InputStream inputStream = getResources().openRawResource(R.raw.sample2);
        InputStream inputStream = getResources().openRawResource(R.raw.filetosend);
        InputStreamEntity streamEntity = new InputStreamEntity(inputStream, -1);
        client.post(getActivity(), BASE_URL + "recording/" + recordingID, streamEntity, "Content-Type: multipart/form-data", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String responseOK = new String(responseBody);
                snackbar = Snackbar.make(getView(),"Done!", Snackbar.LENGTH_SHORT);
                snackbar.getView().setBackgroundColor(ContextCompat.getColor(getView().getContext(), R.color.green));
                snackbar.show();
                responseText.setText(responseOK);
                Log.i(TAG, responseOK);
                pDialog.dismiss();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String responseError = new String(responseBody);
                responseText.setText(responseError);
                snackbar = Snackbar.make(getView(),"Error",Snackbar.LENGTH_SHORT);
                snackbar.getView().setBackgroundColor(ContextCompat.getColor(getView().getContext(), R.color.red));
                snackbar.show();
                Log.i(TAG, responseError);
                pDialog.dismiss();
            }
        });
    }




    private String getConfigData() {
        try {
            // Instantiate a JSON Object and fill with Configuration Data
            // (Currently set to Auto Config)
            JSONObject inner_json = new JSONObject();
            inner_json.put("type", "WAV");
            inner_json.put("channels", 1);
            inner_json.put("sample_rate", 0);
            inner_json.put("bits_per_sample", 0);
            inner_json.put("auto_detect", true);
            JSONObject json = new JSONObject();
            json.put("data_format", inner_json);

            return json.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


}
