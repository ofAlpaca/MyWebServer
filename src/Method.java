public enum Method {
    GET("GET"),
    HEAD("HEAD"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE"),
    UNKNOW(null); // default value

    private final String method;

    Method(String method) {
        this.method = method;
    }
}