package com.hcmute.topicmanagement.service;

public interface EmailService {

    /**
     * Gửi email thông báo kết quả cho từng sinh viên thuộc các nhóm có đề tài
     * trong đợt vừa được công bố. Chạy bất đồng bộ; nếu chưa cấu hình SMTP
     * (app.mail) thì bỏ qua, không ảnh hưởng luồng công bố.
     */
    void notifyResultsPublished(Long periodId);
}
