public class CircaTimeInterval extends Time {
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
