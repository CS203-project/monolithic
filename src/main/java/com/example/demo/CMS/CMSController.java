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
// import org.json.simple.JSONObject;
// import org.json.simple.JSONArray;
// import org.json.simple.parser.ParseException;
// import org.json.simple.parser.JSONParser;

import org.springframework.beans.factory.annotation.Autowired;   

import java.lang.IllegalArgumentException;

import com.example.demo.config.*;
import com.example.demo.security.*;


    @RestController
    public class CMSController {

        
        private CMSService cmsService;
        
        @Autowired
        public CMSController(CMSService cms) {
            this.cmsService = cms;
        }

        /*  
        CHECK PERMISSIONS
        According to roles:
            ANALYST CAN CREATE, GET, UPDATE, DELETE
            MANAGER CAN CREATE, GET, UPDATE, DELETE, APPROVE
            --> only approved content can be displayed <--
            USER CAN ONLY GET CONTENT
        */

        @GetMapping(path="/contents/{id}")
        public @ResponseBody Content getContent(@PathVariable int id){
            return cmsService.getContent(id);
        }


        @GetMapping(path="/contents")
        public @ResponseBody List<Content> getContents() {
                return cmsService.listContent();
        }

        @PostMapping(path="/contents")
        public @ResponseBody Content addContent (@RequestBody Content content) {
            AuthorizedUser context = new AuthorizedUser();
            if (context.isManager() || context.isAnalyst())
                return cmsService.addContent(content);
            else 
                return null;
        }

        @PutMapping("/content/{id}")
        public Content updateContent(@PathVariable int id, @RequestBody Content newContentInfo){
            AuthorizedUser context = new AuthorizedUser();
            if (context.isManager() || context.isAnalyst())
                return cmsService.updateContent(id, newContentInfo);
            else 
                return null;
        }


        @DeleteMapping("/content/{id}")
        public void deleteBook(@PathVariable int id){
            AuthorizedUser context = new AuthorizedUser();
            if (context.isManager() || context.isAnalyst()){
                try{
                    cmsService.deleteContent(id);
                } catch(EmptyResultDataAccessException e) {
                    throw new ContentNotFoundException(id);
                }
            }
        }
        

        @PutMapping("/content/{id}")
        public Content isApproved(@PathVariable int id, boolean approvalValue){
            AuthorizedUser context = new AuthorizedUser();
            if (context.isManager()) {
                return cmsService.isApproved(id, approvalValue);    
           }
           else
                return null;
        }
        
    }

