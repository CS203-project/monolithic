package com.example.demo.cms;

import java.util.*;
import org.springframework.stereotype.Service;


// @Service
// public class CMSServiceImpl implements CMSService {
   
//     private CMSRepository cms;
    

//     public CMSServiceImpl(CMSRepository cms){
//         this.cms = cms;
//     }

//     @Override
//     public List<Content> listContent() {
//         List<Content> buffer = cms.findAll();
//         for (int i =0; i< buffer.size(); i++){
//             if (!buffer.get(i).getApproved()){
//                 buffer.remove(i);
//             }
//         }
//         return buffer;
//     }

//     @Override
//     public Content getContent(int id) {
//         Optional<Content> contentEntity = cms.findById(id);
//         Content content;
//         if (!contentEntity.isPresent()) {
//             throw new ContentNotFoundException(id);
//         } else {
//             content = contentEntity.get();
//             if(!content.getApproved()){
//                 return null;
//             }
//         }
//         return content;
//     }
    
//     @Override
//     public Content addContent(Content content) {
//         // Optional<Content> contentEntity = cms.findById(id);
//         // if (!contentEntity.isPresent()) {
//         //     throw new ContentNotFoundException(id);
//         // } else {
//             return cms.save(content);
//         // }
//     }
    
//     @Override
//     public Content updateContent(int id, Content newContentInfo){
//             Optional<Content> c = cms.findById(id);
//             if (c.isPresent()) {
//                 Content content = c.get();
//                 content.setTitle(newContentInfo.getTitle());
//                 return cms.save(content);
//             }
//             else 
//                 return null;
//     }

 
//     @Override
//     public void deleteContent(int id){
//         Optional<Content> contentEntity = cms.findById(id);
//         if (!contentEntity.isPresent()) {
//             throw new ContentNotFoundException(id);
//         } else {
//             cms.deleteById(id);
//         }
//     }


// }  