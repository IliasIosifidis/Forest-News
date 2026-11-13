import { ref } from 'vue'
import { getLatestDigest } from '@/api/digest'
import type { Digest } from '@/types/digest'

// The latest daily digest and whether its dialog is open. It is fetched again each time the dialog
// opens, so a page left open overnight picks up the new one.
export function useDigest() {
  const isOpen = ref(false)
  /** Null until the first response arrives, and while digest-service has not written one yet. */
  const digest = ref<Digest | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)
  // Only the latest request may update state; a new one, or closing the dialog, aborts the one in flight.
  let controller: AbortController | null = null

  async function load() {
    controller?.abort()
    const current = new AbortController()
    controller = current
    loading.value = true
    error.value = null

    try {
      digest.value = await getLatestDigest(current.signal)
    } catch (e) {
      if (!current.signal.aborted) {
        console.error(e)
        error.value = 'Could not load the digest. Please try again.'
      }
    } finally {
      if (!current.signal.aborted) loading.value = false
    }
  }

  function open() {
    isOpen.value = true
    load()
  }

  function close() {
    isOpen.value = false
    controller?.abort()
  }

  return { isOpen, digest, loading, error, open, close, retry: load }
}
