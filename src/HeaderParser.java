import HeaderField.*;

public class HeaderParser {
    String h_field = null;

    public HeaderField parse(String str, boolean isReqLine) throws SyntaxErrorException{
    // decide the header field's type and then parse it.
        h_field = "";
        int ch = 0 ;
        if (!isReqLine) { // headers : header_content
            while (str.charAt(ch) != ':' && ch < str.length()) { // get the header field attributes
                h_field += str.charAt(ch);
                ch++;
            }

            if (ch == str.length()) // should get the ':' but no
                throw new SyntaxErrorException("400");
            else
                str = str.substring(ch+1); // after the char of ':'
        }
        if (isReqLine) { // parse request line
            return new _RequestLine(str);
        } else {
            switch (h_field.toUpperCase()) {
                case "HOST":
                    return new _Host(str);
                case "ACCEPT":
                    return new _Accept(str);
                case "ACCEPT-CHARSET":
                    return new _Accept_Charset(str);
                case "ACCEPT-ENCODING":
                    return new _Accept_Encoding(str);
                case "ACCEPT-LANGUAGE":
                    return new _Accept_Language(str);
                case "CACHE-CONTROL":
                    return new _Cache_Control(str);
                case "CONNECTION":
                    return new _Connection(str);
                case "CONTENT-LENGTH":
                    return new _Content_Length(str);
                case "CONTENT-TYPE":
                    return new _Content_Type(str);
                case "COOKIE":
                    return new _Cookie(str);
                case "DATE":
                    return new _Date(str);
                case "IF-MATCH":
                    return new _If_Match(str);
                case "IF-MODIFIED-SINCE":
                    return new _If_Modified_Since(str);
                case "IF-NONE-MATCH":
                    return new _If_None_Match(str);
                case "IF-RANGE":
                    return new _If_Range(str);
                case "IF-UNMODIFIED-SINCE":
                    return new _If_Unmodified_Since(str);
                case "USER-AGENT":
                    return new _User_Agent(str);
                case "RANGE":
                    return new _Range(str);
                default:
                    return new HeaderField(str); // other header fields
            }
        }
    }
}
