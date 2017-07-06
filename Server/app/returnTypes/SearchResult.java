package returnTypes;

import java.util.ArrayList;
import java.util.List;

public class SearchResult {
    public int id;
    public String firstname;
    public String lastname;
    public String image;
    public int gender;

    public SearchResult(int id, String firstname, String lastname, String profilePicture, int gender) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.image = profilePicture;
        this.gender = gender;
    }

    @Override
    public String toString() {
        return "SearchResult{" +
                "id=" + id +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", profilePicture='" + image + '\'' +
                ", gender: '" + gender + "'\'" +
                '}';
    }

    public static List<SearchResult> createSearchResultPersonFromQueryResult(List<Object[]> kids) {
        List<SearchResult> results = new ArrayList<>();
        for (Object[] resultObj : kids) {
            int resid = (int) resultObj[0];
            String resfirstname = (String) resultObj[1];
            String reslastname = (String) resultObj[2];
            String resPath = (String) resultObj[3];
            int resGender = (int) resultObj[4];
            boolean alreadyIn = false;
            for (SearchResult par : results) {
                if (par.id == resid) {
                    alreadyIn = true;
                }
            }
            if (!alreadyIn) {
                results.add(new SearchResult(resid, resfirstname, reslastname, resPath, resGender));
            }
        }
        return results;
    }
}
