package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.*;
import org.hibernate.Query;
import org.hibernate.Session;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import returnTypes.EventResult;
import returnTypes.LocatedEventResult;
import returnTypes.LocationResult;
import returnTypes.WorkEventResult;
import utils.SessionHandler;
import utils.UploadFile;
import utils.Util;

import java.sql.Date;
import java.text.*;
import java.util.ArrayList;
import java.util.List;

import static play.mvc.Controller.request;
import static play.mvc.Results.badRequest;
import static play.mvc.Results.notFound;
import static play.mvc.Results.ok;
import static utils.Util.getEventMedia;

/**
 * Created by Henrik on 07/07/2017.
 */
public class EventController {
    @Transactional
    public Result addEvent() {
        Session session = SessionHandler.getInstance().getSessionFactory().openSession();

        JsonNode jsonNode = request().body().asJson();
        /**
         * Need:
         * name, description, time (st or ti), media, owner id, visibility
         */
        String name = jsonNode.get("name").asText();
        String description = jsonNode.get("description").asText();
        int id = -1;
        if (jsonNode.has("id"))
            id = jsonNode.get("id").asInt();

        int ownerId = jsonNode.get("owner").asInt();

        boolean timeInterval = false;
        String begin = jsonNode.get("time").get(0).asText();
        String end = null;
        if (jsonNode.get("time").size() > 1) {
            timeInterval = true;
            end = jsonNode.get("time").get(1).asText();
        }

        session.getTransaction().begin();

        // Time
        Date beginDate = null;
        Date endDate = null;

        Time time = new Time();
        session.save(time);
        SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MM-yyyy");

        try {
            if (timeInterval)
                endDate = new Date(sdf1.parse(end).getTime());
            beginDate = new Date(sdf1.parse(begin).getTime());
        } catch (ParseException e) {
            System.out.println("Exception while parsing date: " + e.getMessage());
        }
        int timeId;
        if (timeInterval)
            timeId = Util.getOrCreateTime(new Date[]{beginDate, endDate});
        else
            timeId = Util.getOrCreateTime(new Date[]{beginDate});

        if (id < 0) {
            // Timed entity
            Timedentity timedentity = new Timedentity();
            timedentity.setTimeid(timeId);
            session.save(timedentity);

            // Owner
            Timedentityowner owner = new Timedentityowner();
            owner.setPeopleorrelationshipid(ownerId);
            owner.setTimedentityid(timedentity.getId());
            session.save(owner);

            id = timedentity.getId();
        }
        // Post
        Post post = new Post();
        post.setTimedentityid(id);
        session.save(post);

        // Event
        Event event = new Event();
        event.setPostid(post.getTimedentityid());
        event.setName(name);
        event.setDescription(description);
        session.save(event);

        // Media
        ArrayList<UploadFile> media = new ArrayList<>();
        for (int i = 0; i < jsonNode.get("media").size(); i++) {
            String path = jsonNode.get("media").get(i).get("path").asText();
            String type = jsonNode.get("media").get(i).get("type").asText();
            media.add(new UploadFile(type, path));
        }
        for (UploadFile uploadFile : media) {
            Timedentity mediaTimedEntity = new Timedentity();
            mediaTimedEntity.setTimeid(time.getId());
            session.save(mediaTimedEntity);

            Timedentityowner mediaOwner = new Timedentityowner();
            mediaOwner.setTimedentityid(mediaTimedEntity.getId());
            mediaOwner.setPeopleorrelationshipid(ownerId);
            session.save(mediaOwner);

            Post mediaPost = new Post();
            mediaPost.setTimedentityid(mediaTimedEntity.getId());
            session.save(mediaPost);

            Media m = new Media();
            m.setPath(uploadFile.path);
            if (uploadFile.type.equals("image"))
                m.setType(0);
            else if (uploadFile.type.equals("video"))
                m.setType(1);
            else if (uploadFile.type.equals("audio"))
                m.setType(2);
            else
                return badRequest();
            m.setPostid(mediaPost.getTimedentityid());
            session.save(m);

            Eventmedia eventmedia = new Eventmedia();
            eventmedia.setEventid(event.getPostid());
            eventmedia.setMediaid(m.getPostid());
            session.save(eventmedia);
        }

        // All event subtypes have a location
        String type = jsonNode.get("type").asText();
        if (type.equals("WorkEvent")) {
            int locationId = Util.createOrGetLocation(jsonNode.get("location").get("city").asText(), jsonNode.get("location").get("province").asText(), jsonNode.get("location").get("country").asText());

            Company company;
            Query query = session.createQuery("from Company where name = :companyName").setParameter("companyName", jsonNode.get("company").asText());
            if (query.list().size() == 0) {
                company = new Company();
                company.setName(jsonNode.get("company").asText());
                session.save(company);
            } else {
                company = (Company) query.list().get(0);
            }

            Workevent workevent = new Workevent();
            workevent.setCompanyid(company.getId());
            workevent.setEventid(event.getPostid());
            workevent.setPositionheld(jsonNode.get("position").asText());
            workevent.setLocationid(locationId);
            session.save(workevent);

        } else if (type.equals("LocatedEvent")) {

            int locationId = Util.createOrGetLocation(jsonNode.get("location").get("city").asText(), jsonNode.get("location").get("province").asText(), jsonNode.get("location").get("country").asText());

            Locatedevent locatedevent = new Locatedevent();
            locatedevent.setEventid(event.getPostid());
            locatedevent.setLocationid(locationId);
            session.save(locatedevent);
        } else if (type.equals("MoveEvent")) {
            int locationId = Util.createOrGetLocation(jsonNode.get("location").get("city").asText(), jsonNode.get("location").get("province").asText(), jsonNode.get("location").get("country").asText());

            Moveevent moveevent = new Moveevent();
            moveevent.setEventid(event.getPostid());
            moveevent.setLocationid(locationId);
            session.save(moveevent);
        }
        session.getTransaction().commit();

        if (jsonNode.has("visibility")) {
            if (!Util.setVisibilityToEntity(event.getPostid(), jsonNode.get("visibility"))) {
                session.getTransaction().rollback();
                session.close();
                return badRequest();
            }
        }
        session.close();
        return ok(Json.toJson(event));
    }

    public Result getEvent(Integer id) {
        Session session = SessionHandler.getInstance().getSessionFactory().openSession();
        Query query = session.createQuery("from Locatedevent where eventid = :id").setParameter("id", id);
        if (query.list().size() > 0) {
            session.close();
            return getLocatedEvent(id);
        }
        query = session.createQuery("from Workevent where eventid = :id").setParameter("id", id);
        if (query.list().size() > 0) {
            session.close();
            return getWorkEvent(id);
        }
        query = session.createQuery("from Moveevent where eventid = :id").setParameter("id", id);
        if (query.list().size() > 0) {
            session.close();
            return getMoveEvent(id);
        }
        Timedentity timedentity = (Timedentity) session.createQuery("from Timedentity where id = :id").setParameter("id", id).list().get(0);
        int teId = timedentity.getTimeid();
        query = session.createQuery("from Singletime where timeid = :timeid");
        query.setParameter("timeid", teId);
        Singletime st = (Singletime) query.list().get(0);
        String[] times = new String[]{st.getTime().toString()};

        query = session.createQuery("from Timeinterval where timeid = :timeid");
        query.setParameter("timeid", teId);
        for (Timeinterval ti : (List<Timeinterval>) query.list()) {
            times = new String[]{ti.getBegintime().toString(), ti.getEndtime().toString()};
        }
        query = session.createQuery("from Event where postid = :id").setParameter("id", id);
        if (query.list().size() > 0) {
            Event event = (Event) query.list().get(0);
            EventResult eventResult = new EventResult(event.getPostid(), event.getName(), event.getDescription(), times, getEventMedia(session, event.getPostid()));

            session.close();
            try {
                if (!request().hasHeader("requester")) {
                    return badRequest("No requester header specified");
                }
                int requester = Integer.parseInt(request().getHeader("requester"));
                System.out.println("Requester = " + requester);
                if (Util.isAllowedToSeeEntity(requester, event.getPostid()))
                    return ok(Json.toJson(eventResult));
                else
                    return Results.forbidden();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return ok(Json.toJson(eventResult));
        } else {
            return notFound();
        }
    }

    private Result getWorkEvent(Integer id) {
        Session session = SessionHandler.getInstance().getSessionFactory().openSession();
        Timedentity timedentity = (Timedentity) session.createQuery("from Timedentity where id = :id").setParameter("id", id).list().get(0);
        int teId = timedentity.getTimeid();
        Query query;
        query = session.createQuery("from Singletime where timeid = :timeid");
        query.setParameter("timeid", teId);
        Singletime st = (Singletime) query.list().get(0);
        String[] times = new String[]{st.getTime().toString()};

        query = session.createQuery("from Timeinterval where timeid = :timeid");
        query.setParameter("timeid", teId);
        for (Timeinterval ti : (List<Timeinterval>) query.list()) {
            times = new String[]{ti.getBegintime().toString(), ti.getEndtime().toString()};
        }
        Event event = (Event) session.createQuery("from Event where postid = :id").setParameter("id", id).list().get(0);
        query = session.createQuery("from Workevent where eventid = :eventId");
        query.setParameter("eventId", id);
        Workevent workevent = null;
        WorkEventResult workEventResult = null;
        if (query.list().size() > 0) {
            workevent = (Workevent) query.list().get(0);
        }
        if (workevent != null) {
            // Get locations
            query = session.createQuery("from Location where id = " + workevent.getLocationid());
            Location location = (Location) query.list().get(0);
            List<Object[]> attrs = session.createQuery("select ci.name, " +
                    "pr.name, " +
                    "co.name from Location l " +
                    "inner join Cityprovince cp on l.cityprovinceid = cp.id " +
                    "inner join Provincecountry pc on pc.id = l.provincecountryid " +
                    "inner join City ci on ci.id = cp.cityid " +
                    "inner join Province pr on pr.id = cp.provinceid " +
                    "inner join Country co on co.id = pc.countryid where l.id = " + location.getId()).list();
            LocationResult locationResult = new LocationResult((String) attrs.get(0)[0], (String) attrs.get(0)[1], (String) attrs.get(0)[2]);

            Company company = (Company) session.createQuery("from Company where id = " + workevent.getCompanyid()).list().get(0);
            workEventResult = new WorkEventResult(workevent.getEventid(), company.getName(), workevent.getPositionheld(), locationResult, event.getName(), event.getDescription(), times, getEventMedia(session, workevent.getEventid()));
        }
        return ok(Json.toJson(workEventResult));
    }

    private Result getLocatedEvent(Integer id) {
        Session session = SessionHandler.getInstance().getSessionFactory().openSession();
        LocatedEventResult locatedEventResult = null;
        Query query = session.createQuery("from Locatedevent where eventid = :eventId");
        query.setParameter("eventId", id);
        Locatedevent died;
        died = (Locatedevent) query.list().get(0);
        if (died != null) {
            // Get event
            Event diedEvent = (Event) session.createQuery("from Event where postid = " + died.getEventid()).list().get(0);
            // Get locations
            query = session.createQuery("from Location where id = " + died.getLocationid());
            Location location = (Location) query.list().get(0);
            List<Object[]> attrs = session.createQuery("select ci.name, " +
                    "pr.name, " +
                    "co.name from Location l " +
                    "inner join Cityprovince cp on l.cityprovinceid = cp.id " +
                    "inner join Provincecountry pc on pc.id = l.provincecountryid " +
                    "inner join City ci on ci.id = cp.cityid " +
                    "inner join Province pr on pr.id = cp.provinceid " +
                    "inner join Country co on co.id = pc.countryid where l.id = :locationid").setParameter("locationid", location.getId()).list();
            LocationResult locationResult = new LocationResult((String) attrs.get(0)[0], (String) attrs.get(0)[1], (String) attrs.get(0)[2]);
            Timedentity diedTE = (Timedentity) session.createQuery("from Timedentity where id = :id").setParameter("id", diedEvent.getPostid()).list().get(0);
            Singletime time = (Singletime) session.createQuery("from Singletime where timeid = :timeid").setParameter("timeid", diedTE.getTimeid()).list().get(0);
            locatedEventResult = new LocatedEventResult(diedEvent.getPostid(), locationResult, diedEvent.getName(), diedEvent.getDescription(), new String[]{time.getTime().toString()}, Util.getEventMedia(session, diedEvent.getPostid()));
        }
        session.close();
        return ok(Json.toJson(locatedEventResult));
    }

    private Result getMoveEvent(Integer id) {
        return ok();
    }

}
