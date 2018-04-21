package wt23.ru.operator23;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    LinearLayout lay;
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);

        lay = (LinearLayout) findViewById(R.id.mainLay);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                lay.removeAllViews();
                LoadBattlesAsync loadBattlesAsync = new LoadBattlesAsync();
                loadBattlesAsync.execute();
            }
        });

        LoadBattlesAsync loadBattlesAsync = new LoadBattlesAsync();
        loadBattlesAsync.execute();

        /*Intent openStream = new Intent(MainActivity.this, StreamActivity.class);
        startActivity(openStream);*/

    }

    private class LoadBattlesAsync extends AsyncTask<Void, Void, Void> {
        int size;
        ArrayList<String> battle_id = new ArrayList<>();
        ArrayList<String> type = new ArrayList<>();
        ArrayList<String> category = new ArrayList<>();
        ArrayList<String> count_users = new ArrayList<>();
        ArrayList<String> date_start = new ArrayList<>();
        ArrayList<String> status = new ArrayList<>();

        @Override
        protected Void doInBackground(Void... voids) {
            InetWork inetWork = new InetWork();
            inetWork.getStreamBattles();
            size = inetWork.getSize();
            if (size == -1) {

            } else {
                battle_id = inetWork.getBattle_id();
                type = inetWork.getType();
                category = inetWork.getCategory();
                count_users = inetWork.getCount_users();
                date_start = inetWork.getDate_start();
                status = inetWork.getStatus();
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            swipeRefreshLayout.setRefreshing(false);

            if (size != -1) {
                for (int i = 0; i < size; i++) {
                    final int finalI = i;
                    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                    int width = (int) (displayMetrics.widthPixels / 5);
                    int height = (int) (displayMetrics.heightPixels / 5);
                    TableLayout.LayoutParams lp = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    lp.setMargins(10, 10, 10, 10);
                    TableLayout tableLayout = new TableLayout(getApplicationContext());
                    if (status.get(i).equalsIgnoreCase("ACTIVE")) {
                        tableLayout.setBackgroundColor(getResources().getColor(R.color.colorSiteGreen));
                    } else if (status.get(i).equalsIgnoreCase("EXECUTION")) {
                        tableLayout.setBackgroundColor(getResources().getColor(R.color.colorSiteRedPrizrak));
                    }
                    tableLayout.setOrientation(LinearLayout.VERTICAL);
                    tableLayout.setLayoutParams(lp);

                    TableRow tableRow = new TableRow(getApplicationContext());
                    tableRow.setLayoutParams(new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    TextView tvType = new TextView(getApplicationContext());
                    tvType.setTextColor(Color.WHITE);
                    tvType.setText(type.get(i));
                    tvType.setTextSize(23);
                    tvType.setWidth(width);
                    tvType.setHeight(height);
                    tvType.setGravity(Gravity.CENTER);
                    tableRow.addView(tvType, 0);

                    TextView tvCategory = new TextView(getApplicationContext());
                    tvCategory.setTextColor(Color.WHITE);
                    tvCategory.setText(category.get(i));
                    tvCategory.setWidth(width);
                    tvCategory.setHeight(height);
                    tvCategory.setTextSize(23);
                    tvCategory.setGravity(Gravity.CENTER);
                    tableRow.addView(tvCategory, 1);

                    TextView tvCount = new TextView(getApplicationContext());
                    tvCount.setTextColor(Color.WHITE);
                    tvCount.setText(count_users.get(i));
                    tvCount.setWidth(width);
                    tvCount.setHeight(height);
                    tvCount.setTextSize(23);
                    tvCount.setGravity(Gravity.CENTER);
                    tableRow.addView(tvCount, 2);

                    TextView tvDate = new TextView(getApplicationContext());
                    tvDate.setTextColor(Color.WHITE);
                    tvDate.setText(date_start.get(i));
                    tvDate.setWidth(width);
                    tvDate.setHeight(height);
                    tvDate.setTextSize(23);
                    tvDate.setGravity(Gravity.CENTER);
                    tableRow.addView(tvDate, 3);

                    Button start = new Button(getApplicationContext());
                    start.setTextColor(Color.RED);
                    start.setText("Start");
                    start.setTextSize(23);
                    start.setGravity(Gravity.CENTER);
                    start.setWidth(width);
                    start.setHeight(height / 2);
                    start.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent openStream = new Intent(MainActivity.this, StreamActivity.class);
                            openStream.putExtra("battle_id", battle_id.get(finalI));
                            startActivity(openStream);
                        }
                    });
                    tableRow.addView(start, 4);

                    tableLayout.addView(tableRow);
                    lay.addView(tableLayout);
                }

            } else {
                Toast.makeText(getApplicationContext(), "No data", Toast.LENGTH_SHORT).show();
            }


        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }


}
