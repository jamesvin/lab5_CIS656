
package edu.gvsu.restapi.client;

import java.io.IOException;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

import edu.gvsu.restapi.common.PresenceService;
import edu.gvsu.restapi.common.RegistrationInfo;


public class ChatClient implements PresenceService{
	
    public static final String BASE_URL = "http://localhost:8080";


    public void register(RegistrationInfo reg) throws Exception {

    	
    	Request request = new Request(Method.POST,BASE_URL +  "/v1/users"); 	
        JSONObject registration = new JSONObject();
        registration.put("name", reg.getUserName());
        registration.put("ipAddress", reg.getHost());
        registration.put("port", reg.getPort());
        registration.put("status", true);
    	request.setEntity(registration.toString(), MediaType.APPLICATION_JSON);
       
        //request.getClientInfo().getAcceptedMediaTypes().add(new Preference(MediaType.APPLICATION_JSON));

        Response resp = new Client(Protocol.HTTP).handle(request);
        System.out.println(resp.getStatus());
        System.out.println(resp.getEntityAsText());

    }

    public void unregister(String userName) throws Exception {

        String usersResourceURL = BASE_URL + "/v1/users/" + userName;
        Request request = new Request(Method.DELETE, usersResourceURL);
        request.getClientInfo().getAcceptedMediaTypes().add(new Preference<MediaType>(MediaType.APPLICATION_JSON));
        Response resp = new Client(Protocol.HTTP).handle(request);
    }

    public RegistrationInfo lookup(String name) throws Exception {

        String usersURL = BASE_URL + "/v1/users/" + name;
        Request request = new Request(Method.GET, usersURL);
        request.getClientInfo().getAcceptedMediaTypes().add(new Preference(MediaType.APPLICATION_JSON));
        Response resp = new Client(Protocol.HTTP).handle(request);
        
        if (resp.getStatus().getCode() == 200 ){
            JSONObject userJson = new JSONObject(resp.getEntity().getText());
            RegistrationInfo user = new RegistrationInfo();
            user.setUserName(userJson.getString("name"));
            user.setHost(userJson.getString("ipAddress"));
            user.setPort(Integer.parseInt(userJson.getString("port")));
            user.setStatus(userJson.getBoolean("status"));
            return user;
        }
        else {
            return null;
        }
    }

    public void setStatus(String userName, boolean status) throws Exception {
        JSONObject statusObject = new JSONObject();
        statusObject.put("name", userName);
        statusObject.put("status", status);

        String usersResourceURL = BASE_URL + "/v1/users/" + userName;
        Request request = new Request(Method.PUT, usersResourceURL);
        request.getClientInfo().getAcceptedMediaTypes().add(new Preference(MediaType.APPLICATION_JSON));
        request.setEntity(statusObject.toString(), MediaType.APPLICATION_JSON);
        Response resp = new Client(Protocol.HTTP).handle(request);
    }

    public RegistrationInfo[] listRegisteredUsers() {

        String usersURL = BASE_URL + "/v1/users";
        RegistrationInfo[] registeredUsers = new RegistrationInfo[0];
        Request request = new Request(Method.GET, usersURL);
        request.getClientInfo().getAcceptedMediaTypes().add(new Preference(MediaType.APPLICATION_JSON));
        Response resp = new Client(Protocol.HTTP).handle(request);

        if (resp.getStatus().equals(Status.SUCCESS_OK)) {
            Representation responseData = resp.getEntity();
            try {
                String jsonString = responseData.getText();
                JSONArray jObj = new JSONArray(jsonString);
                registeredUsers = new RegistrationInfo[jObj.length()];
                for (int i = 0; i < jObj.length(); i++) {
                    JSONObject item = jObj.getJSONObject(i);
                    Map itemMap = item.toMap();
                    RegistrationInfo info = new RegistrationInfo();
                    info.setUserName(itemMap.get("name").toString());
                    info.setHost(itemMap.get("ipAddress").toString());
                    info.setPort(Integer.parseInt(itemMap.get("port").toString()));
                    info.setStatus(Boolean.parseBoolean(itemMap.get("status").toString()));
                    registeredUsers[i] = info;
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (JSONException je) {
                je.printStackTrace();
            }
        }
        return registeredUsers;
    }
}
    
    