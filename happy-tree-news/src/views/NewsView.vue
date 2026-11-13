<script setup lang="ts">
import { reactive } from 'vue'
import TopBar from '@/components/TopBar.vue'
import PageLink from '@/components/PageLink.vue'
import SearchBar from '@/components/SearchBar.vue'
import SourceList from '@/components/SourceList.vue'
import ArticleGrid from '@/components/ArticleGrid.vue'
import FeedStatus from '@/components/FeedStatus.vue'
import ArticleModal from '@/components/ArticleModal.vue'
import DigestButton from '@/components/DigestButton.vue'
import DigestModal from '@/components/DigestModal.vue'
import { SOURCES } from '@/constants/sources'
import { useArticlesStore } from '@/stores/articles'
import { useDigest } from '@/composables/useDigest'

const store = useArticlesStore()
const digest = reactive(useDigest())

store.loadArticles()
</script>

<template>
  <TopBar>
    <template #start>
      <span class="hidden text-4xl font-bold tracking-tight text-forest-800 sm:block">
        FOREST NEWS
      </span>
    </template>

    <SearchBar :source="store.selectedSource" @search="store.search" @select="store.openArticle" />

    <template #end>
      <PageLink to="/paths">FOREST PATHS <span aria-hidden="true">&gt;&gt;</span></PageLink>
    </template>
  </TopBar>

  <!-- The extra bottom padding lets the end of the feed scroll clear of the digest button. -->
  <main class="mx-auto max-w-7xl px-4 pt-6 pb-24">
    <SourceList :sources="SOURCES" :selected="store.selectedSource" @select="store.selectSource" />
    <ArticleGrid class="mt-6" :articles="store.articles" @read-more="store.openArticle" />
    <FeedStatus
      :loading="store.loading"
      :error="store.error"
      :is-empty="store.isEmpty"
      :has-more="store.hasMore"
      @retry="store.retry"
      @load-more="store.loadMore"
    />
  </main>

  <ArticleModal :article="store.activeArticle" @close="store.closeArticle" />

  <DigestButton @click="digest.open" />
  <DigestModal
    :open="digest.isOpen"
    :digest="digest.digest"
    :loading="digest.loading"
    :error="digest.error"
    @close="digest.close"
    @retry="digest.retry"
  />
</template>
