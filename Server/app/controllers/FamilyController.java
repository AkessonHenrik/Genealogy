package controllers;

import models.Parentsof;
import models.Relationship;
import org.hibernate.Session;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import returnTypes.FamilyResult;
import returnTypes.ParentResult;
import returnTypes.ProfileResult;
import returnTypes.RelationshipResult;
import utils.SessionHandler;
import utils.Util;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import static utils.Util.getSimplifiedProfile;
import static utils.Util.isAllowedToSeeEntity;

public class FamilyController extends Controller {
    public Result getFamily(Integer id) {
        Integer requester = -1;
        if (request().hasHeader("requester")) {
            requester = Integer.parseInt(request().getHeader("requester"));
        }
        final Integer requesterId = requester;
        if (!isAllowedToSeeEntity(requesterId, id)) {
            return forbidden();
        }
        Session session = SessionHandler.getInstance().getSessionFactory().openSession();

        // Select person whose id is given as parameter
        ProfileResult mainProfile = getSimplifiedProfile(id, session);

        // Select profile's relationships and their times
        List<Relationship> relationships = session.createQuery("from Relationship where profile1 = :id or profile2 = :id").setParameter("id", id).list();
        relationships.removeIf(rel -> !isAllowedToSeeEntity(requesterId, rel.getPeopleentityid()) || !isAllowedToSeeEntity(requesterId, rel.getProfile1()) || !isAllowedToSeeEntity(requesterId, rel.getProfile2()));


        // People from mainProfile's relationship
        List<ProfileResult> otherPeople = new ArrayList<>();
        List<Parentsof> allParentsof = new ArrayList<>();
        List<RelationshipResult> allRelationships = new ArrayList<>();

        // Children had from relationships
        List<Parentsof> relationshipChildren = new ArrayList<>();

        for (Relationship relationship : relationships) {
            Date[] dates = Util.getDates(relationship.getPeopleentityid());
            if (dates.length == 1)
                allRelationships.add(new RelationshipResult(relationship.getPeopleentityid(), relationship.getProfile1(), relationship.getProfile2(), relationship.getType(), dates[0], null, null));
            else
                allRelationships.add(new RelationshipResult(relationship.getPeopleentityid(), relationship.getProfile1(), relationship.getProfile2(), relationship.getType(), null, dates[0], dates[1]));

            if (relationship.getProfile1().intValue() == id) {
                otherPeople.add(getSimplifiedProfile(relationship.getProfile2(), session));
            } else {
                otherPeople.add(getSimplifiedProfile(relationship.getProfile1(), session));
            }
            List<Parentsof> thisRelationshipParents = session.createQuery("from Parentsof where parentsid = :id").setParameter("id", relationship.getPeopleentityid()).list();
            thisRelationshipParents.removeIf(parent -> !isAllowedToSeeEntity(requesterId, parent.getChildid()) || !isAllowedToSeeEntity(requesterId, parent.getTimedentityid()));
            allParentsof.addAll(thisRelationshipParents);
            for (Parentsof parentsof : thisRelationshipParents) {
                otherPeople.add(getSimplifiedProfile(parentsof.getChildid(), session));
            }
            relationshipChildren.addAll(thisRelationshipParents);
        }

        // Children had alone by main profile
        List<Parentsof> singleChildrenFromMainProfile = session.createQuery("from Parentsof where parentsid = :id").setParameter("id", id).list();
        singleChildrenFromMainProfile.removeIf(parent -> !isAllowedToSeeEntity(requesterId, parent.getTimedentityid()) || !isAllowedToSeeEntity(requesterId, parent.getChildid()));
        for (Parentsof parentsof : singleChildrenFromMainProfile) {
            otherPeople.add(getSimplifiedProfile(parentsof.getChildid(), session));
        }
        allParentsof.addAll(singleChildrenFromMainProfile);

        // Main profile's parents
        List<Parentsof> mainProfileParents = session.createQuery("from Parentsof where childid = :id").setParameter("id", id).list();
        // We need to check if requester is allowed to see:
        // - The Parentsof entity
        // - The Parentsof parent entity (relationship or profile)
        //      - If the parentsof parent is a relationship, check if requester is allowed to see both parents
        // If any of the conditions above is false, we remove the Parentsof from the list

        mainProfileParents.removeIf(parent -> {
            if (!isAllowedToSeeEntity(requesterId, parent.getTimedentityid())) {
                return true;
            }
            // Whether the parent timedentity is a profile or a relationship, if requester is not allowed to see it
            // it will be removed
            if (!isAllowedToSeeEntity(requesterId, parent.getParentsid())) {
                return true;
            }
            // Here, the requester is allowed to see the parent entity.
            // But is the requester allowed to see both parents if it's a relationship?
            Relationship parentRel = session.get(Relationship.class, parent.getParentsid());
            if (parentRel != null) {
                if (!isAllowedToSeeEntity(requesterId, parentRel.getProfile1()) || !isAllowedToSeeEntity(requesterId, parentRel.getProfile2())) {
                    return true;
                }
            }
            // Is allowed to see entity
            return false;
        });


        // For each of the main profile's parents
        for (Parentsof mainProfileParent : mainProfileParents) {
            // If the parent is a relationship, we need to add both profiles and the relationship
            Relationship parentRelationship = session.get(Relationship.class, mainProfileParent.getParentsid());
            if (parentRelationship != null) {
                // Add profile 1
                otherPeople.add(getSimplifiedProfile(parentRelationship.getProfile1(), session));
                // Add profile 2
                otherPeople.add(getSimplifiedProfile(parentRelationship.getProfile2(), session));

                // Add relationship
                // Get relationship time
                Date[] dates = Util.getDates(parentRelationship.getPeopleentityid());
                RelationshipResult relationshipResult;
                if (dates.length == 1)
                    relationshipResult = new RelationshipResult(parentRelationship.getPeopleentityid(), parentRelationship.getProfile1(), parentRelationship.getProfile2(), parentRelationship.getType(), dates[0], null, null);
                else
                    relationshipResult = new RelationshipResult(parentRelationship.getPeopleentityid(), parentRelationship.getProfile1(), parentRelationship.getProfile2(), parentRelationship.getType(), null, dates[0], dates[1]);
                if (!allRelationships.contains(relationshipResult)) {
                    allRelationships.add(relationshipResult);
                }


                // For each of the main profile's parents, add all other children
                List<Parentsof> otherChildrenFromParent = session.createQuery("from Parentsof where parentsid = :parentid and childid != :id").setParameter("parentid", parentRelationship.getPeopleentityid()).setParameter("id", id).list();
                for (Parentsof otherChildFromParent : otherChildrenFromParent) {
                    if (Util.isAllowedToSeeEntity(requesterId, otherChildFromParent.getChildid())) {
                        otherPeople.add(getSimplifiedProfile(otherChildFromParent.getChildid(), session));
                    }
                }
            } else {
                // If the parent is a profile, we need to add it
                otherPeople.add(getSimplifiedProfile(mainProfileParent.getParentsid(), session));
                // For each of the main profile's parents, add all other children
                List<Parentsof> otherChildrenFromParent = session.createQuery("from Parentsof where parentsid = :parentid and childid != :id").setParameter("parentid", mainProfileParent.getParentsid()).setParameter("id", id).list();
                for (Parentsof otherChildFromParent : otherChildrenFromParent) {
                    if (Util.isAllowedToSeeEntity(requesterId, otherChildFromParent.getChildid())) {
                        otherPeople.add(getSimplifiedProfile(otherChildFromParent.getChildid(), session));
                    }
                }
            }

            // Add ParentResult
            Date[] dates = Util.getDates(mainProfileParent.getTimedentityid());
            RelationshipResult parentRel;
            if (dates.length == 1)
                parentRel = new RelationshipResult(parentRelationship.getPeopleentityid(), parentRelationship.getProfile1(), parentRelationship.getProfile2(), parentRelationship.getType(), dates[0], null, null);
            else
                parentRel = new RelationshipResult(parentRelationship.getPeopleentityid(), parentRelationship.getProfile1(), parentRelationship.getProfile2(), parentRelationship.getType(), null, dates[0], dates[1]);
            if (!allRelationships.contains(parentRel))
                allRelationships.add(parentRel);
        }

        // Get all full ParentResults
        List<ParentResult> allParents = new ArrayList<>();
        for (Parentsof parentsof : allParentsof) {
            ParentResult newParent = new ParentResult(parentsof);
            if (!allParents.contains(newParent)) allParents.add(newParent);
        }
        for (Parentsof parentsof : mainProfileParents) {
            ParentResult newParent = new ParentResult(parentsof);
            if (!allParents.contains(newParent)) allParents.add(newParent);
        }

        FamilyResult familyResult = new FamilyResult();
        familyResult.relationships = allRelationships;
        familyResult.parents = allParents;
        otherPeople.add(mainProfile);
        familyResult.people = otherPeople;

        return ok(Json.toJson(familyResult));
    }
}
