import { useEffect, useState } from 'react';

export interface Option {
  value: string;
  label: string;
}

export interface FieldDef {
  name: string;
  label: string;
  type?: 'text' | 'number' | 'email' | 'date' | 'select' | 'tel';
  required?: boolean;
  placeholder?: string;
  /** Для select: статичний список або асинхронний завантажувач. */
  options?: Option[] | (() => Promise<Option[]>);
  step?: string;
  /** Поле на всю ширину форми. */
  wide?: boolean;
}

export type FormValues = Record<string, string>;

function useOptions(source: FieldDef['options']): Option[] {
  const [options, setOptions] = useState<Option[]>(Array.isArray(source) ? source : []);
  useEffect(() => {
    if (typeof source === 'function') {
      source().then(setOptions).catch(() => setOptions([]));
    } else if (source) {
      setOptions(source);
    }
  }, [source]);
  return options;
}

function Field({ def, value, error, onChange }: {
  def: FieldDef;
  value: string;
  error?: string;
  onChange: (value: string) => void;
}) {
  const options = useOptions(def.options);
  return (
    <label className={`field ${def.wide ? 'field-wide' : ''}`}>
      <span>
        {def.label}
        {def.required && <em className="required">*</em>}
      </span>
      {def.type === 'select' ? (
        <select value={value} onChange={(e) => onChange(e.target.value)} required={def.required}>
          <option value="">— оберіть —</option>
          {options.map((o) => (
            <option key={o.value} value={o.value}>
              {o.label}
            </option>
          ))}
        </select>
      ) : (
        <input
          type={def.type ?? 'text'}
          value={value}
          step={def.step}
          placeholder={def.placeholder}
          required={def.required}
          onChange={(e) => onChange(e.target.value)}
        />
      )}
      {error && <span className="field-error">{error}</span>}
    </label>
  );
}

/** Набір полів форми з помилками валідації від сервера (поле → повідомлення). */
export function FormFields({ fields, values, errors, onChange }: {
  fields: FieldDef[];
  values: FormValues;
  errors: Record<string, string>;
  onChange: (name: string, value: string) => void;
}) {
  return (
    <div className="form-grid">
      {fields.map((f) => (
        <Field key={f.name} def={f} value={values[f.name] ?? ''} error={errors[f.name]} onChange={(v) => onChange(f.name, v)} />
      ))}
    </div>
  );
}

/** Перетворення значення форми: порожній рядок → null, числові поля → number. */
export function num(value: string | undefined): number | null {
  if (value === undefined || value.trim() === '') return null;
  const n = Number(value.replace(',', '.'));
  return Number.isNaN(n) ? null : n;
}

export function str(value: string | undefined): string | null {
  return value === undefined || value.trim() === '' ? null : value.trim();
}
