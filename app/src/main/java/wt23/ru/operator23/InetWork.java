package wt23.ru.operator23;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class InetWork {
    String adress = "https://wt23.ru/api/";

    int size = 0;
    private ArrayList<String> battle_id = new ArrayList<>();
    private ArrayList<String> type = new ArrayList<>();
    private ArrayList<String> name = new ArrayList<>();
    private ArrayList<String> category = new ArrayList<>();
    private ArrayList<String> count_users = new ArrayList<>();
    private ArrayList<String> date_start = new ArrayList<>();
    private ArrayList<String> status = new ArrayList<>();

    public ArrayList<String> getBattle_id() {
        return battle_id;
    }

    public ArrayList<String> getType() {
        return type;
    }

    public ArrayList<String> getName() {
        return name;
    }

    public ArrayList<String> getCategory() {
        return category;
    }

    public ArrayList<String> getCount_users() {
        return count_users;
    }

    public ArrayList<String> getDate_start() {
        return date_start;
    }

    public ArrayList<String> getStatus() {
        return status;
    }

    public int getSize() {
        return size;
    }

    public void getStreamBattles() {
        getStreamBattlesJSON();
    }

    public void startStreamBattle(String battleID) {
        startStreamBattlePOST(battleID);
    }


    private void getStreamBattlesJSON() {
        try {
            URL url = new URL(adress + "get_stream_battles");
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            //cert
            SSLContext sslContext = SSLContext.getInstance("TLS");
            TrustManager[] trustManagers = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };
            HostnameVerifier hostnameVerifier = new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return s.equals(sslSession.getPeerHost());
                }
            };
            conn.setHostnameVerifier(hostnameVerifier);
            sslContext.init(null, trustManagers, null);
            conn.setSSLSocketFactory(sslContext.getSocketFactory());
            //cert
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(50000);
            conn.connect();

            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(in);
            String line;

            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            String text = sb.toString();

            JSONArray dataJSON = new JSONArray(text);
            for (int i = 0; i < dataJSON.length(); i++) {
                JSONObject columns = dataJSON.getJSONObject(i);

                this.battle_id.add(columns.getString("battle_id"));
                this.type.add(columns.getString("type"));
                this.name.add(columns.getString("name_battle"));
                this.category.add(columns.getString("category"));
                this.count_users.add(columns.getString("count_users"));
                this.date_start.add(columns.getString("date_start"));
                this.status.add(columns.getString("status"));

            }
            this.size = dataJSON.length();

            conn.disconnect();
            br.close();
            in.close();
        } catch (Exception e) {
            this.size = -1;
        }
    }

    private void startStreamBattlePOST(String battleID) {
        try {
            URL url = new URL(adress + "start_stream_battle");
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            //cert
            SSLContext sslContext = SSLContext.getInstance("TLS");
            TrustManager[] trustManagers = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };
            HostnameVerifier hostnameVerifier = new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return s.equals(sslSession.getPeerHost());
                }
            };
            conn.setHostnameVerifier(hostnameVerifier);
            sslContext.init(null, trustManagers, null);
            conn.setSSLSocketFactory(sslContext.getSocketFactory());
            //cert
            conn.setRequestMethod("POST");
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(5000);
            conn.connect();

            String params = "battle_id=" + battleID;
            //params = new String(params.getBytes("UTF-8"), "windows-1251");
            DataOutputStream out = new DataOutputStream(conn.getOutputStream());
            out.writeBytes(params);

            out.flush();
            out.close();

            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            this.size = conn.getResponseCode();

            conn.disconnect();
            in.close();

        } catch (Exception e) {
            this.size = 404;
        }
    }


}
