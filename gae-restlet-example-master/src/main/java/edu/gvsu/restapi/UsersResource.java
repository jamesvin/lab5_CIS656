package edu.gvsu.restapi;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.googlecode.objectify.ObjectifyService;

public class UsersResource extends ServerResource {
	
	private List<User> users = null;

    @SuppressWarnings("unchecked")
    @Override
    public void doInit() {

        this.users = ObjectifyService.ofy()
                .load()
                .type(User.class) // We want only Widgets
                .list();

        // these are the representation types this resource can use to describe the
        // set of widgets with.
        getVariants().add(new Variant(MediaType.TEXT_HTML));
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));

    }


    /**
     * Handle an HTTP GET. Represent the user object in the requested format.
     *
     * @param variant
     * @return
     * @throws ResourceException
     */
    @Get
    public Representation get(Variant variant) throws ResourceException {
        Representation result = null;
        System.out.println("Variant Type; " + variant.getMediaType());
        if (null == this.users) {
            ErrorMessage em = new ErrorMessage();
            return representError(variant, em);
        } else {

            if (variant.getMediaType().equals(MediaType.APPLICATION_JSON)) {

                JSONArray userArray = new JSONArray();
                for (Object o : this.users) {
                    User u = (User) o;
                    JSONObject jsonobj = new JSONObject();
                    jsonobj.put("name", u.getName());
                    jsonobj.put("ipAddress", u.getIpAddress());
                    jsonobj.put("port", u.getPort());
                    jsonobj.put("status", u.isStatus());
                    userArray.put(jsonobj);
                }

                result = new JsonRepresentation(userArray);
                result.setMediaType(MediaType.APPLICATION_JSON);

            } /*else {

                // create a plain text representation of our list of users
                StringBuffer buf = new StringBuffer("<html><head><title>User Resources</title><head><body><h1>User Resources</h1>");
                buf.append("<form name=\"input\" action=\"/users\" method=\"POST\">");
                buf.append("User name: ");
                buf.append("<input type=\"text\" name=\"name\" />");
                buf.append("<br></br>");
                buf.append("IP Address: ");
                buf.append("<input type=\"text\" name=\"ipAddress\" />");
                buf.append("<input type=\"submit\" value=\"Create\" />");
                buf.append("</form>");
                buf.append("<br/><h2> There are " + this.users.size() + " total.</h2>");
                for (Object o : this.users) {
                    User u = (User) o;
                    buf.append(u.toHtml(true));
                }
                buf.append("</body></html>");
                result = new StringRepresentation(buf.toString());
                result.setMediaType(MediaType.TEXT_HTML);
            }*/
        }
        return result;
    }

    @Post
    public Representation post(Representation entity, Variant variant) throws ResourceException {

        Representation rep = null;

        try {
            if (entity.getMediaType().equals(MediaType.APPLICATION_WWW_FORM,
                    true)) {
                // Use the incoming data in the POST request to create/store a new widget resource.
                Form form = new Form(entity);
                User u = new User();
                u.setName(form.getFirstValue("name"));
                u.setIpAddress(form.getFirstValue("ipAddress"));

                // persist updated object
                ObjectifyService.ofy().save().entity(u).now();

                rep = new StringRepresentation(u.toString());
                rep.setMediaType(MediaType.TEXT_HTML);
                getResponse().setStatus(Status.SUCCESS_OK);
                getResponse().setEntity(rep);

            }
            else if (entity.getMediaType().equals(MediaType.APPLICATION_JSON)) {
                JSONObject requestBody = new JSONObject(entity);
                JSONObject body = new JSONObject(requestBody.get("text").toString());

                String name = body.get("name").toString();
                String ipAddress = body.get("ipAddress").toString();
                String port = body.get("port").toString();

                User u = new User();
                u.setName(name);
                u.setIpAddress(ipAddress);
                u.setPort(port);

                ObjectifyService.ofy().save().entity(u).now();
                
                JSONObject jsonobj = new JSONObject();
                jsonobj.put("name", u.getName());
                jsonobj.put("ipAddress", u.getIpAddress());
                jsonobj.put("port", u.getPort());
                jsonobj.put("status", u.isStatus());
                
                rep = new JsonRepresentation(jsonobj);
                rep.setMediaType(MediaType.APPLICATION_JSON);
                getResponse().setStatus(Status.SUCCESS_CREATED);
                getResponse().setEntity(rep);
            }
            else {
                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            }
        } catch (Exception e) {
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return rep;
    }

    /**
     * Represent an error message in the requested format.
     *
     * @param variant
     * @param em
     * @return
     * @throws ResourceException
     */
    private Representation representError(Variant variant, ErrorMessage em)
            throws ResourceException {
        Representation result = null;
        if (variant.getMediaType().equals(MediaType.APPLICATION_JSON)) {
            result = new JsonRepresentation(em.toJSON());
        } else {
            result = new StringRepresentation(em.toString());
        }
        return result;
    }

    protected Representation representError(MediaType type, ErrorMessage em)
            throws ResourceException {
        Representation result = null;
        if (type.equals(MediaType.APPLICATION_JSON)) {
            result = new JsonRepresentation(em.toJSON());
        } else {
            result = new StringRepresentation(em.toString());
        }
        return result;
    }

}
