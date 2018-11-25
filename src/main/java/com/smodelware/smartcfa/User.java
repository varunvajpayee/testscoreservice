package com.smodelware.smartcfa;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.smodelware.smartcfa.service.UserService;
import com.smodelware.smartcfa.vo.UserVO;
import org.glassfish.jersey.server.JSONP;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URISyntaxException;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class User
{
	 private final UserService userService;

	    @Inject
	    public User(UserService userService) {
	        this.userService = userService;
	    }

		@POST
	    @Path("/saveUser")
	    @Produces({"application/javascript"})
	    public Response saveUser(@FormParam("fullName")String fullName,
	    		@FormParam("userName")String userName,@FormParam("email")String email,
	    		@FormParam("password")String password,@FormParam("isTermAccepted")String isTermAccepted,@FormParam("course")String course)
	    {
			Entity user = userService.saveUser(fullName,userName,email,password,"LOCAL","",course);
			NewCookie cookie = new NewCookie("login", String.valueOf(user.getProperty("userId")));
	        return Response.ok(String.valueOf(user.getProperty("userId"))).cookie(cookie).build();
	    }

	@POST
	@Path("/saveUserObject")
	@Produces({"application/json"})
	@Consumes({"application/json"})
	public Response saveUserObject(String userString)
	{
		Gson g =new Gson();
		UserVO userInput =g.fromJson(userString,UserVO.class);
		Entity user = userService.saveUser(userInput.getFullName(),userInput.getUserName(),userInput.getEmail(),userInput.getPassword(),"LOCAL","","ALL");
		NewCookie cookie = new NewCookie("login", String.valueOf(user.getProperty("userId")));
		user.setProperty("password","ENCRYPTED");
		return Response.ok(user).cookie(cookie).build();
	}

	/*@OPTIONS
	@Path("/saveUserObject")
	@Produces({"application/javascript"})
	@Consumes({"application/json"})
	public Response getOptions() {
		return Response.ok()
				.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "POST,HEAD, GET, PUT, UPDATE, OPTIONS")
				.header("Access-Control-Allow-Headers",  "content-type").build();
	}*/

	@GET
	@Path("/saveCourseEnrollment")
	@Produces({"application/javascript"})
	public Response saveCourseEnrollment(@CookieParam("login") Cookie cookie
			,@QueryParam("course")String course)
	{
		java.net.URI location = null;
		try {
			location = new java.net.URI("../");
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		if (cookie != null) {
			Entity user = userService.findUserBasedOnUserId(cookie.getValue());
			user.setProperty("course",course);
			userService.saveUserEntity(user);
			return Response.ok(user).build();
		}
		return Response.temporaryRedirect(location).build();
	}
	@GET
	@Path("/saveUserSetting")
	@JSONP(queryParam="callback")
	@Produces({"application/javascript"})
	public Response saveUserSetting(@QueryParam("callback") String callback,@QueryParam("noteContentLevel")String noteContentLevel
			,@QueryParam("questionContentLevel")String questionContentLevel
			,@QueryParam("questionPerPage")String questionPerPage
			,@CookieParam("login") Cookie cookie
			,@QueryParam("showCourses")String showCourses)
	{
		java.net.URI location = null;
		try {
			location = new java.net.URI("../");
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		if (cookie != null) {
			Entity userSetting = userService.saveUserSetting(cookie.getValue(),noteContentLevel,questionContentLevel,questionPerPage,showCourses);
			return Response.ok(userSetting).build();
		}
		return Response.temporaryRedirect(location).build();
	}

		@POST
		@Path("/login")
		@Produces({"application/javascript"})
		public Response login(@FormParam("userName") String userName,@FormParam("password")String password)
		{

			Entity user = userService.findUserBasedOnUserNameAndPassword(userName,password);
			if(user!=null)
			{
				NewCookie cookie = new NewCookie("login", String.valueOf(user.getProperty("userId")));
				return Response.ok(user.getProperty("userId")).cookie(cookie).build();
			}
			else
			{
				return Response.serverError().entity("User Not Found").build();
			}
		}

		@POST
		@Path("/setCookie")
		@Produces({"application/javascript"})
		public Response setCookie(String userId)
		{
			Entity user = userService.findUserBasedOnUserId(userId);
			if(user!=null)
			{
				NewCookie cookie = new NewCookie("login", String.valueOf(user.getProperty("userId")));
				return Response.ok(user.getProperty("userId")).cookie(cookie).build();
			}
			else
			{
				return Response.serverError().entity("User Not Found").build();
			}
		}


		@POST
		@Path("/loginUser")
		@Produces({"application/json"})
		@Consumes({"application/json"})
		public Response loginUser(String userString)
		{
			Gson g =new Gson();
			UserVO userInput =g.fromJson(userString,UserVO.class);
			Entity user = userService.findUserBasedOnUserNameAndPassword(userInput.getUserName(),userInput.getPassword());
			if(user!=null)
			{
				NewCookie cookie = new NewCookie("login", String.valueOf(user.getProperty("userId")));
				user.setProperty("password","ENCRYPTED");
				return Response.ok(user).cookie(cookie).build();
			}
			else
			{
				return Response.serverError().entity("User Not Found").build();
			}
		}

	/*	@OPTIONS
		@Path("/loginUser")
		@Produces({"application/json"})
		@Consumes({"application/json"})
		public Response getloginUserOptions() {
			return Response.ok()
					.header("Access-Control-Allow-Origin", "*")
					.header("Access-Control-Allow-Methods", "POST,HEAD, GET, PUT, UPDATE, OPTIONS")
					.header("Access-Control-Allow-Headers",  "content-type").build();
		}*/


	@POST
		@Path("/passwordReset")
		@Produces({"application/javascript"})
		public Response passwordReset(@FormParam("userName")String userName,@FormParam("password")String password)
		{
			Entity user = userService.findUserBasedOnUserName(userName);
			if(user!=null)
			{
				user.setProperty("password",password);
				userService.saveUserEntity(user);
				NewCookie cookie = new NewCookie("login", String.valueOf(user.getProperty("userId")));
				return Response.ok(1).cookie(cookie).build();
			}
			else
			{
				return Response.serverError().entity("User Not Found").build();
			}
		}

		@POST
	    @Path("/getGoogleLoginURL")
		@Produces({"text/plain"})
	    public Response getGoogleLoginURL(@QueryParam("isTermAccepted")String isTermAccepted,
	    		@Context HttpServletRequest httpRequest,@Context HttpServletResponse httpResponse)
	    {
	    	Gson gson = new Gson();
	    	com.google.appengine.api.users.UserService googleUserService = UserServiceFactory.getUserService();
	    	StringBuffer url = httpRequest.getRequestURL();
	    	String uri = httpRequest.getRequestURI();
	    	String hostPort = url.substring(0, url.indexOf(uri));
	    	
			System.out.println("hostPort:1::"+hostPort);
	    	String requestUri = googleUserService.createLoginURL(hostPort+"/loginGoogle");
	    	/*try {
				httpResponse.sendRedirect(requestUri);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
	    	
	    	//String jsonStr = gson.toJson(requestUri);

			return Response.ok(requestUri).build();
	    	/*//Response rs =Response.ok(jsonStr,MediaType.APPLICATION_JSON).header("").build();
			Response rs =Response.ok(jsonStr,MediaType.APPLICATION_JSON).header("x-Content-Type-Options",null)
					.type(MediaType.APPLICATION_JSON)
					.header("Content-type","application/json")
					.build();
	    	//httpResponse.setContentType("application/javascript");
			//httpResponse.setHeader("x-Content-Type-Options",null);*/
	        //return rs;
	    }

	@GET
	@Path("/loginGoogle")
	@Produces(MediaType.TEXT_PLAIN)
	public Response loginGoogle()
	{
		com.google.appengine.api.users.UserService googleUserService = UserServiceFactory.getUserService();
		com.google.appengine.api.users.User googleUser = googleUserService.getCurrentUser();

		if(googleUser!=null)
		{
			Entity user = userService.saveUser(googleUser.getNickname(),googleUser.getEmail(),googleUser.getEmail(),googleUser.getEmail()
					,"GOOGLE",googleUser.getUserId(),"ALL");
			NewCookie cookie = new NewCookie("login", String.valueOf(user.getProperty("userId")));
			java.net.URI location = null;
			try {
				location = new java.net.URI("../");
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			return Response.temporaryRedirect(location).cookie(cookie).build();
		}
		else
		{
			return Response.serverError().entity("Google User Not Found").build();
		}
	}

	@GET
	@Path("/logout")
	@Produces(MediaType.TEXT_PLAIN)
	public Response logout(@CookieParam("login") Cookie cookie)
	{
		java.net.URI location = null;
		try {
			location = new java.net.URI("../");
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		if (cookie != null) {
			NewCookie newCookie = new NewCookie(cookie, null, 0, false);
			return Response.temporaryRedirect(location).cookie(newCookie).build();
		}
		return Response.temporaryRedirect(location).build();
	}
	
	@GET
	@Path("/getUser")
	@JSONP(queryParam="callback")
	@Produces({"application/javascript"})
	public Response getUser(@QueryParam("callback") String callback,@CookieParam("login") Cookie cookie)
	{
		java.net.URI location = null;
		try {
			location = new java.net.URI("../");
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		if (cookie != null) 
		{
			Entity user = userService.findUserBasedOnUserId(cookie.getValue());
			if(user!=null)
			{
				return Response.ok(user).build();
			}
			else
			{
				return Response.temporaryRedirect(location).build();	
			}
		}		
		return Response.temporaryRedirect(location).build();
	}

	@GET
	@Path("/getEmail")
	public Response getEmail()
	{
		return Response.ok(userService.getUserEmails()).build();
	}

	@GET
	@Path("/getUserSetting")
	@JSONP(queryParam="callback")
	@Produces({"application/javascript"})
	public Response getUserSetting(@QueryParam("callback") String callback,@CookieParam("login") Cookie cookie)
	{
		if (cookie != null)
		{
            return Response.ok(userService.getUserSetting(cookie.getValue())).build();
		}
		else{
            return Response.ok(userService.getDefaultUserSetting()).build();
        }
	}
	    
}
