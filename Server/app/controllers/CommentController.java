package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.Comment;
import models.Profile;
import org.hibernate.Query;
import org.hibernate.Session;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import returnTypes.CommentResult;
import utils.SessionHandler;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Henrik on 17/07/2017.
 */
public class CommentController extends Controller {
    public Result getComments(Integer postid) {
        Session session = SessionHandler.getInstance().getSessionFactory().openSession();

        Query query = session.createQuery("select p.firstname, p.lastname, c.content, c.postedon from Comment c inner join Profile p on c.commenter = p.peopleentityid where c.postid = :postid");
        query.setParameter("postid", postid);
        List<Object[]> comments = query.list();
        List<CommentResult> results = new ArrayList<>();
        for (Object[] comment : comments) {
            String name = comment[0].toString() + " " + comment[1].toString();
            String content = comment[2].toString();
            String date = new Date(((Timestamp) comment[3]).getTime()).toString();
            results.add(new CommentResult(date, content, name));
        }
        session.close();
        return ok(Json.toJson(results));
    }

    public Result postComment() {
        Session session = SessionHandler.getInstance().getSessionFactory().openSession();
        JsonNode body = request().body().asJson();

        Comment comment = new Comment();
        comment.setPostid(body.get("postid").asInt());
        comment.setCommenter(body.get("commenterid").asInt());
        comment.setPostedon(new Timestamp(System.currentTimeMillis()));
        comment.setContent(body.get("content").asText());
        session.save(comment);

        Profile p = (Profile) session.createQuery("from Profile where peopleentityid = :id").setParameter("id", comment.getCommenter()).list().get(0);
        CommentResult result = new CommentResult(new Date(((Timestamp) comment.getPostedon()).getTime()).toString(), comment.getContent(), p.getFirstname() + " " + p.getLastname());
        session.close();
        return ok(Json.toJson(result));
    }

}
