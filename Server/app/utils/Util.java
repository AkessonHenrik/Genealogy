package utils;

import models.*;
import org.hibernate.*;
import play.libs.Json;

import javax.persistence.LockModeType;
import javax.validation.ConstraintViolationException;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class Util {
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

    public static List<Media> getEventMedia(Session session, int eventId) {
        List<Eventmedia> em = session.createQuery("from Eventmedia where eventid = " + eventId).list();

        List<Media> mediaList = new ArrayList<>();
        for (Eventmedia eventmedia : em) {
            mediaList.addAll(session.createQuery("from Media where postid = " + eventmedia.getMediaid()).list());
        }
        System.out.println(Json.toJson(mediaList));
        return mediaList;
    }

    public static int getOrCreateTime(Date[] dates) {
        Session session = SessionHandler.getInstance().getSessionFactory().openSession();

        if (dates.length == 2) {
            // Time interval
            Query query = session.createQuery("from Timeinterval ti where ti.begintime = :time1 and ti.enddate = :time2").setParameter("time1", dates[0]).setParameter("time2", dates[1]);
            if (query.list().size() == 0) {
                System.out.println("Create new time interval");
                session.getTransaction().begin();
                Time time = new Time();
                session.save(time);
                Timeinterval ti = new Timeinterval();
                ti.setTimeid(time.getId());
                ti.setBegintime(dates[0]);
                ti.setEnddate(dates[1]);
                session.save(ti);
                session.getTransaction().commit();
                session.close();
                return ti.getTimeid();
            } else {
                session.close();
                System.out.println("Return existing timeinterval");
                return ((Timeinterval) query.list().get(0)).getTimeid();
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

    public static Date parseDateFromString(String dateToParse) {
        try {
            SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MM-yyyy");
            return new Date(sdf1.parse(dateToParse).getTime());
        } catch (ParseException e) {
            System.out.println("Exception while parsing date: " + e.getMessage());
        }
        return null;
    }
}
