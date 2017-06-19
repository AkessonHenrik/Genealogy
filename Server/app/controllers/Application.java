package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import play.db.jpa.Transactional;
import play.mvc.*;

import java.util.List;

public class Application extends Controller {

    @Transactional
    public Result createLocation() {
        JsonNode json = request().body().asJson();
        Configuration configuration = new Configuration();
        SessionFactory sessionFactory = configuration.configure().buildSessionFactory();
        Session session = sessionFactory.openSession();

        City city = new City();
        city.setName(json.get("city").asText());

        Province province = new Province();
        province.setName(json.get("province").asText());

        Country country = new Country();
        country.setName(json.get("country").asText());

        session.getTransaction().begin();

        session.saveOrUpdate(city);
        session.saveOrUpdate(province);
        session.saveOrUpdate(country);

        Cityprovince cityprovince = new Cityprovince();
        cityprovince.setCityid(city.getId());
        cityprovince.setProvinceid(province.getId());

        session.saveOrUpdate(cityprovince);

        Provincecountry pc = new Provincecountry();
        pc.setCountryid(country.getId());
        pc.setProvinceid(province.getId());

        session.saveOrUpdate(pc);

        Location location = new Location();
        location.setCityprovinceid(cityprovince.getId());
        location.setProvincecountryid(pc.getId());

        session.saveOrUpdate(location);

        session.getTransaction().commit();

        session.close();

        return ok(city.getName() + ", " + province.getName() + ", " + country.getName());
    }

    @Transactional
    public Result getLocations() {
        Configuration configuration = new Configuration();
        SessionFactory sessionFactory = configuration.configure().buildSessionFactory();
        Session session = sessionFactory.openSession();
        List<City> cityList = session.createQuery("from City").list();

        return ok();

    }

}
