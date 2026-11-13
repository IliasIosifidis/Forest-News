<script setup lang="ts">
const emit = defineEmits<{
  click: []
}>()
</script>

<template>
  <!-- Floats over the bottom-right corner of the page, below the top bar and the dialogs. -->
  <button
    type="button"
    aria-haspopup="dialog"
    class="fixed right-4 bottom-4 z-40 inline-flex items-center gap-2 rounded-full bg-forest-700 px-5 py-3 font-semibold text-white shadow-lg ring-1 ring-white/20 hover:bg-forest-800 sm:right-6 sm:bottom-6"
    @click="emit('click')"
  >
    <svg
      class="size-5"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      stroke-width="2"
      stroke-linecap="round"
      stroke-linejoin="round"
      aria-hidden="true"
    >
      <path d="M4 22h16a2 2 0 0 0 2-2V4a2 2 0 0 0-2-2H8a2 2 0 0 0-2 2v16a2 2 0 0 1-2 2Zm0 0a2 2 0 0 1-2-2v-9c0-1.1.9-2 2-2h2" />
      <path d="M18 14h-8M15 18h-5M10 6h8v4h-8z" />
    </svg>
    Daily digest
  </button>
</template>
