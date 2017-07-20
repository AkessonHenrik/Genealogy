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
import utils.SessionHandler;
import utils.Util;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Henrik on 19/06/2017.
 */
public class RelationshipController extends Controller {

    @Transactional
    public Result createRelationship() {
        JsonNode jsonNode = request().body().asJson();
        int profile1 = jsonNode.get("profile1").asInt();
        int profile2 = jsonNode.get("profile2").asInt();
        String relTypeString = jsonNode.get("type").asText();
        int type = 5;
        switch (relTypeString) {
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

        Session session = SessionHandler.getInstance().getSessionFactory().openSession();
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

        // Compare p1 and p2 dates with relationship dates
//        if (!validateRelationshipDates(session, p1.getTimeid(), beginDate, endDate) || !validateRelationshipDates(session, p2.getTimeid(), beginDate, endDate)) {
//            session.close();
//            return badRequest("Incoherent relationship dates with profiles");
//        }

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
        relationship.setId(peopleentity.getTimedentityid());
        relationship.setProfile1(profile1);
        relationship.setProfile2(profile2);
        relationship.setType(type);
        session.save(relationship);
        if (jsonNode.has("visibility")) {
            if (!Util.setVisibilityToEntity(relationship.getId(), jsonNode.get("visibility"))) {
                session.getTransaction().rollback();
                session.close();
                return badRequest();
            }
        }
        session.getTransaction().commit();
        return ok(Json.toJson(relationship).toString());
    }

    @Transactional
    public Result getFamily(Integer id) {

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
            results.addAll(SearchResult.createSearchResultPersonFromQueryResult(otherresult));
        }
        results.add(caller);

        // Select children had from relationships
        List<Parentsof> parents = new ArrayList<>();
        for (RelationshipSearchResult rel : relationships) {
            query = "from Parentsof where parentsid = " + rel.id;
            parents.addAll(session.createQuery(query).list());
        }

        // select caller's single children
        query = "from Parentsof where parentsid = " + id;
        parents.addAll(session.createQuery(query).list());

        // Now that we have caller's single children and caller's relationship's children
        // Let's get caller's single parents
        query = "from Parentsof where childid = " + id;
        parents.addAll(session.createQuery(query).list());

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
                if (!alreadyIn)
                    results.add(childrenResults);
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
                    results.add(caller);
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
                parents.addAll(theirChildren);
                results.addAll(getPeopleFromParents(theirChildren, session));
            }
            for (Integer otherId : otherParentIds) {
                String otherquery = "select p.peopleentityid as id, p.firstname as firstname, p.lastname as lastname, m.path as profilePicture, p.gender as gender from Profile as p inner join Media as m on m.postid = p.profilepicture where p.peopleentityid = " + otherId;
                List<Object[]> otherresult = session.createQuery(otherquery).list();
                results.addAll(SearchResult.createSearchResultPersonFromQueryResult(otherresult));
            }
            results.add(caller);
        }

        FinalResult fr = new FinalResult();
        List<Integer> ids = new ArrayList<>();
        results.removeIf(r -> {
            if (ids.contains(r.id))
                return true;
            ids.add(r.id);
            return false;
        });
        fr.people = results;
        ids.clear();
        relationships.removeIf(rel -> {
            if (ids.contains(rel.id))
                return true;
            ids.add(rel.id);
            return false;
        });
        fr.relationships = relationships;
        ids.clear();
        List<ParentSearchResult> parentSearchResults = new ArrayList<>();
        for (Parentsof p : parents) {
            parentSearchResults.add(new ParentSearchResult(p));
        }
        parentSearchResults.removeIf(p -> {
            if (ids.contains(p.timedentityid))
                return true;
            ids.add(p.timedentityid);
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
}

