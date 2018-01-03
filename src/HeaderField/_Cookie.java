package HeaderField;
import java.util.HashMap;

public class _Cookie extends HeaderField {
    private HashMap<String, String> hashMap = new HashMap<String, String>();

    public _Cookie(String str) throws SyntaxErrorException {
        super(str);

        String key = null;
        String value = null;

        while(more()) {
            key = next().toString();
            if (!eat("="))
                throw new SyntaxErrorException("400");
            value = next().toString();
            hashMap.put(key.toUpperCase(), value);
            eat(";");
        }
    }

    public String getValue(String key) {
        if (hashMap.containsKey(key.toUpperCase()))
            return hashMap.get(key);
        else
            return null;
    }
}
