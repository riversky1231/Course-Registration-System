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

function readCookie(name) {
  const match = document.cookie.match(new RegExp("(^|;\\s*)" + name + "=([^;]+)"));
  return match ? decodeURIComponent(match[2]) : null;
}

function parseSseEvent(raw) {
  let event = "message";
  const dataLines = [];
  for (const line of raw.split("\n")) {
    if (line.startsWith("event:")) event = line.slice(6).trim();
    else if (line.startsWith("data:")) dataLines.push(line.slice(5).replace(/^ /, ""));
  }
  if (!dataLines.length) return null;
  return { event, data: dataLines.join("\n") };
}

// 选课助手「流式」对话：用 fetch 读取 SSE，逐块回调增量内容。
// onDelta(token) 每收到一段增量触发；返回 { configured } 表示是否已配置真实密钥。
export async function streamAiChat(data, { onDelta, signal } = {}) {
  const headers = { "Content-Type": "application/json" };
  const csrf = readCookie("XSRF-TOKEN");
  if (csrf) headers["X-XSRF-TOKEN"] = csrf;

  const response = await fetch("/api/ai/chat/stream", {
    method: "POST",
    headers,
    credentials: "include",
    body: JSON.stringify(data),
    signal,
  });

  if (!response.ok || !response.body) {
    let message = `请求失败(${response.status})`;
    try {
      const payload = await response.json();
      if (payload && payload.message) message = payload.message;
    } catch (error) {
      /* 忽略解析失败，沿用默认提示 */
    }
    throw new Error(message);
  }

  const reader = response.body.getReader();
  const decoder = new TextDecoder("utf-8");
  let buffer = "";
  let configured = true;

  for (;;) {
    const { value, done } = await reader.read();
    if (done) break;
    buffer += decoder.decode(value, { stream: true });

    let boundary;
    while ((boundary = buffer.indexOf("\n\n")) >= 0) {
      const rawEvent = buffer.slice(0, boundary);
      buffer = buffer.slice(boundary + 2);
      const evt = parseSseEvent(rawEvent);
      if (!evt) continue;

      if (evt.event === "delta") {
        const payload = JSON.parse(evt.data);
        if (payload.content) onDelta?.(payload.content);
      } else if (evt.event === "done") {
        configured = JSON.parse(evt.data).configured;
      } else if (evt.event === "error") {
        throw new Error(JSON.parse(evt.data).message || "AI 流式调用失败");
      }
    }
  }

  return { configured };
}

export const evaluationApi = {
  list: (params) => request.get(`/api/evaluations${buildQuery(params)}`),
  summary: (courseId) => request.get(`/api/evaluations/summary${buildQuery({ courseId })}`),
  create: (data) => request.post("/api/evaluations", data),
  remove: (id) => request.delete(`/api/evaluations/${id}`),
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
