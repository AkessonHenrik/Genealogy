package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.*;
import org.hibernate.Query;
import org.hibernate.Session;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import returnTypes.*;
import utils.Globals;
import utils.SessionHandler;
import utils.UploadFile;
import utils.Util;

import java.io.File;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static utils.Util.getEventMedia;

public class ProfileController extends Controller {

    @Transactional
    public Result createProfile() {

        Session session = SessionHandler.getInstance().getSessionFactory().openSession();

        JsonNode jsonNode = request().body().asJson();

        // Extract values from request body
        Integer gender = jsonNode.get("gender").asInt();

        String birthDay = jsonNode.get("birthDay").asText();
        String deathDay = "";
        if (jsonNode.has("deathDay")) {
            deathDay = jsonNode.get("deathDay").asText();
        }

        session.getTransaction().begin();

        // Profile entity
        Profile profile = new Profile();
        profile.setFirstname(jsonNode.get("firstname").asText());
        profile.setLastname(jsonNode.get("lastname").asText());
        profile.setGender(gender);

        // Profile values from request body
        JsonNode bornNode = jsonNode.get("born");
        String bornName = bornNode.get("name").asText();
        String bornDescription = bornNode.get("description").asText();
        JsonNode bornLocationNode = bornNode.get("location");
        String bornCity = bornLocationNode.get("city").asText();
        String bornProvince = bornLocationNode.get("province").asText();
        String bornCountry = bornLocationNode.get("country").asText();

        Date birthDayDate = Util.parseDateFromString(birthDay);
        Date deathDayDate = null;
        if (!deathDay.equals("")) {
            deathDayDate = Util.parseDateFromString(deathDay);
        }

        // Born locatedevent
        // First, born timedEntity
        Timedentity bornTimedEntity = new Timedentity();
        bornTimedEntity.setTimeid(Util.getOrCreateTime(new Date[]{birthDayDate}));
        session.save(bornTimedEntity);

        // Second, born Post entity
        Post bornPost = new Post();
        bornPost.setTimedentityid(bornTimedEntity.getId());
        session.save(bornPost);

        // Fourth, born Event
        Event bornEvent = new Event();
        bornEvent.setPostid(bornPost.getTimedentityid());
        bornEvent.setName(bornName);
        bornEvent.setDescription(bornDescription);
        session.save(bornEvent);

        // Born location
        int bornLocationId = Util.createOrGetLocation(bornCity, bornProvince, bornCountry);

        // Lastly, born LocatedEvent
        Locatedevent bornLocatedEvent = new Locatedevent();
        bornLocatedEvent.setLocationid(bornLocationId);
        bornLocatedEvent.setEventid(bornEvent.getPostid());
        session.save(bornLocatedEvent);

        profile.setBorn(bornLocatedEvent.getEventid());


        String diedCity = null;
        String diedProvince = null;
        String diedCountry = null;
        String diedName = null;
        String diedDescription = null;
        if (jsonNode.has("died")) {
            JsonNode died = jsonNode.get("died");

            if (died.has("location")) {

                diedName = died.get("name").asText();
                diedDescription = died.get("description").asText();

                diedCity = died.get("location").get("city").asText();
                diedProvince = died.get("location").get("province").asText();
                diedCountry = died.get("location").get("country").asText();

            }
        }
        // Profile timedEntity
        Timedentity profileTimedEntity = new Timedentity();

        // Died locatedevent
        Timedentity diedEntity = null;
        if (deathDayDate != null) {
            diedEntity = new Timedentity();
            diedEntity.setTimeid(Util.getOrCreateTime(new Date[]{deathDayDate}));
            session.save(diedEntity);

            Post diedPost = new Post();
            diedPost.setTimedentityid(diedEntity.getId());
            session.save(diedPost);

            Event diedEvent = new Event();
            diedEvent.setPostid(diedPost.getTimedentityid());
            diedEvent.setName(diedName);
            diedEvent.setDescription(diedDescription);
            session.save(diedEvent);

            // Born location
            int diedLocationId = Util.createOrGetLocation(diedCity, diedProvince, diedCountry);

            Locatedevent diedLocatedEvent = new Locatedevent();
            diedLocatedEvent.setLocationid(diedLocationId);
            diedLocatedEvent.setEventid(diedEvent.getPostid());
            session.save(diedLocatedEvent);

            profile.setDied(diedLocatedEvent.getEventid());
            profileTimedEntity.setTimeid(Util.getOrCreateTime(new Date[]{birthDayDate, deathDayDate}));
        } else {
            profileTimedEntity.setTimeid(bornTimedEntity.getTimeid());
        }
        session.save(profileTimedEntity);

        // Profile peopleentity
        Peopleentity profilePeopleEntity = new Peopleentity();
        profilePeopleEntity.setTimedentityid(profileTimedEntity.getId());
        session.save(profilePeopleEntity);
        profile.setPeopleentityid(profilePeopleEntity.getTimedentityid());

        session.save(profilePeopleEntity);

        // Profile picture
        String profilePicturePath;
        if (jsonNode.has("profilePicture")) {
            profilePicturePath = jsonNode.get("profilePicture").asText();
        } else {
            if (gender == 0) {
                profilePicturePath = "http://s3.amazonaws.com/37assets/svn/765-default-avatar.png";
            } else if (gender == 1) {
                profilePicturePath = "https://singlesdatingworld.com/images/woman.jpg";
            } else {
                profilePicturePath = "https://maxcdn.icons8.com/Share/icon/Alphabet//question_mark1600.png";
            }
        }

        Timedentity profilePictureTimedEntity = new Timedentity();
        profilePictureTimedEntity.setTimeid(Util.getOrCreateTime(new Date[]{new Date(System.currentTimeMillis())}));
        session.save(profilePictureTimedEntity);

        Post profilePicturePost = new Post();
        profilePicturePost.setTimedentityid(profilePictureTimedEntity.getId());
        session.save(profilePicturePost);

        Media profilePictureMedia = new Media();
        profilePictureMedia.setPostid(profilePicturePost.getTimedentityid());
        profilePictureMedia.setType(0);
        profilePictureMedia.setPath(profilePicturePath);
        session.save(profilePictureMedia);

        profile.setProfilepicture(profilePictureMedia.getPostid());
        session.save(profile);

        // Add ownership of events
        Timedentityowner bornOwner = new Timedentityowner();
        bornOwner.setPeopleorrelationshipid(profileTimedEntity.getId());
        bornOwner.setTimedentityid(bornTimedEntity.getId());
        session.save(bornOwner);

        if (diedEntity != null) {
            Timedentityowner diedOwner = new Timedentityowner();
            diedOwner.setPeopleorrelationshipid(profile.getPeopleentityid());
            diedOwner.setTimedentityid(diedEntity.getId());
            session.save(diedOwner);
        }

        Timedentityowner profilePictureOwner = new Timedentityowner();
        profilePictureOwner.setTimedentityid(profilePictureMedia.getPostid());
        profilePictureOwner.setPeopleorrelationshipid(profile.getPeopleentityid());
        session.save(profilePictureOwner);

        Timedentityowner profileOwner = new Timedentityowner();
        profileOwner.setTimedentityid(profileTimedEntity.getId());
        profileOwner.setPeopleorrelationshipid(profile.getPeopleentityid());
        session.save(profileOwner);
        session.getTransaction().commit();

        if (jsonNode.has("visibility")) {
            System.out.println("Got visibility!" + jsonNode.get("visibility").asText());
            if (!Util.setVisibilityToEntity(profile.getPeopleentityid(), jsonNode.get("visibility"))) {
                session.getTransaction().rollback();
                session.close();
                return badRequest();
            }
        }
        System.out.println(profile.getPeopleentityid());
        System.out.println(profile.getFirstname());
        System.out.println(profile.getLastname());
        System.out.println(profilePicturePath);
        System.out.println(profile.getGender());
        SearchResult searchResult = new SearchResult(profile.getPeopleentityid(), profile.getFirstname(), profile.getLastname(), profilePicturePath, profile.getGender());


        session.close();
        System.out.println(Json.toJson(searchResult));
        return ok(Json.toJson(searchResult));
    }

    @Transactional
    public Result addGhost() {
        JsonNode jsonNode = request().body().asJson();
        int ownerId = jsonNode.get("ownerId").asInt();
        int ghostId = jsonNode.get("profileId").asInt();
        Session session = SessionHandler.getInstance().getSessionFactory().openSession();
        if (session.get(Ghost.class, ghostId) == null) {
            session.getTransaction().begin();

            Ghost ghost = new Ghost();
            ghost.setOwner(ownerId);
            ghost.setProfileid(ghostId);
            session.save(ghost);
            session.getTransaction().commit();
            session.close();
            return ok(Json.toJson(ghost));
        }
        session.close();
        return badRequest("Invalid profile id");

    }

    @Transactional
    public Result getOwnedProfiles(Integer id) {
        Session session = SessionHandler.getInstance().getSessionFactory().openSession();

        String query = "select profileid from Ghost where owner = " + id;


        List<Integer> ghosts = session.createQuery(query).list();
        List<SearchResult> results = new ArrayList<>();
        for (Integer ghostId : ghosts) {
            query = "select p.peopleentityid as id, p.firstname as firstname, p.lastname as lastname, m.path as profilePicture, p.gender as gender from Profile as p inner join Media as m on m.postid = p.profilepicture where p.peopleentityid = " + ghostId;
            List<Object[]> result = session.createQuery(query).list();
            SearchResult caller;
            for (Object[] resultObj : result) {
                int resid = (int) resultObj[0];
                String resfirstname = (String) resultObj[1];
                String reslastname = (String) resultObj[2];
                String resPath = (String) resultObj[3];
                int resGender = (int) resultObj[4];
                caller = new SearchResult(resid, resfirstname, reslastname, resPath, resGender);
                results.add(caller);
            }
        }

        session.close();
        return ok(Json.toJson(results));
    }

    @BodyParser.Of(value = BodyParser.MultipartFormData.class, maxLength = 1024 * 1024 * 1024)
    public Result upload() {
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart formFile = body.getFile("file");
        if (formFile == null) {
            return ok();
        }
        String contentType = formFile.getContentType();
        String fileType = contentType.substring(0, contentType.indexOf("/"));
        String extension = contentType.substring(contentType.indexOf("/") + 1);
        File file = formFile.getFile();
        String filename;
        if (fileType.equals("video") || fileType.equals("image") || fileType.equals("audio")) {
            filename = String.valueOf(Math.abs(file.hashCode())) + "." + extension;
            File definiteFile = new File("public/" + filename);
            if (file.renameTo(definiteFile)) {
                return ok(Json.toJson(new UploadFile(fileType, Globals.thisFileHost + filename)));
            } else {
                return internalServerError();
            }
        } else {
            return badRequest();
        }
    }

    @Transactional
    public Result getProfile(Integer id) {

        Session session = SessionHandler.getInstance().getSessionFactory().openSession();
        // Get profile SearchResult and model
        Timedentity t = (Timedentity) session.createQuery("from Timedentity where id = :id").setParameter("id", id).list().get(0);

        if (!request().hasHeader("requester") || t.getVisibility() != 0) {
            session.close();
            return badRequest("No requester header specified");
        }
        int requesterId = Integer.parseInt(request().getHeader("requester"));
        try {
            if (!Util.isAllowedToSeeEntity(requesterId, t.getId())) {
                session.close();
                return forbidden();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Profile p = session.get(Profile.class, id);
        String queryString = "select p.peopleentityid as id, p.firstname as firstname, p.lastname as lastname, m.path as profilePicture, p.gender as gender from Profile as p inner join Media as m on m.postid = p.profilepicture where p.peopleentityid = " + id;
        Query query = session.createQuery(queryString);
        SearchResult profile;
        profile = (SearchResult) SearchResult.createSearchResultPersonFromQueryResult(query.list()).get(0);

        // Get born and died events
        List<EventResult> eventResults = new ArrayList<>();
        query = session.createQuery("from Timedentityowner where peopleorrelationshipid = :id");
        query.setParameter("id", profile.id);
        List<Timedentityowner> ownedEvents = query.list();

        List<Timedentity> eventsTE = new ArrayList<>();

        for (Timedentityowner teOwner : ownedEvents) {
            Query query2 = session.createQuery("from Timedentity where id = :id");
            query2.setParameter("id", teOwner.getTimedentityid());
            eventsTE.addAll(query2.list());
        }

        List<Post> posts = new ArrayList<>();
        for (Timedentity timedentity : eventsTE) {
            query = session.createQuery("from Post where timedentityid = " + timedentity.getId());
            posts.addAll(query.list());
        }

        List<Event> events = new ArrayList<>();
        for (Post post : posts) {
            query = session.createQuery("from Event where postid = :postid").setParameter("postid", post.getTimedentityid());
            events.addAll(query.list());
        }
        query = session.createQuery("from Locatedevent where eventid = :eventId");
        query.setParameter("eventId", p.getBorn());
        Locatedevent born;
        LocatedEventResult bornEventResult = null;
        born = session.get(Locatedevent.class, p.getBorn());
        if (born != null) {
            // Get event
            Event bornEvent = (Event) session.createQuery("from Event where postid = " + born.getEventid()).list().get(0);
            // Get locations
            query = session.createQuery("from Location where id = " + born.getLocationid());
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
            Timedentity bornTE = (Timedentity) session.createQuery("from Timedentity where id = :id").setParameter("id", bornEvent.getPostid()).list().get(0);
            Date[] dates = Util.getDates(bornTE.getId());
            System.out.println(Json.toJson(dates));
            String[] times;
            if (dates.length == 2) {
                times = new String[]{dates[0].toString(), dates[1].toString()};
                System.out.println("Times");
                System.out.println(Json.toJson(times));
            } else {
                System.out.println("Time");
                times = new String[]{dates[0].toString()};
                System.out.println(Json.toJson(times));
            }

            bornEventResult = new LocatedEventResult(bornEvent.getPostid(), locationResult, bornEvent.getName(), bornEvent.getDescription(), times, getEventMedia(session, bornEvent.getPostid()));
            for (EventResult er : eventResults) {
                if (er.id == bornEventResult.id) {
                    eventResults.remove(er);
                    break;
                }
            }
        }

        LocatedEventResult diedEventResult = null;
        if (p.getDied() != null) {
            query = session.createQuery("from Locatedevent where eventid = :eventId");
            query.setParameter("eventId", p.getDied());
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
                diedEventResult = new LocatedEventResult(diedEvent.getPostid(), locationResult, diedEvent.getName(), diedEvent.getDescription(), new String[]{time.getTime().toString()}, getEventMedia(session, diedEvent.getPostid()));
                for (EventResult er : eventResults) {
                    if (er.id == diedEventResult.id) {
                        eventResults.remove(er);
                        break;
                    }
                }
            }
        }

        ArrayList<String[]> times = new ArrayList<>();
        for (Event event : events) {
            boolean subEvent = false;
            int teId = -1;
            for (Timedentity timedentity : eventsTE) {
                if (timedentity.getId() == event.getPostid()) {
                    teId = timedentity.getTimeid();
                    break;
                }
            }
            query = session.createQuery("from Singletime where timeid = :timeid");
            query.setParameter("timeid", teId);
            for (Singletime st : (List<Singletime>) query.list()) {
                times.add(new String[]{st.getTime().toString()});
            }

            query = session.createQuery("from Timeinterval where timeid = :timeid");
            query.setParameter("timeid", teId);
            for (Timeinterval ti : (List<Timeinterval>) query.list()) {
                times.add(new String[]{ti.getBegintime().toString(), ti.getEndtime().toString()});
            }

            Date[] dates = Util.getDates(event.getPostid());
            System.out.println(event.getPostid());
            System.out.println(Json.toJson(dates));
            String[] dateStrings = new String[dates.length];
            for (int j = 0; j < dateStrings.length; j++) {
                dateStrings[j] = dates[j].toString();
                System.out.println("Datestring: " + dateStrings[j]);
            }

            // Located event?
            query = session.createQuery("from Locatedevent where eventid = :eventId");
            query.setParameter("eventId", event.getPostid());
            Locatedevent locatedevent = null;
            if (query.list().size() > 0) {
                locatedevent = (Locatedevent) query.list().get(0);
            }
            if (locatedevent != null) {
                subEvent = true;
                // Get locations
                query = session.createQuery("from Location where id = " + locatedevent.getLocationid());
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

                LocatedEventResult locatedEventResult = new LocatedEventResult(event.getPostid(), locationResult, event.getName(), event.getDescription(), dateStrings, getEventMedia(session, event.getPostid()));
                if (locatedEventResult.id != bornEventResult.id) {
                    if (diedEventResult == null || diedEventResult.id != locatedEventResult.id) {
                        try {
                            if (Util.isAllowedToSeeEntity(requesterId, locatedevent.getEventid())) {
                                eventResults.add(locatedEventResult);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            if (!subEvent) {
                // Work event?
                query = session.createQuery("from Workevent where eventid = :eventId");
                query.setParameter("eventId", event.getPostid());
                Workevent workevent = null;

                for (int j = 0; j < dateStrings.length; j++) {
                    dateStrings[j] = dates[j].toString();
                    System.out.println("Datestring: " + dateStrings[j]);
                }

                if (query.list().size() > 0) {
                    workevent = (Workevent) query.list().get(0);
                }
                if (workevent != null) {
                    subEvent = true;
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
                    WorkEventResult workEventResult = new WorkEventResult(workevent.getEventid(), company.getName(), workevent.getPositionheld(), locationResult, event.getName(), event.getDescription(), dateStrings, getEventMedia(session, workevent.getEventid()));
                    try {
                        if (Util.isAllowedToSeeEntity(requesterId, workevent.getEventid())) {
                            eventResults.add(workEventResult);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (!subEvent) {
                    // Move event?
                    query = session.createQuery("from Moveevent where eventid = :eventId");
                    query.setParameter("eventId", event.getPostid());
                    Moveevent moveevent = null;
                    if (query.list().size() > 0) {
                        moveevent = (Moveevent) query.list().get(0);
                    }
                    if (moveevent != null) {
                        subEvent = true;
                        // Get locations
                        query = session.createQuery("from Location where id = " + moveevent.getLocationid());
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

                        MoveEventResult moveEventResult = new MoveEventResult(event.getPostid(), locationResult, event.getName(), event.getDescription(), dateStrings, getEventMedia(session, event.getPostid()));
                        try {
                            if (Util.isAllowedToSeeEntity(requesterId, moveevent.getEventid())) {
                                eventResults.add(moveEventResult);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            // "Normal" event
            if (!subEvent) {
                EventResult eventResult = new EventResult(event.getPostid(), event.getName(), event.getDescription(), dateStrings, getEventMedia(session, event.getPostid()));
                try {
                    if (Util.isAllowedToSeeEntity(requesterId, event.getPostid())) {
                        eventResults.add(eventResult);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


        FullProfile fullProfile = new FullProfile(profile, eventResults);
        fullProfile.born = bornEventResult;
        fullProfile.died = diedEventResult;
        session.close();
        return ok(Json.toJson(fullProfile));
    }

    public Result isOwned() {
        int ownerId = request().body().asJson().get("ownerid").asInt();
        int timedEntityId = request().body().asJson().get("timedentityid").asInt();

        // Replace with JWT verification
        if (ownerId == timedEntityId) {
            return ok(Json.toJson(true));
        }


        Session session = SessionHandler.getInstance().getSessionFactory().openSession();

        // Is entity a profile?
        if (session.get(Profile.class, timedEntityId) != null) {
            if (Util.getOwnerOfProfile(timedEntityId) != ownerId) {
                return ok(Json.toJson(false));
            } else if (Util.getOwnerOfProfile(timedEntityId) == ownerId) {
                return ok(Json.toJson(true));
            }
        }
        Query query = session.createQuery("from Ghost where profileid = :timedEntityId and owner = :ownerId")
                .setParameter("timedEntityId", timedEntityId)
                .setParameter("ownerId", ownerId);
        if (query.list().size() == 0) {
            query = session.createQuery("from Timedentityowner where timedentityid = :tid").setParameter("tid", timedEntityId);
            List<Timedentityowner> owners = query.list();
            for (Timedentityowner owner : owners) {
                if (owner.getPeopleorrelationshipid() == ownerId) {
                    session.close();
                    return ok(Json.toJson(true));
                } else {
                    Ghost ghost = session.get(Ghost.class, owner.getPeopleorrelationshipid());
                    if (ghost != null && ghost.getOwner() == ownerId) {
                        session.close();
                        return ok(Json.toJson(true));
                    } else {
                        if(Util.getOwnerOfProfile(owner.getPeopleorrelationshipid()) == ownerId) {
                            session.close();
                            return ok(Json.toJson(true));
                        }
                    }
                }
            }
        } else {
            session.close();
            return ok(Json.toJson(true));
        }
        session.close();
        return ok(Json.toJson(false));
    }

    @Transactional
    public Result updateProfile(Integer id) {
        Session session = SessionHandler.getInstance().getSessionFactory().openSession();
        Query query = session.createQuery("from Profile where peopleentityid = :id").setParameter("id", id);
        Profile profile;
        if (query.list().size() == 0) {
            return notFound();
        }
        profile = (Profile) query.list().get(0);

        JsonNode jsonNode = request().body().asJson();
        if (jsonNode.has("firstname")) {
            profile.setFirstname(jsonNode.get("firstname").asText());
        }
        if (jsonNode.has("lastname")) {
            profile.setLastname(jsonNode.get("lastname").asText());
        }
        session.getTransaction().begin();
        if (jsonNode.has("image")) {
            Media media = (Media) session.createQuery("from Media where postid = :postid").setParameter("postid", profile.getProfilepicture()).list().get(0);
            media.setPath(jsonNode.get("image").asText());
            session.save(media);
            profile.setProfilepicture(media.getPostid());
        }

        if (jsonNode.has("birthDay")) {
            Timedentity birthDay = (Timedentity) session.createQuery("from Timedentity where id = :id").setParameter("id", profile.getBorn()).list().get(0);
            int newTimeId = Util.getOrCreateTime(new Date[]{Util.parseDateFromString(jsonNode.get("birthDay").asText())});
            birthDay.setTimeid(newTimeId);
            session.save(birthDay);
        }
        if (jsonNode.has("born")) {
            Event bornEvent = (Event) session.createQuery("from Event where postid = :id").setParameter("id", profile.getBorn()).list().get(0);
            bornEvent.setDescription(jsonNode.get("born").get("description").asText());
            session.save(bornEvent);
            Locatedevent bornLocatedEvent = (Locatedevent) session.createQuery("from Locatedevent where eventid = :id").setParameter("id", profile.getBorn()).list().get(0);
            bornLocatedEvent.setLocationid(Util.createOrGetLocation(jsonNode.get("born").get("location").get("city").asText(), jsonNode.get("born").get("location").get("province").asText(), jsonNode.get("born").get("location").get("country").asText()));
            session.save(bornLocatedEvent);
        }
        if (jsonNode.has("deathDay") || jsonNode.has("died")) {
            if (profile.getDied() == null) {
                // Need to create death :-O
                int deathTimeId = Util.getOrCreateTime(new Date[]{Util.parseDateFromString(jsonNode.get("deathDay").asText())});
                Timedentity deathTE = new Timedentity();
                deathTE.setTimeid(deathTimeId);
                session.save(deathTE);

                Post deathPost = new Post();
                deathPost.setTimedentityid(deathTE.getId());
                session.save(deathPost);

                Event deathEvent = new Event();
                deathEvent.setDescription(jsonNode.get("died").get("description").asText());
                deathEvent.setName(jsonNode.get("died").get("name").asText());
                deathEvent.setPostid(deathPost.getTimedentityid());
                session.save(deathEvent);

                Locatedevent death = new Locatedevent();
                death.setEventid(deathEvent.getPostid());
                death.setLocationid(Util.createOrGetLocation(jsonNode.get("died").get("location").get("city").asText(), jsonNode.get("died").get("location").get("province").asText(), jsonNode.get("died").get("location").get("country").asText()));
                session.save(death);

                profile.setDied(death.getEventid());

            } else {
                // Update date if necessary
                if (jsonNode.has("deathDay")) {
                    Timedentity deathDay = (Timedentity) session.createQuery("from Timedentity where id = :id").setParameter("id", profile.getDied()).list().get(0);

                    deathDay.setTimeid(Util.getOrCreateTime(new Date[]{Util.parseDateFromString(jsonNode.get("deathDay").asText())}));
                    session.save(deathDay);
                }
                // Update event if necessary
                if (jsonNode.has("died")) {
                    Event diedEvent = (Event) session.createQuery("from Event where postid = :id").setParameter("id", profile.getDied()).list().get(0);
                    diedEvent.setDescription(jsonNode.get("died").get("description").asText());
                    session.save(diedEvent);
                    Locatedevent diedLocatedEvent = (Locatedevent) session.createQuery("from Locatedevent where eventid = :id").setParameter("id", profile.getDied()).list().get(0);
                    diedLocatedEvent.setLocationid(Util.createOrGetLocation(jsonNode.get("died").get("location").get("city").asText(), jsonNode.get("died").get("location").get("province").asText(), jsonNode.get("died").get("location").get("country").asText()));
                    session.save(diedLocatedEvent);
                }
            }
        }
        session.save(profile);
        session.getTransaction().commit();
        session.close();
        return ok();
    }

    public Result delete(Integer id) {
        Session session = SessionHandler.getInstance().getSessionFactory().openSession();

        Timedentity timedentity = session.get(Timedentity.class, id);
        if (timedentity != null) {
            session.getTransaction().begin();
            session.delete(timedentity);
            session.getTransaction().commit();
        } else {
            session.close();
            return notFound();
        }
        session.close();
        return ok();
    }
}
