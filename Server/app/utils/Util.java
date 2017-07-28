package utils;

import com.fasterxml.jackson.databind.JsonNode;
import models.*;
import org.hibernate.*;
import org.hibernate.transform.Transformers;
import play.libs.Json;
import returnTypes.LocationResult;
import returnTypes.ProfileResult;

import javax.persistence.LockModeType;
import javax.validation.ConstraintViolationException;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Util {
    /**
     * @param cityName
     * @param provinceName
     * @param countryName
     * @return
     */
    public static int createOrGetLocation(String cityName, String provinceName, String countryName) {
        City city;
        Session session = SessionHandler.getInstance().getSessionFactory().openSession();
        Transaction tx = session.getTransaction();
        try {
            tx.begin();
            List<City> cities = (List<City>) session.createQuery("FROM City where name = :cityname").setParameter("cityname", cityName).list();
            if (cities.size() > 0) {
                city = cities.get(0);
            } else {
                city = new City();
                city.setName(cityName);
                session.save(city);
            }

            Province province;
            List<Province> provinces = session.createQuery("from Province where name = :provincename").setParameter("provincename", provinceName).list();
            if (provinces.size() > 0) {
                province = provinces.get(0);
            } else {
                province = new Province();
                province.setName(provinceName);
                session.save(province);
            }

            Country country;
            List<Country> countries = session.createQuery("from Country where name = :countryname").setParameter("countryname", countryName).list();
            if (countries.size() > 0) {
                country = countries.get(0);
            } else {
                country = new Country();
                country.setName(countryName);
                session.save(country);
            }

            Cityprovince cityprovince;
            List<Cityprovince> cityprovinces = session.createQuery("from Cityprovince where provinceid = :provinceid and cityid = :cityid")
                    .setParameter("provinceid", province.getId())
                    .setParameter("cityid", city.getId()).list();
            if (cityprovinces.size() > 0) {
                cityprovince = cityprovinces.get(0);
            } else {
                cityprovince = new Cityprovince();
                cityprovince.setProvinceid(province.getId());
                cityprovince.setCityid(city.getId());
                session.save(cityprovince);
            }

            Provincecountry provinceCountry;
            List<Provincecountry> provincecountries = session.createQuery("from Provincecountry where provinceid = :provinceid and countryid = :countryid")
                    .setParameter("provinceid", province.getId())
                    .setParameter("countryid", country.getId()).list();
            if (provincecountries.size() > 0) {
                provinceCountry = provincecountries.get(0);
            } else {
                provinceCountry = new Provincecountry();
                provinceCountry.setProvinceid(province.getId());
                provinceCountry.setCountryid(country.getId());
                session.save(provinceCountry);
            }


            Location location;
            List<Location> locations = session.createQuery("from Location where cityprovinceid = :cityprovinceid and provincecountryid = :provincecountryid")
                    .setParameter("cityprovinceid", cityprovince.getId())
                    .setParameter("provincecountryid", provinceCountry.getId()).list();
            if (locations.size() > 0) {
                location = locations.get(0);
            } else {
                location = new Location();
                location.setProvincecountryid(provinceCountry.getId());
                location.setCityprovinceid(cityprovince.getId());
                session.save(location);
            }
            tx.commit();
            return location.getId();
        } catch (Exception e) {
            // Hibernate envoie un rollback sur la connexion JDBC
            System.out.println("HElLELLELLELLELLE");
            System.out.println(e.getMessage());
            tx.rollback();
            session.close();
        }
        return createOrGetLocation(cityName, provinceName, countryName);
    }

    /**
     * @param session
     * @param eventId
     * @return
     */
    public static List<Media> getEventMedia(Session session, int eventId) {
        List<Eventmedia> em = session.createQuery("from Eventmedia where eventid = " + eventId).list();

        List<Media> mediaList = new ArrayList<>();
        for (Eventmedia eventmedia : em) {
            mediaList.addAll(session.createQuery("from Media where postid = " + eventmedia.getMediaid()).list());
        }
        return mediaList;
    }

    /**
     * @param dates date or dates to save as a Time and Timeinterval or Singletime.
     * @return id of Time entity created
     */
    public static int getOrCreateTime(Date[] dates) {
        Session session = SessionHandler.getInstance().getSessionFactory().openSession();

        if (dates.length == 2) {
            // Time interval
            Query query = session.createQuery("from Timeinterval ti where ti.begintime = :time1 and ti.endtime = :time2").setParameter("time1", dates[0]).setParameter("time2", dates[1]);
            if (query.list().size() == 0) {
                System.out.println("Create new time interval");
                session.getTransaction().begin();
                Time time = new Time();
                session.save(time);
                Timeinterval ti = new Timeinterval();
                ti.setTimeid(time.getId());
                ti.setBegintime(dates[0]);
                ti.setEndtime(dates[1]);
                session.save(ti);
                session.getTransaction().commit();
                session.close();
                return ti.getTimeid();
            } else {
                int timeintervalid = ((Timeinterval) query.list().get(0)).getTimeid();
                session.close();
                System.out.println("Return existing timeinterval");
                return timeintervalid;
            }
        } else {
            // Single time
            Query query = session.createQuery("from Singletime st where st.time = :time").setParameter("time", dates[0]);
            if (query.list().size() == 0) {
                System.out.println("Create new single time");
                session.getTransaction().begin();
                Time time = new Time();
                session.save(time);
                Singletime st = new Singletime();
                st.setTime(dates[0]);
                st.setTimeid(time.getId());
                session.save(st);
                session.getTransaction().commit();
                session.close();
                return st.getTimeid();
            } else {
                System.out.println("Return existing singletime");
                int st = ((Singletime) query.list().get(0)).getTimeid();
                session.close();
                return st;
            }
        }
    }

    /**
     * @param dateToParse String in format "yyyy-mm-dd" to convert to Date object
     * @return a sql.Date object
     */
    public static Date parseDateFromString(String dateToParse) {
        try {
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
            return new Date(sdf1.parse(dateToParse).getTime());
        } catch (ParseException e) {
            System.out.println("Exception while parsing date: " + e.getMessage());
        }
        return null;
    }

    /**
     * @param entityid: entity whose Dates need to be retrieved
     * @return date(s) of entity. Array of length 1 if entity is associated to a Singletime, length 2 if Timeinterval
     */
    public static Date[] getDates(int entityid) {
        Session session = SessionHandler.getInstance().getSessionFactory().openSession();
        // Single time
        Timedentity timedentity = session.get(Timedentity.class, entityid);
        int id = timedentity.getTimeid();
        Query query = session.createQuery("from Singletime where timeid = :id").setParameter("id", id);
        if (query.list().size() > 0) {
            Singletime singletime = (Singletime) query.list().get(0);
            session.close();
            return new Date[]{singletime.getTime()};
        }

        // Time interval
        query = session.createQuery("from Timeinterval where timeid = :id").setParameter("id", id);
        if (query.list().size() > 0) {
            Timeinterval timeinterval = (Timeinterval) query.list().get(0);
            session.close();
            return new Date[]{timeinterval.getBegintime(), timeinterval.getEndtime()};
        }

        session.close();
        return null;
    }

    /**
     * @param entityId    : Entity whose visibility is set
     * @param visibility: Unaltered JsonNode containing ids of groups and people to include / exclude
     * @return
     */
    public static boolean setVisibilityToEntity(int entityId, JsonNode visibility) {
        System.out.println(visibility);
        Session session = SessionHandler.getInstance().getSessionFactory().openSession();
        Timedentity entity = (Timedentity) session.createQuery("from Timedentity where id = :id").setParameter("id", entityId).list().get(0);
        System.out.println(Json.toJson(entity));
        session.getTransaction().begin();

        if (visibility.has("visibility")) {
            if (visibility.get("visibility").asText().equals("private")) {

                System.out.println("PRIVATATE");
                entity.setVisibility(1);
                session.saveOrUpdate(entity);
            } else if (visibility.get("visibility").asText().equals("public")) {
                entity.setVisibility(0);
                session.saveOrUpdate(entity);
            } else if (visibility.get("visibility").asText().equals("limited")) {

                entity.setVisibility(2);
                JsonNode included = visibility.get("included");
                JsonNode excluded = visibility.get("excluded");


                if (included.has("groups")) {
                    for (int i = 0; i < included.get("groups").size(); i++) {
                        int groupId = included.get("groups").get(i).asInt();

                        Access access = new Access();
                        access.setTimedentityid(entityId);
                        session.save(access);

                        Groupaccess groupaccess = new Groupaccess();
                        groupaccess.setAccessid(access.getId());
                        groupaccess.setGroupid(groupId);
                        session.save(groupaccess);

                        Visibleby visibleby = new Visibleby();
                        visibleby.setAccessid(access.getId());
                        visibleby.setTimedentityid(entityId);
                        session.save(visibleby);
                    }
                }
                if (included.has("people")) {
                    for (int i = 0; i < included.get("people").size(); i++) {
                        int profileId = included.get("people").get(i).asInt();

                        Access access = new Access();
                        access.setTimedentityid(entityId);
                        session.save(access);

                        Profileaccess profileaccess = new Profileaccess();
                        profileaccess.setAccessid(access.getId());
                        profileaccess.setProfileid(profileId);
                        session.save(profileaccess);

                        Visibleby visibleby = new Visibleby();
                        visibleby.setAccessid(access.getId());
                        visibleby.setTimedentityid(entityId);
                        session.save(visibleby);
                    }
                }
                if (excluded.has("groups")) {
                    for (int i = 0; i < excluded.get("groups").size(); i++) {
                        int groupId = excluded.get("groups").get(i).asInt();

                        Access access = new Access();
                        access.setTimedentityid(entityId);
                        session.save(access);

                        Groupaccess groupaccess = new Groupaccess();
                        groupaccess.setAccessid(access.getId());
                        groupaccess.setGroupid(groupId);
                        session.save(groupaccess);

                        Notvisibleby notvisibleby = new Notvisibleby();
                        notvisibleby.setAccessid(access.getId());
                        notvisibleby.setTimedentityid(entityId);
                        session.save(notvisibleby);
                    }
                }
                if (excluded.has("people")) {
                    for (int i = 0; i < excluded.get("people").size(); i++) {
                        int profileId = excluded.get("people").get(i).asInt();

                        Access access = new Access();
                        access.setTimedentityid(entityId);
                        session.save(access);

                        Profileaccess profileaccess = new Profileaccess();
                        profileaccess.setAccessid(access.getId());
                        profileaccess.setProfileid(profileId);
                        session.save(profileaccess);

                        Notvisibleby notvisibleby = new Notvisibleby();
                        notvisibleby.setAccessid(access.getId());
                        notvisibleby.setTimedentityid(entityId);
                        session.save(notvisibleby);
                    }
                }
                session.save(entity);


            } else {
                session.getTransaction().rollback();
                session.close();
                return false;
            }
        }
        session.getTransaction().commit();
        session.close();
        return true;
    }

    public static boolean setVisibilityToEntity2(int entityId, JsonNode visibility, Session session) {
        System.out.println(visibility);
        Timedentity entity = (Timedentity) session.createQuery("from Timedentity where id = :id").setParameter("id", entityId).list().get(0);
        if (visibility.has("visibility")) {
            if (visibility.get("visibility").asText().equals("private")) {
                entity.setVisibility(1);
                session.getTransaction().begin();
                session.saveOrUpdate(entity);
            } else if (visibility.get("visibility").asText().equals("public")) {
                entity.setVisibility(0);
                session.getTransaction().begin();
                session.saveOrUpdate(entity);
            } else if (visibility.get("visibility").asText().equals("limited")) {

                entity.setVisibility(2);

                JsonNode included = null;
                if (visibility.has("included")) {
                    included = visibility.get("included");
                }
                JsonNode excluded = null;
                if (visibility.has("excluded")) {
                    excluded = visibility.get("excluded");
                }

                if (excluded == null && included == null) {
                    return false;
                }

                if (included != null) {
                    if (included.has("groups")) {
                        for (int i = 0; i < included.get("groups").size(); i++) {
                            int groupId = included.get("groups").get(i).asInt();

                            Access access = new Access();
                            access.setTimedentityid(entityId);
                            session.save(access);

                            Groupaccess groupaccess = new Groupaccess();
                            groupaccess.setAccessid(access.getId());
                            groupaccess.setGroupid(groupId);
                            session.save(groupaccess);

                            Visibleby visibleby = new Visibleby();
                            visibleby.setAccessid(access.getId());
                            visibleby.setTimedentityid(entityId);
                            session.save(visibleby);
                        }
                    }
                    if (included.has("people")) {
                        for (int i = 0; i < included.get("people").size(); i++) {
                            int profileId = included.get("people").get(i).asInt();

                            Access access = new Access();
                            access.setTimedentityid(entityId);
                            session.save(access);

                            Profileaccess profileaccess = new Profileaccess();
                            profileaccess.setAccessid(access.getId());
                            profileaccess.setProfileid(profileId);
                            session.save(profileaccess);

                            Visibleby visibleby = new Visibleby();
                            visibleby.setAccessid(access.getId());
                            visibleby.setTimedentityid(entityId);
                            session.save(visibleby);
                        }
                    }
                }
                if (excluded != null) {
                    if (excluded.has("groups")) {
                        for (int i = 0; i < excluded.get("groups").size(); i++) {
                            int groupId = excluded.get("groups").get(i).asInt();

                            Access access = new Access();
                            access.setTimedentityid(entityId);
                            session.save(access);

                            Groupaccess groupaccess = new Groupaccess();
                            groupaccess.setAccessid(access.getId());
                            groupaccess.setGroupid(groupId);
                            session.save(groupaccess);

                            Notvisibleby notvisibleby = new Notvisibleby();
                            notvisibleby.setAccessid(access.getId());
                            notvisibleby.setTimedentityid(entityId);
                            session.save(notvisibleby);
                        }
                    }
                    if (excluded.has("people")) {
                        for (int i = 0; i < excluded.get("people").size(); i++) {
                            int profileId = excluded.get("people").get(i).asInt();

                            Access access = new Access();
                            access.setTimedentityid(entityId);
                            session.save(access);

                            Profileaccess profileaccess = new Profileaccess();
                            profileaccess.setAccessid(access.getId());
                            profileaccess.setProfileid(profileId);
                            session.save(profileaccess);

                            Notvisibleby notvisibleby = new Notvisibleby();
                            notvisibleby.setAccessid(access.getId());
                            notvisibleby.setTimedentityid(entityId);
                            session.save(notvisibleby);
                        }
                    }
                }
                session.save(entity);
            } else {
                return false;
            }
        }
        return true;
    }


    public static boolean isAllowedToSeeEntity(Integer requesterId, int timedEntityId) {
        Session session = SessionHandler.getInstance().getSessionFactory().openSession();

        Query query = session.createQuery("from Timedentity where id = :id").setParameter("id", timedEntityId);
        Timedentity timedentity;
        if (query.list().size() == 0) {
            session.close();
            return false;
        }
        timedentity = (Timedentity) query.list().get(0);

        // Public
        if (timedentity.getVisibility() == 0) {
            session.close();
            return true;
        } else if (timedentity.getVisibility() == 1) {
            if (requesterId == null || requesterId.equals(-1)) {
                session.close();
                return false;
            }
            // Private
            // Is requester owner of the entity?
            query = session.createQuery("from Timedentityowner where timedentityid = :id and peopleorrelationshipid = :requesterId").setParameter("id", timedEntityId).setParameter("requesterId", requesterId);
            if (query.list().size() > 0) {
                session.close();
                return true;
            }
            // Is requester the entity?
            if (requesterId.equals(timedentity.getId())) {
                session.close();
                return true;
            }


            // Is requester the account associated to the owner of the entity?
            List<Timedentityowner> ownersOfEntity = session.createQuery("from Timedentityowner where timedentityid = :id").setParameter("id", timedEntityId).list();
            for (Timedentityowner ownerOfEntity : ownersOfEntity) {
                if (requesterId.equals(ownerOfEntity.getPeopleorrelationshipid())) {
                    session.close();
                    return true;
                } else if (requesterId.equals(getOwnerOfProfile(ownerOfEntity.getPeopleorrelationshipid()))) {
                    session.close();
                    return true;
                }
            }
        } else {
            // Limited
            if (requesterId.intValue() == -1 || requesterId == null) {
                session.close();
                System.out.println(1);
                return false;
            }

            // A limited visibility is by definition forbidden to non registered users

            // Is requester owner of the entity?
            List<Timedentityowner> owners = session.createQuery("from Timedentityowner where timedentityid = :id").setParameter("id", timedEntityId).list();
            if (query.list().size() > 0) {
                for (Timedentityowner timedentityowner : owners) {
                    if (requesterId.equals(timedentityowner.getPeopleorrelationshipid()) || requesterId.equals(getOwnerOfProfile(timedentityowner.getPeopleorrelationshipid()))) {
                        session.close();
                        return true;
                    }
                }
            }

            // At this point, requester is neither owner of the entity nor owner of one of the owners of the entity
            // Check access
            boolean isAllowed = false;

            // First, see if requester is among the visible by
            query = session.createQuery("from Visibleby where timedentityid = :id").setParameter("id", timedEntityId);
            List<Visibleby> visibleby = query.list();
            if (visibleby.size() > 0) { // Only some can see the entity
                for (Visibleby v : visibleby) {
                    // group or person?
                    /**
                     * Check if requester is a member of groups if there are some
                     */
                    // Group
                    List<Groupaccess> groupaccesses = session.createQuery("from Groupaccess where accessid = :accessid").setParameter("accessid", v.getAccessid()).list();
                    for (Groupaccess groupaccess : groupaccesses) {
                        Group group = (Group) session.createQuery("from Group where id = :groupid").setParameter("groupid", groupaccess.getGroupid()).list().get(0);
                        List<Grouppeople> grouppeople = session.createQuery("from Grouppeople where groupid = :groupid").setParameter("groupid", group.getId()).list();
                        for (Grouppeople groupperson : grouppeople) {
                            if (requesterId.equals(groupperson.getProfileid()) || requesterId.equals(getOwnerOfProfile(groupperson.getProfileid()))) {
                                isAllowed = true;
                            }
                        }
                    }
                    if (!isAllowed) {
                        /**
                         * Check if requester is an individual profileaccess
                         */
                        // Person
                        List<Profileaccess> profileaccesses = session.createQuery("from Profileaccess where accessid = :accessid").setParameter("accessid", v.getAccessid()).list();
                        for (Profileaccess profileaccess : profileaccesses) {
                            if (requesterId.equals(profileaccess.getProfileid()) || requesterId.equals(getOwnerOfProfile(profileaccess.getProfileid()))) {
                                isAllowed = true;
                            }
                        }
                    }
                }
            } else {
                // Anyone but the notVisibleBy are allowed to see
                isAllowed = true;
            }

            // Check if requester is among the not visible by
            query = session.createQuery("from Notvisibleby where timedentityid = :id").setParameter("id", timedEntityId);
            List<Notvisibleby> notvisiblebylist = query.list();
            if (notvisiblebylist.size() > 0) { // Some cannot see the entity
                for (Notvisibleby notvisibleby : notvisiblebylist) {
                    // group or person?
                    // Check if requester is a member of groups if there are some
                    List<Groupaccess> groupaccesses = session.createQuery("from Groupaccess where accessid = :accessid").setParameter("accessid", notvisibleby.getAccessid()).list();
                    for (Groupaccess groupaccess : groupaccesses) {
                        Group group = (Group) session.createQuery("from Group where id = :groupid").setParameter("groupid", groupaccess.getGroupid()).list().get(0);
                        List<Grouppeople> grouppeople = session.createQuery("from Grouppeople where groupid = :groupid").setParameter("groupid", group.getId()).list();
                        for (Grouppeople groupperson : grouppeople) {
                            if (requesterId.equals(groupperson.getProfileid()) || requesterId.equals(getOwnerOfProfile(groupperson.getProfileid()))) {
                                isAllowed = false;
                            }
                        }
                    }
                    // Check if requester is an individual profileaccess
                    List<Profileaccess> profileaccesses = session.createQuery("from Profileaccess where accessid = :accessid").setParameter("accessid", notvisibleby.getAccessid()).list();
                    for (Profileaccess profileaccess : profileaccesses) {
                        if (requesterId.equals(profileaccess.getProfileid()) || requesterId.equals(getOwnerOfProfile(profileaccess.getProfileid()))) {
                            isAllowed = false;
                        }
                    }
                }
            }
            session.close();
            return isAllowed;
        }

        session.close();
        return false;
    }

    public static EntityType getTypeOfEntity(int timedentityId) {
        Session session = SessionHandler.getInstance().getSessionFactory().openSession();
        if (session.get(Profile.class, timedentityId) != null) {
            session.close();
            return EntityType.Profile;
        }
        if (session.get(Account.class, timedentityId) != null) {
            session.close();
            return EntityType.Account;
        }
        if (session.get(Relationship.class, timedentityId) != null) {
            session.close();
            return EntityType.Relationship;
        }
        if (session.get(Parentsof.class, timedentityId) != null) {
            session.close();
            return EntityType.Parent;
        }
        if (session.get(Event.class, timedentityId) != null) {
            session.close();
            return EntityType.Event;
        }
        if (session.get(Media.class, timedentityId) != null) {
            session.close();
            return EntityType.Media;
        }

        session.close();
        return null;
    }

    public static LocationResult getLocationFromId(Integer id, Session session) {
        System.out.println("LocationFromId: " + id);
        Location location = session.get(Location.class, id);
        Provincecountry provincecountry = session.get(Provincecountry.class, location.getProvincecountryid());
        Cityprovince cityprovince = session.get(Cityprovince.class, location.getCityprovinceid());
        Country country = session.get(Country.class, provincecountry.getCountryid());
        Province province = session.get(Province.class, provincecountry.getProvinceid());
        City city = session.get(City.class, cityprovince.getCityid());
        return new LocationResult(city.getName(), province.getName(), country.getName());
    }

    public static int getOwnerOfProfile(int profileid) {
        Session session = SessionHandler.getInstance().getSessionFactory().openSession();
        Ghost ghost = session.get(Ghost.class, profileid);
        if (ghost == null) {
            // This profile is associated to an account
            Query query = session.createQuery("from Account where profileid = :id").setParameter("id", profileid);
            if (query.list().size() > 0) {
                Account account = (Account) query.list().get(0);
                session.close();
                return account.getId();
            } else {
                session.close();
                return -1;
            }
        } else {
            session.close();
            return ghost.getOwner();
        }
    }

    public static boolean isValid(JsonNode jsonNode, String[] mandatoryFields) {
        for (String mandatoryField : mandatoryFields) {
            if (!jsonNode.has(mandatoryField)) {
                System.out.println("Missing " + mandatoryField);
                return false;
            }
        }
        return true;
    }

    public static ProfileResult getSimplifiedProfile(Integer id, Session session) {
        System.out.println("ID = " + id);
        // Select person whose id is given as parameter
        String query = "select p.peopleentityid as id, p.firstname as firstname, p.lastname as lastname, m.path as profilePicture, p.gender as gender from Profile as p inner join Media as m on m.postid = p.profilepicture where p.peopleentityid = " + id;
        List<Object[]> result = session.createQuery(query).list();
        ProfileResult caller = null;
        if (result.size() == 0) {
            return null;
        }
        for (Object[] resultObj : result) {
            int resid = (int) resultObj[0];
            String resfirstname = (String) resultObj[1];
            String reslastname = (String) resultObj[2];
            String resPath = (String) resultObj[3];
            int resGender = (int) resultObj[4];
            caller = new ProfileResult(resid, resfirstname, reslastname, resPath, resGender);
            System.out.println("Adding: " + Json.toJson(caller));
        }
        return caller;
    }
}

