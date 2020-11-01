package com.example.demo.cms;

import java.util.*;
import org.springframework.stereotype.Service;


@Service
public class CMSServiceImpl implements CMSService {
   
    private CMSRepository cmsRepository;
    

    public CMSServiceImpl(CMSRepository cms){
        this.cmsRepository = cms;
    }

    @Override
    public List<Content> listContent() {
        List<Content> buffer = cmsRepository.findAll();
        for (int i =0; i< buffer.size(); i++){
            if (!buffer.get(i).getApproved()){
                buffer.remove(i);
            }
        }
        return buffer;
    }

    @Override
    public Content getContent(int id) {
        Optional<Content> contentEntity = cmsRepository.findById(id);
        Content content;
        if (!contentEntity.isPresent()) {
            throw new ContentNotFoundException(id);
        } else {
            content = contentEntity.get();
            if(!content.getApproved()){
                return null;
            }
        }
        return content;
    }
    
    @Override
    public Content addContent(Content content) {
            return cmsRepository.save(content);
    }
    
    @Override
    public Content updateContent(int id, Content newContentInfo){
            Optional<Content> c = cmsRepository.findById(id);
            if (c.isPresent()) {
                Content content = c.get();
                content.setTitle(newContentInfo.getTitle());
                return cmsRepository.save(content);
            }
            else 
                return null;
    }

 
    @Override
    public void deleteContent(int id){
        Optional<Content> contentEntity = cmsRepository.findById(id);
        if (!contentEntity.isPresent()) {
            throw new ContentNotFoundException(id);
        } else {
            cmsRepository.deleteById(id);
        }
    }

    @Override
    public Content isApproved(int id, boolean approvalValue){
        Optional<Content> contentEntity = cmsRepository.findById(id);
        Content content;
        if (contentEntity.isPresent()) {
            content = contentEntity.get();
            content.setApproved(approvalValue);
            return content;
        } else 
            return null;

    }

    
}
  