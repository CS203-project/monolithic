package com.example.demo.cms;

import java.util.*;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.util.MultiValueMap;

import org.springframework.web.server.ResponseStatusException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

// JSON imports
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;

import org.springframework.beans.factory.annotation.Autowired;   

import java.lang.IllegalArgumentException;

@RestController
public class CMSController {

    
    private CMSService cms;

    private JSONObject userObj;
    
    @Autowired
    public CMSController(CMSService cms) {
        this.cms = cms;
    }

    // public void getCredentials(String jwt) {
    //     String output = "";
    //     try {
    //         URL url = new URL("http://13.212.86.115/api/customers/verification");
    //         HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    //         conn.setRequestMethod("GET");
    //         conn.setRequestProperty("Authentication", jwt);

    //         if (conn.getResponseCode() != 200) throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());

    //         BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

    //         // System.out.println("Output from Server .... \n");
    //         while ((output = br.readLine()) != null) {
    //             setUserObj(output);
    //         }
            

    //         conn.disconnect();

    //     } catch (MalformedURLException e) {
    //         e.printStackTrace();
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }
    // }

    // public void setUserObj(String userJSON) {
    //     JSONParser parser = new JSONParser();
    //     JSONObject userObj = new JSONObject();
    //     try {
    //         Object obj  = parser.parse(userJSON);
    //         JSONArray array = new JSONArray();
    //         array.add(obj);

    //         userObj = (JSONObject)array.get(0);

    //         this.userObj = userObj;

    //     } catch(ParseException pe) {
		
    //         System.out.println("position: " + pe.getPosition());
    //         System.out.println(pe);
    //     } catch (NullPointerException npe) {

    //         System.out.println("No user found.");
    //     }
    // }

    // public int getUID(String AuthHeader) {
    //     getCredentials(AuthHeader);
    //     if (this.userObj != null) {
    //         long userLong = (long)this.userObj.get("id");
    //         int userID = Math.toIntExact(userLong);
    //         return userID;
    //     }
    //     return 0;
    // }

 
    /*  
    CHECK PERMISSIONS
    According to roles:
        ANALYST CAN CREATE, GET, UPDATE, DELETE
        MANAGER CAN CREATE, GET, UPDATE, DELETE, APPROVE
        --> only approved content can be displayed <--
        USER CAN ONLY GET CONTENT
    */



    @PostMapping(path="/contents")
    public @ResponseBody Content addContent (@RequestBody Content content) {
        // cms.save(content);
        // return "Saved new article";
        return cms.addContent(content);
    }

    @GetMapping(path="/contents")
    public @ResponseBody List<Content> getContents() {
        //  Iterable<Content> contents = cmsRepository.findAll();
        //  Iterator<Content> iter = contents.iterator();
        //  while (iter.hasNext()) {
        //      Content c = (Content) iter.next();
        //      if (!c.getApproved()) {
        //          iter.remove();
        //      }
         //}
         return cms.listContent();
    }

    @PutMapping("/content/{id}")
    public Content updateContent(@PathVariable int id, @RequestBody Content newContentInfo){
        Content content = cms.updateContent(id, newContentInfo);
        if(content == null) throw new ContentNotFoundException(id);      
        return content;
    }

    @DeleteMapping("/content/{id}")
    public void deleteBook(@PathVariable int id){
        try{
            cms.deleteContent(id);
         }catch(EmptyResultDataAccessException e) {
            throw new ContentNotFoundException(id);
         }
    }

}