package utils;

import models.*;
import org.hibernate.Session;

import java.util.List;

/**
 * Created by Henrik on 07/07/2017.
 */
public class Util {
    public static int createLocation(Session session, String cityName, String provinceName, String countryName) {
        City city = null;
        System.out.println("==================================================");
        System.out.println(cityName + ", " + provinceName + ", " + countryName);
        System.out.println("==================================================");
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
