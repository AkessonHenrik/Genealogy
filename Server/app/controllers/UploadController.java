package controllers;

import play.libs.Json;
import play.mvc.*;
import utils.*;

import java.io.File;

public class UploadController extends Controller {
    @BodyParser.Of(value = BodyParser.MultipartFormData.class, maxLength = 1024 * 1024 * 1024)
    public Result upload() {
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart formFile = body.getFile("file");
        if (formFile == null) {
            return ok();
        }
        String contentType = formFile.getContentType();
        String fileType = contentType.substring(0, contentType.indexOf("/"));
        String extension = contentType.substring(contentType.indexOf("/") + 1);
        File file = formFile.getFile();
        String filename;
        if (fileType.equals("video") || fileType.equals("image") || fileType.equals("audio")) {
            filename = String.valueOf(Math.abs(file.hashCode())) + "." + extension;
            File definiteFile = new File("public/" + filename);
            if (file.renameTo(definiteFile)) {
                return ok(Json.toJson(new UploadFile(fileType, Globals.thisFileHost + filename)));
            } else {
                return internalServerError();
            }
        } else {
            return badRequest();
        }
    }
}
