import MarkdownIt from "markdown-it";

// AI 回复渲染：开启自动换行与链接识别；默认 html:false，会转义内嵌 HTML，避免 XSS。
const md = new MarkdownIt({
  html: false,
  linkify: true,
  breaks: true,
});

// 让所有链接在新标签页打开。
const defaultLinkRender =
  md.renderer.rules.link_open ||
  ((tokens, idx, options, env, self) => self.renderToken(tokens, idx, options));
md.renderer.rules.link_open = (tokens, idx, options, env, self) => {
  tokens[idx].attrSet("target", "_blank");
  tokens[idx].attrSet("rel", "noopener noreferrer");
  return defaultLinkRender(tokens, idx, options, env, self);
};

export function renderMarkdown(text) {
  if (!text) return "";
  return md.render(text);
}
