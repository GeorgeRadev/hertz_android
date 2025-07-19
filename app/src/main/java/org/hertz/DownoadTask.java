package org.hertz;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class DownoadTask extends AsyncTask<String, String, String> {

    static volatile boolean downloading = false;
    static volatile String status = "";

    @Override
    protected String doInBackground(String... uri) {
        HttpsURLConnection conn = null;
        try {
            downloading = true;
            status = "downloading";
            URL url = new URL("https://drive.usercontent.google.com/download?id=1Wamd3yu89eEzObsUpmbaOS0KR9_T4Z98&export=download");
            conn = (HttpsURLConnection) url.openConnection();

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String response;
                {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    {
                        InputStream is = conn.getInputStream();
                        byte[] buf = new byte[8192];
                        for (int len = 0; (len = is.read(buf)) != -1; ) {
                            baos.write(buf, 0, len);
                        }
                        is.close();
                    }
                    response = new String(baos.toByteArray());
                }

                JSONArray jsonResponse = new JSONArray(response);
                for (int i = 0, len = jsonResponse.length(); i < len; i++) {
                    JSONObject sequence = (JSONObject) jsonResponse.get(i);
                    String name = (String) sequence.get("name");
                    String frequencies = (String) sequence.get("frequencies");
                    String description = null;
                    if (sequence.has("description")) {
                        description = (String) sequence.get("description");
                    }
                    if (name != null && frequencies != null) {
                        Sequence s = new Sequence(name, description, frequencies);
                        SequencesManager.dbSequenceUpsert(s);
                    }
                }
                SequencesManager.sequencesReload();
                status = "download completed";
            }else{
                status = "response code: "+ responseCode;
            }
            return "OK";

        } catch (Exception e) {
            e.printStackTrace();
            status = "error: "+ e.getMessage();
            return e.getMessage();
        } finally {
            downloading = false;
            if (conn != null) {
                conn.disconnect();
            }
        }

    }
}
