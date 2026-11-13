<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, useTemplateRef, watch } from 'vue'
import type { Digest } from '@/types/digest'
import { formatDate, formatDay } from '@/utils/format'

const props = defineProps<{
  open: boolean
  /** Null until loaded, and while digest-service has not written one yet. */
  digest: Digest | null
  loading: boolean
  error: string | null
}>()

const emit = defineEmits<{
  close: []
  retry: []
}>()

// The model writes one story per line, but small models sometimes number or bullet them anyway.
const stories = computed(() =>
  (props.digest?.content ?? '')
    .split('\n')
    .map((line) => line.replace(/^\s*(?:[-*•]|\d+[.)])\s*/, '').trim())
    .filter((line) => line !== ''),
)

function onKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape' && props.open) emit('close')
}

onMounted(() => window.addEventListener('keydown', onKeydown))
onBeforeUnmount(() => window.removeEventListener('keydown', onKeydown))

const dialog = useTemplateRef<HTMLElement>('dialog')
let opener: HTMLElement | null = null

watch(
  () => props.open,
  async (open) => {
    // Keep the page behind the dialog from scrolling while it is open.
    document.body.classList.toggle('overflow-hidden', open)

    // Move focus into the dialog for keyboard and screen reader users, and hand it back on close.
    if (open) {
      opener = document.activeElement instanceof HTMLElement ? document.activeElement : null
      await nextTick()
      dialog.value?.focus()
    } else {
      opener?.focus()
      opener = null
    }
  },
)
</script>

<template>
  <Teleport to="body">
    <Transition
      enter-active-class="transition-opacity duration-200"
      leave-active-class="transition-opacity duration-200"
      enter-from-class="opacity-0"
      leave-to-class="opacity-0"
    >
      <div
        v-if="open"
        class="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4"
        @click.self="emit('close')"
      >
        <div
          ref="dialog"
          role="dialog"
          aria-modal="true"
          aria-labelledby="digest-modal-title"
          tabindex="-1"
          class="max-h-[85vh] w-full max-w-2xl overflow-y-auto rounded-2xl bg-white p-6 shadow-2xl focus:outline-none sm:p-8"
        >
          <div class="flex items-start justify-between gap-4">
            <p class="text-xs font-semibold tracking-wide text-forest-700 uppercase">
              Daily digest<template v-if="digest"> · {{ formatDay(digest.day) }}</template>
            </p>
            <button
              type="button"
              aria-label="Close"
              class="-mt-2 -mr-2 rounded-full p-2 text-slate-500 hover:bg-slate-100 hover:text-slate-900"
              @click="emit('close')"
            >
              <svg
                class="size-5"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                aria-hidden="true"
              >
                <path d="M6 6l12 12M18 6 6 18" />
              </svg>
            </button>
          </div>
          <h2 id="digest-modal-title" class="mt-2 text-2xl leading-tight font-bold text-slate-900">
            The day in brief
          </h2>

          <div v-if="error" class="mt-4">
            <p class="text-red-600">{{ error }}</p>
            <button
              type="button"
              class="mt-2 font-semibold text-forest-700 hover:text-forest-900 hover:underline"
              @click="emit('retry')"
            >
              Try again
            </button>
          </div>
          <!-- When the dialog is opened again, the digest loaded last time stays up while it is refreshed. -->
          <template v-else-if="digest">
            <ul class="mt-4 space-y-3">
              <li
                v-for="(story, index) in stories"
                :key="index"
                class="border-l-2 border-forest-100 pl-4 leading-relaxed text-slate-700"
              >
                {{ story }}
              </li>
            </ul>
            <p class="mt-6 text-sm text-slate-500">
              Written by AI from the day's articles on {{ formatDate(digest.createdAt) }}.
            </p>
          </template>
          <p v-else-if="loading" class="mt-4 text-slate-500">Loading the digest…</p>
          <p v-else class="mt-4 text-slate-400 italic">
            No digest yet. A new one is written every evening.
          </p>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>
