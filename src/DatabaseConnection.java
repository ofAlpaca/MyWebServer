import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

public class DatabaseConnection {
    private static Connection conn = null;

    public static Connection getConnection() {
        if (conn == null) {
            try {
                conn = DriverManager.getConnection("jdbc:sqlite:src/user.db");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return conn;
    }

    // Get the hashed password by username.
    public String getHashCode(String usrname){
        String sql = "SELECT hash_passwd FROM usr_table WHERE username = ?";

        PreparedStatement ps = null;
        String usr_hash = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, usrname);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                usr_hash = rs.getString("hash_passwd");

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return usr_hash;
    }

    public String getUsrname(String session){
        String sql = "SELECT username FROM usr_table WHERE hash_passwd = ?";

        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, session);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getString("username");

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Adding new user to the database.
    public void insert(String usrname, String password){
        byte[] dig = getHashDigest(password);

        System.out.println("hashed " + byte2hex(dig));
        String sql = "INSERT INTO usr_table(username, hash_passwd) VALUES (?, ?)";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, usrname);
            ps.setString(2, byte2hex(dig));
            ps.executeUpdate();
            // conn.commit();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    // This method is used to look up the correction of username and password.
    public boolean lookupUsr(String usrname, String password){
        String sql = "SELECT * FROM usr_table WHERE username = ?";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, usrname);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String usr_pwd = rs.getString("hash_passwd");
                String input_pwd = byte2hex(getHashDigest(password));

                if (usr_pwd.equals(input_pwd)) // the password is correct
                    return true;
                else // password is wrong
                    return false;
            } else
                return false;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean lookupSession(String session){
        String sql = "SELECT * FROM usr_table WHERE hash_passwd = ?";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, session);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return true;
            else
                return false;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Use SHA-1 hash function to get the digest of password.
    private byte[] getHashDigest(String hs){
        MessageDigest alg = null;
        try {
            alg = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.out.println("illegal algorithm");
        }
        alg.update(hs.getBytes());
        return alg.digest();
    }

    private String byte2hex(byte[] b) {
        String hs="";
        String stmp="";
        for (int n=0 ; n < b.length ; n++) {
            stmp=(java.lang.Integer.toHexString(b[n] & 0XFF));
            if (stmp.length() == 1)
                hs = hs + "0" + stmp;
            else
                hs = hs + stmp;
        }
        return hs.toUpperCase();
    }
}
