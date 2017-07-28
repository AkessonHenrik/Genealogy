package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.Account;
import models.Comment;
import models.Profile;
import org.hibernate.Query;
import org.hibernate.Session;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import returnTypes.CommentResult;
import utils.SessionHandler;
import utils.Util;

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
        Integer requesterId = -1;
        if (request().hasHeader("requester"))
            requesterId = Integer.parseInt(request().getHeader("requester"));
        if (Util.isAllowedToSeeEntity(requesterId, postid)) {
            Session session = SessionHandler.getInstance().getSessionFactory().openSession();

            Query query = session.createQuery(
                    "select p.firstname, " +
                            "p.lastname, " +
                            "c.content, " +
                            "c.postedon " +
                            "from Comment c " +
                            "inner join Profile p on c.commenter = p.peopleentityid " +
                            "where c.postid = :postid");

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
        } else {
            return forbidden();
        }
    }

    public Result postComment() {
        Session session = SessionHandler.getInstance().getSessionFactory().openSession();
        JsonNode body = request().body().asJson();

        Comment comment = new Comment();
        comment.setPostid(body.get("postid").asInt());
        Integer commenterId = body.get("commenterid").asInt();
        comment.setCommenter(commenterId);
        comment.setPostedon(new Timestamp(System.currentTimeMillis()));
        comment.setContent(body.get("content").asText());
        session.save(comment);
        System.out.println("Commenter: " + commenterId);

        Account account = session.get(Account.class, commenterId);
        Profile profile = session.get(Profile.class, account.getProfileid());

        CommentResult result = new CommentResult(new Date(comment.getPostedon().getTime()).toString(), comment.getContent(), profile.getFirstname() + " " + profile.getLastname());
        session.close();
        return ok(Json.toJson(result));
    }

}
