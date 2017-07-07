package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.*;
import org.hibernate.Session;
import play.db.jpa.Transactional;
import play.mvc.Result;
import utils.SessionHandler;

import java.io.IOException;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

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
        System.out.println(name);
        String description = jsonNode.get("description").asText();
        System.out.println(description);
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


        session.getTransaction().commit();

        session.close();
        return ok();
    }
}
