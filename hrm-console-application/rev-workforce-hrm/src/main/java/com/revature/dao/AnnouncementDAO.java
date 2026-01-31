package com.revature.dao;

import com.revature.model.Announcement;
import java.util.List;

public interface AnnouncementDAO extends GenericDAO<Announcement> {
    int createAnnouncement(Announcement announcement);
    boolean updateAnnouncement(Announcement announcement);
    boolean deleteAnnouncement(int id);
    List<Announcement> findActive();
    boolean setActive(int announcementId, boolean isActive);
}


