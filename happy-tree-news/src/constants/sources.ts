export interface SourceTheme {
  /** Card background and outline. */
  card: string
  /** Source and date label. */
  accent: string
}

export interface NewsSource {
  id: string
  label: string
  theme: SourceTheme
}

// `id` must match the `source` value the API stores for each feed.
// Theme classes are written out in full so Tailwind can find them; the colors are defined in main.css.
export const SOURCES: NewsSource[] = [
  {
    id: 'hackernews',
    label: 'Hacker News',
    theme: {
      card: 'bg-hackernews-surface ring-hackernews-border',
      accent: 'text-hackernews-accent',
    },
  },
  {
    id: 'guardian',
    label: 'Guardian',
    theme: {
      card: 'bg-guardian-surface ring-guardian-border',
      accent: 'text-guardian-accent',
    },
  },
  {
    id: 'currents',
    label: 'Current News',
    theme: {
      card: 'bg-currents-surface ring-currents-border',
      accent: 'text-currents-accent',
    },
  },
  {
    id: 'freenews',
    label: 'Free News',
    theme: {
      card: 'bg-free-surface ring-free-border',
      accent: 'text-free-accent',
    },
  }
]

const DEFAULT_THEME: SourceTheme = {
  card: 'bg-white ring-black/5',
  accent: 'text-forest-700',
}

function findSource(id: string): NewsSource | undefined {
  return SOURCES.find((source) => source.id === id)
}

export function sourceLabel(id: string): string {
  return findSource(id)?.label ?? id
}

export function sourceTheme(id: string): SourceTheme {
  return findSource(id)?.theme ?? DEFAULT_THEME
}
