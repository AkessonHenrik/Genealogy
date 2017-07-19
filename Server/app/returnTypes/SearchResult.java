package returnTypes;

import models.Time;
import org.hibernate.Session;
import utils.SessionHandler;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import static utils.Util.getDates;

public class SearchResult {
    public int id;
    public String firstname;
    public String lastname;
    public String image;
    public int gender;
    public Date born, died;

    public SearchResult(int id, String firstname, String lastname, String profilePicture, int gender) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.image = profilePicture;
        this.gender = gender;
        Date[] dates = getDates(id);
        this.born = dates[0];
        if (dates.length > 1)
            this.died = dates[1];
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
        Session session = SessionHandler.getInstance().getSessionFactory().openSession();
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
        session.close();
        return results;
    }
}
