package json;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class JSONParser extends AsyncTask<Void, Void, String>{
	private String strUrl;

	public JSONParser(String strUrl) {
		this.strUrl = strUrl;
        System.out.println("URL: " + strUrl);
    }

	public String parse() {
		String response = null;
		try {
            // create HTTPURLConnection
            URL url = new URL(strUrl);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();

            // set connection properties
            connection.setReadTimeout(20 * 1000);
            connection.setConnectTimeout(25 * 1000);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);

            // then connect
            connection.connect();

            // get response from connection
            InputStream is = connection.getInputStream();
            response = convertStreamToString(is);

		} catch (Exception e) {
			response = null;
			e.printStackTrace();
		}

		return response;
	}

    @Override
    protected String doInBackground(Void... params) {
        String response = parse();
        return response;
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
