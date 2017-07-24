package controllers;

import org.hibernate.Query;
import org.hibernate.Session;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import returnTypes.SearchResult;
import utils.SessionHandler;
import utils.Util;

import java.io.File;
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
        String lastname = request().body().asJson().get("lastname").asText();

        if (firstname.length() == 0 && lastname.length() == 0) {
            session.close();
            return badRequest("Empty search parameters");
        }

        Query query = session.createQuery("select peopleentityid from Profile where firstname like '%" + firstname + "%' and lastname like '%" + lastname + "%'");
        List<Integer> ids = query.list();
        List<SearchResult> results = new ArrayList<>();

        for (Integer id : ids) {
            List<Object[]> profiles = session.createQuery("select p.peopleentityid as id, p.firstname as firstname, p.lastname as lastname, m.path as profilePicture, p.gender as gender from Profile as p inner join Media as m on m.postid = p.profilepicture where p.peopleentityid = :id").setParameter("id", id).list();
            for (Object[] resultObj : profiles) {
                int resid = (int) resultObj[0];
                String resfirstname = (String) resultObj[1];
                String reslastname = (String) resultObj[2];
                String resPath = (String) resultObj[3];
                int resGender = (int) resultObj[4];
                SearchResult caller = new SearchResult(resid, resfirstname, reslastname, resPath, resGender);
                boolean alreadyIn = false;
                for (SearchResult searchResult : results) {
                    if (searchResult.id == resid) {
                        alreadyIn = true;
                    }
                }
                if (!alreadyIn) {
                    try {
                        if (Util.isAllowedToSeeEntity(requesterId, caller.id))
                            results.add(caller);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        session.close();
        return ok(Json.toJson(results));
    }
}
