package HeaderField;


public class _RequestLine extends HeaderField {
    String method;
    String uri;
    String version;

    public _RequestLine(String str) throws SyntaxErrorException {
        super(str);

        if (peek().toString().equals("GET") || peek().toString().equals("POST") || peek().toString().equals("DELETE") ||
                peek().toString().equals("PUT") || peek().toString().equals("HEAD")) {
            this.method = next().toString();
        } else
            throw new SyntaxErrorException("400");


        this.uri = next().toString();

        this.version = next().toString();
        if (!this.version.equals("HTTP/1.1")) // the version is not supported
            throw new SyntaxErrorException("505");
    }

    public String getMethod() {return method;}
    public String getUri() {return uri;}
    public String getVersion() {return version;}
}
