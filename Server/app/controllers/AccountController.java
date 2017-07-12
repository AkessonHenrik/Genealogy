package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.Account;
import org.hibernate.Query;
import org.hibernate.Session;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.SessionHandler;

/**
 * Created by Henrik on 18/06/2017.
 */
public class AccountController extends Controller {

    @Transactional
    public Result addAccount() {
        JsonNode json = request().body().asJson();
        System.out.println(json.toString());
        SessionHandler sessionHandler = SessionHandler.getInstance();
        Session session = sessionHandler.getSessionFactory().openSession();


        int profileId = json.get("profileId").asInt();
        String email = json.get("email").asText();
        String password = json.get("password").asText();

        session.getTransaction().begin();

        Account account = new Account();
        account.setEmail(email);
        account.setPassword(password);
        account.setProfileid(profileId);

        session.save(account);
        session.getTransaction().commit();

        boolean claiming;
        claiming = session.createQuery("from Ghost where profileid = :profileid").setParameter("profileid", profileId).getFirstResult() != null;
        if (claiming) {
            session.createQuery("delete from Ghost where profileid = :profileid").setParameter("profileid", profileId).executeUpdate();
        }
        session.close();
        JsonNode response = Json.toJson(account);

        System.out.println(response.asText());
        return ok(response);
    }

    @Transactional
    public Result login() {
        JsonNode json = request().body().asJson();
        System.out.println(json.toString());
        Session session = SessionHandler.getInstance().getSessionFactory().openSession();
        String queryString = "from Account where email = :emailParam";
        Query query = session.createQuery(queryString);
        query.setParameter("emailParam", json.get("email").asText());
        try {
            Account result = (Account) query.list().get(0);
            session.close();
            if (result.getPassword().equals(json.get("password").asText())) {
                result.setPassword("");
                JsonNode resultJson = Json.toJson(result);
                return ok(resultJson);
            } else {
                return badRequest("Wrong password");
            }
        } catch (Exception e) {
            return notFound();
        }
    }

}
