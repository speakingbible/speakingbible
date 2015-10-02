package org.speakingbible.speakingbible;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    final static int PERIODICITY = 1000;
    Handler mHandler;

    final static int BUTTON_ID_BASE = 1000;
    ArrayList<Button> btns = new ArrayList<Button>();

    static int currentWord;

    final static int STATE_UNHANDLED = 0;
    final static int STATE_IN_PROGRESS = 1;
    final static int STATE_HANDLED = 2;



    class TextUnit {
        public int index;
        public String word;
        public int begin;
        public int end;
        public int state;

        public TextUnit(int index, String word, int begin, int end, int state) {
            this.index = index;
            this.word = word;
            this.begin = begin;
            this.end = end;
            this.state = state;
        }
    }
    ArrayList<TextUnit> textList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button prepStart = (Button)findViewById(R.id.prepStart);
        prepStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);

                Button stop = (Button)findViewById(R.id.stop);
                stop.setEnabled(true);

                new PrepStartWorker().execute();
            }
        });

        Button stop = (Button)findViewById(R.id.stop);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTimer();

                Button prepStart = (Button)findViewById(R.id.prepStart);
                prepStart.setEnabled(true);

                v.setEnabled(false);
            }
        });
    }

    class PrepStartWorker extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] params) {

            textList = new ArrayList<TextUnit>();

         /*   try {
                InputStream cmuFile = getResources().openRawResource(R.raw.jn1trunc);

                BufferedReader r = new BufferedReader(new InputStreamReader(cmuFile));
                String line;
                String actualWord = "";
                int start = 0;
                int end = 0;
                String split[];

                int index;
                int count = 0;
                while ((line = r.readLine()) != null) {
                    if(line.startsWith("-")) {
                        actualWord = line.substring(2, line.length());
                        start = 0;
                        end = 0;
                    }
                    else {
                        index = line.lastIndexOf("[");
                        if(index > 0) {
                            split = line.substring(index + 1, line.length() - 1).split(":");
                            start = Integer.valueOf(split[0]);
                            end = Integer.valueOf(split[1]);
                            actualWord = line.substring(0, index - 1).trim();
                        }
                    }

                    textList.add(new TextUnit(count, actualWord, start, end, STATE_UNHANDLED));
                    count++;
                }

                cmuFile.close();

                for(int i=0;i<textList.size();i++) {
                    Log.d("textUnit", "i = " + i + ", index: " + textList.get(i).index
                            + ", word: " + textList.get(i).word
                            + ", begin: " + textList.get(i).begin
                            + ", end: " + textList.get(i).end);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } */

            InputStream is = getResources().openRawResource(R.raw.word);
            Writer writer = new StringWriter();
            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }

                is.close();
            }
            catch (UnsupportedEncodingException e){
                e.printStackTrace();
            }
            catch (IOException io){
                io.printStackTrace();

            }

            String jsonString = writer.toString();

            try {

                JSONObject table = new JSONObject(jsonString);
                JSONArray jarray = table.getJSONArray("Verses");

                for (int i = 0; i < jarray.length(); i++) {
                    String j1 = jarray.getString("word");
                    String j2 = jarray.getString(1);
                    String j3 = jarray.getString(2);

                   Log.d("JSON", "line: " + j1 + ", " + j2 + ", " + j3);



                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.d("TAG", jsonString);

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            if(textList != null && textList.size() > 0) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.main_relative_layout);
                for (int i = 0; i < textList.size(); i++) {
                    Button word = new Button(MainActivity.this);
                    word.setId(BUTTON_ID_BASE + i);
                    word.setText(textList.get(i).word);
                    layout.addView(word);    //add button into the layout dynamically
                    btns.add(word);
                }
                layout.post(new Runnable() {    //post a Runnable that call reLayout to layout object
                    @Override
                    public void run() {
                        reLayout();
                    }
                });
            }
            initTimer();
        }
    }

    public void initTimer() {
        currentWord = 0;
        mHandler = new Handler();
        mHandler.postDelayed(mRunnable, PERIODICITY);
    }

    private Runnable mRunnable = new Runnable() {

        @Override
        public void run() {
            TextView msg = (TextView)findViewById(R.id.msg);
            msg.setText(String.valueOf(System.currentTimeMillis()));
            if(textList != null && textList.size() > 0 && currentWord < textList.size()) {
                if(textList.get(currentWord).begin != 0) {
                    if(textList.get(currentWord).state == STATE_UNHANDLED) {
                        textList.get(currentWord).state = STATE_IN_PROGRESS;
                        btns.get(currentWord).setBackgroundColor(Color.parseColor("#00FF00"));
                    }
                    else if(textList.get(currentWord).state == STATE_IN_PROGRESS) {
                        textList.get(currentWord).state = STATE_HANDLED;
                        btns.get(currentWord).setBackgroundColor(Color.parseColor("#800000"));

                        currentWord++;
                    }
                }
                else {
                    currentWord++;
                }
            }
            /** Do something **/
            mHandler.postDelayed(mRunnable, PERIODICITY);
        }
    };

    public void stopTimer() {
        mHandler.removeCallbacks(mRunnable);
    }

    public void resumeTimer() {
        mHandler.postDelayed(mRunnable, 1000);
    }

    protected void reLayout() {
        int totalWidth;
        int curWidth;
        int layoutPadX;
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.main_relative_layout);
        int w = layout.getMeasuredWidth();  //get width of current layout
        totalWidth = 0;
        layoutPadX = layout.getPaddingLeft() + layout.getPaddingRight();
        w = w - layoutPadX;
        Button upBtn = null, leftBtn = btns.get(0);
        for (int i = 0; i < btns.size(); i++) {
            //create a layout parameter first
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            curWidth = btns.get(i).getMeasuredWidth(); //get the width, beware of the caller site
            if (i > 0) {
                lp.addRule(RelativeLayout.END_OF, btns.get(i - 1).getId()); //add END_OF property by default.
                if (totalWidth + curWidth > w) {	//check if need to wrap
                    upBtn = leftBtn;
                    leftBtn = btns.get(i);
                    totalWidth = curWidth;
                    lp.removeRule(RelativeLayout.END_OF);   //remove the END_OF for wrap case
                } else {
                    totalWidth += curWidth;
                }
                if (upBtn != null)  //add below property for none-first "line"
                    lp.addRule(RelativeLayout.BELOW, upBtn.getId());
            } else {
                totalWidth += curWidth;
            }
            btns.get(i).setLayoutParams(lp);	//set layout parameter for button
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

