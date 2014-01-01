package com.igearbook.dao;

import com.igearbook.constant.UrlType;
import com.igearbook.entities.CustomUrl;

public interface CustomUrlDao extends BaseDao<CustomUrl> {
    public CustomUrl getByTypeModuleId(UrlType type, int moduleId);
    
    public CustomUrl getByUrl(String url);
}
