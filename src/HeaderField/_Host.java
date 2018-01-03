package HeaderField;

import jdk.nashorn.internal.runtime.regexp.joni.exception.SyntaxException;

public class _Host extends HeaderField {
    String host ;
    String port = "80";
    public _Host(String str) throws SyntaxException {
        super(str);

        this.host = next().toString();
        if(eat(":"))
            this.port = next().toString();

    }
}
