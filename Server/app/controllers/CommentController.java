package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.Account;
import models.Comment;
import models.Media;
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


            List<Comment> comments = session.createQuery("from Comment where postid = :id").setParameter("id", postid).list();
            List<CommentResult> commentResults = new ArrayList<>();
            for (Comment comment : comments) {
                String content = comment.getContent();
                String time = new Date(comment.getPostedon().getTime()).toString();
                Account account = session.get(Account.class, comment.getCommenter());
                Profile profile = session.get(Profile.class, account.getProfileid());
                String profilePicture = session.get(Media.class, profile.getProfilepicture()).getPath();
                CommentResult commentResult = new CommentResult(time, content, profile.getFirstname() + " " + profile.getLastname());
                commentResult.profilePicture = profilePicture;
                if (Util.isAllowedToSeeEntity(requesterId, profile.getPeopleentityid()))
                    commentResults.add(commentResult);
            }

            session.close();
            return ok(Json.toJson(commentResults));
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

        Account account = session.get(Account.class, commenterId);
        Profile profile = session.get(Profile.class, account.getProfileid());
        Media picture = session.get(Media.class, profile.getProfilepicture());
        CommentResult result = new CommentResult(new Date(comment.getPostedon().getTime()).toString(), comment.getContent(), profile.getFirstname() + " " + profile.getLastname());
        result.profilePicture = picture.getPath();
        session.close();
        return ok(Json.toJson(result));
    }

}
