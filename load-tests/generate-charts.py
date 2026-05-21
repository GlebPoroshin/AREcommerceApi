#!/usr/bin/env python3
import html
import json
import os
from pathlib import Path


ROOT = Path(__file__).resolve().parent
CHARTS_DIR = ROOT / "charts"


def load_metrics(filename):
    with (ROOT / filename).open("r", encoding="utf-8") as file:
        return json.load(file)["metrics"]


def fmt_ms(value):
    if value >= 1000:
        return f"{value / 1000:.2f} s"
    return f"{value:.0f} ms"


def write_svg(filename, content):
    CHARTS_DIR.mkdir(exist_ok=True)
    (CHARTS_DIR / filename).write_text(content, encoding="utf-8")


def line_chart(title, subtitle, points, filename, slo_ms=None):
    width, height = 920, 560
    left, right, top, bottom = 86, 42, 84, 78
    plot_w = width - left - right
    plot_h = height - top - bottom
    max_y = max(value for _, value in points)
    if slo_ms is not None:
        max_y = max(max_y, slo_ms)
    max_y = ((int(max_y / 500) + 1) * 500)
    min_x, max_x = points[0][0], points[-1][0]

    def x_scale(x):
        return left + (x - min_x) / (max_x - min_x) * plot_w

    def y_scale(y):
        return top + plot_h - (y / max_y) * plot_h

    y_ticks = [0, max_y * 0.25, max_y * 0.5, max_y * 0.75, max_y]
    polyline = " ".join(f"{x_scale(x):.1f},{y_scale(y):.1f}" for x, y in points)

    lines = [
        f'<svg xmlns="http://www.w3.org/2000/svg" width="{width}" height="{height}" viewBox="0 0 {width} {height}">',
        '<rect width="100%" height="100%" fill="#ffffff"/>',
        f'<text x="{left}" y="38" font-family="Arial, sans-serif" font-size="24" font-weight="700" fill="#172033">{html.escape(title)}</text>',
        f'<text x="{left}" y="64" font-family="Arial, sans-serif" font-size="14" fill="#526070">{html.escape(subtitle)}</text>',
    ]

    for tick in y_ticks:
        y = y_scale(tick)
        lines.append(f'<line x1="{left}" y1="{y:.1f}" x2="{width - right}" y2="{y:.1f}" stroke="#e6eaf0" stroke-width="1"/>')
        lines.append(f'<text x="{left - 12}" y="{y + 4:.1f}" font-family="Arial, sans-serif" font-size="12" text-anchor="end" fill="#526070">{fmt_ms(tick)}</text>')

    for x, _ in points:
        sx = x_scale(x)
        lines.append(f'<line x1="{sx:.1f}" y1="{top}" x2="{sx:.1f}" y2="{top + plot_h}" stroke="#f0f3f7" stroke-width="1"/>')
        lines.append(f'<text x="{sx:.1f}" y="{height - 34}" font-family="Arial, sans-serif" font-size="12" text-anchor="middle" fill="#526070">{x}</text>')

    lines.append(f'<line x1="{left}" y1="{top + plot_h}" x2="{width - right}" y2="{top + plot_h}" stroke="#9aa6b2" stroke-width="1.2"/>')
    lines.append(f'<line x1="{left}" y1="{top}" x2="{left}" y2="{top + plot_h}" stroke="#9aa6b2" stroke-width="1.2"/>')

    if slo_ms is not None:
        y = y_scale(slo_ms)
        lines.append(f'<line x1="{left}" y1="{y:.1f}" x2="{width - right}" y2="{y:.1f}" stroke="#d94841" stroke-width="1.6" stroke-dasharray="7 6"/>')
        lines.append(f'<text x="{width - right}" y="{y - 8:.1f}" font-family="Arial, sans-serif" font-size="12" text-anchor="end" fill="#b72f2a">SLO {fmt_ms(slo_ms)}</text>')

    lines.append(f'<polyline points="{polyline}" fill="none" stroke="#2266cc" stroke-width="3.5" stroke-linejoin="round" stroke-linecap="round"/>')
    for x, value in points:
        sx, sy = x_scale(x), y_scale(value)
        lines.append(f'<circle cx="{sx:.1f}" cy="{sy:.1f}" r="5.5" fill="#2266cc" stroke="#ffffff" stroke-width="2"/>')
        lines.append(f'<text x="{sx:.1f}" y="{sy - 12:.1f}" font-family="Arial, sans-serif" font-size="12" text-anchor="middle" fill="#172033">{fmt_ms(value)}</text>')

    lines.append(f'<text x="{left + plot_w / 2:.1f}" y="{height - 10}" font-family="Arial, sans-serif" font-size="13" text-anchor="middle" fill="#526070">Количество виртуальных пользователей, VUs</text>')
    lines.append(f'<text x="22" y="{top + plot_h / 2:.1f}" transform="rotate(-90 22 {top + plot_h / 2:.1f})" font-family="Arial, sans-serif" font-size="13" text-anchor="middle" fill="#526070">p95 времени ответа</text>')
    lines.append("</svg>")
    write_svg(filename, "\n".join(lines))


def bar_chart(title, subtitle, bars, filename):
    width, height = 920, 560
    left, right, top, bottom = 92, 38, 84, 106
    plot_w = width - left - right
    plot_h = height - top - bottom
    max_y = ((int(max(value for _, value in bars) / 10) + 1) * 10)
    bar_gap = 22
    bar_w = (plot_w - bar_gap * (len(bars) - 1)) / len(bars)
    colors = ["#3b73d9", "#2f9e8f", "#7b61d1", "#d97706", "#64748b"]

    def y_scale(y):
        return top + plot_h - (y / max_y) * plot_h

    y_ticks = [0, max_y * 0.25, max_y * 0.5, max_y * 0.75, max_y]
    lines = [
        f'<svg xmlns="http://www.w3.org/2000/svg" width="{width}" height="{height}" viewBox="0 0 {width} {height}">',
        '<rect width="100%" height="100%" fill="#ffffff"/>',
        f'<text x="{left}" y="38" font-family="Arial, sans-serif" font-size="24" font-weight="700" fill="#172033">{html.escape(title)}</text>',
        f'<text x="{left}" y="64" font-family="Arial, sans-serif" font-size="14" fill="#526070">{html.escape(subtitle)}</text>',
    ]

    for tick in y_ticks:
        y = y_scale(tick)
        lines.append(f'<line x1="{left}" y1="{y:.1f}" x2="{width - right}" y2="{y:.1f}" stroke="#e6eaf0" stroke-width="1"/>')
        lines.append(f'<text x="{left - 12}" y="{y + 4:.1f}" font-family="Arial, sans-serif" font-size="12" text-anchor="end" fill="#526070">{tick:.0f} ms</text>')

    for idx, (label, value) in enumerate(bars):
        x = left + idx * (bar_w + bar_gap)
        y = y_scale(value)
        h = top + plot_h - y
        color = colors[idx % len(colors)]
        lines.append(f'<rect x="{x:.1f}" y="{y:.1f}" width="{bar_w:.1f}" height="{h:.1f}" rx="3" fill="{color}"/>')
        lines.append(f'<text x="{x + bar_w / 2:.1f}" y="{y - 10:.1f}" font-family="Arial, sans-serif" font-size="12" text-anchor="middle" fill="#172033">{value:.1f} ms</text>')
        lines.append(f'<text x="{x + bar_w / 2:.1f}" y="{height - 58}" font-family="Arial, sans-serif" font-size="12" text-anchor="middle" fill="#526070">{html.escape(label)}</text>')

    lines.append(f'<line x1="{left}" y1="{top + plot_h}" x2="{width - right}" y2="{top + plot_h}" stroke="#9aa6b2" stroke-width="1.2"/>')
    lines.append(f'<line x1="{left}" y1="{top}" x2="{left}" y2="{top + plot_h}" stroke="#9aa6b2" stroke-width="1.2"/>')
    lines.append(f'<text x="{left + plot_w / 2:.1f}" y="{height - 20}" font-family="Arial, sans-serif" font-size="13" text-anchor="middle" fill="#526070">Эндпоинты основного load-test</text>')
    lines.append(f'<text x="22" y="{top + plot_h / 2:.1f}" transform="rotate(-90 22 {top + plot_h / 2:.1f})" font-family="Arial, sans-serif" font-size="13" text-anchor="middle" fill="#526070">p95, мс</text>')
    lines.append("</svg>")
    write_svg(filename, "\n".join(lines))


def main():
    capacity = load_metrics("capacity-summary.json")
    load = load_metrics("summary.json")

    points = []
    for vus in [100, 200, 300, 400, 600]:
        metric = capacity[f"http_req_duration{{scenario:vus_{vus}}}"]
        points.append((vus, metric["p(95)"]))

    endpoint_bars = [
        ("createUserId", load["lat_create_user"]["p(95)"]),
        ("GET /plp", load["lat_plp"]["p(95)"]),
        ("GET /pdp", load["lat_pdp"]["p(95)"]),
        ("POST /basket", load["lat_basket_add"]["p(95)"]),
        ("GET /basket", load["lat_basket_get"]["p(95)"]),
    ]

    line_chart(
        "Зависимость p95 latency от нагрузки",
        "Capacity-прогон: последовательные уровни 100, 200, 300, 400 и 600 VUs",
        points,
        "latency-vus.svg",
        slo_ms=500,
    )
    bar_chart(
        "p95 latency по эндпоинтам",
        "Основной load-test: ramping 0 → 100 VUs, полный покупательский сценарий",
        endpoint_bars,
        "endpoint-p95.svg",
    )

    data = {
        "latencyByVus": [{"vus": vus, "p95Ms": round(value, 3)} for vus, value in points],
        "endpointP95": [{"endpoint": name, "p95Ms": round(value, 3)} for name, value in endpoint_bars],
    }
    (CHARTS_DIR / "chart-data.json").write_text(json.dumps(data, ensure_ascii=False, indent=2), encoding="utf-8")


if __name__ == "__main__":
    main()
