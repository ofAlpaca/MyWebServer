package HeaderField;

public class _Connection extends HeaderField {
    String con;
    public _Connection(String str) {
        super(str);
        con = next().toString();
    }

    public String getCon() {return this.con;}
}
