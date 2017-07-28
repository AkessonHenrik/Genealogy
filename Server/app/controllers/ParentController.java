package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.*;
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
import java.util.List;

import static utils.Util.getOwnerOfProfile;
import static utils.Util.parseDateFromString;

public class ParentController extends Controller {

    @Transactional
    public Result addParent() {

        if (!request().hasHeader("requester")) {
            return forbidden("You need to be logged in to create an entity");
        }
        Integer requesterId = Integer.parseInt(request().getHeader("requester"));
        JsonNode jsonNode = request().body().asJson();
        String parentType = jsonNode.get("parentType").asText();
        int parentId = jsonNode.get("parent").asInt();
        int childId = jsonNode.get("child").asInt();

        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");

        Date begin = null;
        Date end = null;

        Session session = SessionHandler.getInstance().getSessionFactory().openSession();


        if (session.createQuery("from Account where id = :id").setParameter("id", requesterId).list().size() == 0) {
            session.close();
            return badRequest("No account associated to id " + requesterId);
        }
        session.getTransaction().begin();


        if (!jsonNode.get("time").get("begin").asText().equals("null")) {
            try {
                begin = new Date(sdf1.parse(jsonNode.get("time").get("begin").asText()).getTime());
                if (jsonNode.get("time").has("end")) {
                    end = new Date(sdf1.parse(jsonNode.get("time").get("end").asText()).getTime());
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            if (parentType.equals("biological")) {
                begin = (Date) session.createQuery("select st.time from Singletime st inner join Time t on t.id = st.timeid inner join Timedentity te on te.timeid = t.id where te.id = :childid").setParameter("childid", childId).list().get(0);
            } else {
                session.getTransaction().rollback();
                session.close();
                return badRequest("If parent type is not biological, a begin date should be given");
            }
        }


        int timeId;
        if (end != null) {
            timeId = Util.getOrCreateTime(new Date[]{begin, end});
        } else {
            timeId = Util.getOrCreateTime(new Date[]{begin});
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
        if (parentType.equals("biological")) {
            parent.setParentType(0);
        } else if (parentType.equals("adoptive")) {
            parent.setParentType(1);
        } else if (parentType.equals("guardian")) {
            parent.setParentType(2);
        } else {
            session.getTransaction().rollback();
            session.close();
            return badRequest("Invalid parent type\n accepted types are: {\"adoptive\", \"biological\", \"guardian\"");
        }

        session.save(parent);

        setParentEvent(parent, session);


        // Notifications
        // Parent is profile
        Relationship parentRel = session.get(Relationship.class, parent.getParentsid());
        Profile childProfile = session.get(Profile.class, parent.getChildid());
        Profile[] profiles;
        if (parentRel != null) {
            profiles = new Profile[]{session.get(Profile.class, parentRel.getProfile1()), session.get(Profile.class, parentRel.getProfile2())};

        } else {
            profiles = new Profile[]{session.get(Profile.class, parent.getParentsid())};
        }
        for (Profile p : profiles) {
            int pOwner = Util.getOwnerOfProfile(p.getPeopleentityid());
            if (pOwner != requesterId) {
                Notification notification = new Notification();
                notification.setEntityid(parent.getTimedentityid());
                notification.setAccountid(pOwner);
                notification.setContent(session.get(Account.class, requesterId).getEmail() + " set " + childProfile.getFirstname() + " " + childProfile.getLastname() + " as a " + parentType + " parent of " + childProfile.getFirstname() + " " + childProfile.getLastname());
                session.save(notification);
            }
        }

        int ownerOfChild = Util.getOwnerOfProfile(childId);
        if (ownerOfChild != requesterId) {
            Notification notification = new Notification();
            notification.setAccountid(ownerOfChild);
            notification.setEntityid(parent.getTimedentityid());
            String parents = "";
            for (int i = 0; i < profiles.length; i++) {
                parents += profiles[i].getFirstname() + " " + profiles[i].getLastname();
                if (i != profiles.length - 1) {
                    parents += " and ";
                }
            }
            notification.setContent(session.get(Account.class, requesterId).getEmail() + " set you as an " + parentType + " child of " + parents);
            session.save(notification);
        }

        Timedentityowner owner;
        // If parent is a relationship, both profiles should be owners
        if (Util.getTypeOfEntity(parent.getParentsid()) == EntityType.Relationship) {
            Relationship rel = (Relationship) session.createQuery("from Relationship where peopleentityid = :id").setParameter("id", parent.getParentsid()).list().get(0);
            owner = new Timedentityowner();
            owner.setTimedentityid(parent.getTimedentityid());
            owner.setPeopleorrelationshipid(rel.getProfile1());
            session.save(owner);
            owner = new Timedentityowner();
            owner.setTimedentityid(parent.getTimedentityid());
            owner.setPeopleorrelationshipid(rel.getProfile2());
            session.save(owner);
        } else {
            // Otherwise, parent is a profile and a Timedentityowner can be created directly
            // Parents are owners
            owner = new Timedentityowner();
            owner.setPeopleorrelationshipid(parent.getParentsid());
            owner.setTimedentityid(parent.getTimedentityid());
            session.save(owner);
        }
        // Child is owner
        owner = new Timedentityowner();
        owner.setPeopleorrelationshipid(parent.getChildid());
        owner.setTimedentityid(parent.getTimedentityid());
        session.save(owner);


        if (jsonNode.has("visibility")) {
            if (!Util.setVisibilityToEntity2(parent.getTimedentityid(), jsonNode.get("visibility"), session)) {
                session.getTransaction().rollback();
                session.close();
                return badRequest("Invalid visibility attributes");
            }
        }
        session.getTransaction().commit();

        session.close();
        return ok(Json.toJson(parent));
    }

    @Transactional
    public Result updateParent(Integer id) {
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

        Parentsof parentsof = session.get(Parentsof.class, id);
        boolean canAccess = false;
        if (Util.getOwnerOfProfile(parentsof.getChildid()) == requesterId) {
            canAccess = true;
        }
        if (!canAccess && owner == null) {

            // Get parent people
            Relationship parentRelationship = session.get(Relationship.class, parentsof.getParentsid());
            if (parentRelationship != null) {
                int p1 = parentRelationship.getProfile1();
                int p2 = parentRelationship.getProfile2();
                if (session.createQuery("from Ghost where profileid = :p1 and owner = :requester").setParameter("p1", p1).setParameter("requester", requesterId).list().size() == 1) {
                    canAccess = true;
                } else if (session.createQuery("from Ghost where profileid = :p1 and owner = :requester").setParameter("p1", p2).setParameter("requester", requesterId).list().size() == 1) {
                    canAccess = true;
                } else if (Util.getOwnerOfProfile(p1) == requesterId) {
                    canAccess = true;
                } else if (Util.getOwnerOfProfile(p2) == requesterId) {
                    canAccess = true;
                }
            } else {
                Profile parent = session.get(Profile.class, parentsof.getParentsid());
                if (parent.getPeopleentityid() == requesterId) {
                    canAccess = true;
                } else if (Util.getOwnerOfProfile(parent.getPeopleentityid()) == requesterId) {
                    canAccess = true;
                }
            }

        } else if (!canAccess && (owner.getPeopleorrelationshipid() == requesterId || Util.getOwnerOfProfile(owner.getPeopleorrelationshipid()) == requesterId)) {
            canAccess = true;
        }
        if (!canAccess) {
            session.close();
            return forbidden();
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
            String parentTypeString = body.get("type").asText();
            int parentTypeInt = getParentTypeAsIntFromString(parentTypeString);
            if (parentTypeInt == -1) {
                session.getTransaction().rollback();
                session.close();
                return badRequest("Invalid parent type");
            }
            parentsof.setParentType(parentTypeInt);
            if (parentTypeInt == 0) {
                Timedentity parentEntity = session.get(Timedentity.class, parentsof.getTimedentityid());
                parentEntity.setTimeid(Util.getOrCreateTime(Util.getDates(parentsof.getChildid())));
                session.saveOrUpdate(parentEntity);
            }
            session.save(parentsof);

            setParentEvent(parentsof, session);

        }
        if (body.has("visibility")) {
            if (!Util.setVisibilityToEntity(parentsof.getTimedentityid(), body.get("visibility"))) {
                session.getTransaction().rollback();
                session.close();
                return badRequest("Visibility is invalid");
            }
        }
        session.getTransaction().commit();
        session.close();
        return ok();
    }

    public int getParentTypeAsIntFromString(String parentTypeString) {
        if (parentTypeString.equals("biological")) {
            return 0;
        } else if (parentTypeString.equals("adoptive")) {
            return 1;
        } else if (parentTypeString.equals("guardian")) {
            return 2;
        } else {
            return -1;
        }
    }

    private void setParentEvent(Parentsof parent, Session session) {
        Post post = session.get(Post.class, parent.getTimedentityid());
        if (post == null) {
            post = new Post();
            post.setTimedentityid(parent.getTimedentityid());
            session.save(post);
        }

        Event event = session.get(Event.class, parent.getTimedentityid());
        if (event == null)
            event = new Event();
        event.setPostid(post.getTimedentityid());
        int childId = parent.getChildid();
        int parentId = parent.getParentsid();
        int parentType = parent.getParentType();
        Profile child = session.get(Profile.class, childId);

        Relationship parentRelationship = session.get(Relationship.class, parentId);
        String parentdescription;
        if (parentRelationship != null) {
            Profile p1 = session.get(Profile.class, parentRelationship.getProfile1());
            Profile p2 = session.get(Profile.class, parentRelationship.getProfile2());
            parentdescription = p1.getFirstname() + " and " + p2.getFirstname();
        } else {
            Profile parentProfile = session.get(Profile.class, parentId);
            parentdescription = parentProfile.getFirstname();
        }
        if (parentType == 0) {
            event.setName(child.getFirstname() + " is born");
            event.setDescription(child.getFirstname() + " is the biological child of " + parentdescription);
        } else if (parentType == 1) {
            event.setName("Adopted");
            event.setDescription(child.getFirstname() + " was adopted by " + parentdescription);
        } else {
            event.setName("Guarded");
            event.setDescription(child.getFirstname() + " is guarded by " + parentdescription);
        }
        session.save(event);
    }

    @Transactional
    public Result deleteParent(Integer id) {
        if (!request().hasHeader("requester")) {
            return forbidden();
        }
        Integer requesterId = Integer.parseInt(request().getHeader("requester"));

        Session session = SessionHandler.getInstance().getSessionFactory().openSession();

        Parentsof parent = session.get(Parentsof.class, id);
        if (parent == null) {
            session.close();
            return notFound();
        }
        boolean isAllowed;
        Relationship parentRelationship = session.get(Relationship.class, parent.getParentsid());
        if (parentRelationship != null)
            isAllowed = requesterId == getOwnerOfProfile(parentRelationship.getProfile1()) || requesterId == getOwnerOfProfile(parentRelationship.getProfile2());
        else
            isAllowed = requesterId == getOwnerOfProfile(parent.getParentsid()) || requesterId == getOwnerOfProfile(parent.getChildid());

        if (!isAllowed) {
            session.close();
            return forbidden();
        }
        session.getTransaction().begin();
        Timedentity parentTimedEntity = session.get(Timedentity.class, parent.getTimedentityid());
        session.delete(parentTimedEntity);
        session.getTransaction().commit();
        session.close();
        return ok();
    }
}