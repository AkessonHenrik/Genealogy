import java.text.ParseException;
import java.util.Date;

public class SingleTime extends Time {
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
