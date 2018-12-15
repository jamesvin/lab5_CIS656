package edu.gvsu.restapi;

import org.restlet.resource.ServerResource;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import org.json.JSONObject;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.Delete;

public class UserResource extends ServerResource {

	private User user = null;

    @Override
    public void doInit() {

        String username = null;
        username = (String) getRequest().getAttributes().get("name");

        Key<User> theKey = Key.create(User.class, username);
        this.user = ObjectifyService.ofy()
                .load()
                .key(theKey)
                .now();

        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
    }

    @Get
    public Representation get(Variant variant) throws ResourceException {
        Representation result = null;
        if (null == this.user) {
            ErrorMessage em = new ErrorMessage();
            result = representError(variant.getMediaType(), em);
            getResponse().setEntity(result);
            getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return result;
        } else {
            if (variant.getMediaType().equals(MediaType.APPLICATION_JSON)) {
                result = new JsonRepresentation(this.user);
                result.setMediaType(MediaType.APPLICATION_JSON);
            }
        }
        return result;
    }

    @Put
    public Representation put(Representation entity, Variant variant) throws ResourceException {
        Representation rep = null;

       if (null == this.user) {
    	   ErrorMessage em = new ErrorMessage();
           rep = representError(entity.getMediaType(), em);
           getResponse().setEntity(rep);
           getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
           return rep;
       }
       if (entity.getMediaType().equals(MediaType.APPLICATION_WWW_FORM, true)) {
    	   Form form = new Form(entity);
           this.user.setName(form.getFirstValue("name"));
           this.user.setIpAddress(form.getFirstValue("ipAddress"));
           this.user.setPort(form.getFirstValue("port"));

           ObjectifyService.ofy().save().entity(this.user).now();

           getResponse().setStatus(Status.SUCCESS_OK);
           rep = new JsonRepresentation(this.user);
           getResponse().setEntity(rep);
      } else if (entity.getMediaType().equals(MediaType.APPLICATION_JSON, true)) {
           JSONObject requestBody = new JSONObject(entity);
           JSONObject body = new JSONObject(requestBody.get("text").toString());
           Boolean status = Boolean.parseBoolean(body.get("status").toString());
           this.user.setStatus(status);

           ObjectifyService.ofy().save().entity(this.user).now();

           getResponse().setStatus(Status.SUCCESS_OK);
           rep = new JsonRepresentation(this.user);
           getResponse().setEntity(rep);

      } else {
           getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
     }
     return rep;
   }

    @Delete
    public Representation delete(Variant variant) throws ResourceException {
        Representation rep = null;
   
        if (null == this.user) {
        	ErrorMessage em = new ErrorMessage();
            rep = representError(MediaType.APPLICATION_JSON, em);
            getResponse().setEntity(rep);
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return rep;
        }
        rep = new JsonRepresentation(this.user);

       ObjectifyService.ofy().delete().entity(this.user);

       getResponse().setStatus(Status.SUCCESS_OK);
       
       return rep;
    }

    protected Representation representError(MediaType type, ErrorMessage em) throws ResourceException {
        Representation result = null;
        if (type.equals(MediaType.APPLICATION_JSON)) {
            result = new JsonRepresentation(em.toJSON());
        } else {
            result = new StringRepresentation(em.toString());
        }
        return result;
    }
}
