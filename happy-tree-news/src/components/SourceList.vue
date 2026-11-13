<script setup lang="ts">
import { computed } from 'vue'
import type { NewsSource } from '@/constants/sources'

const props = defineProps<{
  sources: NewsSource[]
  selected: string | null
}>()

const emit = defineEmits<{
  select: [source: string | null]
}>()

const options = computed(() => [{ id: null, label: 'All' }, ...props.sources])
</script>

<template>
  <nav aria-label="News sources" class="flex overflow-scroll justify-end gap-2">
    <button
      v-for="option in options"
      :key="option.id ?? 'all'"
      type="button"
      :aria-pressed="option.id === selected"
      class="rounded-full px-4 py-1.5 text-sm font-semibold shadow-sm transition-colors"
      :class="
        option.id === selected
          ? 'bg-forest-700 text-white'
          : 'bg-white/90 text-slate-700 hover:bg-white hover:text-forest-700'
      "
      @click="emit('select', option.id)"
    >
      {{ option.label }}
    </button>
  </nav>
</template>
