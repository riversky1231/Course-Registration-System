// AI 回复渲染：轻量、安全的 Markdown 子集，避免页面加载依赖外部包失败。
// 支持标题、列表、加粗、行内代码、链接和普通段落；所有输入先转义 HTML。

function escapeHtml(value) {
  return String(value)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

function safeHref(value) {
  const href = String(value || "").trim();
  if (/^https?:\/\//i.test(href) || /^mailto:/i.test(href)) {
    return escapeHtml(href);
  }
  return "#";
}

function renderInline(value) {
  let html = escapeHtml(value);

  html = html.replace(/`([^`]+)`/g, "<code>$1</code>");
  html = html.replace(/\*\*([^*]+)\*\*/g, "<strong>$1</strong>");
  html = html.replace(/\[([^\]]+)\]\(([^)\s]+)\)/g, (_match, text, href) => {
    return `<a href="${safeHref(href)}" target="_blank" rel="noopener noreferrer">${text}</a>`;
  });

  return html;
}

function closeList(state, output) {
  if (!state.inList) {
    return;
  }
  output.push("</ul>");
  state.inList = false;
}

function closeParagraph(state, output) {
  if (!state.paragraph.length) {
    return;
  }
  output.push(`<p>${state.paragraph.map(renderInline).join("<br>")}</p>`);
  state.paragraph = [];
}

export function renderMarkdown(text) {
  if (!text) {
    return "";
  }

  const lines = String(text).replace(/\r\n/g, "\n").split("\n");
  const output = [];
  const state = { inList: false, paragraph: [] };

  for (const rawLine of lines) {
    const line = rawLine.trimEnd();

    if (!line.trim()) {
      closeParagraph(state, output);
      closeList(state, output);
      continue;
    }

    const heading = /^(#{1,4})\s+(.+)$/.exec(line);
    if (heading) {
      closeParagraph(state, output);
      closeList(state, output);
      const level = heading[1].length;
      output.push(`<h${level}>${renderInline(heading[2])}</h${level}>`);
      continue;
    }

    const bullet = /^\s*[-*]\s+(.+)$/.exec(line);
    if (bullet) {
      closeParagraph(state, output);
      if (!state.inList) {
        output.push("<ul>");
        state.inList = true;
      }
      output.push(`<li>${renderInline(bullet[1])}</li>`);
      continue;
    }

    closeList(state, output);
    state.paragraph.push(line.trim());
  }

  closeParagraph(state, output);
  closeList(state, output);
  return output.join("");
}
