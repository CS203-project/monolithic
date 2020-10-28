package com.example.demo.cms;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;


@Service
public class CMSServiceImpl implements CMSService {
   
    private CMSRepository cms;
    

    public CMSServiceImpl(CMSRepository cms){
        this.cms = cms;
    }

    @Override
    public List<Content> listContent() {
        return cms.findAll();
    }

    //i don't get how implement this properly, check if you can do it. 
    public void contentApproved(int id, boolean isApproved){
        // Verify role
        Optional<Content> c = cms.findById(id);

        // TODO: Check for roles (try antMatchers? go look at the slides / stack overflow) - mark on Jira!
        // if (c.isPresent()) {

        //     if (this.userObj != null) {
            
        //         String role = (String)(this.userObj.get("authorities"));
        //         if (role.compareTo("ROLE_MANAGER") == 1) {
        //             // then can approve
        //             Content content = c.get();
        //             c.setApproved(isApproved);
        //             cms.save(content);
        //         }
        //     }
        // }
    }
    
    @Override
    // not checking if content exists already, it should (?)
    public Content addContent(Content content) {
        // TODO: Check for roles - mark on Jira / extract to method

        // String role = (String)(this.userObj.get("authorities"));
        // if (role.compareTo("ROLE_MANAGER") == 1 || role.compareTo("ROLE_ANALYST") == 1) {
        //     return cms.save(content);
        // }
        return null;
    }
    
    @Override
    public Content updateContent(int id, Content newContentInfo){
        // ROLES
        // String role = (String)(this.userObj.get("authorities"));
        // if (role.compareTo("ROLE_MANAGER") == 1 || role.compareTo("ROLE_ANALYST") == 1) {
        //     Optional<Content> c = cms.findById(id);
        //     if (c.isPresent()) {
        //         Content content = c.get();
        //         c.setTitle(newContentInfo.getTitle());
        //         return cms.save(content);
        //     }
        // }
        return null;
    }

 
    @Override
    public void deleteContent(int id){
        // String role = (String)(this.userObj.get("authorities"));
        // if (role.compareTo("ROLE_MANAGER") == 1 || role.compareTo("ROLE_ANALYST") == 1) {
        //     cms.deleteById(id);
        // }
    }

    @Override
    public Content getContent(int id) {
        Optional<Content> contentEntity = cms.findById(id);
        Content content;
        if (!contentEntity.isPresent()) {
            throw new ContentNotFoundException(id);
        } else {
            content = contentEntity.get();
        }
        return content;
    }
}