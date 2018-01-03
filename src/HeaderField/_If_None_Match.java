package HeaderField;

import java.util.ArrayList;
import java.util.List;

public class _If_None_Match extends HeaderField {
    List<String> etag_ls = new ArrayList<String>();
    String etag;
    public _If_None_Match(String str) {
        super(str);
        while (more()) {
            etag = next().toString();
            etag = etag.replaceAll("\"", "");
            etag_ls.add(etag);
            eat(",");
        }
    }

    public String getEtag(int i) {
        return etag_ls.get(i);
    }

    public boolean isMatchEtag(String file_tag) {
        for ( int i = 0 ; i < etag_ls.size() ; i++) {
            if (etag_ls.get(i).equals(file_tag))
                return true;
        }
        return false;
    }
}
