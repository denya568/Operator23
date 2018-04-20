package wt23.ru.operator23;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    LinearLayout lay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        lay = (LinearLayout) findViewById(R.id.mainLay);

    }

    private class LoadBattlesAsync extends AsyncTask<Void, Void, Void> {
        int size;
        ArrayList<String> battle_id = new ArrayList<>();
        ArrayList<String> type = new ArrayList<>();
        ArrayList<String> category = new ArrayList<>();
        ArrayList<String> count_users = new ArrayList<>();
        ArrayList<String> date_start = new ArrayList<>();

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
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (size != -1) {

                for (int i = 0; i < size; i++) {
                    final int finalI = i;
                    TableLayout.LayoutParams lp = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    lp.setMargins(10, 10, 10, 10);
                    TableLayout tableLayout = new TableLayout(getApplicationContext());
                    tableLayout.setOrientation(LinearLayout.VERTICAL);
                    tableLayout.setLayoutParams(lp);

                    TableRow tableRow = new TableRow(getApplicationContext());
                    tableRow.setLayoutParams(new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    for (int j = 0; j < 5; j++) {
                        TextView tvType = new TextView(getApplicationContext());
                        tvType.setTextColor(Color.BLACK);
                        tvType.setText(type.get(i));

                        TextView tvCategory = new TextView(getApplicationContext());
                        tvCategory.setTextColor(Color.BLACK);
                        tvCategory.setText(category.get(i));

                        TextView tvCount = new TextView(getApplicationContext());
                        tvCount.setTextColor(Color.BLACK);
                        tvCount.setText(count_users.get(i));

                        TextView tvDate = new TextView(getApplicationContext());
                        tvDate.setTextColor(Color.BLACK);
                        tvDate.setText(date_start.get(i));

                        Button start = new Button(getApplicationContext());
                        start.setTextColor(Color.RED);
                        start.setText("Start");
                        start.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent openStream = new Intent(MainActivity.this, StreamActivity.class);
                                openStream.putExtra("battle_id", battle_id.get(finalI));
                            }
                        });


                    }


                }


            } else {
                Toast.makeText(getApplicationContext(), "No network", Toast.LENGTH_SHORT).show();
            }


        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }


}
