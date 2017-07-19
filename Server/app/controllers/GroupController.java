package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.Group;
import models.Grouppeople;
import org.hibernate.Session;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import utils.SessionHandler;

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
}
