public class CircaSingleTime extends Time {
    public TimeInterval interval;

    public CircaSingleTime(String str) {
        interval = new TimeInterval(str.substring(1, str.length() - 1));
    }

    @Override
    public String toString() {
        return "Between " + interval.dates[0] + " and " + interval.dates[1];
    }
}
