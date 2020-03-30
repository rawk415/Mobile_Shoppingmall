package jo.beacon.project.network;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import jo.beacon.project.util.AddressInfo;
import jo.beacon.project.util.PreferenceUtil;

public class Task extends AsyncTask<String, Void, String> {
    public AddressInfo info;
    public String sIP;
    public int sPORT;
    public String type, receiveMsg;
    public Context context;

    public Task(String type, Context context) {
        this.type = type;
        info = new AddressInfo();
        this.sIP = info.getIp();
        this.sPORT = info.getPort();
        this.context = context;
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            String sendMsg = "";
            URL url = null;
            String path = "";
            String urlName = "http://" + sIP + ":" + sPORT + "/";
            String userId = null;
            if (type.equals("LOGIN")) { // 로그인
                path = "login";
                sendMsg = strings[0] + "," + strings[1] + ",";
            } else if (type.equals("COMMUNICATION")) { // 로그인 후 데이터 주고받을 때
                userId = PreferenceUtil.getInstance(context).get(PreferenceUtil.PreferenceKey.userId, "");
                sendMsg = strings[0] + "," + strings[1] + "," + strings[2]
                        + "," + strings[3] + "," + strings[4] + "," + strings[5] + "," + userId + ",";
            } else if (type.equals("REGISTER")) {
                path = "register";
                sendMsg = strings[0] + "," + strings[1] + ",";
            } else if (type.equals("HOME")) {
                path = "home";
            } else if (type.equals("MYPAGE")) {
                path = "mypage";
                sendMsg = strings[0] + ",";
            } else if (type.equals("ORDER")) {
                path = "order";
                sendMsg = strings[0] + "," + strings[1] + "," + strings[2] + ",";
            }
            url = new URL(urlName + path);
            // Connection
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestMethod("POST");
            OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
            osw.write(sendMsg);
            osw.flush();

            if (conn.getResponseCode() == conn.HTTP_OK) {
                InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                BufferedReader reader = new BufferedReader(tmp);

                StringBuffer buffer = new StringBuffer();
                while ((sendMsg = reader.readLine()) != null) {
                    buffer.append(sendMsg);
                }
                receiveMsg = buffer.toString(); // 서버로 부터 데이터 수신
            } else {
                Log.e("LOG", "Error!");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return receiveMsg;

    }
}


