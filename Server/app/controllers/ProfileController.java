package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.*;
import org.hibernate.Query;
import org.hibernate.Session;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import returnTypes.*;
import utils.SessionHandler;

import java.io.File;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ProfileController extends Controller {

    @Transactional
    public Result createProfile() {
        JsonNode jsonNode = request().body().asJson();

        // Extract values from request body
        String genderText = jsonNode.get("gender").asText();
        Integer gender = genderText.equals("male") ? 0 : genderText.equals("female") ? 1 : 2;

        String birthDay = jsonNode.get("birthDay").asText();
        String deathDay = "";
        if (jsonNode.has("deathDay")) {
            deathDay = jsonNode.get("deathDay").asText();
        }

        // Start creating entities
        Session session = SessionHandler.getInstance().getSessionFactory().openSession();
        session.getTransaction().begin();

        // Profile entity
        Profile profile = new Profile();
        profile.setFirstname(jsonNode.get("firstName").asText());
        profile.setLastname(jsonNode.get("lastName").asText());
        profile.setGender(gender);

        // Profile values from request body
        JsonNode bornNode = jsonNode.get("born");
        String bornName = bornNode.get("name").asText();
        String bornDescription = bornNode.get("description").asText();
        JsonNode bornLocationNode = bornNode.get("location");
        String bornCity = bornLocationNode.get("city").asText();
        String bornProvince = bornLocationNode.get("province").asText();
        String bornCountry = bornLocationNode.get("country").asText();
        JsonNode bornMedia = bornNode.get("media");


        // Create Time for birth
        Time birthtime = new Time();
        session.save(birthtime);

        Date birthDayDate = null;
        Date deathDayDate = null;
        try {
            SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MM-yyyy");
            birthDayDate = new Date(sdf1.parse(birthDay).getTime());
            if (!deathDay.equals("")) {
                deathDayDate = new Date(sdf1.parse(deathDay).getTime());
            }
        } catch (ParseException e) {
            System.out.println("Exception while parsing date: " + e.getMessage());
        }

        Singletime birthSingleTime = new Singletime();
        birthSingleTime.setTimeid(birthtime.getId());
        birthSingleTime.setTime(birthDayDate);
        session.save(birthSingleTime);

        // Born locatedevent
        // First, born timedEntity
        Timedentity bornTimedEntity = new Timedentity();
        bornTimedEntity.setTimeid(birthSingleTime.getTimeid());
        session.save(bornTimedEntity);

        // Second, born Post entity
        Post bornPost = new Post();
        bornPost.setTimedentityid(bornTimedEntity.getId());
        session.save(bornPost);


        // Third, born Event
        Event bornEvent = new Event();
        bornEvent.setPostid(bornPost.getTimedentityid());
        bornEvent.setName(bornName);
        bornEvent.setDescription(bornDescription);
        session.save(bornEvent);
        // Born location
        int bornLocationId = this.createLocation(session, bornCity, bornProvince, bornCountry);

        // Lastly, born LocatedEvent
        Locatedevent bornLocatedEvent = new Locatedevent();
        bornLocatedEvent.setLocationid(bornLocationId);
        bornLocatedEvent.setEventid(bornEvent.getPostid());
        session.save(bornLocatedEvent);

        profile.setBorn(bornLocatedEvent.getEventid());


        JsonNode died = jsonNode.get("died");
        String diedCity = null;
        String diedProvince = null;
        String diedCountry = null;
        String diedName = null;
        String diedDescription = null;

        if (died.has("location")) {

            diedName = died.get("name").asText();
            diedDescription = died.get("description").asText();

            diedCity = died.get("location").get("city").asText();
            diedProvince = died.get("location").get("province").asText();
            diedCountry = died.get("location").get("country").asText();

        }

        // Profile timedEntity
        Timedentity profileTimedEntity = new Timedentity();

        // Died locatedevent
        Timedentity diedEntity = null;
        if (deathDayDate != null) {
            Time diedTime = new Time();
            session.save(diedTime);

            Singletime diedSingleTime = new Singletime();
            diedSingleTime.setTimeid(diedTime.getId());
            diedSingleTime.setTime(deathDayDate);
            session.save(diedSingleTime);

            diedEntity = new Timedentity();
            diedEntity.setTimeid(diedTime.getId());
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
            int diedLocationId = this.createLocation(session, diedCity, diedProvince, diedCountry);

            Locatedevent diedLocatedEvent = new Locatedevent();
            diedLocatedEvent.setLocationid(diedLocationId);
            diedLocatedEvent.setEventid(diedEvent.getPostid());
            session.save(diedLocatedEvent);

            profile.setDied(diedLocatedEvent.getEventid());
            Time lifeTime = new Time();
            session.save(lifeTime);
            Timeinterval lifeSpan = new Timeinterval();
            lifeSpan.setTimeid(lifeTime.getId());
            lifeSpan.setBegintime(birthSingleTime.getTime());
            lifeSpan.setEnddate(deathDayDate);
            session.save(lifeSpan);
            profileTimedEntity.setTimeid(lifeSpan.getTimeid());
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
            profilePicturePath = "http://www.wikiality.com/file/2016/11/bears1.jpg";
        }
        Time profilePictureTime = new Time();
        session.save(profilePictureTime);

        Singletime profilePictureSingleTime = new Singletime();
        profilePictureSingleTime.setTimeid(profilePictureTime.getId());
        profilePictureSingleTime.setTime(new java.sql.Date(System.currentTimeMillis()));
        session.save(profilePictureSingleTime);

        Timedentity profilePictureTimedEntity = new Timedentity();
        profilePictureTimedEntity.setTimeid(profilePictureTime.getId());
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
        System.out.println(Json.toJson(bornOwner).asText());

        if (diedEntity != null) {
            Timedentityowner diedOwner = new Timedentityowner();
            diedOwner.setPeopleorrelationshipid(profile.getPeopleentityid());
            diedOwner.setTimedentityid(diedEntity.getId());
            System.out.println(Json.toJson(diedOwner).asText());
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

        JsonNode newProfile = Json.toJson(profile);
        System.out.println(newProfile.toString());


        session.close();
        return ok(newProfile.toString());
    }

    int createLocation(Session session, String cityName, String provinceName, String countryName) {
        City city = null;


        List<City> cities = (List<City>) session.createQuery("FROM City").list();
        for (City c : cities) {
            System.out.println(c.getName());
            if (c.getName().equals(cityName)) {
                city = c;
            }
        }
        if (city == null) {
            city = new City();
            city.setName(cityName);
            session.save(city);
        }

        Province province = null;

        List<Province> provinces = session.createQuery("from Province").list();
        for (Province p : provinces) {
            if (p.getName().equals(provinceName)) {
                province = p;
            }
        }
        if (province == null) {
            province = new Province();
            province.setName(provinceName);
            session.save(province);
        }

        Country country = null;

        List<Country> countries = session.createQuery("from Country").list();
        for (Country c : countries) {
            if (c.getName().equals(countryName)) {
                country = c;
            }
        }
        if (country == null) {
            country = new Country();
            country.setName(countryName);
            session.save(country);
        }

        Cityprovince cityprovince = null;
        List<Cityprovince> cityprovinces = session.createQuery("from Cityprovince ").list();
        for (Cityprovince cp : cityprovinces) {
            if (cp.getCityid() == city.getId() && cp.getProvinceid() == province.getId()) {
                cityprovince = cp;
            }
        }
        if (cityprovince == null) {
            cityprovince = new Cityprovince();
            cityprovince.setProvinceid(province.getId());
            cityprovince.setCityid(city.getId());
            session.save(cityprovince);
        }


        Provincecountry provinceCountry = null;

        List<Provincecountry> provincecountries = session.createQuery("from Provincecountry ").list();

        for (Provincecountry pc : provincecountries) {
            if (pc.getCountryid() == country.getId() && pc.getProvinceid() == province.getId()) {
                provinceCountry = pc;
            }
        }
        if (provinceCountry == null) {
            provinceCountry = new Provincecountry();
            provinceCountry.setProvinceid(province.getId());
            provinceCountry.setCountryid(country.getId());
            session.save(provinceCountry);
        }

        Location location = null;
        List<Location> locations = session.createQuery("from Location").list();
        for (Location l : locations) {
            if (l.getCityprovinceid() == cityprovince.getId() && l.getProvincecountryid() == provinceCountry.getId()) {
                location = l;
            }
        }
        if (location == null) {
            location = new Location();
            location.setProvincecountryid(provinceCountry.getId());
            location.setCityprovinceid(cityprovince.getId());
            session.save(location);
        }
        return location.getId();
    }

    @Transactional
    public Result addGhost() {
        JsonNode jsonNode = request().body().asJson();
        int ownerId = jsonNode.get("ownerId").asInt();
        int ghostId = jsonNode.get("profileId").asInt();
        Session session = SessionHandler.getInstance().getSessionFactory().openSession();
        session.getTransaction().begin();

        Ghost ghost = new Ghost();
        ghost.setOwner(ownerId);
        ghost.setProfileid(ghostId);
        session.save(ghost);
        session.getTransaction().commit();
        session.close();
        return ok(Json.toJson(ghost));
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

    //@BodyParser.Of(value = BodyParser.Text.class, maxLength = 10 * 1024)
    public Result upload() {
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart picture = body.getFile("picture");
        if (picture != null) {
            String fileName = picture.getFilename();
            String contentType = picture.getContentType();
            File file = picture.getFile();
            String filename = String.valueOf(Math.abs(file.hashCode())) + ".jpg";
            File definiteFile = new File("public/" + filename);
            file.renameTo(definiteFile);
            System.out.println(filename);
            JsonNode responseJson = Json.toJson(definiteFile.getName());
            return ok(responseJson);
        } else {
            return badRequest();
        }
    }

    @Transactional
    public Result getProfile(Integer id) {

        Session session = SessionHandler.getInstance().getSessionFactory().openSession();

        // Get profile SearchResult and model
        Profile p = (Profile) session.createQuery("from Profile where peopleentityid = :id").setParameter("id", id).list().get(0);
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
            try {
                posts.addAll(query.list());
            } catch (Exception e) {
            }
        }


        query = session.createQuery("from Locatedevent where eventid = :eventId");
        query.setParameter("eventId", p.getBorn());
        Locatedevent born = null;
        LocatedEventResult bornEventResult = null;
        try {
            born = (Locatedevent) query.list().get(0);
        } catch (Exception e) {
        }
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
            Timedentity diedTE = (Timedentity) session.createQuery("from Timedentity where id = :id").setParameter("id", bornEvent.getPostid()).list().get(0);
            Singletime time = (Singletime) session.createQuery("from Singletime where timeid = :timeid").setParameter("timeid", diedTE.getTimeid()).list().get(0);

            bornEventResult = new LocatedEventResult(bornEvent.getPostid(), locationResult, bornEvent.getName(), bornEvent.getDescription(), new String[]{time.getTime().toString()});
            System.out.println(Json.toJson(bornEventResult));
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
            Locatedevent died = null;
            try {
                died = (Locatedevent) query.list().get(0);
            } catch (Exception e) {
            }
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
                        "inner join Country co on co.id = pc.countryid where l.id = " + location.getId()).list();
                LocationResult locationResult = new LocationResult((String) attrs.get(0)[0], (String) attrs.get(0)[1], (String) attrs.get(0)[2]);
                Timedentity diedTE = (Timedentity) session.createQuery("from Timedentity where id = :id").setParameter("id", diedEvent.getPostid()).list().get(0);
                Singletime time = (Singletime) session.createQuery("from Singletime where timeid = :timeid").setParameter("timeid", diedTE.getTimeid()).list().get(0);
                diedEventResult = new LocatedEventResult(diedEvent.getPostid(), locationResult, diedEvent.getName(), diedEvent.getDescription(), new String[]{time.getTime().toString()});
                System.out.println(Json.toJson(diedEventResult));
                for (EventResult er : eventResults) {
                    if (er.id == diedEventResult.id) {
                        eventResults.remove(er);
                        break;
                    }
                }
            }
        }

        List<Event> events = new ArrayList<>();
        List<Media> media = new ArrayList<>();
        for (Post post : posts) {
            query = session.createQuery("from Event where postid = " + post.getTimedentityid());
            events.addAll(query.list());
            query = session.createQuery("from Media where postid = " + post.getTimedentityid());
            media.addAll(query.list());
        }

        ArrayList<String[]> times = new ArrayList<>();
        int i = 0;
        for (Event event : events) {
            boolean subEvent = false;
            int teId = -1;
            for (Timedentity timedentity : eventsTE) {
                if (timedentity.getId() == event.getPostid()) {
                    teId = timedentity.getTimeid();
                    break;
                }
            }
            System.out.println(teId);
            query = session.createQuery("from Singletime where timeid = :timeid");
            query.setParameter("timeid", teId);
            for (Singletime st : (List<Singletime>) query.list()) {
                times.add(new String[]{st.getTime().toString()});
            }

            query = session.createQuery("from Timeinterval where timeid = :timeid");
            query.setParameter("timeid", teId);
            for (Timeinterval ti : (List<Timeinterval>) query.list()) {
                times.add(new String[]{ti.getBegintime().toString(), ti.getEnddate().toString()});
            }

            // Located event?
            query = session.createQuery("from Locatedevent where eventid = :eventId");
            query.setParameter("eventId", event.getPostid());
            Locatedevent locatedevent = null;
            try {
                locatedevent = (Locatedevent) query.list().get(0);
            } catch (Exception e) {
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

                LocatedEventResult locatedEventResult = new LocatedEventResult(event.getPostid(), locationResult, event.getName(), event.getDescription(), times.get(i));
                System.out.println(Json.toJson(locatedEventResult));
                if (locatedEventResult.id != bornEventResult.id && locatedEventResult.id != diedEventResult.id) {
                    eventResults.add(locatedEventResult);
                }
            }

            // Work event?
            query = session.createQuery("from Workevent where eventid = :eventId");
            query.setParameter("eventId", event.getPostid());
            Workevent workevent = null;
            try {
                workevent = (Workevent) query.list().get(0);
            } catch (Exception e) {
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
                WorkEventResult workEventResult = new WorkEventResult(workevent.getEventid(), company.getName(), workevent.getPositionheld(), locationResult, event.getName(), event.getDescription(), times.get(i));
                System.out.println(Json.toJson(workEventResult));
                eventResults.add(workEventResult);
            }

            // Move event?
            query = session.createQuery("from Moveevent where eventid = :eventId");
            query.setParameter("eventId", event.getPostid());
            Moveevent moveevent = null;
            try {
                moveevent = (Moveevent) query.list().get(0);
            } catch (Exception e) {
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

                MoveEventResult moveEventResult = new MoveEventResult(event.getPostid(), locationResult, event.getName(), event.getDescription(), times.get(i));
                System.out.println(Json.toJson(moveEventResult));
                eventResults.add(moveEventResult);
            }


            // "Normal" event
            if(!subEvent) {
                EventResult eventResult = new EventResult(event.getPostid(), event.getName(), event.getDescription(), times.get(i));
                eventResults.add(eventResult);
            }
            i++;
        }


        FullProfile fullProfile = new FullProfile(profile, eventResults);
        fullProfile.born = bornEventResult;
        fullProfile.died = diedEventResult;
        session.close();
        return ok(Json.toJson(fullProfile));
    }
}
