package com.codeying.stuselect.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.codeying.stuselect.common.IdGenerator;
import com.codeying.stuselect.common.PageQuery;
import com.codeying.stuselect.common.PageResult;
import com.codeying.stuselect.common.Role;
import com.codeying.stuselect.common.UserSession;
import com.codeying.stuselect.mapper.NotificationRecordMapper;
import com.codeying.stuselect.model.NotificationRecord;
import com.codeying.stuselect.model.Student;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

  private final NotificationRecordMapper notificationRecordMapper;
  private final SessionService sessionService;
  private final StudentService studentService;

  public NotificationService(
      NotificationRecordMapper notificationRecordMapper,
      SessionService sessionService,
      StudentService studentService) {
    this.notificationRecordMapper = notificationRecordMapper;
    this.sessionService = sessionService;
    this.studentService = studentService;
  }

  public PageResult<NotificationRecord> list(
      String keyword, Integer page, Integer pageSize, HttpSession session) {
    UserSession current = sessionService.requireUser(session);
    LambdaQueryWrapper<NotificationRecord> wrapper = baseWrapper(keyword, current);
    wrapper.orderByDesc(NotificationRecord::getCreateTime).orderByDesc(NotificationRecord::getId);
    List<NotificationRecord> records = notificationRecordMapper.selectList(wrapper);
    return PageResult.of(records, PageQuery.of(page, pageSize));
  }

  public long count(HttpSession session) {
    UserSession current = sessionService.requireUser(session);
    return notificationRecordMapper.selectCount(baseWrapper(null, current));
  }

  public void notifySelectionCreated(String studentId, String courseName, String timeSlot) {
    Student student = studentService.require(studentId);
    deliverToStudent(
        student,
        "选课成功通知",
        "你已成功选上《" + safeText(courseName, "未命名课程") + "》，上课时间：" + safeText(timeSlot, "未排课") + "。");
  }

  public void notifySelectionDropped(String studentId, String courseName) {
    Student student = studentService.require(studentId);
    deliverToStudent(
        student,
        "退课成功通知",
        "你已成功退选《" + safeText(courseName, "未命名课程") + "》，如需重新选课请在开放时间内操作。");
  }

  public void notifyGradePublished(String studentId, String courseName, Double score) {
    Student student = studentService.require(studentId);
    deliverToStudent(
        student,
        "成绩发布通知",
        "《" + safeText(courseName, "未命名课程") + "》成绩已发布，当前成绩为 " + safeText(score, "-") + " 分。");
  }

  private LambdaQueryWrapper<NotificationRecord> baseWrapper(String keyword, UserSession current) {
    LambdaQueryWrapper<NotificationRecord> wrapper = new LambdaQueryWrapper<>();
    if (current.getRole() != Role.ADMIN) {
      wrapper.eq(NotificationRecord::getRecipientId, current.getId())
          .eq(NotificationRecord::getRecipientRole, current.getRole().getCode());
    }
    if (StringUtils.hasText(keyword)) {
      wrapper.and(
          w ->
              w.like(NotificationRecord::getRecipientName, keyword)
                  .or()
                  .like(NotificationRecord::getChannel, keyword)
                  .or()
                  .like(NotificationRecord::getStatus, keyword)
                  .or()
                  .like(NotificationRecord::getTitle, keyword)
                  .or()
                  .like(NotificationRecord::getContent, keyword));
    }
    return wrapper;
  }

  private void deliverToStudent(Student student, String title, String content) {
    String recipientName = StringUtils.hasText(student.getSname()) ? student.getSname() : student.getUsername();
    saveRecord(
        student.getId(),
        Role.STUDENT.getCode(),
        recipientName,
        "SYSTEM",
        "SENT",
        title,
        content,
        "",
        "站内通知已生成",
        LocalDateTime.now());
  }

  private void saveRecord(
      String recipientId,
      String recipientRole,
      String recipientName,
      String channel,
      String status,
      String title,
      String content,
      String contact,
      String resultMessage,
      LocalDateTime sentTime) {
    NotificationRecord record = new NotificationRecord();
    record.setId(IdGenerator.newId());
    record.setRecipientId(recipientId);
    record.setRecipientRole(recipientRole);
    record.setRecipientName(recipientName);
    record.setChannel(channel);
    record.setStatus(status);
    record.setTitle(title);
    record.setContent(content);
    record.setContact(contact);
    record.setResultMessage(resultMessage);
    record.setCreateTime(LocalDateTime.now());
    record.setSentTime(sentTime);
    notificationRecordMapper.insert(record);
  }

  private String safeText(Object value, String fallback) {
    if (value == null) {
      return fallback;
    }
    String text = value.toString().trim();
    return text.isEmpty() ? fallback : text;
  }
}
