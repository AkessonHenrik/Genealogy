package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.*;
import org.hibernate.Query;
import org.hibernate.Session;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Result;
import returnTypes.WorkEventResult;
import utils.SessionHandler;
import utils.Util;

import java.sql.Date;
import java.text.*;
import java.util.ArrayList;

import static play.mvc.Controller.request;
import static play.mvc.Results.ok;

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
        if (timeInterval) {
            Timeinterval timeinterval = new Timeinterval();
            timeinterval.setTimeid(time.getId());
            timeinterval.setBegintime(beginDate);
            timeinterval.setEnddate(endDate);
            session.save(timeinterval);
        } else {
            Singletime singletime = new Singletime();
            singletime.setTimeid(time.getId());
            singletime.setTime(beginDate);
            session.save(singletime);
        }

        // Timed entity
        Timedentity timedentity = new Timedentity();
        timedentity.setTimeid(time.getId());
        session.save(timedentity);

        // Owner
        Timedentityowner owner = new Timedentityowner();
        owner.setPeopleorrelationshipid(ownerId);
        owner.setTimedentityid(timedentity.getId());
        session.save(owner);

        // Post
        Post post = new Post();
        post.setTimedentityid(timedentity.getId());
        session.save(post);

        // Event
        Event event = new Event();
        event.setPostid(post.getTimedentityid());
        event.setName(name);
        event.setDescription(description);
        session.save(event);

        // Media
        ArrayList<String> mediaUrls = new ArrayList<>();
        for (int i = 0; i < jsonNode.get("media").size(); i++) {
            mediaUrls.add(jsonNode.get("media").get(i).get("path").asText());
        }
        for (String mediaUrl : mediaUrls) {
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

            Media media = new Media();
            media.setPath(mediaUrl);
            media.setType(0);
            media.setPostid(mediaPost.getTimedentityid());
            session.save(media);

            Eventmedia eventmedia = new Eventmedia();
            eventmedia.setEventid(event.getPostid());
            eventmedia.setMediaid(media.getPostid());
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

        session.close();
        return ok(Json.toJson(event));
    }
}
