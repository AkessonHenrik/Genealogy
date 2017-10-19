import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Properties;

public class Parser {

    public static SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd");

    public static void main(String... args) throws ClassNotFoundException, SQLException, ParseException {
        Class.forName("org.postgresql.Driver");
        String url = "jdbc:postgresql://localhost/postgres";
        Properties props = new Properties();
        props.setProperty("user", "postgres");
        props.setProperty("password", "toor");
        Connection conn = DriverManager.getConnection(url, props);
        conn.setSchema("familytree");
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM vtime");
        ArrayList<Time> times = new ArrayList<>();
        while (rs.next()) {
            String time = rs.getString(2);
            int nbOfCurlies = numberOfCurlies(time);

            switch (nbOfCurlies) {
                case 1:
                    times.add(new SingleTime(time));
                    break;
                case 3:
                    times.add(new TimeInterval(time));
                    break;
                case 4:
                    times.add(new CircaSingleTime(time));
                    break;
                case 6:
                    times.add(new SingleTimeAndCircaSingleTime(time));
                    break;
                case 9:
                    times.add(new CircaTimeInterval(time));
                    break;
            }
        }
        for (Time time : times) {
            System.out.println(time);
        }

        rs.close();
        st.close();
    }

    public static int numberOfCurlies(String str) {
        int counter = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '{') counter++;
        }
        return counter;
    }
}