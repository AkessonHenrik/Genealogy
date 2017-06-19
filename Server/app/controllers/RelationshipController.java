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

        String query = "select p.peopleentityid as id, p.firstname as firstname, p.lastname as lastname, m.path as profilePicture from Profile as p inner join Media as m on m.postid = p.profilepicture where p.peopleentityid = " + id;
        System.out.println(query);
        List<Object[]> result = session.createQuery(query).list();
        SearchResult caller = null;
        for (Object[] resultObj : result) {
            int resid = (int) resultObj[0];
            String resfirstname = (String) resultObj[1];
            String reslastname = (String) resultObj[2];
            String resPath = (String) resultObj[3];
            caller = new SearchResult(resid, resfirstname, reslastname, resPath);
        }

        List<Relationship> relationships = session.createQuery("from Relationship where profile1 = " + id + " or profile2 = " + id).list();
        ArrayList<Integer> otherIds = new ArrayList<>();
        for (Relationship rel : relationships) {
            if (rel.getProfile2() == id) {
                otherIds.add(rel.getProfile1());
            } else {
                otherIds.add(rel.getProfile2());
            }
        }


        ArrayList<SearchResult> results = new ArrayList<>();
        for (Integer otherId : otherIds) {
            String otherquery = "select p.peopleentityid as id, p.firstname as firstname, p.lastname as lastname, m.path as profilePicture from Profile as p inner join Media as m on m.postid = p.profilepicture where p.peopleentityid = " + otherId;
            List<Object[]> otherresult = session.createQuery(otherquery).list();
            for (Object[] resultObj : otherresult) {
                int resid = (int) resultObj[0];
                String resfirstname = (String) resultObj[1];
                String reslastname = (String) resultObj[2];
                String resPath = (String) resultObj[3];
                results.add(new SearchResult(resid, resfirstname, reslastname, resPath));
            }
        }
        results.add(caller);

        FinalResult fr = new FinalResult();
        fr.people = results;
        fr.relationships = relationships;


        session.getTransaction().commit();
        session.close();
        return ok(Json.toJson(fr).toString());
    }

}

class SearchResult {
    public int id;
    public String firstname;
    public String lastname;
    public String profilePicture;

    SearchResult(int id, String firstname, String lastname, String profilePicture) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.profilePicture = profilePicture;
    }

    @Override
    public String toString() {
        return "SearchResult{" +
                "id=" + id +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", profilePicture='" + profilePicture + '\'' +
                '}';
    }
}

class FinalResult {
    public List<Relationship> relationships;
    public List<SearchResult> people;
}