public class SingleTimeAndCircaSingleTime extends Time {
    public SingleTime singleTime;
    public CircaSingleTime circaSingleTime;

    public SingleTimeAndCircaSingleTime(String str) {
        int idxComma = str.indexOf(',');
        singleTime = new SingleTime(str.substring(1, idxComma));
        circaSingleTime = new CircaSingleTime(str.substring(idxComma + 1, str.length() - 1));
    }

    @Override
    public String toString() {
        return "From " + singleTime + " to " + circaSingleTime;
    }
}
