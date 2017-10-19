import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

public class Parser {
    public static SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd");

    public static void main(String... args) throws ClassNotFoundException, SQLException, ParseException {
        Class.forName("org.postgresql.Driver");
        String url = "jdbc:postgresql://localhost/postgres";
        Properties props = new Properties();
        props.setProperty("user", "postgres");
        props.setProperty("password", "toor");
        props.setProperty("schema", "familytree");
        Connection conn = DriverManager.getConnection(url, props);
        conn.setSchema("familytree");
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM vtime");
        ArrayList<Time> times = new ArrayList<>();
        while (rs.next()) {
            String time = rs.getString(2);
            int nbOfCurlies = numberOfCurlies(time);

            switch (nbOfCurlies) {
                case 1: {
                    times.add(new SingleTime(time));
                    break;
                }
                case 3: {
                    times.add(new TimeInterval(time));
                    break;
                }
                case 4: {
                    times.add(new CircaSingleTime(time));
                    break;
                }
                case 6: {
                    times.add(new SingleTimeAndCircaSingleTime(time));
                    break;
                }
                case 9: {
                    times.add(new CircaTimeInterval(time));
                    break;
                }
            }
        }
        System.out.println(times);

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


class Time { }

class SingleTime extends Time {
    public Date date;

    public SingleTime(String str) {
        String dateString = str.substring(1, str.length() - 1);
        try {
            date = Parser.parser.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return Parser.parser.format(date);
    }
}

class TimeInterval extends Time {
    public SingleTime[] dates = new SingleTime[2];

    public TimeInterval(String str) {
        int firstClosingCurlyIndex = str.indexOf('}');
        dates[0] = new SingleTime(str.substring(1, firstClosingCurlyIndex + 1));
        dates[1] = new SingleTime(str.substring(firstClosingCurlyIndex + 2, str.length() - 1));
    }
    @Override
    public String toString() {
        return dates[0] + " to " + dates[1];
    }
}

class CircaSingleTime extends Time {
    public TimeInterval interval;

    public CircaSingleTime(String str) {
        interval = new TimeInterval(str.substring(1, str.length() - 1));
    }

    @Override
    public String toString() {
        return "Between " + interval.dates[0] + " and " + interval.dates[1];
    }
}

class CircaTimeInterval extends Time {
    public CircaSingleTime[] circaSingleTimes = new CircaSingleTime[2];

    public CircaTimeInterval(String str) {
        int idxComma = str.indexOf(',');
        idxComma = str.indexOf(',', idxComma + 1);
        circaSingleTimes[0] = new CircaSingleTime(str.substring(1, idxComma));
        circaSingleTimes[1] = new CircaSingleTime(str.substring(idxComma + 1, str.length() - 1));
    }
    @Override
    public String toString() {
        return circaSingleTimes[0] + " to " + circaSingleTimes[1];
    }
}

class SingleTimeAndCircaSingleTime extends Time {
    public SingleTime singleTime;
    public CircaSingleTime circaSingleTime;

    public SingleTimeAndCircaSingleTime(String str) {
        int idxComma = str.indexOf(',');
        singleTime = new SingleTime(str.substring(1, idxComma));
        circaSingleTime = new CircaSingleTime(str.substring(idxComma+1, str.length() - 1));
    }

    @Override
    public String toString() {
        return "From " + singleTime + " to " + circaSingleTime;
    }
}




