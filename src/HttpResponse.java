import HeaderField.*;
import sun.nio.cs.StandardCharsets;

import java.io.*;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpResponse {
    List<String> headers = new ArrayList<String>();
    HttpRequest request ;
    DatabaseConnection db = null;
    HashMap<String, String> request_body_table = new HashMap<>();
    byte[] body;

    public HttpResponse(HttpRequest req) throws IOException{
        db = new DatabaseConnection();
        db.getConnection();
        File f = null;

        this.request = req;
        if (req.clientErrorStatus == null) { // if there is no error in client request
            switch (req.method) {
                case HEAD:
                    makeHeaderStatus(Status._200);
                    break;
                case GET:
                    // check the authentication
                    if (true && isAuthenticated(req)) {
                        // confirm, find it's id and passwd correct.
                        directoryResponse(req);
                    } else {
                        // no record, must be new user.
                        htmlResponse("login");
                    }
                    break;
                case POST:
                    makeHeaderStatus(Status._200);

                    String[] attributes = req.buffer.split("&");
                    for (String att : attributes) {
                        String[] set = att.split("=");
                        request_body_table.put(set[0], set[1]); // key, value
                    }
                    /*
                    String regex = "username=(.+)&password=(.+)";
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(req.buffer);
                    matcher.find();
                    String usrname = matcher.group(1);
                    String passwd = matcher.group(2);
                    */
                    String usrname = request_body_table.get("username");
                    String passwd = request_body_table.get("password");

                    // Is the user in the database?
                    if (db.lookupUsr(usrname, passwd)) {
                        // login successfully
                        makeHeaderCookie(db.getHashCode(usrname)); // get the hash code as session authorization token.
                        // makeResponse("OKay, u r login");
                        htmlResponse("login_success");
                    } else {
                        // new user register
                        db.insert(usrname, passwd);
                        htmlResponse("redirect");
                    }
                    break;
                case PUT:
                    f = new File("." + req.uri);
                    if (!f.isDirectory()) {
                        boolean isExist = f.exists();
                        Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f.getPath())));
                        writer.write(req.buffer);
                        writer.close();
                        if (isExist) { // if the file exist, update it and response 200 OK.
                            makeHeaderStatus(Status._200);
                            makeResponse(Status._200.toString());
                        } else { // file does not exist, create a new one.
                            makeHeaderStatus(Status._201);
                            makeResponse(Status._201.toString());
                        }
                    }
                    break;
                case DELETE:
                    f = new File("." + req.uri);
                    if (f.exists() && !f.isDirectory()) {
                        f.delete();
                        makeHeaderStatus(Status._200);
                        makeResponse(Status._200.toString());
                    } else {
                        makeHeaderStatus(Status._204);
                        makeResponse(Status._204.toString());
                    }
                    break;
                default:
                    makeHeaderStatus(Status._301);
                    makeHeaderField("Location", "code_301.html");
                    // makeHeaderField("Cache-Control", "no-store");
                    // dynamicResponse("code_301", req.headers.get("Referer".toUpperCase()));
                    htmlResponse("code_301");

            }
        } else { // if there is a error status, then response it.
            makeHeaderStatus(Status.valueOf("_" + req.clientErrorStatus));
            makeResponse(Status.valueOf("_" + req.clientErrorStatus).toString());
        }
    }

    // Header + Body
    // this method is used to provide the html page.
    private void htmlResponse(String filename) throws IOException {
        File f = new File("./" + filename + ".html");
        makeHeaderStatus(Status._200);
        makeHeaderType("HTML");
        makeResponse(Files.readAllBytes(f.toPath()));
    }

    // Header + Body
    // this method is to provide directory html page.
    private void directoryResponse(HttpRequest req){
        try {
            File file = new File("." + req.uri);

            // specify the file directory.
            if (file.isDirectory()) {
                makeHeaderStatus(Status._200);
                makeHeaderType("HTML");
                StringBuilder content = new StringBuilder();
                _Cookie cookie_h = (_Cookie) req.headers.get("COOKIE");
                String usr = getUsername(cookie_h.getValue("SESSION"));
                content.append("<!DOCTYPE html>");
                content.append("<html><head><title>Index of "
                        + req.uri
                        + "</title><script src=\"https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js\"></script></head>"
                        + "<body><h1>Index of "
                        + req.uri
                        + "</h1>"
                        + "<p>Welcome, user \""
                        + usr
                        + "\"</p><hr><ol>");
                File[] subls = file.listFiles();
                for (File subfile : subls) {
                    content.append("<li><a href=\"" + subfile.getPath() + "\">" + subfile.getName() + "</a></li>");
                }
                content.append("</ol></body></html>");
                makeResponse(content.toString());
            } else if (file.exists()) {

                Date f_mod = getFileGMT(file);
                String f_rtag = makeEtag(file);
                _If_Modified_Since if_mod_since = (_If_Modified_Since) req.headers.get("IF-MODIFIED-SINCE");
                _If_None_Match if_none_match = (_If_None_Match) req.headers.get("IF-NONE-MATCH");
                _If_Match if_match = (_If_Match) req.headers.get("IF-MATCH");
                _If_Unmodified_Since if_unmodified_since = (_If_Unmodified_Since) req.headers.get("IF-UNMODIFIED-SINCE");
                _If_Range if_range = (_If_Range) req.headers.get("IF-RANGE");
                _Range range = (_Range) req.headers.get("RANGE");

                if ( if_range != null && range != null )
                    if( (if_range.isEtag() && if_range.getEtag().equals(f_rtag)) ||
                            (!if_range.isEtag() && if_range.getMod_date().compareTo(f_mod) == 0 )){
                        byte[] f_content = Files.readAllBytes(file.toPath());
                        int content_size = f_content.length;
                        range.updateBoundary(content_size);
                        System.out.println("Partial Response needed");
                        if (range.isBoundaryLegal()) { // ignore the if-range request
                            if (!range.isBoundarySatisfiable(content_size)) {
                                makeHeaderStatus(Status._416);
                                makeResponse(Status._416.toString());
                                return;
                            } else {
                                String[] ranges = range.getRanges(0);
                                int first_pos = Integer.valueOf(ranges[0]);
                                int last_pos = Integer.valueOf(ranges[1]);
                                byte[] range_byte = Arrays.copyOfRange(f_content, first_pos, last_pos);
                                makeHeaderStatus(Status._206);
                                String ext = req.uri.substring(req.uri.indexOf(".") + 1);
                                makeHeaderType(ext);
                                makeHeaderField("Etag", makeEtag(file));
                                makeHeaderField("Last-Modified", makeLastMod(file));
                                makeHeaderField("Content-Range", "bytes " + ranges[0] + "-" + ranges[1] +
                                        "/" + content_size);
                                makeResponse(range_byte);
                                return;
                            }
                        }
                }

                if ( if_none_match != null && if_none_match.isMatchEtag(f_rtag) ) {
                    makeHeaderStatus(Status._304);
                    makeResponse(Status._304.toString());
                    System.out.println("No need 2 fresh by Etag");
                    return; // No need to go continue.
                }
                else if ( if_mod_since != null && !f_mod.after(if_mod_since.getMod_date())) { // Cache: no need to update cause it did not expire.
                    makeHeaderStatus(Status._304);
                    makeResponse(Status._304.toString());
                    System.out.println("No need 2 fresh by Last Modified");
                    return; // No need to go continue.
                } else if (if_match != null && if_match.notMatchEtag(f_rtag)) {
                    makeHeaderStatus(Status._412);
                    makeResponse(Status._412.toString());
                    System.out.println("Etag is not same");
                    return; // No need to go continue.
                } else if (if_unmodified_since != null && f_mod.after(if_unmodified_since.getMod_date())) {
                    makeHeaderStatus(Status._412);
                    makeResponse(Status._412.toString());
                    System.out.println("Modified Since is expired");
                    return; // No need to go continue.
                }

                makeHeaderStatus(Status._200);
                String ext = req.uri.substring(req.uri.indexOf(".") + 1);
                makeHeaderType(ext);
                makeHeaderField("Etag", makeEtag(file));
                makeHeaderField("Last-Modified", makeLastMod(file));
                try {
                    makeResponse(Files.readAllBytes(file.toPath()));
                    // make the content of the file into the html page.
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else { // Unknowing file directory.
                makeHeaderStatus(Status._404);
                makeResponse(Status._404.toString());
            }
        }catch (Exception e) {
            e.printStackTrace();
            makeHeaderStatus(Status._400);
            makeResponse(Status._400.toString());
        }
    }

    // response through the socket.
    public void response(OutputStream out) throws IOException{
        DataOutputStream output =  new DataOutputStream(out);
        System.out.println("Response -------------------->");
        System.out.println(this.headers);
        for(String h : this.headers)
            output.writeBytes(h + "\r\n");

        output.writeBytes("\r\n");
        if (this.request.method != Method.HEAD) {
            output.write(this.body);

            System.out.println(new String(this.body, "BIG5")+"\n");
        }
        output.flush();
    }

    private void makeResponse(String content){
        this.body = content.getBytes();
    }

    private void makeResponse(byte[] content){
        this.body = content;
    }

    private void makeHeaderStatus(Status status){
        headers.add(this.request.version + " " + status.toString()); // status line

        SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss", Locale.ENGLISH);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date gmt_time = new Date();
        String t = format.format(gmt_time);
        headers.add("Date: " + t + " GMT");
        // ex. Mon, 11 Dec 2017 18:54:57 GMT

        headers.add("Server: Shau-Shian WebServer");
        headers.add("Cache-Control: max-age=0");
        headers.add("Accept-Ranges: bytes");
        _Connection con = (_Connection) request.headers.get("CONNECTION");
        if (con != null && request.clientErrorStatus == null && con.getCon().toUpperCase().equals("KEEP-ALIVE"))
            headers.add("Connection: keep-alive");
        else
            headers.add("Connection: closed");
    }

    private void makeHeaderCookie(String session){
        headers.add("Set-Cookie: " + "SESSION=" + session ) ;// + "; Secure; HttpOnly");
    }

    private void makeHeaderType(String ext){
        String type = ContentType.valueOf(ext.toUpperCase()).toString();
        headers.add(type);
    }

    private void makeHeaderField(String field, String content){
        String data = field + ": " + content;
        headers.add(data);
    }

    private boolean isAuthenticated(HttpRequest req){
        _Cookie cookie_hash = (_Cookie) req.headers.get("COOKIE");
        if (cookie_hash != null) {
            String session_id = cookie_hash.getValue("SESSION");
            if (session_id != null)
                if (db.lookupSession(session_id))
                    return true;
        }
        return false;
    }

    private String makeEtag(File file) {
        MessageDigest alg = null;
        byte[] content = null;
        try {
             content = Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            alg = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.out.println("illegal algorithm");
        }
        alg.update(content);

        byte[] b = alg.digest();
        String stmp, hs = "";
        for (int n = 0 ; n < b.length ; n++) {
            stmp = Integer.toHexString(b[n] & 0XFF);
            if (stmp.length() == 1)
                hs += "0" + stmp;
            else
                hs += stmp;
        }

        return hs.toUpperCase();
    }

    private String makeLastMod(File file) {
        long last_mod = file.lastModified();
        SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss", Locale.ENGLISH);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date d = new Date(last_mod);
        String mod_date = format.format(d);
        return mod_date;
    }

    private String getUsername(String session_id) {
        if (session_id != null)
            return db.getUsrname(session_id); // used the session id to search from database
        return null;
    }

    private Date getFileGMT(File file) {
        String file_CST = makeLastMod(file);
        SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss", Locale.ENGLISH);
        try {
            return format.parse(file_CST);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
