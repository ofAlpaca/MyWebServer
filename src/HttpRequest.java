import HeaderField.HeaderField;
import HeaderField._RequestLine;
import HeaderField._Content_Length;
import HeaderField.SyntaxErrorException;
import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpRequest {
    Map<String, HeaderField> headers = new HashMap<String, HeaderField>();
    // Map<String, String> headers = new HashMap<String, String>();
    Method method;
    String uri;
    String redir_uri = "/";
    String version = "HTTP/1.1";
    String buffer;
    String clientErrorStatus = null ;

    public HttpRequest(InputStream in) throws IOException{
        BufferedReader input = new BufferedReader( new InputStreamReader(in) );
        HeaderParser hp = new HeaderParser();

        String str = input.readLine();
        try {
            _RequestLine rl = (_RequestLine) hp.parse(str, true);
            this.method = Method.valueOf(rl.getMethod());
            this.uri = rl.getUri();
            this.version = rl.getVersion();
           /*
            String[] splits = str.split("\\s");
            this.method = Method.valueOf(splits[0]);
            this.uri = splits[1];
            this.version = splits[2];

            matcher = MethodPattern.matcher(str);
            if (matcher.matches()) { // does the request line fit the syntax
                method = Method.valueOf(matcher.group(1).toUpperCase());
                //System.out.println(method.toString());
                uri = matcher.group(2);
                //System.out.println(uri);
                version = matcher.group(3);
                //System.out.println(version);
                if (!version.equals("1.1"))
                    clientErrorStatus = "_505"; // Not supported version
            } else
                clientErrorStatus = "_400"; // Bad request
            */

            System.out.println("Request -------------------- >\n" + str);
            str = input.readLine();
            HeaderField hf = null;
            while (!str.equals("")) {
                System.out.println(str);
                hf = hp.parse(str, false);
                headers.put(hp.h_field.toUpperCase(), hf);
                // String[] hf = str.split(": ");
                // headers.put(hf[0].toUpperCase(), hf[1]);
                str = input.readLine();
            }
            System.out.println("");

            if (!headers.containsKey("HOST")) // must have host header
                this.clientErrorStatus = "400";
        } catch(SyntaxErrorException e) {
            System.out.println("Got Error Code " + e.getMessage());
            this.clientErrorStatus = e.getMessage();
        }
        // get the payload of the request.
        _Content_Length length = (_Content_Length) headers.get("CONTENT-LENGTH");
        // String length = headers.get("CONTENT-LENGTH");
        if (length != null) { // Used for POST, PUT
            int l = Integer.valueOf(length.getLength());
            char[] b = new char[l];
            input.read(b); // This line should be message body of post request.
            buffer = String.valueOf(b);
            System.out.println(buffer);
        }

        // if redir_uri == null, no need to redirect.
        // if redir_uri != null, redirection is needed.
        this.redir_uri = isRedirect(uri);
    }

    private String isRedirect(String uri){
        List<String> listStr ;
        File access_table = new File("./access.txt");
        if (access_table.exists()){
            try {
                listStr = Files.readAllLines(access_table.toPath());
                for (String l:listStr) {
                    String[] splists = l.split(" ");
                    String rex = splists[0] ; // original uri
                    String redir = splists[1]; // new redirect uri

                    if (rex.equals(uri)){ // if the uri is match the regex.
                        System.out.println("Need 2 Redirect.");
                        method = Method.UNKNOW;
                        return redir;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
