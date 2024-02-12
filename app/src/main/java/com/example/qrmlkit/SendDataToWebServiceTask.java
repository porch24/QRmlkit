package com.example.qrmlkit;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

public class SendDataToWebServiceTask extends AsyncTask<String, String, Boolean> {

    private final WeakReference<TextView> resultIvReference;
    private String PUID;

    public SendDataToWebServiceTask(TextView resultIv) {
        this.resultIvReference = new WeakReference<>(resultIv);
    }

    @Override
    protected Boolean doInBackground(String... params) {
        // Your web service URL
        String webServiceUrl = "http://194.10.10.15/ServiceHana/PUIDChecking.svc/rest/PUIDCheck/";

        try {
            String PUID = params[0];
            this.PUID = PUID;
            // Use Uri.Builder to build the URL
            Uri.Builder builder = Uri.parse(webServiceUrl).buildUpon();
            builder.appendPath(PUID);
            String urlString = builder.build().toString();

            URL url = new URL(urlString);

            // Open connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set request method to GET
            connection.setRequestMethod("GET");

            // Enable input stream
            connection.setDoInput(true);

            // Get the response code
            int responseCode = connection.getResponseCode();

            // Check if the request was successful (HTTP status code 200)
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read the response
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Process the response as JSON
                try {
                    JSONObject jsonResponse = new JSONObject(response.toString());

                    // Extract data from JSON
                    String customer = jsonResponse.optString("Customer", "Not Found");
                    String hanaPart = jsonResponse.optString("HanaPart", "Not Found");
                    String description = jsonResponse.optString("Description", "Not Found");
                    String qty = jsonResponse.optString("Qty", "Not Found");
                    String receiveDate = jsonResponse.optString("ReceiveDate", "Not Found");

                    // Build the result string
                    String displayResult = "Customer: " + customer + "\n"
                            + "HanaPart: " + hanaPart + "\n"
                            + "Description: " + description + "\n"
                            + "Qty: " + qty + "\n"
                            + "ReceiveDate: " + receiveDate + "\n";

                    // Log to see the result in Logcat
                    Log.d("SendDataToWebService", "Response: " + displayResult);

                    // Update UI using onProgressUpdate within doInBackground
                    publishProgress(displayResult);

                    return true;
                } catch (JSONException e) {
                    Log.e("SendDataToWebService", "JSONException: " + e.getMessage());
                    e.printStackTrace();
                    // Handle the exception or show an error message
                }
            } else {
                Log.e("SendDataToWebService", "Error sending data. Response Code: " + responseCode);
            }

            // Close the connection
            connection.disconnect();
        } catch (Exception e) {
            Log.e("SendDataToWebService", "Exception: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Check for UI updates within doInBackground
    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        // Get TextView through WeakReference
        TextView resultIv = resultIvReference.get();
        if (resultIv != null) {
            // Update text in TextView
            resultIv.setText(values[0]);
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);

        if (!result) {
            // Handle failure case, show an error message or take appropriate action
            Log.e("SendDataToWebService", "Failed to retrieve data from web service");
            TextView resultIv = resultIvReference.get();
            if (resultIv != null) {
                resultIv.setText("Failed to retrieve data from web service");
            }
        } else {
            // Success case, you may perform additional actions here if needed
        }
    }
}
