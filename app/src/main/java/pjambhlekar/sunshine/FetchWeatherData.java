package pjambhlekar.sunshine;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FetchWeatherData extends AsyncTask<String, Void, String[]>
{
    private final String LOG_TAG = FetchWeatherData.class.getSimpleName();

    @Override
    protected String[] doInBackground (String... params )
    {

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;

        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are avaiable at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast
            final String URL_BASE = "http://api.openweathermap.org/data/2.5/forecast/daily?";
            final String URL_PARAM_Q = "q";
            final String URL_PARAM_MODE = "mode";
            final String URL_PARAM_UNITS = "units";
            final String URL_PARAM_CNT = "cnt";
            final String URL_PARAM_APPID = "appid";
            final String APPID = "a9855aa59e38001a35dd93aa9811ecaa";

            String mode = "json";
            String units = params[1];
            String cnt = "7";

            Uri buildUri = Uri.parse(URL_BASE).buildUpon()
                    .appendQueryParameter(URL_PARAM_Q,params[0])
                    .appendQueryParameter(URL_PARAM_CNT,cnt)
                    .appendQueryParameter(URL_PARAM_MODE,mode)
                    .appendQueryParameter(URL_PARAM_UNITS,units)
                    .appendQueryParameter(URL_PARAM_APPID,APPID)
                    .build();
            URL url = new URL(buildUri.toString());
            Log.v(LOG_TAG, "URL is " + buildUri.toString());
            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            forecastJsonStr = buffer.toString();
            Log.v(LOG_TAG, "JSON output is " + forecastJsonStr);
            WeatherDataParser dataParser = new WeatherDataParser();
            try {
                return dataParser.getWeatherDataFromJson(forecastJsonStr, 7);
            }
            catch (JSONException e) {}
        } catch (IOException e) {
            Log.e("PlaceholderFragment", "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
            return null;
        } finally{
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e("PlaceholderFragment", "Error closing stream", e);
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String[] strings) {
        if (strings != null)
        {
            ForecastActivityFragment.mForecastAdapter.addAll(strings);
        }
    }
}