package returnTypes;

import models.Parentsof;

import java.sql.Date;

import static utils.Util.getDates;

/**
 * Created by Henrik on 19/07/2017.
 */
public class ParentSearchResult {
    public int timedentityid, childid, parentsid, parentType;
    public Date begin, end;

    public ParentSearchResult(int timedentityid, int childid, int parentsid, int parentType) {
        this.timedentityid = timedentityid;
        this.childid = childid;
        this.parentsid = parentsid;
        this.parentType = parentType;
    }

    public ParentSearchResult(Parentsof parentsof) {
        this.timedentityid = parentsof.getTimedentityid();
        this.childid = parentsof.getChildid();
        this.parentType = parentsof.getParentType();
        this.parentsid = parentsof.getParentsid();
        Date[] dates = getDates(this.timedentityid);
        this.begin = dates[0];
        if (dates.length > 1)
            this.end = dates[1];
    }
}
