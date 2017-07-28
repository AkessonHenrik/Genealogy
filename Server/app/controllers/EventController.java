package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.*;
import org.hibernate.Query;
import org.hibernate.Session;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import returnTypes.*;
import utils.SessionHandler;
import utils.UploadFile;
import utils.Util;

import java.sql.Date;
import java.util.*;

import static play.mvc.Controller.request;
import static play.mvc.Results.*;
import static utils.Util.*;

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
        Date beginDate;
        Date endDate = null;

        if (end != null)
            endDate = Util.parseDateFromString(end);
        beginDate = Util.parseDateFromString(begin);
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
            mediaTimedEntity.setTimeid(timeId);
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
            else {
                session.getTransaction().rollback();
                session.close();
                return badRequest();
            }
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

        Integer requesterId = null;
        if (request().hasHeader("requester")) {
            requesterId = Integer.parseInt(request().getHeader("requester"));
        }
        try {
            if (!Util.isAllowedToSeeEntity(requesterId, id)) {
                return forbidden();
            }
        } catch (Exception e) {
            return notFound();
        }
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


        Timedentity timedentity = session.get(Timedentity.class, id);

        Date[] dates = Util.getDates(timedentity.getId());
        System.out.println(timedentity.getTimeid());
        System.out.println(Json.toJson(dates));
        String[] dateStrings = new String[dates.length];
        for (int i = 0; i < dateStrings.length; i++) {
            dateStrings[i] = dates[i].toString();
            System.out.println("Datestring: " + dateStrings[i]);
        }

        query = session.createQuery("from Event where postid = :id").setParameter("id", id);
        if (query.list().size() > 0) {
            Event event = (Event) query.list().get(0);
            EventResult eventResult = new EventResult(event.getPostid(), event.getName(), event.getDescription(), dateStrings, getEventMedia(session, event.getPostid()));

            session.close();
            try {
                if (!request().hasHeader("requester")) {
                    return badRequest("No requester header specified");
                }
                int requester = Integer.parseInt(request().getHeader("requester"));
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

        Workevent workevent = session.get(Workevent.class, id);
        Event event = session.get(Event.class, id);
        Timedentity timedentity = session.get(Timedentity.class, id);


        Date[] dates = Util.getDates(timedentity.getId());
        String[] dateStrings = new String[dates.length];
        for (int i = 0; i < dateStrings.length; i++) {
            dateStrings[i] = dates[i].toString();
        }

        // Get location
        LocationResult locationResult = Util.getLocationFromId(workevent.getLocationid(), session);

        Company company = session.get(Company.class, workevent.getCompanyid());
        WorkEventResult workEventResult = new WorkEventResult(workevent.getEventid(), company.getName(), workevent.getPositionheld(), locationResult, event.getName(), event.getDescription(), dateStrings, getEventMedia(session, workevent.getEventid()));
        session.close();
        return ok(Json.toJson(workEventResult));
    }

    private Result getLocatedEvent(Integer id) {
        Session session = SessionHandler.getInstance().getSessionFactory().openSession();
        Locatedevent locatedEvent = session.get(Locatedevent.class, id);
        // Get event
        Event event = (Event) session.createQuery("from Event where postid = " + locatedEvent.getEventid()).list().get(0);

        // Get location
        Location location = session.get(Location.class, locatedEvent.getLocationid());
        LocationResult locationResult = Util.getLocationFromId(location.getId(), session);
        Timedentity timedentity = session.get(Timedentity.class, id);

        Date[] dates = Util.getDates(timedentity.getId());
        String[] dateStrings = new String[dates.length];
        for (int i = 0; i < dateStrings.length; i++) {
            dateStrings[i] = dates[i].toString();
        }
        LocatedEventResult locatedEventResult = new LocatedEventResult(event.getPostid(), locationResult, event.getName(), event.getDescription(), dateStrings, Util.getEventMedia(session, event.getPostid()));
        session.close();
        return ok(Json.toJson(locatedEventResult));
    }

    private Result getMoveEvent(Integer id) {
        Session session = SessionHandler.getInstance().getSessionFactory().openSession();
        Moveevent moveEvent = session.get(Moveevent.class, id);
        // Get event
        Event event = (Event) session.createQuery("from Event where postid = " + moveEvent.getEventid()).list().get(0);

        // Get location
        Location location = session.get(Location.class, moveEvent.getLocationid());
        LocationResult locationResult = Util.getLocationFromId(location.getId(), session);
        Timedentity timedentity = (Timedentity) session.createQuery("from Timedentity where id = :id").setParameter("id", event.getPostid()).list().get(0);

        Date[] dates = Util.getDates(timedentity.getId());
        String[] dateStrings = new String[dates.length];
        for (int i = 0; i < dateStrings.length; i++) {
            dateStrings[i] = dates[i].toString();
        }
        MoveEventResult moveEventResult = new MoveEventResult(event.getPostid(), locationResult, event.getName(), event.getDescription(), dateStrings, Util.getEventMedia(session, event.getPostid()));
        session.close();
        return ok(Json.toJson(moveEventResult));
    }

    @Transactional
    public Result updateEvent(Integer eventid) {
        Session session = SessionHandler.getInstance().getSessionFactory().openSession();

        JsonNode body = request().body().asJson();

        session.getTransaction().begin();

        Event event = session.get(Event.class, eventid);
        Timedentity eventTimedEntity = session.get(Timedentity.class, eventid);
        if (event == null) {
            session.getTransaction().rollback();
            session.close();
            return notFound();
        }
        if (body.has("name")) {
            event.setName(body.get("name").asText());
        }
        if (body.has("description")) {
            event.setDescription(body.get("description").asText());
        }
        if (body.has("time")) {
            if (body.get("time").size() == 1) {
                Timedentity timedentity = session.get(Timedentity.class, eventid);
                timedentity.setTimeid(Util.getOrCreateTime(new Date[]{Util.parseDateFromString(body.get("time").get(0).asText())}));
            } else if (body.get("time").size() == 2) {
                Timedentity timedentity = session.get(Timedentity.class, eventid);
                System.out.println(timedentity.getTimeid());
                Date[] dates = new Date[]{Util.parseDateFromString(body.get("time").get(0).asText()), Util.parseDateFromString(body.get("time").get(1).asText())};
                timedentity.setTimeid(Util.getOrCreateTime(dates));
            }
        }
        if (body.has("visibility")) {
            if (!Util.setVisibilityToEntity2(eventid, body.get("visibility"), session)) {
                session.getTransaction().rollback();
                session.close();
                return badRequest("Invalid visibility");
            }
        }

        // Add event type
        if (body.has("type")) {

            String type = body.get("type").asText();
            if (!type.equals("LocatedEvent")) {
                if (session.createQuery("from Profile where born = :id or died = :id").setParameter("id", eventid).list().size() > 0) {
                    session.getTransaction().rollback();
                    session.close();
                    return forbidden("This event cannot have another type");
                }
            }
            List<String> eventTypes = new ArrayList<>();
            eventTypes.add("LocatedEvent");
            eventTypes.add("MoveEvent");
            eventTypes.add("WorkEvent");
            eventTypes.add("Event");
            if (eventTypes.indexOf(type) == -1) {
                session.getTransaction().rollback();
                session.close();
                return badRequest("Invalid event type");
            }

            Integer locationId = null;
            if (body.has("location")) {
                String city = body.get("location").get("city").asText();
                String province = body.get("location").get("province").asText();
                String country = body.get("location").get("country").asText();
                locationId = createOrGetLocation(city, province, country);
                System.out.println("New Location!: " + locationId);
            }
            if (type.equals("LocatedEvent")) {
                Moveevent moveevent = session.get(Moveevent.class, eventid);
                if (moveevent != null) {
                    if (locationId == null) { // Get former location
                        locationId = moveevent.getLocationid();
                    }
                    session.delete(moveevent);
                }

                Workevent workevent = session.get(Workevent.class, eventid);
                if (workevent != null) {
                    if (locationId == null) { // Get former location
                        locationId = workevent.getLocationid();
                    }
                    session.delete(workevent);
                }

                Locatedevent locatedevent = session.get(Locatedevent.class, eventid);
                if (locatedevent == null) {
                    // Create new event
                    locatedevent = new Locatedevent();
                    if (locationId != null) {
                        locatedevent.setLocationid(locationId);
                    } else {
                        session.getTransaction().rollback();
                        session.close();
                        return badRequest("No location specified for new Locatedevent");
                    }
                    locatedevent.setEventid(eventid);
                    session.saveOrUpdate(locatedevent);
                } else {
                    if (locationId != null)
                        locatedevent.setLocationid(locationId);
                }
            } else if (type.equals("WorkEvent")) {

                System.out.println("Work event");
                // First, remove other subevents
                Locatedevent locatedevent = session.get(Locatedevent.class, eventid);
                if (locatedevent != null) {
                    System.out.println("Located event!");
                    if (locationId == null) { // Get former location
                        locationId = locatedevent.getLocationid();
                    }
                    session.delete(locatedevent);
                }
                System.out.println(locationId);
                Moveevent moveevent = session.get(Moveevent.class, eventid);
                if (moveevent != null) {
                    if (locationId == null) { // Get former location
                        locationId = moveevent.getLocationid();
                    }
                    session.delete(moveevent);
                }


                Workevent workevent = session.get(Workevent.class, eventid);
                boolean wasAWorkEvent = true;
                if (workevent == null) {
                    workevent = new Workevent();
                    workevent.setEventid(eventid);
                    wasAWorkEvent = false;
                }
                if (locationId != null)
                    workevent.setLocationid(locationId);
                if (body.has("company")) {
                    List<Company> companies = session.createQuery("from Company where name = :name").setParameter("name", body.get("company").asText()).list();

                    Company company;
                    if (companies.size() == 0) {
                        company = new Company();
                        company.setName(body.get("company").asText());
                        session.save(company);
                    } else {
                        company = companies.get(0);
                    }
                    workevent.setCompanyid(company.getId());
                } else if (!wasAWorkEvent) {
                    session.getTransaction().rollback();
                    session.close();
                    return badRequest("No work event existed for this event. Please specify a company");
                }
                if (body.has("position")) {
                    workevent.setPositionheld(body.get("position").asText());
                } else if (!wasAWorkEvent) {
                    return badRequest("No Workevent existed for this event. Please specify a position");
                }
                session.saveOrUpdate(workevent);

            } else if (type.equals("MoveEvent")) {
                // First, remove other subevents
                Locatedevent locatedevent = session.get(Locatedevent.class, eventid);
                if (locatedevent != null) {
                    if (locationId == null) { // Get former location
                        locationId = locatedevent.getLocationid();
                    }
                    session.delete(locatedevent);
                }

                Workevent workevent = session.get(Workevent.class, eventid);
                if (workevent != null) {
                    if (locationId == null) { // Get former location
                        locationId = workevent.getLocationid();
                    }
                    session.delete(workevent);
                }

                Moveevent moveevent = session.get(Moveevent.class, eventid);
                if (moveevent == null) {
                    moveevent = new Moveevent();
                    if (locationId != null) {
                        moveevent.setLocationid(locationId);
                    } else {
                        // New Moveevent but no location specified
                        session.getTransaction().rollback();
                        session.close();
                        return badRequest("No location specified for new Moveevent");
                    }
                    moveevent.setEventid(eventid);
                }
                session.saveOrUpdate(moveevent);
            }
        }
        if (body.has("media")) {
            for (int i = 0; i < body.get("media").size(); i++) {
                JsonNode mediaNode = body.get("media").get(i);
                Timedentity timedentity = new Timedentity();
                int timeid = Util.getOrCreateTime(new Date[]{new Date(System.currentTimeMillis())});
                timedentity.setTimeid(timeid);
                timedentity.setVisibility(eventTimedEntity.getVisibility());
                session.save(timedentity);

                Post post = new Post();
                post.setTimedentityid(timedentity.getId());
                session.save(post);

                Media media = new Media();
                media.setPostid(post.getTimedentityid());
                media.setPath(body.get("media").get(i).get("path").asText());

                if (mediaNode.get("type").asText().equals("image"))
                    media.setType(0);
                else if (mediaNode.get("type").asText().equals("video"))
                    media.setType(1);
                else if (mediaNode.get("type").asText().equals("audio"))
                    media.setType(2);
                else {
                    session.getTransaction().rollback();
                    session.close();
                    return badRequest("Bad media type: " + mediaNode.get("type"));
                }
                session.save(media);

                Eventmedia eventmedia = new Eventmedia();
                eventmedia.setEventid(eventid);
                eventmedia.setMediaid(media.getPostid());
                session.saveOrUpdate(eventmedia);
            }
        }
        session.saveOrUpdate(event);
        session.getTransaction().commit();
        session.close();
        return ok();
    }
}


