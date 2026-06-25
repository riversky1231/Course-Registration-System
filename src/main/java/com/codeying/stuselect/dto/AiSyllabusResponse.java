package com.codeying.stuselect.dto;

import java.util.List;

/** AI 课程简介/大纲生成结果。 */
public record AiSyllabusResponse(String jianjie, List<String> outline, boolean configured) {}
