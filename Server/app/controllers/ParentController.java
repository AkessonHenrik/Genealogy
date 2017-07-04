package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.*;
import org.hibernate.Session;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

public class ParentController extends Controller {
    Session session;

    public Result addParent() {
        JsonNode jsonNode = request().body().asJson();
        String parentType = jsonNode.get("parentType").asText();
        int parentId = jsonNode.get("parent").get("id").asInt();
        int childId = jsonNode.get("child").asInt();
        String relOrSingle = jsonNode.get("parent").get("type").asText();


        SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MM-yyyy");

        Date begin = null;
        Date end = null;

        try {
            begin = new Date(sdf1.parse(jsonNode.get("time").get("begin").asText()).getTime());
            if (jsonNode.get("time").has("end")) {
                end = new Date(sdf1.parse(jsonNode.get("time").get("end").asText()).getTime());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }


        System.out.println("Parent type: " + parentType);
        System.out.println("Parent id : " + parentId);
        System.out.println("Child id : " + childId);
        System.out.println("Rel or single: " + relOrSingle);
        System.out.println("Begin: " + begin);
        System.out.println("End: " + end);

        session = SessionHandler.getInstance().getSessionFactory().openSession();
        session.getTransaction().begin();

        int timeId = -1;
        if (end == null) {
            timeId = findSingleTime(begin);
        } else {
            timeId = findTimeInterval(begin, end);
        }

        // Create time if doesn't exist
        if (timeId == -1) {
            Time time = new Time();
            session.save(time);
            timeId = time.getId();
            // Create singletime
            if (end == null) {
                Singletime st = new Singletime();
                st.setTimeid(time.getId());
                st.setTime(begin);
                session.save(st);
            } else { // create timeinterval
                Timeinterval ti = new Timeinterval();
                ti.setTimeid(time.getId());
                ti.setBegintime(begin);
                ti.setEnddate(end);
                session.save(ti);
            }
        }

        // Create timedentitiy
        Timedentity timedentity = new Timedentity();
        timedentity.setTimeid(timeId);
        session.save(timedentity);

        // Create parent
        Parentsof parent = new Parentsof();
        parent.setTimedentityid(timedentity.getId());
        parent.setChildid(childId);
        parent.setParentsid(parentId);
        if(parentType.equals("biological")) {
            parent.setParentType(0);
        } else if (parentType.equals("adoptive")) {
            parent.setParentType(1);
        } else if(parentType.equals("guardian")) {
            parent.setParentType(2);
        } else {
            return badRequest("Invalid parent type\n accepted types are: {\"adoptive\", \"biological\", \"guardian\"");
        }
        session.save(parent);

        // Return parent

        session.getTransaction().commit();
        session.close();
        return ok(Json.toJson(parent));
    }

    public int findSingleTime(Date date) {

        List<Singletime> times = session.createQuery("from Singletime where time = '" + date.toString() + "'").list();
        if (times.size() == 0) {
            System.out.println("No times");
            return -1;
        }
        return times.get(0).getTimeid();
    }

    public int findTimeInterval(Date begin, Date end) {

        List<Timeinterval> times = session.createQuery("from Timeinterval where begintime = '" + begin.toString() + "' and enddate = '" + end.toString() + "'").list();
        if (times.size() == 0) {
            System.out.println("No Times");
            return -1;
        }
        return times.get(0).getTimeid();
    }
}
