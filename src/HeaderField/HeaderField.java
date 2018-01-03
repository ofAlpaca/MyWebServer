package HeaderField;

public class HeaderField {
    protected String input;

    public HeaderField(String str){
        while(str.charAt(0) == ' ')
            str = str.substring(1);

        this.input = str;
    }

    public String get_origin_content() {
        return this.input;
    }

    // peek the next char
    protected HeaderToken peek(){
        HeaderToken tk = null;
        String str = "";
        int i = 0;
        char next ;
        if (more()) next = this.input.charAt(i); else return null;

        while (next == ' ' || next == '\n') { // get the first non-white space char.
            this.input = this.input.substring(1);
            if (this.input.length() > i)
                next = this.input.charAt(i);
            else
                break;
        }

        if ( next == ':' || next == ';' || next == ',' || next == '=') // if char is separator, make it into token.
            tk = new HeaderToken(next + "");
        else { // keep read char till separator.
            while ( next != '\t' && next != ' ' && next != ';' && next != ':' && next != '=' && next != '\n'){
                str += next;
                i ++;
                try {
                    next = this.input.charAt(i);
                } catch (StringIndexOutOfBoundsException e){
                    break;
                }
            }
            tk = new HeaderToken(str);
        }

        // System.out.println("peek-> " + tk.toString());
        return tk;
    }

    // if char c equals to next char, then consume it.
    // otherwise, there is a syntax error happened.
    protected boolean eat(String t) {
        if(more() && peek().toString().equals(t)) {
            this.input = this.input.substring(t.length());
            //System.out.println("eat-> " + t);
            return true;
        }
        else
            return false;
            //throw new RuntimeException("Expected: " + t + "; got: " + peek()) ;
    }

    // peek the next char then try to consume it.
    protected HeaderToken next() {
        if (more()) {
            HeaderToken t = peek();
            eat(t.toString());
            return t;
        } else
            return null;
    }

    // test if the input string empty ?
    protected boolean more() {
        // System.out.println("Remain-> " + input.length());
        if (input == null)
            return false;
        return input.length() > 1 ;
    }
}
