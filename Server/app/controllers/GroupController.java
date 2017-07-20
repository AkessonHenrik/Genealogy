package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.Group;
import models.Grouppeople;
import org.hibernate.Query;
import org.hibernate.Session;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import returnTypes.GroupSearchResult;
import utils.SessionHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Henrik on 19/07/2017.
 */
public class GroupController extends Controller {
    @Transactional
    public Result createGroup() {
        Session session = SessionHandler.getInstance().getSessionFactory().openSession();
        session.getTransaction().begin();


        Group group = new Group();
        JsonNode jsonNode = request().body().asJson();
        group.setName(jsonNode.get("name").asText());
        group.setOwner(jsonNode.get("owner").asInt());
        session.save(group);

        for (int i = 0; i < jsonNode.get("people").size(); i++) {
            Grouppeople grouppeople = new Grouppeople();
            grouppeople.setGroupid(group.getId());
            grouppeople.setProfileid(jsonNode.get("people").get(i).asInt());
            session.save(grouppeople);
        }

        session.getTransaction().commit();
        session.close();
        return ok();
    }

    public Result getOwnedGroups(Integer id) {
        Session session = SessionHandler.getInstance().getSessionFactory().openSession();
        List<GroupSearchResult> ownedGroups = new ArrayList<>();
        // Get owned groups
        Query query = session.createQuery("from Group where owner = :id").setParameter("id", id);
        List<Group> groups = query.list();

        // Get people associated to each group
        for (Group group : groups) {
            query = session.createQuery("from Grouppeople where groupid = :id").setParameter("id", group.getId());
            List<Grouppeople> grouppeople = query.list();
            List<String> people = new ArrayList<>();
            for (Grouppeople person : grouppeople) {
                query = session.createQuery("select concat(firstname, ' ', lastname) from Profile where id = :id").setParameter("id", person.getProfileid());
                people.add((String) query.list().get(0));
            }
            ownedGroups.add(new GroupSearchResult(group.getId(), group.getOwner(), people, group.getName()));
        }
        session.close();
        return ok(Json.toJson(ownedGroups));
    }
}
