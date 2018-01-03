package HeaderField;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class _Range extends HeaderField {
    String unit ;
    String next_range;
    List<String> first_pos_ls = new ArrayList<String>();
    List<String> last_pos_ls = new ArrayList<String>();

    public _Range(String str) throws SyntaxErrorException{
        super(str);

        unit = next().toString();
        if (!eat("="))
            throw new SyntaxErrorException("400");

        while(more()) {
            Pattern p = Pattern.compile("(\\d*)-(\\d*)");
            next_range = next().toString();
            Matcher m = p.matcher(next_range);
            if (m.matches()) {
                first_pos_ls.add(m.group(1));
                last_pos_ls.add(m.group(2));
            }
            /*
            first_pos = next().toString();
            if (first_pos.equals("-")) { // ex. -499
                last_pos = next().toString();
                first_pos_ls.add("FRONT");
                last_pos_ls.add(last_pos);
            } else { // ex. 0-499, 500-
                first_pos_ls.add(first_pos);
                eat("-");
                if (!more() || peek().toString().equals(",")) { // 500-
                    last_pos_ls.add("END");
                    eat(",");
                } else { // 0-499
                    last_pos = next().toString();
                    last_pos_ls.add(last_pos);
                }
            }
            */
        }
    }

    public String[] getRanges(int i){
        String[] range = new String[2];
        range[0] = first_pos_ls.get(i);
        range[1] = last_pos_ls.get(i);
        return range;
    }

    public void updateBoundary(int file_size) {
        int length = first_pos_ls.size();
        int byte_len;
        int new_front_pos, new_last_pos;

        for (int i = 0 ; i < length ; i++) {
            if (first_pos_ls.get(i).equals("")){
                byte_len = Integer.valueOf(last_pos_ls.get(i));
                new_front_pos = file_size - byte_len;
                new_last_pos =  file_size - 1;
                first_pos_ls.set(i, "" + new_front_pos);
                last_pos_ls.set(i, "" + new_last_pos);
            } else if (last_pos_ls.get(i).equals("")) {
                new_last_pos = file_size - 1;
                last_pos_ls.set(i, "" + new_last_pos );
            }
        }
    }

    public boolean isBoundaryLegal() {
        int length = first_pos_ls.size();

        for (int i = 0 ; i < length ; i++){
            int first_pos = Integer.valueOf(first_pos_ls.get(i));
            int last_pos = Integer.valueOf(last_pos_ls.get(i));

            if (first_pos > last_pos)
                return false;
        }
        return true;
    }

    public boolean isBoundarySatisfiable(int file_size) {
        int length = first_pos_ls.size();

        for (int i = 0 ; i < length ; i++){
            int first_pos = Integer.valueOf(first_pos_ls.get(i));
            int last_pos = Integer.valueOf(last_pos_ls.get(i));

            if (first_pos > file_size || last_pos > file_size || first_pos == last_pos)
                return false;
        }
        return true;
    }
}
