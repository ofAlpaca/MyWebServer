package HeaderField;

public class _Content_Length extends HeaderField {
    String length ;
    public _Content_Length(String str) {
        super(str);
        this.length = next().toString();
    }

    public String getLength(){return this.length;}
}
