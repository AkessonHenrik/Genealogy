package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.*;
import org.hibernate.Query;
import org.hibernate.Session;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import returnTypes.FinalResult;
import returnTypes.ParentSearchResult;
import returnTypes.RelationshipSearchResult;
import returnTypes.SearchResult;
import utils.EntityType;
import utils.SessionHandler;
import utils.Util;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static utils.Util.parseDateFromString;

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

        if (Util.getTypeOfEntity(requesterId) != EntityType.Profile) {
            return badRequest("Invalid requester");
        }

        int profile1 = jsonNode.get("profile1").asInt();
        int profile2 = jsonNode.get("profile2").asInt();
        String relTypeString = jsonNode.get("type").asText();
        Session session = SessionHandler.getInstance().getSessionFactory().openSession();
        if (session.createQuery("from Account where id = :id").setParameter("id", requesterId).list().size() == 0) {
            session.close();
            return badRequest("No account associated to id: " + requesterId);
        }
        String beginTime = jsonNode.get("time").get("begin").asText();
        String endTime = null;
        if (jsonNode.get("time").has("end")) {
            endTime = jsonNode.get("time").get("end").asText();
        }

        if (Util.getTypeOfEntity(profile1) != EntityType.Profile) {
            session.close();
            return badRequest("Invalid profile 1");
        }
        if (Util.getTypeOfEntity(profile2) != EntityType.Profile) {
            session.close();
            return badRequest("Invalid profile 2");
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
        session.getTransaction().commit();
        session.close();
        return ok(Json.toJson(relationship).toString());
    }

    @Transactional
    public Result getFamily(Integer id) {

        Integer requester = -1;
        if (request().hasHeader("requester")) {
            requester = Integer.parseInt(request().getHeader("requester"));
        }
        final Integer requesterId = requester;

        try {
            if (!Util.isAllowedToSeeEntity(requesterId, id)) {
                return forbidden();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Session session = SessionHandler.getInstance().getSessionFactory().openSession();
        session.getTransaction().begin();

        // Select person whose id is given as parameter
        String query = "select p.peopleentityid as id, p.firstname as firstname, p.lastname as lastname, m.path as profilePicture, p.gender as gender from Profile as p inner join Media as m on m.postid = p.profilepicture where p.peopleentityid = " + id;
        List<Object[]> result = session.createQuery(query).list();
        SearchResult caller = null;
        if (result.size() == 0) {
            return notFound("User not found");
        }
        for (Object[] resultObj : result) {
            int resid = (int) resultObj[0];
            String resfirstname = (String) resultObj[1];
            String reslastname = (String) resultObj[2];
            String resPath = (String) resultObj[3];
            int resGender = (int) resultObj[4];
            caller = new SearchResult(resid, resfirstname, reslastname, resPath, resGender);
        }

        // Select profile's relationships and their times
        String queryString = "select " +
                "r.id as id, " +
                "r.profile1 as profile1, " +
                "r.profile2 as profile2, " +
                "r.type as type, " +
                "st.time as time, " +
                "ti.begintime as begintime, " +
                "ti.endtime as endtime " +
                "from Relationship as r " +
                "join Peopleentity as pe on pe.timedentityid = r.id " +
                "join Timedentity as td on td.id = pe.timedentityid " +
                "join Time as t on td.timeid = t.id " +
                "left join Singletime as st on st.timeid = t.id " +
                "left join Timeinterval as ti on ti.timeid = t.id " +
                "where r.profile1 = " + id + " or r.profile2 = " + id;

        List<Object[]> searchrelationships = session.createQuery(queryString).list();
        searchrelationships.removeIf(rel -> {
            try {
                if (!Util.isAllowedToSeeEntity(requesterId, (int) rel[0])) {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        });
        List<RelationshipSearchResult> relationships = new ArrayList<>();


        ArrayList<Integer> otherIds = new ArrayList<>();
        for (Object[] rel : searchrelationships) {
            int resId = (int) rel[0];
            int resP1 = (int) rel[1];
            int resP2 = (int) rel[2];
            if (resP1 == id) {
                otherIds.add(resP2);
            } else {
                otherIds.add(resP1);
            }
            int resType = (int) rel[3];
            Date resTime = (Date) rel[4];
            Date resBeginTime = (Date) rel[5];
            Date resEndTime = (Date) rel[6];
            RelationshipSearchResult toAdd = new RelationshipSearchResult(resId, resP1, resP2, resType, resTime, resBeginTime, resEndTime);
            relationships.add(toAdd);
        }

        // Select people from caller's relationship
        ArrayList<SearchResult> results = new ArrayList<>();
        for (Integer otherId : otherIds) {
            String otherquery = "select p.peopleentityid as id, p.firstname as firstname, p.lastname as lastname, m.path as profilePicture, p.gender as gender from Profile as p inner join Media as m on m.postid = p.profilepicture where p.peopleentityid = " + otherId;
            List<Object[]> otherresult = session.createQuery(otherquery).list();
            List<SearchResult> searchResults = SearchResult.createSearchResultPersonFromQueryResult(otherresult);
            for (SearchResult sr : searchResults) {
                try {
                    if (Util.isAllowedToSeeEntity(requesterId, sr.id)) {
                        results.add(sr);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        results.add(caller);

        // Select children had from relationships
        List<Parentsof> parents = new ArrayList<>();
        for (RelationshipSearchResult rel : relationships) {
            query = "from Parentsof where parentsid = " + rel.id;
            List<Parentsof> parentsofs = session.createQuery(query).list();
            for (Parentsof p : parentsofs) {
                try {
                    if (Util.isAllowedToSeeEntity(requesterId, p.getTimedentityid())) {
                        parents.add(p);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // select caller's single children
        queryString = "from Parentsof where parentsid = " + id;
        List<Parentsof> parentsofs = session.createQuery(queryString).list();
        for (Parentsof p : parentsofs) {
            try {
                if (Util.isAllowedToSeeEntity(requesterId, p.getTimedentityid())) {
                    parents.add(p);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Now that we have caller's single children and caller's relationship's children
        // Let's get caller's single parents
        queryString = "from Parentsof where childid = " + id;
        parentsofs = session.createQuery(queryString).list();
        for (Parentsof p : parentsofs) {
            try {
                if (Util.isAllowedToSeeEntity(requesterId, p.getTimedentityid())) {
                    parents.add(p);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Add children profiles
        for (Parentsof parent : parents) {
            query = "select p.peopleentityid as id, p.firstname as firstname, p.lastname as lastname, m.path as profilePicture, p.gender as gender from Profile as p inner join Parentsof as pr on p.peopleentityid = pr.childid inner join Media as m on m.postid = p.profilepicture where pr.childid = " + parent.getChildid();
            List<Object[]> children = session.createQuery(query).list();
            for (Object[] resultObj : children) {
                int resid = (int) resultObj[0];
                String resfirstname = (String) resultObj[1];
                String reslastname = (String) resultObj[2];
                String resPath = (String) resultObj[3];
                int resGender = (int) resultObj[4];
                SearchResult childrenResults = new SearchResult(resid, resfirstname, reslastname, resPath, resGender);
                boolean alreadyIn = false;
                for (SearchResult sr : results)
                    if (sr.id == resid)
                        alreadyIn = true;
                if (!alreadyIn) {
                    try {
                        System.out.println("Checking if " + requesterId + " can see " + childrenResults.id);
                        if (Util.isAllowedToSeeEntity(requesterId, childrenResults.id)) {
                            results.add(childrenResults);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // Add parents

        // Add parent TimedEntity Ids
        ArrayList<Integer> parentIds = new ArrayList<>();
        for (Parentsof parent : parents) {
            parentIds.add(parent.getParentsid());
        }
        // Add parent people and relationships
        for (Integer parentId : parentIds) {
            List<Object[]> singleParents = session.createQuery("select p.peopleentityid as id, p.firstname as firstname, p.lastname as lastname, m.path as profilePicture, p.gender as gender from Profile as p inner join Media as m on m.postid = p.profilepicture where p.peopleentityid = " + parentId).list();
            for (Object[] resultObj : singleParents) {
                int resid = (int) resultObj[0];
                String resfirstname = (String) resultObj[1];
                String reslastname = (String) resultObj[2];
                String resPath = (String) resultObj[3];
                int resGender = (int) resultObj[4];
                caller = new SearchResult(resid, resfirstname, reslastname, resPath, resGender);
                boolean alreadyIn = false;
                for (SearchResult thing : results) {
                    if (thing.id == resid) {
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

            // Add parent relationships
            queryString = "select " +
                    "r.id as id, " +
                    "r.profile1 as profile1, " +
                    "r.profile2 as profile2, " +
                    "r.type as type, " +
                    "st.time as time, " +
                    "ti.begintime as begintime, " +
                    "ti.endtime as endtime " +
                    "from Relationship as r " +
                    "join Peopleentity as pe on pe.timedentityid = r.id " +
                    "join Timedentity as td on td.id = pe.timedentityid " +
                    "join Time as t on td.timeid = t.id " +
                    "left join Singletime as st on st.timeid = t.id " +
                    "left join Timeinterval as ti on ti.timeid = t.id " +
                    "where r.id = " + parentId;

            List<Object[]> searchParentrelationships = session.createQuery(queryString).list();
            ArrayList<Integer> otherParentIds = new ArrayList<>();
            for (Object[] rel : searchParentrelationships) {
                int resId = (int) rel[0];
                int resP1 = (int) rel[1];
                int resP2 = (int) rel[2];
                otherParentIds.add(resP1);
                otherParentIds.add(resP2);
                int resType = (int) rel[3];
                Date resTime = (Date) rel[4];
                Date resBeginTime = (Date) rel[5];
                Date resEndTime = (Date) rel[6];
                RelationshipSearchResult toAdd = new RelationshipSearchResult(resId, resP1, resP2, resType, resTime, resBeginTime, resEndTime);
                boolean alreadyIn = false;
                for (RelationshipSearchResult relation : relationships) {
                    if (relation.id == toAdd.id)
                        alreadyIn = true;
                }
                if (!alreadyIn)
                    relationships.add(toAdd);
                // Get their other children
                String childrenRelQuery = "from Parentsof where parentsid = " + resId;
                List<Parentsof> theirChildren = session.createQuery(childrenRelQuery).list();
                for (Parentsof p : theirChildren) {
                    try {
                        if (Util.isAllowedToSeeEntity(requesterId, p.getTimedentityid())) {
                            parents.add(p);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                List<SearchResult> res = getPeopleFromParents(theirChildren, session);
                for (SearchResult sr : res) {
                    try {
                        if (Util.isAllowedToSeeEntity(requesterId, sr.id)) {
                            results.add(sr);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            for (Integer otherId : otherParentIds) {
                String otherquery = "select p.peopleentityid as id, p.firstname as firstname, p.lastname as lastname, m.path as profilePicture, p.gender as gender from Profile as p inner join Media as m on m.postid = p.profilepicture where p.peopleentityid = " + otherId;
                List<Object[]> otherresult = session.createQuery(otherquery).list();
                List<SearchResult> res = SearchResult.createSearchResultPersonFromQueryResult(otherresult);
                for (SearchResult sr : res) {
                    try {
                        if (Util.isAllowedToSeeEntity(requesterId, sr.id)) {
                            results.add(sr);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                if (Util.isAllowedToSeeEntity(requesterId, caller.id))
                    results.add(caller);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        FinalResult fr = new FinalResult();
        List<Integer> peopleids = new ArrayList<>();
        results.removeIf(r -> {
            if (peopleids.contains(r.id))
                return true;
            peopleids.add(r.id);
            return false;
        });
        fr.people = results;
        List<Integer> relationshipIds = new ArrayList<>();
        relationships.removeIf(rel -> {
            if (relationshipIds.contains(rel.id))
                return true;
            else if (!peopleids.contains(rel.profile1) || !peopleids.contains(rel.profile2))
                return true;
            relationshipIds.add(rel.id);
            return false;
        });
        fr.relationships = relationships;
        List<Integer> parentsIds = new ArrayList<>();
        List<ParentSearchResult> parentSearchResults = new ArrayList<>();
        for (Parentsof p : parents) {
            parentSearchResults.add(new ParentSearchResult(p));
        }
        parentSearchResults.removeIf(p -> {
            if (parentsIds.contains(p.timedentityid))
                return true;
            else if (!peopleids.contains(p.childid) || (!peopleids.contains(p.parentsid) && !relationshipIds.contains(p.parentsid)))
                return true;
            parentsIds.add(p.timedentityid);
            return false;
        });
        fr.parents = parentSearchResults;

        session.getTransaction().commit();
        session.close();
        return ok(Json.toJson(fr).toString());
    }

    List<SearchResult> getPeopleFromParents(List<Parentsof> parents, Session session) {
        List<SearchResult> results = new ArrayList<>();
        for (Parentsof theirChild : parents) {
            String childrenQuery = "select p.peopleentityid as id, p.firstname as firstname, p.lastname as lastname, m.path as profilePicture, p.gender as gender from Profile as p inner join Parentsof as pr on p.peopleentityid = pr.childid inner join Media as m on m.postid = p.profilepicture where pr.childid = " + theirChild.getChildid();
            List<Object[]> kids = session.createQuery(childrenQuery).list();
            results.addAll(SearchResult.createSearchResultPersonFromQueryResult(kids));
        }
        return results;
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

        session.getTransaction().commit();
        session.close();
        return ok();
    }

    private boolean validateRelationshipDates(Session session, int timeId, Date beginDate, Date endDate) {
        Query query = session.createQuery("from Singletime where timeid = :id").setParameter("id", timeId);
        if (query.list().size() > 0) { // Single time
            Singletime st = (Singletime) query.list().get(0);
            // Only compare to relationship begin time because relationship begin and end times have already been validated
            if (st.getTime().after(beginDate)) {
                return false;
            }
        } else {
            // Profile is a time interval
            query = session.createQuery("from Timeinterval where timeid = :id").setParameter("id", timeId);
            Timeinterval ti = (Timeinterval) query.list().get(0);
            if (endDate != null) {
                if (!(ti.getBegintime().compareTo(beginDate) <= 0 && ti.getEndtime().compareTo(endDate) >= 0)) {
                    return false;
                }
            } else {
                if (ti.getBegintime().compareTo(beginDate) > 0) {
                    return false;
                }
            }
        }
        return true;
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

        boolean isAllowed = requesterId == relationship.getProfile1() || requesterId == relationship.getProfile2();
        if (!isAllowed) {
            Ghost ghost = session.get(Ghost.class, relationship.getProfile1());
            isAllowed = ghost != null && ghost.getOwner() == requesterId;
            if (!isAllowed) {
                ghost = session.get(Ghost.class, relationship.getProfile2());
                isAllowed = ghost != null && ghost.getOwner() == requesterId;
            }
        }

        if (isAllowed) {
            session.getTransaction().begin();
            session.delete(relationship);
            session.getTransaction().commit();
            session.close();
            return ok();
        }

        session.close();
        return forbidden();
    }
}