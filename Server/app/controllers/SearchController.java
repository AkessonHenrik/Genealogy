package controllers;

import org.hibernate.Query;
import org.hibernate.Session;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import returnTypes.ProfileResult;
import utils.SessionHandler;
import utils.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Henrik on 11/07/2017.
 */
public class SearchController extends Controller {

    public Result search() {
        Integer requesterId = -1;
        if (request().hasHeader("requester")) {
            requesterId = Integer.parseInt(request().getHeader("requester"));
        }
        Session session = SessionHandler.getInstance().getSessionFactory().openSession();

        String firstname = request().body().asJson().get("firstname").asText();
        String lastname = "";
        if (request().body().asJson().has("lastname"))
            lastname = request().body().asJson().get("lastname").asText();

        if (firstname.length() == 0 && lastname.length() == 0) {
            session.close();
            return badRequest("Empty search parameters");
        }

        Query query = session.createQuery("select peopleentityid from Profile where lower(firstname) like lower('%" + firstname + "%') and lower(lastname) like lower('%" + lastname + "%')");
        List<Integer> ids = query.list();
        List<ProfileResult> results = new ArrayList<>();

        for (Integer id : ids) {
            if (Util.isAllowedToSeeEntity(requesterId, id))
                results.add(Util.getSimplifiedProfile(id, session));
        }
        session.close();
        return ok(Json.toJson(results));
    }
}
