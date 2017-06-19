package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.Account;
import org.hibernate.Session;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * Created by Henrik on 18/06/2017.
 */
public class AccountController extends Controller {

    @Transactional
    public Result addAccount() {
        JsonNode json = request().body().asJson();
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

        JsonNode response = Json.toJson(account);


        return ok(response.asText());
    }

}
