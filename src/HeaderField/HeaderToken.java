package HeaderField;

public class HeaderToken {
    private String oneToken;

    public HeaderToken(String token) {
        this.oneToken = token;
    }

    @Override
    public String toString () {
        return this.oneToken;
    }
}
