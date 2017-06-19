package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.*;
import org.hibernate.Session;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
        if(jsonNode.has("deathDay")) {
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
        if (deathDayDate != null) {
            Time diedTime = new Time();
            session.save(diedTime);

            Singletime diedSingleTime = new Singletime();
            diedSingleTime.setTimeid(diedTime.getId());
            diedSingleTime.setTime(deathDayDate);
            session.save(diedSingleTime);

            Timedentity diedEntity = new Timedentity();
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
        String profilePicturePath = jsonNode.get("profilePicture").asText();

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

        session.getTransaction().commit();

        JsonNode newProfile = Json.toJson(profile);
        System.out.println(newProfile.toString());
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


}
