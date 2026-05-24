package com.library.book.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.book.entity.Announcement;
import com.library.book.mapper.AnnouncementMapper;
import com.library.book.service.AnnouncementService;
import org.springframework.stereotype.Service;

@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements AnnouncementService {
}
