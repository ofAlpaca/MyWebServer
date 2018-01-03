package HeaderField;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class _If_Unmodified_Since extends HeaderField {
    Date mod_date;
    public _If_Unmodified_Since(String str) throws SyntaxErrorException{
        super(str);
        SimpleDateFormat f = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.ENGLISH);
        try {
            mod_date = f.parse(this.input);
        } catch (ParseException e) {
            e.printStackTrace();
            throw new SyntaxErrorException("400");
        }
    }

    public Date getMod_date() {
        return mod_date;
    }
}
