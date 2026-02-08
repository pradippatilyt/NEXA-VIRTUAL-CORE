package android.app;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class ConnectionUtils {
    public static HttpURLConnection createPostConnection(String url) throws IOException {
        URL m_true = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) m_true.openConnection();
        return connection;
    }
}