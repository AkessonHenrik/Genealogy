package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.*;
import org.hibernate.Query;
import org.hibernate.Session;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.Serializable;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by Henrik on 19/06/2017.
 */
public class RelationshipController extends Controller {

    @Transactional
    public Result createRelationship() {
        JsonNode jsonNode = request().body().asJson();
        int profile1 = jsonNode.get("profile1").asInt();
        int profile2 = jsonNode.get("profile2").asInt();
        String relTypeString = jsonNode.get("type").asText();
        int type = 5;
        switch (relTypeString) {
            case "spouse":
                type = 0;
                break;
            case "partner":
                type = 1;
                break;
            case "sibling":
                type = 2;
                break;
            case "cousin":
                type = 3;
                break;
            case "friend":
                type = 4;
                break;
        }

        Session session = SessionHandler.getInstance().getSessionFactory().openSession();
        session.getTransaction().begin();
        Time time = new Time();
        session.save(time);

        String beginTime = jsonNode.get("time").get("begin").asText();
        String endTime = null;
        if (jsonNode.get("time").has("end")) {
            endTime = jsonNode.get("time").get("end").asText();
        }
        SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MM-yyyy");
        try {
            Date beginDate = new Date(sdf1.parse(beginTime).getTime());

            if (endTime == null) {
                Singletime singletime = new Singletime();
                singletime.setTimeid(time.getId());
                singletime.setTime(beginDate);
                session.save(singletime);
            } else {
                Timeinterval timeinterval = new Timeinterval();
                timeinterval.setTimeid(time.getId());
                timeinterval.setBegintime(beginDate);
                timeinterval.setEnddate(new Date(sdf1.parse(endTime).getTime()));
                session.save(timeinterval);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Timedentity timedentity = new Timedentity();
        timedentity.setTimeid(time.getId());
        session.save(timedentity);

        Peopleentity peopleentity = new Peopleentity();
        peopleentity.setTimedentityid(timedentity.getId());
        session.save(peopleentity);

        Relationship relationship = new Relationship();
        relationship.setId(peopleentity.getTimedentityid());
        relationship.setProfile1(profile1);
        relationship.setProfile2(profile2);
        relationship.setType(type);
        session.save(relationship);

        session.getTransaction().commit();
        return ok(Json.toJson(relationship).toString());
    }

    @Transactional
    public Result getFamily(Integer id) {

        Session session = SessionHandler.getInstance().getSessionFactory().openSession();
        session.getTransaction().begin();

        // Select person whose id is given as parameter
        String query = "select p.peopleentityid as id, p.firstname as firstname, p.lastname as lastname, m.path as profilePicture, p.gender as gender from Profile as p inner join Media as m on m.postid = p.profilepicture where p.peopleentityid = " + id;
        System.out.println(query);
        List<Object[]> result = session.createQuery(query).list();
        SearchResult caller = null;
        for (Object[] resultObj : result) {
            int resid = (int) resultObj[0];
            String resfirstname = (String) resultObj[1];
            String reslastname = (String) resultObj[2];
            String resPath = (String) resultObj[3];
            int resGender = (int) resultObj[4];
            caller = new SearchResult(resid, resfirstname, reslastname, resPath, resGender);
        }

        // Select profile's relationships and their times
        String queryString = "select " +
                "r.id as id, " +
                "r.profile1 as profile1, " +
                "r.profile2 as profile2, " +
                "r.type as type, " +
                "st.time as time, " +
                "ti.begintime as begintime, " +
                "ti.enddate as endtime " +
                "from Relationship as r " +
                "join Peopleentity as pe on pe.timedentityid = r.id " +
                "join Timedentity as td on td.id = pe.timedentityid " +
                "join Time as t on td.timeid = t.id " +
                "left join Singletime as st on st.timeid = t.id " +
                "left join Timeinterval as ti on ti.timeid = t.id " +
                "where r.profile1 = " + id + " or r.profile2 = " + id;

        List<Object[]> searchrelationships = session.createQuery(queryString).list();
        List<RelationshipSearchResult> relationships = new ArrayList<>();


        ArrayList<Integer> otherIds = new ArrayList<>();
        for (Object[] rel : searchrelationships) {
            int resId = (int) rel[0];
            int resP1 = (int) rel[1];
            int resP2 = (int) rel[2];
            if (resP1 == id) {
                otherIds.add(resP2);
            } else {
                otherIds.add(resP1);
            }
            int resType = (int) rel[3];
            Date resTime = (Date) rel[4];
            Date resBeginTime = (Date) rel[5];
            Date resEndTime = (Date) rel[6];
            RelationshipSearchResult toAdd = new RelationshipSearchResult(resId, resP1, resP2, resType, resTime, resBeginTime, resEndTime);
            System.out.println(toAdd);
            relationships.add(toAdd);
        }

        // Select people from caller's relationship
        ArrayList<SearchResult> results = new ArrayList<>();
        for (Integer otherId : otherIds) {
            String otherquery = "select p.peopleentityid as id, p.firstname as firstname, p.lastname as lastname, m.path as profilePicture, p.gender as gender from Profile as p inner join Media as m on m.postid = p.profilepicture where p.peopleentityid = " + otherId;
            List<Object[]> otherresult = session.createQuery(otherquery).list();
            for (Object[] resultObj : otherresult) {
                int resid = (int) resultObj[0];
                String resfirstname = (String) resultObj[1];
                String reslastname = (String) resultObj[2];
                String resPath = (String) resultObj[3];
                int resGender = (int) resultObj[4];
                boolean alreadyIn = false;
                for (SearchResult i : results) {
                    if (i.id == resid) {
                        alreadyIn = true;
                    }
                }
                if (!alreadyIn) {
                    results.add(new SearchResult(resid, resfirstname, reslastname, resPath, resGender));
                }
            }
        }
        results.add(caller);

        // Select children had from relationships
        List<Parentsof> parents = new ArrayList<>();
        for (RelationshipSearchResult rel : relationships) {
            query = "from Parentsof where parentsid = " + rel.id;
            parents.addAll(session.createQuery(query).list());
        }

        // select caller's single children
        query = "from Parentsof where parentsid = " + id;
        parents.addAll(session.createQuery(query).list());

        // Now that we have caller's single children and caller's relationship's children
        // Let's get caller's single parents
        query = "from Parentsof where childid = " + id;
        parents.addAll(session.createQuery(query).list());

        // Add children profiles
        for (Parentsof parent : parents) {
            query = "select p.peopleentityid as id, p.firstname as firstname, p.lastname as lastname, m.path as profilePicture, p.gender as gender from Profile as p inner join Parentsof as pr on p.peopleentityid = pr.childid inner join Media as m on m.postid = p.profilepicture where pr.childid = " + parent.getChildid();
            List<Object[]> children = session.createQuery(query).list();
            for (Object[] resultObj : children) {
                int resid = (int) resultObj[0];
                String resfirstname = (String) resultObj[1];
                String reslastname = (String) resultObj[2];
                String resPath = (String) resultObj[3];
                int resGender = (int) resultObj[4];
                SearchResult childrenResults = new SearchResult(resid, resfirstname, reslastname, resPath, resGender);
                boolean alreadyIn = false;
                for (SearchResult sr : results)
                    if (sr.id == resid)
                        alreadyIn = true;
                if (!alreadyIn)
                    results.add(childrenResults);
            }
        }

        // Add parents

        // Add parent TimedEntity Ids
        ArrayList<Integer> parentIds = new ArrayList<>();
        for (Parentsof parent : parents) {
            parentIds.add(parent.getParentsid());
        }
        // Add parent people and relationships
        for (Integer parentId : parentIds) {
            List<Object[]> singleParents = session.createQuery("select p.peopleentityid as id, p.firstname as firstname, p.lastname as lastname, m.path as profilePicture, p.gender as gender from Profile as p inner join Media as m on m.postid = p.profilepicture where p.peopleentityid = " + parentId).list();
            for (Object[] resultObj : singleParents) {
                int resid = (int) resultObj[0];
                String resfirstname = (String) resultObj[1];
                String reslastname = (String) resultObj[2];
                String resPath = (String) resultObj[3];
                int resGender = (int) resultObj[4];
                caller = new SearchResult(resid, resfirstname, reslastname, resPath, resGender);
                boolean alreadyIn = false;
                for (SearchResult thing : results) {
                    if (thing.id == resid) {
                        alreadyIn = true;
                    }
                }
                if (!alreadyIn) {
                    results.add(caller);
                }
            }

            // Add parent relationships
            queryString = "select " +
                    "r.id as id, " +
                    "r.profile1 as profile1, " +
                    "r.profile2 as profile2, " +
                    "r.type as type, " +
                    "st.time as time, " +
                    "ti.begintime as begintime, " +
                    "ti.enddate as endtime " +
                    "from Relationship as r " +
                    "join Peopleentity as pe on pe.timedentityid = r.id " +
                    "join Timedentity as td on td.id = pe.timedentityid " +
                    "join Time as t on td.timeid = t.id " +
                    "left join Singletime as st on st.timeid = t.id " +
                    "left join Timeinterval as ti on ti.timeid = t.id " +
                    "where r.id = " + parentId;

            List<Object[]> searchParentrelationships = session.createQuery(queryString).list();
            ArrayList<Integer> otherParentIds = new ArrayList<>();
            for (Object[] rel : searchParentrelationships) {
                int resId = (int) rel[0];
                int resP1 = (int) rel[1];
                int resP2 = (int) rel[2];
                otherParentIds.add(resP1);
                otherParentIds.add(resP2);
                int resType = (int) rel[3];
                Date resTime = (Date) rel[4];
                Date resBeginTime = (Date) rel[5];
                Date resEndTime = (Date) rel[6];
                RelationshipSearchResult toAdd = new RelationshipSearchResult(resId, resP1, resP2, resType, resTime, resBeginTime, resEndTime);
                System.out.println(toAdd);
                boolean alreadyIn = false;
                for (RelationshipSearchResult relation : relationships) {
                    if (relation.id == toAdd.id)
                        alreadyIn = true;
                }
                if (!alreadyIn)
                    relationships.add(toAdd);
                // Get their other children
                String childrenRelQuery = "from Parentsof where parentsid = " + resId;
                List <Parentsof> theirChildren = session.createQuery(childrenRelQuery).list();
                parents.addAll(theirChildren);
                for(Parentsof theirChild: theirChildren) {
                    String childrenQuery = "select p.peopleentityid as id, p.firstname as firstname, p.lastname as lastname, m.path as profilePicture, p.gender as gender from Profile as p inner join Parentsof as pr on p.peopleentityid = pr.childid inner join Media as m on m.postid = p.profilepicture where pr.childid = " + theirChild.getChildid();
                    List<Object[]> kids = session.createQuery(childrenQuery).list();
                    for (Object[] resultObj : kids) {
                        int resid = (int) resultObj[0];
                        String resfirstname = (String) resultObj[1];
                        String reslastname = (String) resultObj[2];
                        String resPath = (String) resultObj[3];
                        int resGender = (int) resultObj[4];
                        boolean alreadyIn2 = false;
                        for (SearchResult par : results) {
                            if (par.id == resid) {
                                alreadyIn2 = true;
                            }
                        }
                        if (!alreadyIn2) {
                            results.add(new SearchResult(resid, resfirstname, reslastname, resPath, resGender));
                        }
                    }
                }
            }
            for (Integer otherId : otherParentIds) {
                String otherquery = "select p.peopleentityid as id, p.firstname as firstname, p.lastname as lastname, m.path as profilePicture, p.gender as gender from Profile as p inner join Media as m on m.postid = p.profilepicture where p.peopleentityid = " + otherId;
                List<Object[]> otherresult = session.createQuery(otherquery).list();
                for (Object[] resultObj : otherresult) {
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
            }
            results.add(caller);
        }

        // Add parent's other children
        System.out.println("KIDS");
        for(Parentsof parent: parents) {
            System.out.println(parent.getChildid());
        }
        System.out.println("END KIDS");
        FinalResult fr = new FinalResult();
        fr.people = results;
        fr.relationships = relationships;
        fr.parents = parents;

        session.getTransaction().commit();
        session.close();
        return ok(Json.toJson(fr).toString());
    }

}

class SearchResult {
    public int id;
    public String firstname;
    public String lastname;
    public String image;
    public int gender;

    SearchResult(int id, String firstname, String lastname, String profilePicture, int gender) {
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
}

class RelationshipSearchResult {
    public int id;
    public int profile1;
    public int profile2;
    public int type;
    public Date time;
    public Date begintime;
    public Date endtime;

    public RelationshipSearchResult(int id, int profile1, int profile2, int type, Date time, Date begintime, Date endtime) {
        this.id = id;
        this.profile1 = profile1;
        this.profile2 = profile2;
        this.type = type;
        this.time = time;
        this.begintime = begintime;
        this.endtime = endtime;
    }

    @Override
    public String toString() {
        return "RelationshipSearchResult{" +
                "id=" + id +
                ", profile1=" + profile1 +
                ", profile2=" + profile2 +
                ", type='" + type + '\'' +
                ", time=" + time +
                ", begintime=" + begintime +
                ", endtime=" + endtime +
                '}';
    }
}

class FinalResult {
    public List<RelationshipSearchResult> relationships;
    public List<SearchResult> people;
    public List<Parentsof> parents;
}