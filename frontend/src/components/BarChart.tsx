/**
 * Проста горизонтальна стовпчикова діаграма на SVG (без сторонніх бібліотек).
 */
export interface BarDatum {
  label: string;
  value: number;
}

export function BarChart({ data, max, unit = '', height = 26, threshold }: {
  data: BarDatum[];
  max?: number;
  unit?: string;
  height?: number;
  /** Позначка порогового значення (напр., 60 балів). */
  threshold?: number;
}) {
  if (data.length === 0) return null;
  const top = max ?? Math.max(...data.map((d) => d.value), 1);
  const labelWidth = 150;
  const width = 640;
  const barArea = width - labelWidth - 60;
  const rowH = height + 8;
  const svgHeight = data.length * rowH + 8;
  return (
    <svg className="bar-chart" viewBox={`0 0 ${width} ${svgHeight}`} role="img" aria-label="Діаграма">
      {threshold !== undefined && (
        <line
          x1={labelWidth + (threshold / top) * barArea}
          x2={labelWidth + (threshold / top) * barArea}
          y1={0}
          y2={svgHeight}
          className="bar-threshold"
        />
      )}
      {data.map((d, i) => {
        const w = Math.max(2, (d.value / top) * barArea);
        const y = i * rowH + 4;
        const tone = threshold !== undefined && d.value < threshold ? 'bar bar-low' : 'bar';
        return (
          <g key={d.label + i}>
            <text x={labelWidth - 8} y={y + height / 2 + 5} textAnchor="end" className="bar-label">
              {d.label.length > 20 ? `${d.label.slice(0, 19)}…` : d.label}
            </text>
            <rect x={labelWidth} y={y} width={w} height={height} rx={4} className={tone} />
            <text x={labelWidth + w + 6} y={y + height / 2 + 5} className="bar-value">
              {Number.isInteger(d.value) ? d.value : d.value.toFixed(1)}
              {unit}
            </text>
          </g>
        );
      })}
    </svg>
  );
}
