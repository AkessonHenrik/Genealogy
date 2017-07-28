package returnTypes;

import org.hibernate.Session;
import utils.SessionHandler;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import static utils.Util.getDates;

public class ProfileResult {
    public int id;
    public String firstname;
    public String lastname;
    public String image;
    public int gender;
    public Date born, died;

    public ProfileResult(int id, String firstname, String lastname, String profilePicture, int gender) {
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
        return "ProfileResult{" +
                "id=" + id +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", profilePicture='" + image + '\'' +
                ", gender: '" + gender + "'\'" +
                '}';
    }

    public static List<ProfileResult> createSearchResultPersonFromQueryResult(List<Object[]> kids) {
        Session session = SessionHandler.getInstance().getSessionFactory().openSession();
        List<ProfileResult> results = new ArrayList<>();
        for (Object[] resultObj : kids) {
            int resid = (int) resultObj[0];
            String resfirstname = (String) resultObj[1];
            String reslastname = (String) resultObj[2];
            String resPath = (String) resultObj[3];
            int resGender = (int) resultObj[4];
            boolean alreadyIn = false;
            for (ProfileResult par : results) {
                if (par.id == resid) {
                    alreadyIn = true;
                }
            }
            if (!alreadyIn) {
                results.add(new ProfileResult(resid, resfirstname, reslastname, resPath, resGender));
            }
        }
        session.close();
        return results;
    }
}
