package com.study.newintelrobot;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RobotActivity extends AppCompatActivity {
    private ListView listView;
    private ChatAdapter adpter;
    private List<ChatBean> chatBeanList; //存放所有聊天数据的集合
    private EditText et_send_msg;
    private Button btn_send;
    private String sendMsg;    //发送的信息
    private String welcome[];  //存储欢迎信息
    private MHandler mHandler;
    public static final int MSG_OK = 1;//获取数据

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_robot);
//        View decorView = getWindow().getDecorView();
//        // Hide the status bar.
//        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
//        decorView.setSystemUiVisibility(uiOptions);

        chatBeanList = new ArrayList<ChatBean>();
        mHandler = new MHandler();
        //获取内置的欢迎信息
        welcome = getResources().getStringArray(R.array.welcome);
        initView(); //初始化界面控件
    }

    public void initView() {
        listView = (ListView) findViewById(R.id.list);
        et_send_msg = (EditText) findViewById(R.id.et_send_msg);
        btn_send = (Button) findViewById(R.id.btn_send);
        adpter = new ChatAdapter(chatBeanList, this);
        listView.setAdapter(adpter);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                sendData();//点击发送按钮，发送信息
            }
        });
        et_send_msg.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() ==
                        KeyEvent.ACTION_DOWN) {
                    sendData();//点击Enter键也可以发送信息
                }
                return false;
            }
        });
        int position = (int) (Math.random() * welcome.length - 1); //获取一个随机数
        showData(welcome[position]); //用随机数获取机器人的首次聊天信息
    }

    private void sendData() {
        sendMsg = et_send_msg.getText().toString(); //获取你输入的信息
        if (TextUtils.isEmpty(sendMsg)) {             //判断是否为空
            Toast.makeText(this, "您还未输任何信息哦", Toast.LENGTH_LONG).show();
            return;
        }
        et_send_msg.setText("");
        //替换空格和换行
        sendMsg = sendMsg.replaceAll(" ", "").replaceAll("\n", "").trim();
        ChatBean chatBean = new ChatBean();
        chatBean.setMessage(sendMsg);
        chatBean.setState(chatBean.SEND); //SEND表示自己发送的信息
        chatBeanList.add(chatBean);        //将发送的信息添加到chatBeanList集合中
        adpter.notifyDataSetChanged();    //更新ListView列表
        getDataFromServer();                //从服务器获取机器人发送的信息
    }

    private void getDataFromServer() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Create a URL object
                    URL url = new URL("https://api.openai.com/v1/completions");

                    // Create a HttpURLConnection object
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Authorization", "Bearer YOU_API_KEY");

                    // Create a JSON object with the request data
                    JSONObject requestData = new JSONObject();

                    requestData.put("model", "text-davinci-003");
//                    requestData.put("model", "gpt-3.5-turbo");
                    requestData.put("prompt", sendMsg);
                    requestData.put("temperature", 0.9);
                    requestData.put("max_tokens", 2048);
                    requestData.put("top_p", 1);
                    requestData.put("frequency_penalty", 0.0);
                    requestData.put("presence_penalty", 0.6);

                    // Convert the JSON object to a byte array
                    byte[] requestDataBytes = requestData.toString().getBytes("UTF-8");

                    // Set the content length of the request
                    connection.setRequestProperty("Content-Length", String.valueOf(requestDataBytes.length));

                    // Send the request
                    connection.setDoOutput(true);
                    DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                    outputStream.write(requestDataBytes);
                    outputStream.flush();
                    outputStream.close();

                    // Get the response code
                    int responseCode = connection.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // If the response code is 200, read the response data
                        InputStream inputStream = connection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        String line;
                        StringBuilder responseBuilder = new StringBuilder();
                        while ((line = reader.readLine()) != null) {
                            responseBuilder.append(line);
                        }
                        String responseData = responseBuilder.toString();

                        String res = responseData;
                        Message msg = new Message();
                        msg.what = MSG_OK;
                        msg.obj = res;
                        mHandler.sendMessage(msg);
                        reader.close();
                        inputStream.close();

                        // Handle the response data
                        Log.d("MainActivity", "Response: " + responseData);
                    } else {
                        // If the response code is not 200, log an error
                        Log.e("MainActivity", "Error: " + responseCode);
                    }

                    // Disconnect the connection
                    connection.disconnect();

                } catch (Exception e) {
                    // Log any exceptions that occur
                    Log.e("MainActivity", "Error: " + e.getMessage());
                }
            }
        }).start();
    }

    /**
     * 事件捕获
     */
    class MHandler extends Handler {
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            switch (msg.what) {
                case MSG_OK:
                    if (msg.obj != null) {
                        String vlResult = (String) msg.obj;
                        paresData(vlResult);
                    }
                    break;
            }
        }
    }

    private void paresData(String JsonData) {       //Json解析
        try {
            JSONObject obj = new JSONObject(JsonData);
            Log.i("hello--", obj.toString());
            String content = obj.getString("choices"); //获取的机器人信息
            JSONArray array = new JSONArray(content);
            JSONObject object = new JSONObject(array.get(0).toString());
            content = object.getString("text");
            updateView(content);                 //更新界面
        } catch (JSONException e) {
            e.printStackTrace();
            showData("主人，你的网络不好哦");
        }
    }

    private void showData(String message) {
        ChatBean chatBean = new ChatBean();
        message = message.replaceAll(" ", "").trim();
        while ("\\n".equals(message.substring(0, 2))) {
            message = message.substring(2);
        }
        chatBean.setMessage(message);
        chatBean.setState(ChatBean.RECEIVE);//RECEIVE表示接收到机器人发送的信息
        chatBeanList.add(chatBean);  //将机器人发送的信息添加到chatBeanList集合中
        adpter.notifyDataSetChanged();
    }

    private void updateView(String content) {
        showData(content);
    }

    protected long exitTime;//记录第一次点击时的时间

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(RobotActivity.this, "再按一次退出智能聊天程序",
                        Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                RobotActivity.this.finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
