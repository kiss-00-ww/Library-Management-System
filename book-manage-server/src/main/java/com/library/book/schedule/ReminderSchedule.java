package com.library.book.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.library.book.entity.Book;
import com.library.book.entity.BorrowRecord;
import com.library.book.service.BookService;
import com.library.book.service.BorrowRecordService;
import com.library.book.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
public class ReminderSchedule {

    @Autowired
    private BorrowRecordService borrowRecordService;

    @Autowired
    private BookService bookService;

    @Autowired
    private NotificationService notificationService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 每天上午9点执行：检查到期提醒和逾期催还
     * 注意：分布式环境需加锁（如@SchedulerLock或Redis分布式锁）
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendReminders() {
        log.info("===== 开始执行借阅提醒任务 =====");
        int dueRemindCount = remindDueSoon();
        int overdueRemindCount = remindOverdue();
        log.info("===== 提醒任务完成：到期提醒{}条，逾期催还{}条 =====", dueRemindCount, overdueRemindCount);
    }

    /**
     * 到期提醒：离应还日期 <= 3 天的在借/续借记录
     */
    private int remindDueSoon() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threeDaysLater = now.plusDays(3);

        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.apply("status IN ('BORROWED', 'RENEWED')")
                .ge(BorrowRecord::getDueDate, now)
                .le(BorrowRecord::getDueDate, threeDaysLater);

        List<BorrowRecord> records = borrowRecordService.list(wrapper);
        for (BorrowRecord record : records) {
            Book book = bookService.getById(record.getBookId());
            String bookTitle = book != null ? book.getTitle() : "未知图书";
            String dueDateStr = record.getDueDate().format(FORMATTER);

            String title = "图书即将到期提醒";
            String content = "您借阅的《" + bookTitle + "》将于 " + dueDateStr + " 到期，请按时归还。";

            notificationService.sendNotification(record.getUserId(), title, content);
        }
        return records.size();
    }

    /**
     * 逾期催还：OVERDUE 状态且未归还的记录，每隔7天提醒一次
     */
    private int remindOverdue() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysAgo = now.minusDays(7);

        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BorrowRecord::getStatus, "OVERDUE")
                .and(w -> w.isNull(BorrowRecord::getLastRemindTime)
                        .or()
                        .le(BorrowRecord::getLastRemindTime, sevenDaysAgo));

        List<BorrowRecord> records = borrowRecordService.list(wrapper);
        for (BorrowRecord record : records) {
            Book book = bookService.getById(record.getBookId());
            String bookTitle = book != null ? book.getTitle() : "未知图书";

            long overdueDays = java.time.Duration.between(record.getDueDate(), now).toDays();
            String fineStr = record.getFineAmount() != null ? record.getFineAmount().toString() : "0";

            String title = "图书逾期催还通知";
            String content = "您借阅的《" + bookTitle + "》已逾期 " + overdueDays + " 天，请尽快归还并缴纳罚款（当前罚款：¥" + fineStr + "）。";

            notificationService.sendNotification(record.getUserId(), title, content);

            // 更新上次提醒时间
            record.setLastRemindTime(now);
            borrowRecordService.updateById(record);
        }
        return records.size();
    }
}
