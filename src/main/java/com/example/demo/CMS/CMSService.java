package com.example.demo.cms;

import java.util.List;

public interface CMSService {
    List<Content> listContent();
    Content getContent(int id);
    Content addContent(Content content);
    Content updateContent(int id, Content content);

    /**
     * Change method's signature: do not return a value for delete operation
     * @param id
     */
    void deleteContent(int id);
}