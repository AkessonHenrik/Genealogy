package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.*;
import org.hibernate.Query;
import org.hibernate.Session;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.EntityType;
import utils.SessionHandler;
import utils.Util;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import static utils.Util.*;

/**
 * Created by Henrik on 19/06/2017.
 */
public class RelationshipController extends Controller {

    @Transactional
    public Result createRelationship() {
        JsonNode jsonNode = request().body().asJson();
        if (!request().hasHeader("requester")) {
            return forbidden("You need to be logged in to create relationships");
        }

        Integer requesterId = Integer.parseInt(request().getHeader("requester"));

        if (Util.getTypeOfEntity(requesterId) != EntityType.Account) {
            return badRequest("Invalid requester");
        }

        if (!isValid(jsonNode, new String[]{"profile1", "profile2", "type", "time"})) {
            return badRequest("Missing a field");
        }


        int profile1 = jsonNode.get("profile1").asInt();
        int profile2 = jsonNode.get("profile2").asInt();


        String relTypeString = jsonNode.get("type").asText();
        Session session = SessionHandler.getInstance().getSessionFactory().openSession();
        if (Util.getTypeOfEntity(profile1) != EntityType.Profile) {
            session.close();
            return badRequest("Invalid profile 1");
        }
        if (Util.getTypeOfEntity(profile2) != EntityType.Profile) {
            session.close();
            return badRequest("Invalid profile 2");
        }
        String beginTime = jsonNode.get("time").get("begin").asText();
        String endTime = null;
        if (jsonNode.get("time").has("end")) {
            endTime = jsonNode.get("time").get("end").asText();
        }


        Timedentity p1 = (Timedentity) session.createQuery("from Timedentity where id = :id").setParameter("id", profile1).list().get(0);
        Timedentity p2 = (Timedentity) session.createQuery("from Timedentity where id = :id").setParameter("id", profile2).list().get(0);


        Date beginDate = null;
        Date endDate = null;
        int timeid;
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
        try {
            beginDate = new Date(sdf1.parse(beginTime).getTime());
            if (endTime != null)
                endDate = new Date(sdf1.parse(endTime).getTime());

        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (endDate != null) {
            if (endDate.before(beginDate)) {
                session.close();
                return badRequest("Begin date is after end date");
            }
        }

        if (endDate == null) {
            Query query = session.createQuery("from Timeinterval where timeid = :id").setParameter("id", p1.getTimeid());
            if (query.list().size() > 0) {
                endDate = ((Timeinterval) query.list().get(0)).getEndtime();
                timeid = Util.getOrCreateTime(new Date[]{beginDate, endDate});
            } else {
                timeid = Util.getOrCreateTime(new Date[]{beginDate});
            }
        } else {
            timeid = Util.getOrCreateTime(new Date[]{beginDate, endDate});
        }
        session.getTransaction().begin();

        Timedentity timedentity = new Timedentity();
        timedentity.setTimeid(timeid);
        session.save(timedentity);

        Peopleentity peopleentity = new Peopleentity();
        peopleentity.setTimedentityid(timedentity.getId());
        session.save(peopleentity);

        Relationship relationship = new Relationship();
        relationship.setPeopleentityid(peopleentity.getTimedentityid());
        relationship.setProfile1(profile1);
        relationship.setProfile2(profile2);
        relationship.setType(getRelationshipTypeAsIntFromString(relTypeString));
        session.save(relationship);

        Timedentityowner relationshipOwner = new Timedentityowner();
        relationshipOwner.setTimedentityid(relationship.getPeopleentityid());
        relationshipOwner.setPeopleorrelationshipid(relationship.getProfile1());
        session.save(relationshipOwner);

        relationshipOwner = new Timedentityowner();
        relationshipOwner.setTimedentityid(relationship.getPeopleentityid());
        relationshipOwner.setPeopleorrelationshipid(relationship.getProfile2());
        session.save(relationshipOwner);


        if (jsonNode.has("visibility")) {
            if (!Util.setVisibilityToEntity2(relationship.getPeopleentityid(), jsonNode.get("visibility"), session)) {
                session.getTransaction().rollback();
                session.close();
                return badRequest("Invalid visibility type");
            }
        }
        int p1Owner = Util.getOwnerOfProfile(profile1);
        int p2Owner = Util.getOwnerOfProfile(profile2);

        Profile pr1 = session.get(Profile.class, profile1);
        Profile pr2 = session.get(Profile.class, profile2);
        Account account = session.get(Account.class, p1Owner);
        if (p1Owner != p2Owner) {

            Notification notification = new Notification();
            notification.setEntityid(relationship.getPeopleentityid());
            if (requesterId == p1Owner) {
                notification.setAccountid(p2Owner);
                notification.setContent("A relationship was added between " + pr1.getFirstname() + " and " + pr2.getLastname() + " by " + account.getEmail());
                session.save(notification);
            } else if (requesterId == p2Owner) {
                account = session.get(Account.class, p2Owner);
                notification.setContent("A relationship was added between " + pr1.getFirstname() + " and " + pr2.getLastname() + " by " + account.getEmail());
                notification.setAccountid(p2Owner);
                session.save(notification);
            } else {
                account = session.get(Account.class, requesterId);
                Notification n1 = new Notification();
                n1.setEntityid(relationship.getPeopleentityid());
                n1.setContent("A relationship was added between " + pr1.getFirstname() + " and " + pr2.getLastname() + " by " + account.getEmail());
                n1.setAccountid(p1Owner);
                session.save(n1);
                Notification n2 = new Notification();
                n2.setEntityid(relationship.getPeopleentityid());
                n2.setContent("A relationship was added between " + pr1.getFirstname() + " and " + pr2.getLastname() + " by " + account.getEmail());
                n2.setAccountid(p2Owner);
                session.save(n2);
            }
        } else if (p1Owner != requesterId) {
            Notification notification = new Notification();
            notification.setEntityid(relationship.getPeopleentityid());
            notification.setContent("A relationship was added between " + pr1.getFirstname() + " and " + pr2.getLastname() + " by " + account.getEmail());
        }
        session.getTransaction().commit();
        session.close();
        return ok(Json.toJson(relationship).toString());
    }

    @Transactional
    public Result updateRelationship(Integer id) {
        if (!request().hasHeader("requester")) {
            return forbidden();
        }
        Integer requesterId = Integer.parseInt(request().getHeader("requester"));

        try {
            if (!Util.isAllowedToSeeEntity(requesterId, id)) {
                return forbidden();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Session session = SessionHandler.getInstance().getSessionFactory().openSession();

        Timedentityowner owner = session.get(Timedentityowner.class, requesterId);
        Relationship relationship = session.get(Relationship.class, id);
        if (owner == null) {

            // Get relationship people

            int p1 = relationship.getProfile1();
            int p2 = relationship.getProfile2();

            boolean canAccess = false;

            if (session.createQuery("from Ghost where profileid = :p1 and owner = :requester").setParameter("p1", p1).setParameter("requester", requesterId).list().size() == 1) {
                canAccess = true;
            } else if (session.createQuery("from Ghost where profileid = :p1 and owner = :requester").setParameter("p1", p2).setParameter("requester", requesterId).list().size() == 1) {
                canAccess = true;
            }
            if (!canAccess) {
                session.close();
                return forbidden();
            }
        }

        JsonNode body = request().body().asJson();

        session.getTransaction().begin();

        if (body.has("time")) {
            Date[] dates = null;
            if (body.get("time").size() == 1) {
                dates = new Date[]{parseDateFromString(body.get("time").get(0).asText())};
            } else if (body.get("time").size() == 2) {
                dates = new Date[]{parseDateFromString(body.get("time").get(0).asText()), parseDateFromString(body.get("time").get(1).asText())};
            }
            if (dates != null) {
                int timeid = Util.getOrCreateTime(dates);
                Timedentity timedentity = session.get(Timedentity.class, id);
                timedentity.setTimeid(timeid);
                session.save(timedentity);

            }
        }

        if (body.has("type")) {
            String relTypeString = body.get("type").asText();
            relationship.setType(getRelationshipTypeAsIntFromString(relTypeString));
            session.save(relationship);


            Event relationshipEvent = session.get(Event.class, id);
            relationshipEvent.setDescription("Became " + relTypeString);
            session.save(relationshipEvent);
        }

        if (body.has("visibility")) {
            Util.setVisibilityToEntity2(id, body.get("visibility"), session);
        }
        session.getTransaction().commit();
        session.close();
        return ok();
    }

    private int getRelationshipTypeAsIntFromString(String relType) {
        int type = 5;
        switch (relType) {
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
        return type;
    }

    @Transactional
    public Result deleteRelationship(Integer id) {
        if (!request().hasHeader("requester")) {
            return forbidden();
        }
        Integer requesterId = Integer.parseInt(request().getHeader("requester"));

        Session session = SessionHandler.getInstance().getSessionFactory().openSession();

        Relationship relationship = session.get(Relationship.class, id);
        if (relationship == null) {
            session.close();
            return notFound();
        }

        boolean isAllowed = requesterId.equals(relationship.getProfile1()) || requesterId.equals(relationship.getProfile2());
        if (!isAllowed) {
            Ghost ghost = session.get(Ghost.class, relationship.getProfile1());
            isAllowed = ghost != null && ghost.getOwner().equals(requesterId);
            if (!isAllowed) {
                ghost = session.get(Ghost.class, relationship.getProfile2());
                isAllowed = ghost != null && ghost.getOwner().equals(requesterId);
            }
        }

        if (isAllowed) {
            session.getTransaction().begin();
            Timedentity relationshipTimedEntity = session.get(Timedentity.class, relationship.getPeopleentityid());
            session.delete(relationshipTimedEntity);
            session.getTransaction().commit();
            session.close();
            return ok();
        }

        session.close();
        return forbidden();
    }


}