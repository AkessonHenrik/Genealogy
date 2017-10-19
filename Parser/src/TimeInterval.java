public class TimeInterval extends Time {
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
