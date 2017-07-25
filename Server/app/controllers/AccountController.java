package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.*;
import org.hibernate.Query;
import org.hibernate.Session;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import returnTypes.ClaimResult;
import utils.SessionHandler;

import java.util.ArrayList;
import java.util.List;

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
        boolean claiming = json.get("claim").asBoolean();

        session.getTransaction().begin();

        Account account = new Account();
        account.setEmail(email);
        account.setPassword(password);
        if (!claiming)
            account.setProfileid(profileId);

        session.save(account);
        session.getTransaction().commit();

        if (claiming) {
            Claim claim = new Claim();
            claim.setClaimerid(account.getId());
            claim.setClaimedprofileid(profileId);
            Ghost ghost = session.get(Ghost.class, profileId);
            claim.setOwnerid(ghost.getOwner());
            if (json.has("message"))
                claim.setMessage(json.get("message").asText());
            session.save(claim);
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

    @Transactional
    public Result approveClaim(Integer claimId) {

        Integer requesterId = null;
        if (!request().hasHeader("requester")) {
            return forbidden();
        }
        requesterId = Integer.parseInt(request().getHeader("requester"));
        System.out.println("Requester: " + requesterId);

        Session session = SessionHandler.getInstance().getSessionFactory().openSession();
        session.getTransaction().begin();
        Claim claim = session.get(Claim.class, claimId);
        if (requesterId != claim.getOwnerid()) {
            session.close();
            return forbidden();
        }
        Ghost claiming = (Ghost) session.createQuery("from Ghost where profileid = :profileid").setParameter("profileid", claim.getClaimedprofileid()).list().get(0);
        Account account = session.get(Account.class, claim.getClaimerid());
        session.save(account);
        Profile profile = session.get(Profile.class, claiming.getProfileid());
        session.createQuery("delete from Timedentityowner where timedentityid = :id").setParameter("id", profile.getPeopleentityid()).executeUpdate();

        Timedentityowner timedentityowner = new Timedentityowner();
        timedentityowner.setPeopleorrelationshipid(account.getId());
        timedentityowner.setTimedentityid(profile.getPeopleentityid());
        session.save(timedentityowner);

        // If no profile is associated to the account yet, it will be set as account main profile
        if (account.getProfileid() == 0) {
            account.setProfileid(claim.getClaimedprofileid());
            session.delete(claiming);
        } else {
            // Otherwise, the profile's owner will be the claimer
            claiming.setOwner(account.getId());
            session.save(claiming);
        }

        session.delete(claim);

        Notification notification = new Notification();
        notification.setAccountid(account.getId());
        notification.setContent("You now own " + profile.getFirstname() + " " + profile.getLastname() + ". Log out and log in again to start building your tree");
        session.save(notification);
        session.getTransaction().commit();
        session.close();
        return ok(Json.toJson(claiming));
    }

    @Transactional
    public Result refuseClaim(Integer claimId) {
        Integer requesterId = null;
        if (!request().hasHeader("requester")) {
            return forbidden("No requester header specified");
        }
        requesterId = Integer.parseInt(request().getHeader("requester"));
        System.out.println("Requester: " + requesterId);

        Session session = SessionHandler.getInstance().getSessionFactory().openSession();
        session.getTransaction().begin();
        Claim claim = session.get(Claim.class, claimId);
        System.out.println(Json.toJson(claim));
        if (requesterId != claim.getOwnerid()) {
            session.close();
            return forbidden("You are not the owner of the profile");
        }
        Notification notification = new Notification();
        notification.setAccountid(claim.getClaimerid());
        notification.setContent("Your claim has been refused, create a new profile");
        session.save(notification);
        session.delete(claim);
        session.getTransaction().commit();
        session.close();
        return ok();
    }

    public Result getClaims(Integer id) {
        Session session = SessionHandler.getInstance().getSessionFactory().openSession();
        List<ClaimResult> claimResults = new ArrayList<>();
        Query query = session.createQuery("from Claim where ownerid = :id").setParameter("id", id);
        List<Claim> claims = query.list();

        for (Claim claim : claims) {
            Account claimerAccount = (Account) session.createQuery("from Account where id = :id").setParameter("id", claim.getClaimerid()).list().get(0);
            ClaimResult claimResult = new ClaimResult();
            claimResult.claimerEmail = claimerAccount.getEmail();
            claimResult.message = claim.getMessage();
            claimResult.id = claim.getId();
            Profile profile = session.get(Profile.class, claim.getClaimedprofileid());

            claimResult.claimedProfileFirstname = profile.getFirstname();
            claimResult.claimedProfileLastname = profile.getLastname();

            claimResults.add(claimResult);
        }

        session.close();
        return ok(Json.toJson(claimResults));
    }


    public Result acknowledgeClaim(Integer claimId) {
        Session session = SessionHandler.getInstance().getSessionFactory().openSession();
        Claim claim = session.get(Claim.class, claimId);
        session.getTransaction().begin();
        session.delete(claim);
        session.getTransaction().commit();
        session.close();
        return ok();
    }

    public Result associate() {
        Session session = SessionHandler.getInstance().getSessionFactory().openSession();
        JsonNode jsonNode = request().body().asJson();
        int accountId = jsonNode.get("accountId").asInt();
        int profileId = jsonNode.get("profileId").asInt();

        session.getTransaction().begin();
        Account account = session.get(Account.class, accountId);
        account.setProfileid(profileId);
        session.save(account);
        session.getTransaction().commit();
        session.close();
        return ok();
    }

    public Result getNotifications(Integer accountId) {
        Session session = SessionHandler.getInstance().getSessionFactory().openSession();
        List<Notification> notifications = session.createQuery("from Notification where accountid = :accountid").setParameter("accountid", accountId).list();
        session.close();
        return ok(Json.toJson(notifications));
    }

    public Result deleteNotification(Integer notificationId) {
        Session session = SessionHandler.getInstance().getSessionFactory().openSession();

        Notification notification = session.get(Notification.class, notificationId);
        if (notification == null) {
            session.close();
            return notFound();
        }
        session.getTransaction().begin();
        session.delete(notification);
        session.getTransaction().commit();
        session.close();
        return ok();
    }
}

