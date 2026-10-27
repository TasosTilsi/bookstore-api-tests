package io.github.tasostilsi.bookstore.config;

public final class ApiConfig {

    private static final String DEFAULT_BASE_URI = "https://fakerestapi.azurewebsites.net";
    private static final String DEFAULT_CONNECT_TIMEOUT_MS = "5000";
    private static final String DEFAULT_SOCKET_TIMEOUT_MS = "10000";
    private static final String DEFAULT_LOG_ALL = "false";

    private ApiConfig() {
    }

    public static String baseUri() {
        return System.getProperty("api.baseUri",
                System.getenv().getOrDefault("API_BASE_URI", DEFAULT_BASE_URI));
    }

    public static int connectTimeoutMs() {
        return Integer.parseInt(System.getProperty("api.connectTimeoutMs", DEFAULT_CONNECT_TIMEOUT_MS));
    }

    public static int socketTimeoutMs() {
        return Integer.parseInt(System.getProperty("api.socketTimeoutMs", DEFAULT_SOCKET_TIMEOUT_MS));
    }

    public static boolean logAll() {
        return Boolean.parseBoolean(System.getProperty("api.logAll", DEFAULT_LOG_ALL));
    }
}
