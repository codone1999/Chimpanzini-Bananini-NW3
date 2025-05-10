<script setup>

</script>

<template>
  <!-- Hero Section -->
  <main class="min-h-screen flex flex-col justify-center items-center text-center bg-neutral-900 pt-24 pb-32 px-4">
    <div class="max-w-2xl">
      <h1 class="text-4xl md:text-5xl font-extrabold text-white mb-4 tracking-tight">
        Welcome to <span class="text-purple-500">ITB MShop</span>
      </h1>
      <p class="text-neutral-400 text-lg mb-10">
        Discover the latest trends in tech and fashion — at unbeatable prices.
      </p>

      <!-- Shop Now button (bottom center on mobile) -->
      <div class="fixed bottom-6 inset-x-0 flex justify-center md:static">
        <router-link
          :to="{ name: 'ListGallery' }"
          class="bg-purple-600 hover:bg-purple-700 text-white px-8 py-3 rounded-full text-lg font-semibold shadow-lg transition-all duration-200 ease-in-out"
        >
          Shop Now
        </router-link>
      </div>
    </div>
  </main>
</template>
