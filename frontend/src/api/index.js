import request from "./request";
import { buildQuery } from "@/utils/format";

export const authApi = {
  status: () => request.get("/api/auth/status"),
  me: () => request.get("/api/auth/me"),
  login: (data) => request.post("/api/auth/login", data),
  register: (data) => request.post("/api/auth/register", data),
  logout: () => request.post("/api/auth/logout"),
};

export const dashboardApi = {
  summary: () => request.get("/api/dashboard/summary"),
  insights: () => request.get("/api/dashboard/insights"),
};

export const profileApi = {
  get: () => request.get("/api/profile"),
  update: (data) => request.put("/api/profile", data),
};

export const gradeApi = {
  report: (params) => request.get(`/api/student-grade-report${buildQuery(params)}`),
};

export const aiApi = {
  chat: (data) => request.post("/api/ai/chat", data),
  recommend: () => request.get("/api/ai/recommend"),
  syllabus: (data) => request.post("/api/ai/syllabus", data),
};

// 通用模块 CRUD。
export function listModule(endpoint, params) {
  return request.get(`${endpoint}${buildQuery(params)}`);
}

export function createModule(endpoint, data) {
  return request.post(endpoint, data);
}

export function updateModule(endpoint, id, data) {
  return request.put(`${endpoint}/${id}`, data);
}

export function removeModule(endpoint, id) {
  return request.delete(`${endpoint}/${id}`);
}

// 获取指定课程的选课学生列表。
export function getCourseStudents(courseId) {
  return request.get(`/api/selections/course/${courseId}/students`);
}
