public class Response {
    String content;
    String connectionStatus;

    public Response(String content) {
        this.content = content;
        this.connectionStatus = "alive";
    }

    public void setUnavailable() {
        this.connectionStatus = "unavailable";
    }
}
