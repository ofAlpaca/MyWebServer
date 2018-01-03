package HeaderField;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class _If_Range extends HeaderField {
    String etag = null;
    Date mod_date = null;

    public _If_Range(String str) throws SyntaxErrorException{
        super(str);
        if (this.input.charAt(0) == '"') { // it is Etag
            etag = next().toString();
            etag = etag.replaceAll("\"", "");
        } else { // it is Modified Since
            SimpleDateFormat f = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.ENGLISH);
            try {
                mod_date = f.parse(this.input);
            } catch (ParseException e) {
                e.printStackTrace();
                throw new SyntaxErrorException("400");
            }
        }
    }

    public String getEtag() {return this.etag;}
    public Date getMod_date() {return this.mod_date;}
    public boolean isEtag() {
        if (etag == null)
            return false;
        else
            return true;
    }
}
