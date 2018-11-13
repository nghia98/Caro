package com.dangngocnghia.xmlreading;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.renderscript.ScriptGroup;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.Buffer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static android.support.v4.content.res.TypedArrayUtils.getResourceId;

public class MainActivity extends Activity {

    Button btnInfo;
    TextView txtPercent;
    TextView txtInfo;
    ProgressBar progressBar;
    ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ánh xạ các widget
        btnInfo = (Button)findViewById(R.id.btnInfo);
        txtPercent = (TextView)findViewById(R.id.txtPercent);
        txtInfo = (TextView)findViewById(R.id.txtInfo);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        scrollView = (ScrollView)findViewById(R.id.scrollView);

        // Xử lý sự kiện ấn button
        btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnInfo.setEnabled(false);
                new backgroundAsyncTask().execute(R.raw.players, "Name", "Phone"); }
        });
    }

    public class backgroundAsyncTask extends AsyncTask<Object, String, String> {

        StringBuilder str = new StringBuilder();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            btnInfo.setEnabled(true);
        }

        @Override
        protected String doInBackground(Object... objects) {

            // Số tham số được truyền vào (ID resource của file và các tag)
            int n = objects.length;

            // Lấy ID resource của file
            int fileID = (int) objects[0];

            // Lấy danh sách các tag cần hiển thị
            String[] tagName = new String[n - 1];
            for(int i = 0; i < n - 1; i++) tagName[i] = (String) objects[i + 1];

            // Mở file và tạo DocumentBuilder
            try {

                // Tạo InputStream của file
                InputStream is = getResources().openRawResource(fileID);

                // Tạo DocumentBuilder để dùng W3C
                DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance()
                        .newDocumentBuilder();
                Document document = documentBuilder.parse(is);

                if (document == null) {
                    return "Error Parsing File!";
                }

                // Tạo NodeList cho từng tag
                NodeList[] elementList = new NodeList[n];

                // Xử lý "tạm" để biết tổng số lượng <element> để set up cho progress bar
                int max_progress = 0;
                for(int i = 0; i < n - 1; i++) {

                    // Lấy danh sách các <element> đối với từng tag (Khởi tạo các node)
                    elementList[i] = document.getElementsByTagName(tagName[i]);

                    // Tính tổng toàn bộ <element> của tất cả các tag
                    max_progress = max_progress + elementList[i].getLength();
                }

                // Set up cho progress bar (phải làm ở hàm onProgressUpdate
                publishProgress(str.toString(), String.valueOf(max_progress));

                // Xử lý "thật"
                for(int i = 0; i < n - 1; i++)
                {
                    // Thêm vào chuỗi các thuộc tính và text của từng <element> trong từng danh sách
                    getTextAndAttributesFromNode(elementList[i], tagName[i]);
                }

            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return str.toString();
        }

        private Object getTextAndAttributesFromNode(NodeList list, String tagName) throws InterruptedException {

            // Xử lý danh sách <element> của tagName
            if (str.length() > 0) str.append("\n\n");
            str.append("NodeList for: <" + tagName + "> Tag");

            // Hiển thị ra màn hình
            publishProgress(str.toString(), "0"); // Tham số thứ 2 bằng "0" tức không cần tăng progress bar
            Thread.sleep(10);

            for(int i = 0; i < list.getLength(); i++)
            {
                // Lấy từng <element>
                Node node = list.item(i);

                // Lấy text
                String text = node.getTextContent();
                str.append("\n " + String.valueOf(i/10) + String.valueOf(i%10) + ": " + text);

                // Hiển thị ra màn hình
                publishProgress(str.toString(), "1"); // Tham số thứ 2 bằng "1" tức cần tăng progress bar
                Thread.sleep(10);

                // Lấy thuộc tính
                int size = node.getAttributes().getLength();
                for(int j = 0; j < size; j++)
                {
                    // Tên thuộc tính và giá trị thuộc tính
                    String attrName = node.getAttributes().item(j).getNodeName();
                    String atrrValue = node.getAttributes().item(j).getNodeValue();
                    str.append("\n attr. info-"
                            + String.valueOf(i/10)
                            + String.valueOf(i%10)
                            + "-" + j + ": "
                            + attrName + " " + atrrValue);

                    // Hiển thị ra màn hình
                    publishProgress(str.toString(), "0");
                    Thread.sleep(10);
                }
            }
            return str;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            int arg2nd = Integer.parseInt(values[1]);
            switch (arg2nd)
            {
                // Trường hợp tham số thứ 2 là "1" hoặc "0" thì hiển thị text
                // Riêng "1" thì có tăng progress bar
                case 1: progressBar.incrementProgressBy(1);
                        txtPercent.setText(String
                                .valueOf( 100 * progressBar.getProgress() / progressBar.getMax())
                                + "%");
                case 0: txtInfo.setText(values[0]);
                        scrollView.fullScroll(View.FOCUS_DOWN);
                        break;
                // Trường hợp còn lại, tham số thứ 2 là max_progress của progress bar
                default: progressBar.setProgress(0);
                        progressBar.setMax(arg2nd);
                        break;
            }
        }
    }
}
//Xong rồi
