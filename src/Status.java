public enum Status {
    // reference from RFC 7231
    _200("200 OK"),
    _201("201 Created"), // PUT Request
    _204("204 No Content"), // DELETE request
    _206("206 Partial Content"),
    _301("301 Moved Permanently"),
    _304("304 Not Modified"),
    _400("400 Bad Request"),
    _404("404 Not Found"),
    _412("412 Precondition Failed"),
    _416("416 Range Not Satisfiable"),
    _505("505 HTTP Version Not Support");

    private String status;

    Status(String status){
        this.status = status;
    }

    @Override
    public String toString(){
        return this.status;
    }
}
