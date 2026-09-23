import { useState } from "react";

// ─── Types ────────────────────────────────────────────────────────────────────
type Role = "admin" | "gv" | "sv";
type LoaiDot = "Môn học" | "NCKH" | "TLCN" | "KLTN";
type DotStatus = "Sắp mở" | "GV đăng ký" | "SV đăng ký" | "Đã đóng";

interface DotDangKy {
  id: number;
  ten: string;
  loai: LoaiDot;
  tgGVBD: string;
  tgGVKT: string;
  tgSVBD: string;
  tgSVKT: string;
  hanNopDiem?: string;
  ngayBaoCao?: string;
  status: DotStatus;
}

interface DeTai {
  id: number;
  ma: string;
  ten: string;
  boMon: string;
  gvHD: string[];
  soNhom: number;
  trangThai: "Chờ duyệt" | "Đã duyệt" | "Từ chối";
  dotId: number;
}

interface Nhom {
  id: number;
  ma: string;
  truongNhom: string;
  thanhVien: string[];
  deTaiId: number | null;
  trangThai: "Chưa đăng ký" | "Chờ duyệt" | "Đã duyệt";
  daNopBaoCao: boolean;
  dotId: number;
}

interface HoiDong {
  id: number;
  ten: string;
  chuTich: string;
  thuKy: string;
  thanhVien: string[];
  deTais: number[];
  dotId: number;
}

interface KetQua {
  nhomId: number;
  deTaiId: number;
  diemGVHD: number;
  diemPhanBien: number;
  diemTB: number;
  xepLoai: string;
}

interface ThongBao {
  id: number;
  tieu: string;
  noi: string;
  ngay: string;
  loai: "Nhà trường" | "Khoa";
}

// ─── Mock Data ─────────────────────────────────────────────────────────────────
const mockDots: DotDangKy[] = [
  {
    id: 1, ten: "KLTN HK1 2025-2026", loai: "KLTN",
    tgGVBD: "2025-09-01", tgGVKT: "2025-09-15",
    tgSVBD: "2025-09-16", tgSVKT: "2025-09-30",
    hanNopDiem: "2026-01-10", ngayBaoCao: "2026-01-15",
    status: "SV đăng ký"
  },
  {
    id: 2, ten: "TLCN HK1 2025-2026", loai: "TLCN",
    tgGVBD: "2025-09-01", tgGVKT: "2025-09-10",
    tgSVBD: "2025-09-11", tgSVKT: "2025-09-25",
    hanNopDiem: "2026-01-08",
    status: "GV đăng ký"
  },
  {
    id: 3, ten: "NCKH HK2 2024-2025", loai: "NCKH",
    tgGVBD: "2025-01-05", tgGVKT: "2025-01-15",
    tgSVBD: "2025-01-16", tgSVKT: "2025-01-28",
    status: "Đã đóng"
  },
];

const mockDeTais: DeTai[] = [
  { id: 1, ma: "KLTN001", ten: "Hệ thống gợi ý học tập dựa trên AI", boMon: "Khoa học máy tính", gvHD: ["PGS.TS Nguyễn Văn An", "TS. Trần Thị Bình"], soNhom: 1, trangThai: "Đã duyệt", dotId: 1 },
  { id: 2, ma: "KLTN002", ten: "Ứng dụng nhận diện khuôn mặt trong điểm danh", boMon: "Kỹ thuật phần mềm", gvHD: ["TS. Lê Minh Cường"], soNhom: 1, trangThai: "Đã duyệt", dotId: 1 },
  { id: 3, ma: "KLTN003", ten: "Xây dựng chatbot hỗ trợ sinh viên", boMon: "Hệ thống thông tin", gvHD: ["ThS. Phạm Quốc Dũng"], soNhom: 0, trangThai: "Đã duyệt", dotId: 1 },
  { id: 4, ma: "KLTN004", ten: "Phân tích dữ liệu lớn với Apache Spark", boMon: "Khoa học máy tính", gvHD: ["PGS.TS Nguyễn Văn An"], soNhom: 1, trangThai: "Chờ duyệt", dotId: 1 },
  { id: 5, ma: "KLTN005", ten: "Tối ưu hóa thuật toán tìm kiếm đường đi", boMon: "Kỹ thuật phần mềm", gvHD: ["TS. Trần Thị Bình"], soNhom: 0, trangThai: "Từ chối", dotId: 1 },
  { id: 6, ma: "TLCN001", ten: "Thiết kế giao diện ứng dụng di động", boMon: "Hệ thống thông tin", gvHD: ["ThS. Phạm Quốc Dũng"], soNhom: 2, trangThai: "Đã duyệt", dotId: 2 },
];

const mockNhoms: Nhom[] = [
  { id: 1, ma: "N001", truongNhom: "Nguyễn Hoàng Nam", thanhVien: ["Nguyễn Hoàng Nam", "Trần Thị Thu", "Lê Văn Hùng"], deTaiId: 1, trangThai: "Đã duyệt", daNopBaoCao: true, dotId: 1 },
  { id: 2, ma: "N002", truongNhom: "Phạm Minh Tuấn", thanhVien: ["Phạm Minh Tuấn", "Nguyễn Thị Lan"], deTaiId: 2, trangThai: "Đã duyệt", daNopBaoCao: false, dotId: 1 },
  { id: 3, ma: "N003", truongNhom: "Đỗ Thanh Hà", thanhVien: ["Đỗ Thanh Hà", "Vũ Quang Minh", "Bùi Thị Kim"], deTaiId: 4, trangThai: "Chờ duyệt", daNopBaoCao: false, dotId: 1 },
  { id: 4, ma: "N004", truongNhom: "Hoàng Anh Tuấn", thanhVien: ["Hoàng Anh Tuấn"], deTaiId: null, trangThai: "Chưa đăng ký", daNopBaoCao: false, dotId: 1 },
];

const mockHoiDongs: HoiDong[] = [
  { id: 1, ten: "Hội đồng A", chuTich: "PGS.TS Nguyễn Văn An", thuKy: "TS. Lê Minh Cường", thanhVien: ["ThS. Phạm Quốc Dũng", "TS. Hoàng Văn Đức"], deTais: [2, 3], dotId: 1 },
  { id: 2, ten: "Hội đồng B", chuTich: "TS. Trần Thị Bình", thuKy: "ThS. Nguyễn Thị Mai", thanhVien: ["TS. Lê Văn Phúc", "ThS. Trần Minh Khoa", "TS. Đặng Thị Hoa"], deTais: [1], dotId: 1 },
];

const mockKetQuas: KetQua[] = [
  { nhomId: 1, deTaiId: 1, diemGVHD: 8.5, diemPhanBien: 7.8, diemTB: 8.15, xepLoai: "Giỏi" },
  { nhomId: 2, deTaiId: 2, diemGVHD: 9.0, diemPhanBien: 8.5, diemTB: 8.75, xepLoai: "Xuất sắc" },
];

const mockThongBaos: ThongBao[] = [
  { id: 1, tieu: "Lịch báo cáo Khóa luận tốt nghiệp HK1 2025-2026", noi: "Khoa CNTT thông báo lịch báo cáo KLTN HK1 2025-2026 sẽ diễn ra vào ngày 15/01/2026 tại phòng B4.01. Sinh viên cần có mặt trước 15 phút.", ngay: "2025-09-10", loai: "Khoa" },
  { id: 2, tieu: "Thông báo mở đăng ký đề tài KLTN HK1 2025-2026", noi: "Khoa CNTT thông báo mở đăng ký đề tài Khóa luận tốt nghiệp HK1 2025-2026. Sinh viên đủ điều kiện đăng ký từ ngày 16/09 đến 30/09/2025.", ngay: "2025-09-05", loai: "Khoa" },
  { id: 3, tieu: "Quy định về định dạng báo cáo đề tài", noi: "Trường thông báo áp dụng mẫu báo cáo mới từ HK1 2025-2026. Sinh viên tải mẫu tại cổng thông tin sinh viên.", ngay: "2025-08-28", loai: "Nhà trường" },
];

// ─── Helpers ──────────────────────────────────────────────────────────────────
const statusColor: Record<string, string> = {
  "Đã duyệt": "bg-emerald-50 text-emerald-700 border-emerald-200",
  "Chờ duyệt": "bg-amber-50 text-amber-700 border-amber-200",
  "Từ chối": "bg-red-50 text-red-700 border-red-200",
  "Chưa đăng ký": "bg-gray-100 text-gray-600 border-gray-200",
  "SV đăng ký": "bg-blue-50 text-blue-700 border-blue-200",
  "GV đăng ký": "bg-violet-50 text-violet-700 border-violet-200",
  "Sắp mở": "bg-gray-100 text-gray-500 border-gray-200",
  "Đã đóng": "bg-gray-100 text-gray-400 border-gray-200",
};

function Badge({ label }: { label: string }) {
  return (
    <span className={`inline-block text-xs font-medium px-2 py-0.5 rounded border ${statusColor[label] ?? "bg-gray-100 text-gray-600 border-gray-200"}`}>
      {label}
    </span>
  );
}

function Pill({ text, color = "blue" }: { text: string; color?: string }) {
  const colors: Record<string, string> = {
    blue: "bg-blue-100 text-blue-800",
    green: "bg-emerald-100 text-emerald-800",
    red: "bg-red-100 text-red-700",
    gray: "bg-gray-100 text-gray-600",
    violet: "bg-violet-100 text-violet-800",
  };
  return (
    <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${colors[color]}`}>{text}</span>
  );
}

// ─── Dashboard Stats ──────────────────────────────────────────────────────────
function StatCard({ label, value, sub, accent }: { label: string; value: string | number; sub?: string; accent?: boolean }) {
  return (
    <div className={`rounded p-5 border ${accent ? "bg-[#1b2f6e] border-[#1b2f6e] text-white" : "bg-white border-[#dde1ed] text-[#0f1523]"}`}>
      <div className={`text-3xl font-bold font-[family-name:var(--font-display)] mb-1 ${accent ? "text-white" : "text-[#1b2f6e]"}`}>{value}</div>
      <div className={`text-sm font-medium ${accent ? "text-blue-200" : "text-[#0f1523]"}`}>{label}</div>
      {sub && <div className={`text-xs mt-1 ${accent ? "text-blue-300" : "text-[#64718a]"}`}>{sub}</div>}
    </div>
  );
}

// ─── Sections ─────────────────────────────────────────────────────────────────
function Dashboard({ role }: { role: Role }) {
  return (
    <div>
      <h1 className="text-2xl font-bold font-[family-name:var(--font-display)] text-[#1b2f6e] mb-1">Tổng quan hệ thống</h1>
      <p className="text-sm text-[#64718a] mb-6">Khoa Công nghệ Thông tin · HK1 2025-2026</p>

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        <StatCard label="Đề tài đã duyệt" value={mockDeTais.filter(d => d.trangThai === "Đã duyệt").length} sub="trong tổng số 6 đề tài" accent />
        <StatCard label="Nhóm đang thực hiện" value={mockNhoms.filter(n => n.trangThai === "Đã duyệt").length} sub="đã được phê duyệt" />
        <StatCard label="Chờ phê duyệt" value={mockNhoms.filter(n => n.trangThai === "Chờ duyệt").length + mockDeTais.filter(d => d.trangThai === "Chờ duyệt").length} sub="đề tài + nhóm" />
        <StatCard label="Hội đồng phản biện" value={mockHoiDongs.length} sub="đã thành lập" />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
        {/* Recent dots */}
        <div className="lg:col-span-2 bg-white border border-[#dde1ed] rounded p-5">
          <h2 className="text-sm font-semibold text-[#64718a] uppercase tracking-wider mb-4">Đợt đăng ký gần đây</h2>
          <div className="space-y-3">
            {mockDots.slice(0, 3).map(dot => (
              <div key={dot.id} className="flex items-center justify-between py-2 border-b border-[#eef0f6] last:border-0">
                <div>
                  <div className="text-sm font-semibold text-[#0f1523]">{dot.ten}</div>
                  <div className="text-xs text-[#64718a] font-mono mt-0.5">
                    SV: {dot.tgSVBD} → {dot.tgSVKT}
                  </div>
                </div>
                <Badge label={dot.status} />
              </div>
            ))}
          </div>
        </div>

        {/* Announcements */}
        <div className="bg-white border border-[#dde1ed] rounded p-5">
          <h2 className="text-sm font-semibold text-[#64718a] uppercase tracking-wider mb-4">Thông báo mới</h2>
          <div className="space-y-3">
            {mockThongBaos.slice(0, 3).map(tb => (
              <div key={tb.id} className="py-2 border-b border-[#eef0f6] last:border-0">
                <div className="flex items-center gap-1.5 mb-1">
                  <Pill text={tb.loai} color={tb.loai === "Khoa" ? "blue" : "violet"} />
                  <span className="text-xs text-[#64718a] font-mono">{tb.ngay}</span>
                </div>
                <p className="text-xs text-[#0f1523] leading-snug">{tb.tieu}</p>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Progress table */}
      <div className="mt-4 bg-white border border-[#dde1ed] rounded p-5">
        <h2 className="text-sm font-semibold text-[#64718a] uppercase tracking-wider mb-4">Tiến độ thực hiện đề tài</h2>
        <table className="w-full text-sm">
          <thead>
            <tr className="text-xs text-[#64718a] border-b border-[#eef0f6]">
              <th className="text-left pb-2 font-semibold">Nhóm</th>
              <th className="text-left pb-2 font-semibold">Đề tài</th>
              <th className="text-left pb-2 font-semibold">Nhóm trưởng</th>
              <th className="text-left pb-2 font-semibold">Báo cáo</th>
              <th className="text-left pb-2 font-semibold">Trạng thái</th>
            </tr>
          </thead>
          <tbody>
            {mockNhoms.filter(n => n.deTaiId).map(nhom => {
              const dt = mockDeTais.find(d => d.id === nhom.deTaiId);
              return (
                <tr key={nhom.id} className="border-b border-[#eef0f6] last:border-0 hover:bg-[#f4f6fa] transition-colors">
                  <td className="py-2.5 font-mono text-xs text-[#1b2f6e] font-bold">{nhom.ma}</td>
                  <td className="py-2.5 text-[#0f1523] max-w-[200px]">
                    <span className="truncate block">{dt?.ten ?? "—"}</span>
                  </td>
                  <td className="py-2.5 text-[#64718a]">{nhom.truongNhom}</td>
                  <td className="py-2.5">
                    {nhom.daNopBaoCao
                      ? <span className="text-emerald-600 font-medium text-xs">✓ Đã nộp</span>
                      : <span className="text-[#64718a] text-xs">Chưa nộp</span>}
                  </td>
                  <td className="py-2.5"><Badge label={nhom.trangThai} /></td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function DotDangKySection() {
  const [dots, setDots] = useState<DotDangKy[]>(mockDots);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState<Partial<DotDangKy>>({ loai: "KLTN" });

  const needsHanNopDiem = form.loai === "TLCN" || form.loai === "KLTN";
  const needsBaoCao = form.loai === "KLTN";

  function handleAdd() {
    if (!form.ten || !form.loai) return;
    const newDot: DotDangKy = {
      id: dots.length + 1,
      ten: form.ten!,
      loai: form.loai as LoaiDot,
      tgGVBD: form.tgGVBD ?? "",
      tgGVKT: form.tgGVKT ?? "",
      tgSVBD: form.tgSVBD ?? "",
      tgSVKT: form.tgSVKT ?? "",
      hanNopDiem: form.hanNopDiem,
      ngayBaoCao: form.ngayBaoCao,
      status: "Sắp mở",
    };
    setDots([...dots, newDot]);
    setShowForm(false);
    setForm({ loai: "KLTN" });
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold font-[family-name:var(--font-display)] text-[#1b2f6e]">Đợt đăng ký đề tài</h1>
          <p className="text-sm text-[#64718a] mt-0.5">Quản lý các đợt đăng ký theo giai đoạn</p>
        </div>
        <button onClick={() => setShowForm(!showForm)} className="bg-[#1b2f6e] text-white text-sm px-4 py-2 rounded hover:bg-[#15255a] transition-colors font-medium">
          + Tạo đợt mới
        </button>
      </div>

      {showForm && (
        <div className="bg-white border border-[#dde1ed] rounded p-6 mb-6">
          <h2 className="font-semibold text-[#0f1523] mb-4">Tạo đợt đăng ký mới</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="md:col-span-2">
              <label className="block text-xs font-semibold text-[#64718a] uppercase tracking-wider mb-1">Tên đợt đăng ký *</label>
              <input className="w-full border border-[#dde1ed] rounded px-3 py-2 text-sm focus:outline-none focus:border-[#1b2f6e]"
                value={form.ten ?? ""} onChange={e => setForm({ ...form, ten: e.target.value })} placeholder="VD: KLTN HK2 2025-2026" />
            </div>
            <div>
              <label className="block text-xs font-semibold text-[#64718a] uppercase tracking-wider mb-1">Loại *</label>
              <select className="w-full border border-[#dde1ed] rounded px-3 py-2 text-sm focus:outline-none focus:border-[#1b2f6e]"
                value={form.loai} onChange={e => setForm({ ...form, loai: e.target.value as LoaiDot })}>
                {["Môn học", "NCKH", "TLCN", "KLTN"].map(l => <option key={l}>{l}</option>)}
              </select>
            </div>
            <div />
            <div>
              <label className="block text-xs font-semibold text-[#64718a] uppercase tracking-wider mb-1">GV đăng ký từ</label>
              <input type="date" className="w-full border border-[#dde1ed] rounded px-3 py-2 text-sm focus:outline-none focus:border-[#1b2f6e]"
                value={form.tgGVBD ?? ""} onChange={e => setForm({ ...form, tgGVBD: e.target.value })} />
            </div>
            <div>
              <label className="block text-xs font-semibold text-[#64718a] uppercase tracking-wider mb-1">GV đăng ký đến</label>
              <input type="date" className="w-full border border-[#dde1ed] rounded px-3 py-2 text-sm focus:outline-none focus:border-[#1b2f6e]"
                value={form.tgGVKT ?? ""} onChange={e => setForm({ ...form, tgGVKT: e.target.value })} />
            </div>
            <div>
              <label className="block text-xs font-semibold text-[#64718a] uppercase tracking-wider mb-1">SV đăng ký từ</label>
              <input type="date" className="w-full border border-[#dde1ed] rounded px-3 py-2 text-sm focus:outline-none focus:border-[#1b2f6e]"
                value={form.tgSVBD ?? ""} onChange={e => setForm({ ...form, tgSVBD: e.target.value })} />
            </div>
            <div>
              <label className="block text-xs font-semibold text-[#64718a] uppercase tracking-wider mb-1">SV đăng ký đến</label>
              <input type="date" className="w-full border border-[#dde1ed] rounded px-3 py-2 text-sm focus:outline-none focus:border-[#1b2f6e]"
                value={form.tgSVKT ?? ""} onChange={e => setForm({ ...form, tgSVKT: e.target.value })} />
            </div>
            {needsHanNopDiem && (
              <div>
                <label className="block text-xs font-semibold text-[#64718a] uppercase tracking-wider mb-1">Hạn nộp điểm (GVPB)</label>
                <input type="date" className="w-full border border-[#dde1ed] rounded px-3 py-2 text-sm focus:outline-none focus:border-[#1b2f6e]"
                  value={form.hanNopDiem ?? ""} onChange={e => setForm({ ...form, hanNopDiem: e.target.value })} />
              </div>
            )}
            {needsBaoCao && (
              <div>
                <label className="block text-xs font-semibold text-[#64718a] uppercase tracking-wider mb-1">Ngày báo cáo hội đồng</label>
                <input type="date" className="w-full border border-[#dde1ed] rounded px-3 py-2 text-sm focus:outline-none focus:border-[#1b2f6e]"
                  value={form.ngayBaoCao ?? ""} onChange={e => setForm({ ...form, ngayBaoCao: e.target.value })} />
              </div>
            )}
          </div>
          <div className="flex gap-3 mt-5">
            <button onClick={handleAdd} className="bg-[#1b2f6e] text-white text-sm px-5 py-2 rounded hover:bg-[#15255a] transition-colors font-medium">Tạo đợt</button>
            <button onClick={() => setShowForm(false)} className="border border-[#dde1ed] text-[#64718a] text-sm px-5 py-2 rounded hover:bg-[#f4f6fa] transition-colors">Hủy</button>
          </div>
        </div>
      )}

      <div className="grid gap-4">
        {dots.map(dot => (
          <div key={dot.id} className="bg-white border border-[#dde1ed] rounded p-5 hover:border-[#1b2f6e] transition-colors">
            <div className="flex items-start justify-between mb-3">
              <div>
                <div className="flex items-center gap-2 mb-1">
                  <span className="font-mono text-xs text-[#64718a]">#{dot.id.toString().padStart(3, "0")}</span>
                  <Pill text={dot.loai} color="blue" />
                </div>
                <h3 className="font-semibold text-[#0f1523] font-[family-name:var(--font-display)]">{dot.ten}</h3>
              </div>
              <Badge label={dot.status} />
            </div>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-3 text-xs">
              <div>
                <div className="text-[#64718a] font-semibold uppercase tracking-wider mb-0.5">GV đăng ký</div>
                <div className="font-mono text-[#0f1523]">{dot.tgGVBD} → {dot.tgGVKT}</div>
              </div>
              <div>
                <div className="text-[#64718a] font-semibold uppercase tracking-wider mb-0.5">SV đăng ký</div>
                <div className="font-mono text-[#0f1523]">{dot.tgSVBD} → {dot.tgSVKT}</div>
              </div>
              {dot.hanNopDiem && (
                <div>
                  <div className="text-[#64718a] font-semibold uppercase tracking-wider mb-0.5">Hạn nộp điểm</div>
                  <div className="font-mono text-[#e8410a]">{dot.hanNopDiem}</div>
                </div>
              )}
              {dot.ngayBaoCao && (
                <div>
                  <div className="text-[#64718a] font-semibold uppercase tracking-wider mb-0.5">Ngày báo cáo</div>
                  <div className="font-mono text-[#1b2f6e] font-bold">{dot.ngayBaoCao}</div>
                </div>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

function DeTaiSection({ role }: { role: Role }) {
  const [filter, setFilter] = useState<string>("Tất cả");
  const [search, setSearch] = useState("");
  const [selected, setSelected] = useState<DeTai | null>(null);

  const filters = ["Tất cả", "Đã duyệt", "Chờ duyệt", "Từ chối"];
  const filtered = mockDeTais.filter(d =>
    (filter === "Tất cả" || d.trangThai === filter) &&
    (d.ten.toLowerCase().includes(search.toLowerCase()) || d.ma.toLowerCase().includes(search.toLowerCase()))
  );

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold font-[family-name:var(--font-display)] text-[#1b2f6e]">Danh sách đề tài</h1>
          <p className="text-sm text-[#64718a] mt-0.5">Quản lý theo bộ môn · Đợt KLTN HK1 2025-2026</p>
        </div>
        {(role === "admin" || role === "gv") && (
          <button className="bg-[#1b2f6e] text-white text-sm px-4 py-2 rounded hover:bg-[#15255a] transition-colors font-medium">
            + Đề xuất đề tài
          </button>
        )}
      </div>

      <div className="flex flex-col sm:flex-row gap-3 mb-5">
        <input className="border border-[#dde1ed] rounded px-3 py-2 text-sm flex-1 focus:outline-none focus:border-[#1b2f6e]"
          placeholder="Tìm theo tên hoặc mã đề tài…" value={search} onChange={e => setSearch(e.target.value)} />
        <div className="flex gap-1">
          {filters.map(f => (
            <button key={f} onClick={() => setFilter(f)}
              className={`text-xs px-3 py-2 rounded border transition-colors font-medium ${filter === f ? "bg-[#1b2f6e] border-[#1b2f6e] text-white" : "border-[#dde1ed] text-[#64718a] hover:border-[#1b2f6e]"}`}>
              {f}
            </button>
          ))}
        </div>
      </div>

      <div className="bg-white border border-[#dde1ed] rounded overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-[#f4f6fa] border-b border-[#dde1ed]">
            <tr className="text-xs text-[#64718a] uppercase tracking-wider">
              <th className="text-left px-4 py-3 font-semibold">Mã ĐT</th>
              <th className="text-left px-4 py-3 font-semibold">Tên đề tài</th>
              <th className="text-left px-4 py-3 font-semibold">Bộ môn</th>
              <th className="text-left px-4 py-3 font-semibold">GVHD</th>
              <th className="text-left px-4 py-3 font-semibold">Nhóm</th>
              <th className="text-left px-4 py-3 font-semibold">Trạng thái</th>
              {role === "admin" && <th className="text-left px-4 py-3 font-semibold">Thao tác</th>}
            </tr>
          </thead>
          <tbody>
            {filtered.map(dt => (
              <tr key={dt.id} className="border-b border-[#eef0f6] last:border-0 hover:bg-[#f4f6fa] transition-colors cursor-pointer"
                onClick={() => setSelected(dt)}>
                <td className="px-4 py-3 font-mono text-xs font-bold text-[#1b2f6e]">{dt.ma}</td>
                <td className="px-4 py-3 font-medium text-[#0f1523] max-w-[220px]">
                  <span className="line-clamp-2">{dt.ten}</span>
                </td>
                <td className="px-4 py-3 text-[#64718a] text-xs">{dt.boMon}</td>
                <td className="px-4 py-3 text-xs text-[#0f1523]">
                  {dt.gvHD.map((g, i) => <div key={i}>{g}</div>)}
                </td>
                <td className="px-4 py-3 text-center">
                  <span className={`font-mono text-sm font-bold ${dt.soNhom > 0 ? "text-[#1b2f6e]" : "text-[#dde1ed]"}`}>{dt.soNhom}</span>
                </td>
                <td className="px-4 py-3"><Badge label={dt.trangThai} /></td>
                {role === "admin" && (
                  <td className="px-4 py-3" onClick={e => e.stopPropagation()}>
                    {dt.trangThai === "Chờ duyệt" && (
                      <div className="flex gap-1">
                        <button className="text-xs px-2 py-1 bg-emerald-50 text-emerald-700 border border-emerald-200 rounded hover:bg-emerald-100 transition-colors">Duyệt</button>
                        <button className="text-xs px-2 py-1 bg-red-50 text-red-700 border border-red-200 rounded hover:bg-red-100 transition-colors">Từ chối</button>
                      </div>
                    )}
                  </td>
                )}
              </tr>
            ))}
            {filtered.length === 0 && (
              <tr><td colSpan={7} className="px-4 py-8 text-center text-[#64718a] text-sm">Không tìm thấy đề tài</td></tr>
            )}
          </tbody>
        </table>
      </div>

      {/* Detail modal */}
      {selected && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4" onClick={() => setSelected(null)}>
          <div className="bg-white rounded-lg w-full max-w-lg p-6" onClick={e => e.stopPropagation()}>
            <div className="flex items-start justify-between mb-4">
              <div>
                <span className="font-mono text-xs text-[#64718a]">{selected.ma}</span>
                <h2 className="text-lg font-bold font-[family-name:var(--font-display)] text-[#1b2f6e] mt-1">{selected.ten}</h2>
              </div>
              <button onClick={() => setSelected(null)} className="text-[#64718a] hover:text-[#0f1523] text-xl leading-none">&times;</button>
            </div>
            <div className="space-y-3 text-sm">
              <div className="flex justify-between border-b border-[#eef0f6] pb-2">
                <span className="text-[#64718a]">Bộ môn</span>
                <span className="font-medium">{selected.boMon}</span>
              </div>
              <div className="flex justify-between border-b border-[#eef0f6] pb-2">
                <span className="text-[#64718a]">Giảng viên hướng dẫn</span>
                <div className="text-right">{selected.gvHD.map((g, i) => <div key={i} className="font-medium">{g}</div>)}</div>
              </div>
              <div className="flex justify-between border-b border-[#eef0f6] pb-2">
                <span className="text-[#64718a]">Số nhóm đăng ký</span>
                <span className="font-bold font-mono text-[#1b2f6e]">{selected.soNhom} nhóm</span>
              </div>
              <div className="flex justify-between">
                <span className="text-[#64718a]">Trạng thái</span>
                <Badge label={selected.trangThai} />
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

function NhomSection({ role }: { role: Role }) {
  const [nhoms] = useState<Nhom[]>(mockNhoms);

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold font-[family-name:var(--font-display)] text-[#1b2f6e]">Nhóm sinh viên</h1>
          <p className="text-sm text-[#64718a] mt-0.5">Tối đa 3 thành viên / nhóm · 1 nhóm trưởng</p>
        </div>
        {role === "sv" && (
          <button className="bg-[#1b2f6e] text-white text-sm px-4 py-2 rounded hover:bg-[#15255a] transition-colors font-medium">
            + Tạo nhóm
          </button>
        )}
      </div>

      <div className="grid gap-4">
        {nhoms.map(nhom => {
          const dt = mockDeTais.find(d => d.id === nhom.deTaiId);
          return (
            <div key={nhom.id} className="bg-white border border-[#dde1ed] rounded p-5 hover:border-[#1b2f6e] transition-colors">
              <div className="flex items-start justify-between mb-4">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 bg-[#1b2f6e] rounded flex items-center justify-center text-white font-bold font-mono text-sm">{nhom.ma}</div>
                  <div>
                    <div className="font-semibold text-[#0f1523]">Nhóm trưởng: {nhom.truongNhom}</div>
                    <div className="text-xs text-[#64718a]">{nhom.thanhVien.length} thành viên</div>
                  </div>
                </div>
                <Badge label={nhom.trangThai} />
              </div>

              <div className="flex flex-wrap gap-2 mb-4">
                {nhom.thanhVien.map((tv, i) => (
                  <span key={i} className={`text-xs px-2 py-1 rounded-full border ${tv === nhom.truongNhom ? "bg-[#1b2f6e] text-white border-[#1b2f6e]" : "bg-[#f4f6fa] text-[#64718a] border-[#dde1ed]"}`}>
                    {tv === nhom.truongNhom ? "★ " : ""}{tv}
                  </span>
                ))}
              </div>

              <div className="border-t border-[#eef0f6] pt-3 flex items-center justify-between">
                <div className="text-xs text-[#64718a]">
                  {dt
                    ? <span>Đề tài: <span className="text-[#0f1523] font-medium">{dt.ten}</span></span>
                    : <span className="text-amber-600">Chưa đăng ký đề tài</span>}
                </div>
                <div className="flex items-center gap-3">
                  {nhom.daNopBaoCao
                    ? <span className="text-xs text-emerald-600 font-medium">✓ Đã nộp báo cáo</span>
                    : nhom.trangThai === "Đã duyệt" && <span className="text-xs text-amber-600">Chưa nộp báo cáo</span>}
                  {role === "sv" && nhom.truongNhom === "Nguyễn Hoàng Nam" && !nhom.daNopBaoCao && nhom.trangThai === "Đã duyệt" && (
                    <button className="text-xs px-3 py-1 bg-[#1b2f6e] text-white rounded hover:bg-[#15255a] transition-colors">Nộp báo cáo</button>
                  )}
                  {role === "admin" && nhom.trangThai === "Chờ duyệt" && (
                    <div className="flex gap-1">
                      <button className="text-xs px-2 py-1 bg-emerald-50 text-emerald-700 border border-emerald-200 rounded hover:bg-emerald-100 transition-colors">Duyệt</button>
                      <button className="text-xs px-2 py-1 bg-red-50 text-red-700 border border-red-200 rounded hover:bg-red-100 transition-colors">Từ chối</button>
                    </div>
                  )}
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

function HoiDongSection() {
  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold font-[family-name:var(--font-display)] text-[#1b2f6e]">Hội đồng phản biện</h1>
          <p className="text-sm text-[#64718a] mt-0.5">3–5 GV mỗi hội đồng · 1 chủ tịch · 1 thư ký</p>
        </div>
        <button className="bg-[#1b2f6e] text-white text-sm px-4 py-2 rounded hover:bg-[#15255a] transition-colors font-medium">
          + Thành lập hội đồng
        </button>
      </div>

      <div className="grid gap-5">
        {mockHoiDongs.map(hd => {
          const allMembers = [hd.chuTich, hd.thuKy, ...hd.thanhVien];
          const assignedDeTais = hd.deTais.map(id => mockDeTais.find(d => d.id === id)).filter(Boolean);
          return (
            <div key={hd.id} className="bg-white border border-[#dde1ed] rounded p-5">
              <div className="flex items-center justify-between mb-4">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 bg-[#e8ecf7] rounded flex items-center justify-center text-[#1b2f6e] font-bold text-sm">{hd.id}</div>
                  <div>
                    <h3 className="font-bold font-[family-name:var(--font-display)] text-[#0f1523]">{hd.ten}</h3>
                    <div className="text-xs text-[#64718a]">{allMembers.length} thành viên · {hd.deTais.length} đề tài</div>
                  </div>
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                <div>
                  <div className="text-xs font-semibold text-[#64718a] uppercase tracking-wider mb-2">Thành viên hội đồng</div>
                  <div className="space-y-2">
                    {[
                      { name: hd.chuTich, role: "Chủ tịch" },
                      { name: hd.thuKy, role: "Thư ký" },
                      ...hd.thanhVien.map(tv => ({ name: tv, role: "Thành viên" })),
                    ].map((m, i) => (
                      <div key={i} className="flex items-center justify-between text-sm">
                        <span className="text-[#0f1523]">{m.name}</span>
                        <span className={`text-xs px-2 py-0.5 rounded-full ${m.role === "Chủ tịch" ? "bg-[#1b2f6e] text-white" : m.role === "Thư ký" ? "bg-[#e8ecf7] text-[#1b2f6e]" : "bg-gray-100 text-gray-600"}`}>
                          {m.role}
                        </span>
                      </div>
                    ))}
                  </div>
                </div>
                <div>
                  <div className="text-xs font-semibold text-[#64718a] uppercase tracking-wider mb-2">Đề tài phân công</div>
                  <div className="space-y-2">
                    {assignedDeTais.map(dt => dt && (
                      <div key={dt.id} className="p-2 bg-[#f4f6fa] rounded border border-[#eef0f6]">
                        <div className="font-mono text-xs text-[#1b2f6e] font-bold">{dt.ma}</div>
                        <div className="text-xs text-[#0f1523] mt-0.5 line-clamp-1">{dt.ten}</div>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

function KetQuaSection({ role }: { role: Role }) {
  return (
    <div>
      <h1 className="text-2xl font-bold font-[family-name:var(--font-display)] text-[#1b2f6e] mb-1">Kết quả & Điểm số</h1>
      <p className="text-sm text-[#64718a] mb-6">Điểm = (GVHD + Phản biện) / 2 · GV không chấm đề tài mình hướng dẫn</p>

      <div className="bg-white border border-[#dde1ed] rounded overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-[#f4f6fa] border-b border-[#dde1ed]">
            <tr className="text-xs text-[#64718a] uppercase tracking-wider">
              <th className="text-left px-4 py-3 font-semibold">Nhóm</th>
              <th className="text-left px-4 py-3 font-semibold">Đề tài</th>
              <th className="text-right px-4 py-3 font-semibold">Điểm GVHD</th>
              <th className="text-right px-4 py-3 font-semibold">Điểm PB</th>
              <th className="text-right px-4 py-3 font-semibold">Điểm TB</th>
              <th className="text-center px-4 py-3 font-semibold">Xếp loại</th>
            </tr>
          </thead>
          <tbody>
            {mockKetQuas.map(kq => {
              const nhom = mockNhoms.find(n => n.id === kq.nhomId);
              const dt = mockDeTais.find(d => d.id === kq.deTaiId);
              const xepLoaiColor = kq.xepLoai === "Xuất sắc" ? "text-emerald-600 bg-emerald-50 border-emerald-200" : "text-blue-700 bg-blue-50 border-blue-200";
              return (
                <tr key={kq.nhomId} className="border-b border-[#eef0f6] last:border-0 hover:bg-[#f4f6fa] transition-colors">
                  <td className="px-4 py-3 font-mono text-xs font-bold text-[#1b2f6e]">{nhom?.ma}</td>
                  <td className="px-4 py-3 text-[#0f1523] max-w-[220px]">
                    <div className="line-clamp-1">{dt?.ten}</div>
                    <div className="text-xs text-[#64718a]">{nhom?.truongNhom}</div>
                  </td>
                  <td className="px-4 py-3 text-right font-mono font-bold text-[#0f1523]">{kq.diemGVHD.toFixed(1)}</td>
                  <td className="px-4 py-3 text-right font-mono font-bold text-[#0f1523]">{kq.diemPhanBien.toFixed(1)}</td>
                  <td className="px-4 py-3 text-right font-mono font-bold text-2xl text-[#1b2f6e]">{kq.diemTB.toFixed(2)}</td>
                  <td className="px-4 py-3 text-center">
                    <span className={`text-xs font-semibold px-2 py-1 rounded border ${xepLoaiColor}`}>{kq.xepLoai}</span>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      {role === "gv" && (
        <div className="mt-6 bg-white border border-[#dde1ed] rounded p-5">
          <h2 className="font-semibold text-[#0f1523] mb-4 font-[family-name:var(--font-display)]">Nhập điểm phản biện</h2>
          <p className="text-sm text-[#64718a] mb-4">Bạn được phân công chấm điểm cho đề tài: <strong className="text-[#0f1523]">KLTN002 — Ứng dụng nhận diện khuôn mặt trong điểm danh</strong></p>
          <div className="grid grid-cols-2 gap-4 max-w-sm">
            <div>
              <label className="block text-xs font-semibold text-[#64718a] uppercase tracking-wider mb-1">Điểm nội dung</label>
              <input type="number" min="0" max="10" step="0.5" className="w-full border border-[#dde1ed] rounded px-3 py-2 text-sm focus:outline-none focus:border-[#1b2f6e]" placeholder="0.0 – 10.0" />
            </div>
            <div>
              <label className="block text-xs font-semibold text-[#64718a] uppercase tracking-wider mb-1">Điểm trình bày</label>
              <input type="number" min="0" max="10" step="0.5" className="w-full border border-[#dde1ed] rounded px-3 py-2 text-sm focus:outline-none focus:border-[#1b2f6e]" placeholder="0.0 – 10.0" />
            </div>
          </div>
          <button className="mt-4 bg-[#e8410a] text-white text-sm px-5 py-2 rounded hover:bg-[#c93508] transition-colors font-medium">
            Gửi điểm
          </button>
        </div>
      )}
    </div>
  );
}

function ThongBaoSection() {
  const [selected, setSelected] = useState<ThongBao | null>(null);

  return (
    <div>
      <h1 className="text-2xl font-bold font-[family-name:var(--font-display)] text-[#1b2f6e] mb-1">Thông báo</h1>
      <p className="text-sm text-[#64718a] mb-6">Thông báo chính thức từ Nhà trường và Khoa CNTT</p>

      <div className="grid gap-3">
        {mockThongBaos.map(tb => (
          <div key={tb.id} onClick={() => setSelected(tb)}
            className="bg-white border border-[#dde1ed] rounded p-4 hover:border-[#1b2f6e] transition-colors cursor-pointer">
            <div className="flex items-center gap-2 mb-2">
              <Pill text={tb.loai} color={tb.loai === "Khoa" ? "blue" : "violet"} />
              <span className="font-mono text-xs text-[#64718a]">{tb.ngay}</span>
            </div>
            <h3 className="font-semibold text-[#0f1523] text-sm">{tb.tieu}</h3>
            <p className="text-xs text-[#64718a] mt-1 line-clamp-2">{tb.noi}</p>
          </div>
        ))}
      </div>

      {selected && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4" onClick={() => setSelected(null)}>
          <div className="bg-white rounded-lg w-full max-w-lg p-6" onClick={e => e.stopPropagation()}>
            <div className="flex items-start justify-between mb-4">
              <div className="flex items-center gap-2">
                <Pill text={selected.loai} color={selected.loai === "Khoa" ? "blue" : "violet"} />
                <span className="font-mono text-xs text-[#64718a]">{selected.ngay}</span>
              </div>
              <button onClick={() => setSelected(null)} className="text-[#64718a] hover:text-[#0f1523] text-xl leading-none">&times;</button>
            </div>
            <h2 className="text-lg font-bold font-[family-name:var(--font-display)] text-[#1b2f6e] mb-3">{selected.tieu}</h2>
            <p className="text-sm text-[#0f1523] leading-relaxed">{selected.noi}</p>
          </div>
        </div>
      )}
    </div>
  );
}

// ─── Sidebar ──────────────────────────────────────────────────────────────────
const navItems = [
  { key: "dashboard", label: "Tổng quan", icon: "⊞" },
  { key: "dot", label: "Đợt đăng ký", icon: "📅" },
  { key: "detai", label: "Danh sách đề tài", icon: "📋" },
  { key: "nhom", label: "Nhóm sinh viên", icon: "👥" },
  { key: "hoidong", label: "Hội đồng PB", icon: "⚖️" },
  { key: "ketqua", label: "Kết quả & Điểm", icon: "🏆" },
  { key: "thongbao", label: "Thông báo", icon: "🔔" },
];

const roleLabels: Record<Role, string> = {
  admin: "Trưởng khoa",
  gv: "Giảng viên",
  sv: "Sinh viên",
};

const roleColors: Record<Role, string> = {
  admin: "bg-[#e8410a] text-white",
  gv: "bg-[#1b2f6e] text-white",
  sv: "bg-emerald-600 text-white",
};

// ─── App ──────────────────────────────────────────────────────────────────────
export default function App() {
  const [role, setRole] = useState<Role>("admin");
  const [page, setPage] = useState("dashboard");
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const visibleNav = navItems.filter(item => {
    if (role === "sv") return ["dashboard", "detai", "nhom", "ketqua", "thongbao"].includes(item.key);
    if (role === "gv") return ["dashboard", "detai", "nhom", "hoidong", "ketqua", "thongbao"].includes(item.key);
    return true;
  });

  return (
    <div className="min-h-screen flex bg-[#f4f6fa]" style={{ fontFamily: "var(--font-sans)" }}>
      {/* Sidebar */}
      <aside className={`fixed inset-y-0 left-0 z-40 w-64 bg-[#1b2f6e] flex flex-col transition-transform duration-200 ${sidebarOpen ? "translate-x-0" : "-translate-x-full"} lg:translate-x-0 lg:static lg:flex`}>
        {/* Logo */}
        <div className="px-5 py-5 border-b border-white/10">
          <div className="text-white font-bold text-base font-[family-name:var(--font-display)] leading-tight">
            ĐH Công nghệ Thông tin
          </div>
          <div className="text-blue-300 text-xs mt-0.5">Hệ thống Quản lý Đề tài</div>
        </div>

        {/* Role switcher */}
        <div className="px-4 py-3 border-b border-white/10">
          <div className="text-xs text-blue-300 font-semibold uppercase tracking-wider mb-2">Vai trò</div>
          <div className="flex flex-col gap-1">
            {(["admin", "gv", "sv"] as Role[]).map(r => (
              <button key={r} onClick={() => { setRole(r); setPage("dashboard"); }}
                className={`text-left text-xs px-3 py-1.5 rounded transition-colors font-medium ${role === r ? roleColors[r] : "text-blue-200 hover:text-white hover:bg-white/10"}`}>
                {roleLabels[r]}
              </button>
            ))}
          </div>
        </div>

        {/* Nav */}
        <nav className="flex-1 px-3 py-4 overflow-y-auto">
          {visibleNav.map(item => (
            <button key={item.key} onClick={() => { setPage(item.key); setSidebarOpen(false); }}
              className={`w-full text-left flex items-center gap-3 px-3 py-2.5 rounded text-sm transition-colors mb-0.5 ${page === item.key ? "bg-white/15 text-white font-semibold" : "text-blue-200 hover:text-white hover:bg-white/10"}`}>
              <span className="text-base leading-none">{item.icon}</span>
              {item.label}
            </button>
          ))}
        </nav>

        {/* User */}
        <div className="px-4 py-4 border-t border-white/10">
          <div className="flex items-center gap-3">
            <div className={`w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold ${roleColors[role]}`}>
              {role === "admin" ? "TK" : role === "gv" ? "GV" : "SV"}
            </div>
            <div>
              <div className="text-white text-xs font-semibold">
                {role === "admin" ? "Trưởng khoa" : role === "gv" ? "TS. Nguyễn Văn An" : "Nguyễn Hoàng Nam"}
              </div>
              <div className="text-blue-300 text-xs">{roleLabels[role]}</div>
            </div>
          </div>
        </div>
      </aside>

      {/* Backdrop */}
      {sidebarOpen && <div className="fixed inset-0 bg-black/40 z-30 lg:hidden" onClick={() => setSidebarOpen(false)} />}

      {/* Main */}
      <div className="flex-1 flex flex-col min-w-0">
        {/* Topbar */}
        <header className="bg-white border-b border-[#dde1ed] px-6 py-3 flex items-center justify-between sticky top-0 z-20">
          <button className="lg:hidden text-[#64718a] hover:text-[#0f1523] mr-3" onClick={() => setSidebarOpen(true)}>
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
            </svg>
          </button>
          <div className="flex items-center gap-2">
            <span className="text-xs text-[#64718a]">Năm học 2025-2026 · HK1</span>
            <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 inline-block animate-pulse" />
            <span className="text-xs text-emerald-600 font-medium">Đang mở SV đăng ký</span>
          </div>
          <div className="flex items-center gap-2">
            <span className={`text-xs px-2 py-1 rounded font-medium ${roleColors[role]}`}>{roleLabels[role]}</span>
          </div>
        </header>

        {/* Page content */}
        <main className="flex-1 p-6 overflow-y-auto">
          {page === "dashboard" && <Dashboard role={role} />}
          {page === "dot" && <DotDangKySection />}
          {page === "detai" && <DeTaiSection role={role} />}
          {page === "nhom" && <NhomSection role={role} />}
          {page === "hoidong" && <HoiDongSection />}
          {page === "ketqua" && <KetQuaSection role={role} />}
          {page === "thongbao" && <ThongBaoSection />}
        </main>
      </div>
    </div>
  );
}
