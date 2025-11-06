package tech.id.kasir.api;

public class TokenRequest {
    private final int user_id;
    private final String token;

    public TokenRequest(int user_id, String token) {
        this.user_id = user_id;
        this.token = token;
    }

    public int getUser_id() {
        return user_id;
    }

    public String getToken() {
        return token;
    }
}