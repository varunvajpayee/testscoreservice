package com.smodelware.smartcfa;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.smodelware.smartcfa.service.ContentService;
import com.smodelware.smartcfa.service.UserService;
import com.smodelware.smartcfa.util.ContentType;
import com.smodelware.smartcfa.util.CourseType;
import com.smodelware.smartcfa.vo.Item;
import com.smodelware.smartcfa.vo.Question;
import com.smodelware.smartcfa.vo.UserTest;
import org.glassfish.jersey.server.JSONP;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.util.StringUtils;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Logger;


@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class Catalog {
    private final ContentService contentService;
    private final UserService userService;
    @Context
    ServletContext context;
    private static final Logger log = Logger.getLogger(Catalog.class.getName());
    @Inject
    public Catalog(ContentService contentService,UserService userService) {
        this.contentService = contentService;
        this.userService = userService;
    }

    @GET
    @Path("/catalog/{parent_key}/{query_kind}")
    public Response browseCatalog(@PathParam("parent_key")String parentKey,@PathParam("query_kind")String queryKind) 
    {
    	String iParentKey=parentKey==null?"COURSE-CFA_LEVEL_3":parentKey;
    	String iQueryKind=queryKind.equalsIgnoreCase("ALL")?null:queryKind;
    	
        return Response
            .ok(contentService.getContent(iParentKey,iQueryKind))
            .build();
    }
    
    @GET
    @Path("/loadindex/{course_name}")
    public Response loadCatalog(@PathParam("course_name")String courseName)
    {
        //String filename = context.getContextPath() + "/resources/catalog/SmartCFA.csv";
       // InputStream inputStream = context.getResourceAsStream(filename);
        //InputStream inputStream =  contentService.getContentFromCloudStorage("resources/catalog/SmartCFA.csv");
        //InputStream l3InputStream =  contentService.getContentFromCloudStorage("https://docs.google.com/spreadsheets/d/e/2PACX-1vRrGxbPVhPDzIyFJswEcUQax7yokl4mnVmc7qBC-pFWF9LqESgsHXf9Nxl-tDCm9nJ_hPr_UZvlo-nO/pub?output=csv"); //CFA LEVEL 3
        Entity entity = null;

        if(CourseType.CFA_LEVEL_3.getCourseType().equals(courseName)){
            InputStream l3InputStream =  contentService.getContentFromCloudStorage("https://docs.google.com/spreadsheets/d/e/2PACX-1vTxP_Dv1YgM6OcmhT_4xMuwYn2Q_APGBcIesyPgcyZAABT78J0q9qErwt5d_dfZ4GsnXEi7JcjKBloE/pub?output=csv"); //CFA LEVEL 3
            entity = contentService.loadCatalog(l3InputStream,CourseType.CFA_LEVEL_3.getCourseType(),"2018","https://docs.google.com/document/d/e/2PACX-1vQ_ft5XODlcwXLN3KrCVuWZBcQ57w6lJrluKNtYBHoveDxUwkbSvH9xNlqBQvZnmpkqKbehVhJPv7g7/pub");
        }
        else  {
            InputStream l1InputStream =  contentService.getContentFromCloudStorage("https://docs.google.com/spreadsheets/d/e/2PACX-1vQze80FFjggeHGdEVDcRfavOF5C4X9O1qNn68B80Kqaq2KagInAJ1_m6JSHmG1_3y7Og4dNNcT35IvR/pub?output=csv"); //CFA LEVEL 1
            entity =  contentService.loadCatalog(l1InputStream,CourseType.CFA_LEVEL_1.getCourseType(),"2018","https://docs.google.com/document/d/e/2PACX-1vQtZfBrOIWHVD-ZeLPgILBd8OfRUx7L8oj5ZNzo-7CS7hXHaUhaAPkaDrLwMT6njmG2Brnq6D9qisgl/pub");

        }

        return Response
            .ok(entity)
            .build();
    }

    @GET
    @Path("/performScriptOps")
    public Response performScriptOps()
    {
        contentService.performScriptOps();
        return Response.ok().build();
    }
    
    @GET
    @Path("/loadcatalog/{sheetName}")
    public Response loadCatalogContent(@PathParam("sheetName")String sheetName)
    {
        //String filename = context.getContextPath() + "/resources/catalog/CFAL3_RM_Question.csv";
        //String filename = context.getContextPath() + "/resources/catalog/CFAL3_Question.xlsx";
        //InputStream inputStream = context.getResourceAsStream(filename);
        //InputStream inputStream =  contentService.getContentFromCloudStorage("resources/catalog/CFAL3_Question.xlsx");
        //InputStream inputStream =  contentService.getContentFromCloudStorage("https://docs.google.com/spreadsheets/d/e/2PACX-1vQBxeIyp68lSbXjb4tHCh2jOxOy92zGMkA_py2Eu4vbd0xgl0DKKkDeVnvl3es6MFp-NEHfH0OdH_ku/pub?output=xlsx");
        InputStream inputStream = null;
        if("CFA3".equals(sheetName)) {
            inputStream =  contentService.getContentFromCloudStorage("https://docs.google.com/spreadsheets/d/e/2PACX-1vQBxeIyp68lSbXjb4tHCh2jOxOy92zGMkA_py2Eu4vbd0xgl0DKKkDeVnvl3es6MFp-NEHfH0OdH_ku/pub?output=xlsx");
        }

        if("CFA1".equals(sheetName)) {
            inputStream =  contentService.getContentFromCloudStorage("https://docs.google.com/spreadsheets/d/e/2PACX-1vT_iZwgg2mGZYLVc_EYwatwd8zn2uPoTphpQhG_QF16_zZBh3V-ewQLOUhTbLtK6ArKiArLtldSR31z/pub?output=xlsx");
        }


        return Response.ok(contentService.loadContent(inputStream,null)).build();

    }


    @GET
    @Path("/delete/{parent_key}")
    public Response deleteEntity(@PathParam("parent_key")String parentKey) 
    {
    	String iParentKey=parentKey==null?"COURSE-CFA_LEVEL_3":parentKey;
    	
        return Response
            .ok(contentService.deleteContent(iParentKey))
            .build();
    }
    
    
    @GET
    @Path("/getCatalogTree")
    @JSONP(queryParam="callback")
    @Produces({"application/javascript"})
    public Response  getCatalogTree(@QueryParam("callback") String callback,@CookieParam("login") Cookie cookie,@QueryParam("paramName") String paramName)
    {
        String iParamName=paramName==null?"NOTE":paramName;
        java.net.URI location = null;
        try {
            location = new java.net.URI("../");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        if (cookie != null) {
            Item item = contentService.getCatalogTree(cookie.getValue(),iParamName);
            Response rs = Response.ok(item).build();
            return rs;
        }
        return Response.temporaryRedirect(location).build();
    }

    @GET
    @Path("/getTestResult/{kind}/{id}/{ancestorid}")
    @Produces({"application/javascript"})
    public Response  getTestResult(@PathParam("kind")String kind,@PathParam("id")String id,
                             @PathParam("ancestorid")String ancestorId,
                             @CookieParam("login") Cookie cookie
    ) {

        java.net.URI location = null;
        try {
            location = new java.net.URI("../");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        if (cookie != null) {
            Entity userTestEntity =contentService.getUserTestEntity(cookie.getValue());
            log.info("getNotesForId:Start kind:"+kind+" |id:"+id+" |ancestorId:"+ancestorId);
            String iParentId = ancestorId + "|" + kind + "-" + id;
            log.info("getNotesForId:ParentId:"+iParentId);
            UserTest userTest = new UserTest();
            List<Entity> entities = contentService.queryEntityBasedOnKindAndName(iParentId, "QUESTION");
            userTest.setTotalQuestions(entities.size());
            EmbeddedEntity ee = (EmbeddedEntity) userTestEntity.getProperty("RECORD");
            Integer noQuestionAnswered=0;
            Integer noCorrectAnswered=0;
            for(Entity entity:entities)
            {
               String questionId = String.valueOf(entity.getProperties().get("QUESTION"));
                String answered=(String)ee.getProperty(questionId);
                if(answered!=null)
                {
                    noQuestionAnswered = noQuestionAnswered+1;
                    String correctAnswer = String.valueOf(entity.getProperties().get("ANSWER"));
                    if(answered.equals(correctAnswer))
                    {
                        noCorrectAnswered = noCorrectAnswered+1;
                    }

                }
            }
            userTest.setTotalAnswered(noQuestionAnswered);
            userTest.setTotalCorrectAnswered(noCorrectAnswered);
            if(noCorrectAnswered!=null && userTest.getTotalQuestions()!=0)
            {
               Double pertage =(new Double(noCorrectAnswered)/new Double(userTest.getTotalQuestions()))*100;
                userTest.setScoredPercentage(Math.round(pertage*100.0)/100.0);
            }

            Entity ent =contentService.queryEntityBasedOnKindAndIdWithoutParent(kind,id);
            userTest.setTestId(String.valueOf(ent.getProperty("name")));
            return  Response.ok(userTest).build();
        }

        return Response.temporaryRedirect(location).build();
    }

    @GET
    @Path("/downloadContent/{id}/{view}/{kind}")
    @Produces({"application/pdf"})
    public Response downloadContent(@PathParam("kind")String kind,@PathParam("id")String id,
                                    @PathParam("view")String viewName,@CookieParam("login") Cookie cookie){


        if (cookie != null) {
            Entity user = userService.findUserBasedOnUserId(cookie.getValue());
            if(user!=null && user.getProperty("type").equals("PAID")){
                Entity content =contentService.queryEntityBasedOnKindAndIdWithoutParent(kind,id);
                if(content!=null && content.getProperty("URL")!=null){
                    String bookId = String.valueOf(content.getProperty("BOOK_ID"));
                    Entity bookMapEntity = contentService.queryEntityBasedOnKindAndIdWithoutParent("BOOK_MAP",bookId);
                    if(bookMapEntity!=null ){
                      if("Npanel".equals(viewName))  {
                          String docUrl = String.valueOf(bookMapEntity.getProperty("URL"));
                          String pdfUrl = docUrl + "/export?format=pdf";
                          return Response.ok(pdfUrl).build();
                      }
                      else if("QPanel".equals(viewName)){
                          String pdfUrl = String.valueOf(bookMapEntity.getProperty("Q_URL"));
                          return Response.ok(pdfUrl).build();
                      }

                    }

                }
            }
        }
        return Response.status(Response.Status.PAYMENT_REQUIRED).entity("Only Paid Users can download").build();
    }


    @POST
    @Path("/getTest/{kind}/{id}/{ancestorid}")
    @Produces({"application/javascript"})
    public Response  getTest(@PathParam("kind")String kind,@PathParam("id")String id,
                             @PathParam("ancestorid")String ancestorId,
                             @FormParam("ajax_req") String ajaxReq,
                             @FormParam("index_req") String postBody,
                             @CookieParam("login") Cookie cookie
                        ) {
        log.info("getQuestionsForLos:Start kind:" + postBody + " |id:" + id + " |ancestorId:" + ancestorId + " |startIndex:" + 0 + " |endIndex:" + 100);

        java.net.URI location = null;
        try {
            location = new java.net.URI("../");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        if (cookie != null) {
            Entity userTestEntity =contentService.getUserTestEntity(cookie.getValue());
            Map<String, String> userAnswers = getQuestionAnswerMap(ajaxReq);
            contentService.updateUserTestEntityWithAnswer(userTestEntity,userAnswers);
            JSONObject jObject = new JSONObject(postBody);
            String iParentId = StringUtils.isEmpty(ancestorId) || "null".equals(ancestorId)?kind + "-" + id:ancestorId + "|" + kind + "-" + id;
            log.info("getQuestionsForLos:ParentId:" + iParentId);
            boolean iAllowAnswers = false;
            if (jObject.has("allowAnswers")) {
                iAllowAnswers = "true".equalsIgnoreCase(String.valueOf(jObject.get("allowAnswers"))) ? true : false;
            }
            Integer sIndex = jObject.get("startIndex") == null ? 0 : Integer.parseInt(jObject.get("startIndex").toString());
            Integer eIndex = jObject.get("endIndex") == null ? 1000 : Integer.parseInt(jObject.get("endIndex").toString());
            UserTest userTest = new UserTest();
            List<Entity> entities = contentService.queryEntityBasedOnKindAndName(iParentId, "QUESTION");
            userTest.setTotalQuestions(entities.size());
            List<Question> questions = contentService.getQuestions(entities, sIndex, eIndex, iAllowAnswers);
            questions = contentService.decorateQuestionWithAnswer(userTestEntity,questions,userAnswers);
            userTest.setQuestions(questions);
            return  Response.ok(userTest).build();
        }
        return Response.temporaryRedirect(location).build();
    }

    private Map<String, String> getQuestionAnswerMap(String ajaxReq) {
        Map<String,String> userAnswers = new HashMap<>();
        if (ajaxReq != null) {
            JSONArray jsonMainArr = new JSONArray(ajaxReq);
            for(Object o: jsonMainArr){
                if ( o instanceof JSONObject) {
                    JSONObject jobj= (JSONObject)o;
                    userAnswers.put(jobj.get("question").toString(),jobj.get("answer").toString());
                }
            }
        }
        return userAnswers;
    }

    @GET
    @Path("/getNotesForId/{kind}/{id}/{ancestorid}")
    @Produces("text/html; charset=windows-1252")
    public Response getNotesForId(@PathParam("kind")String kind,@PathParam("id")String id,@PathParam("ancestorid")String ancestorId)
    {
        log.info("getNotesForId:Start kind:"+kind+" |id:"+id+" |ancestorId:"+ancestorId);
        String iParentId = ContentType.LOS.getContentType().equals(kind)?kind+"-"+id:ancestorId+"|"+kind+"-"+id;
        log.info("getNotesForId:ParentId:"+iParentId);
        List<Entity> entities = contentService.queryEntityBasedOnKindAndName(iParentId,ContentType.LOS.getContentType());
        log.info("getNotesForId:Entities Returned:"+entities.size());
        List<InputStream> streams = new ArrayList<InputStream>();
        for(Entity entity:entities) {
            //String filename = context.getContextPath() + "/resources/html/note/"+entity.getProperty("PARENT_ID")+"/"+ entity.getProperty("IDENTITY") + ".htm";
            //InputStream s= context.getResourceAsStream(filename);
            InputStream s =  contentService.getContentFromCloudStorage("resources/html/note/"+entity.getProperty("PARENT_ID")+"/"+ entity.getProperty("IDENTITY") + ".htm");
            if(s!=null) {
                streams.add(s);
            }
        }
        log.info("getNotesForId:Entities Added To Stream");

        InputStream story = new SequenceInputStream(Collections.enumeration(streams));
        Response rs = Response.ok(story).build();
        log.info("getNotesForId:End");
        return rs;
    }
}	