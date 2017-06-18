package controllers;

import models.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.*;
import org.hibernate.cfg.Configuration;
import play.*;
import play.db.jpa.JPA;
import play.db.jpa.JPAApi;
import play.db.jpa.Transactional;
import play.mvc.*;

import scala.App;
import views.html.*;

import javax.inject.Inject;
import javax.persistence.EntityManager;

public class Application extends Controller {
    private static JPAApi jpaApi = null;

    @Inject
    public Application(JPAApi api) {
        this.jpaApi = api;
    }

    @Transactional
    public Result updateSomething() {

        Configuration configuration = new Configuration();
        SessionFactory sessionFactory = configuration.configure().buildSessionFactory();
        Session session = sessionFactory.openSession();

        System.out.println("Hello");
        // do something with the entity manager, per instance
        // save, update or query model objects.
        City city = new City();
        city.setName("Nyon");


        Province province = new Province();
        province.setName("Vaud");

        Country country = new Country();
        country.setName("Suisse");

        session.getTransaction().begin();

        session.saveOrUpdate(city);
        session.saveOrUpdate(province);
        session.saveOrUpdate(country);

        System.out.println("City id = " + city.getId());

        Cityprovince cityprovince = new Cityprovince();
        cityprovince.setCityid(city.getId());
        cityprovince.setProvinceid(province.getId());

        session.save(cityprovince);

        Provincecountry pc = new Provincecountry();
        pc.setCountryid(country.getId());
        pc.setProvinceid(province.getId());

        session.save(pc);

        Location location = new Location();
        location.setCityprovinceid(cityprovince.getId());
        location.setProvincecountryid(pc.getId());

        session.save(location);

        session.getTransaction().commit();

        session.close();

        return ok("Hey");
    }

//    public static Result index() {
//        Application app;
//        app.updateSomething();
//        return ok(index.render("Your new application is ready."));
//    }

}
