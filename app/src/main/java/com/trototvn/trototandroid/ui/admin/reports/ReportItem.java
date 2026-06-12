package com.trototvn.trototandroid.ui.admin.reports;

import java.util.ArrayList;
import java.util.List;

/**
 * MOCK report row for the Reports placeholder screen.
 * There is NO backend for reports yet (api/admin/reports/* does not exist);
 * all data here is hardcoded sample data, mirroring the web mock.
 */
public class ReportItem {

    public final int id;
    public final String category;   // "Người dùng" | "Bài đăng" | "Bình luận"
    public final String subject;
    public final String reportType;
    public final String reportedBy;
    public final String status;     // "pending" | "resolved" | "rejected"
    public final String date;

    public ReportItem(int id, String category, String subject, String reportType,
                      String reportedBy, String status, String date) {
        this.id = id;
        this.category = category;
        this.subject = subject;
        this.reportType = reportType;
        this.reportedBy = reportedBy;
        this.status = status;
        this.date = date;
    }

    /** Hardcoded sample data (demo only). */
    public static List<ReportItem> mockData() {
        List<ReportItem> list = new ArrayList<>();
        list.add(new ReportItem(1, "Người dùng", "Trần Văn X", "Thông tin giả mạo", "Nguyễn Thị Y", "pending", "2026-03-12"));
        list.add(new ReportItem(2, "Bài đăng", "Phòng trọ Quận 1 giá rẻ", "Lừa đảo", "Lê Văn Z", "pending", "2026-03-11"));
        list.add(new ReportItem(3, "Bình luận", "Bình luận spam liên hệ ngoài", "Spam", "Phạm Thị K", "resolved", "2026-03-10"));
        list.add(new ReportItem(4, "Người dùng", "Hoàng Văn M", "Quấy rối", "Trần Thị N", "rejected", "2026-03-09"));
        list.add(new ReportItem(5, "Bài đăng", "Căn hộ 2PN Quận 7", "Thông tin sai lệch", "Đỗ Văn P", "pending", "2026-03-08"));
        return list;
    }
}
