import type { Digest } from '@/types/digest'

// Served by digest-service. Null until it has written its first digest.
export async function getLatestDigest(signal?: AbortSignal): Promise<Digest | null> {
  const response = await fetch('/api/digests/latest', { signal })
  if (response.status === 404) return null
  if (!response.ok) {
    throw new Error(`GET /api/digests/latest failed with HTTP ${response.status}`)
  }
  return response.json()
}
