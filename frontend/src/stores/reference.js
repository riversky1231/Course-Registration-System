import { defineStore } from "pinia";
import { listModule } from "@/api";
import { ENDPOINTS } from "@/constants/modules";

// 缓存教师/学生/课程下拉引用数据，供课程/选课表单与筛选复用。
export const useReferenceStore = defineStore("reference", {
  state: () => ({
    teachers: [],
    students: [],
    courses: [],
    loaded: false,
  }),
  actions: {
    async load(force = false) {
      if (this.loaded && !force) return;
      const safe = (p) => p.catch(() => ({ items: [] }));
      const [teachers, students, courses] = await Promise.all([
        safe(listModule(ENDPOINTS.teachers, { page: 1, pageSize: 200 })),
        safe(listModule(ENDPOINTS.students, { page: 1, pageSize: 200 })),
        safe(listModule(ENDPOINTS.courses, { page: 1, pageSize: 200 })),
      ]);
      this.teachers = teachers?.items || [];
      this.students = students?.items || [];
      this.courses = courses?.items || [];
      this.loaded = true;
    },
  },
});
