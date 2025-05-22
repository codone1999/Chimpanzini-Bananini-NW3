<script setup>

</script>

<template>
  <!-- Hero Section -->
  <main class="relative min-h-screen flex flex-col justify-center items-center text-center bg-gradient-to-br from-neutral-900 via-zinc-900 to-black overflow-hidden pt-24 pb-32 px-4">
    
    <!-- Decorative particles or blurred elements -->
    <div class="absolute inset-0 pointer-events-none">
      <div class="absolute w-75 h-75 bg-purple-700 rounded-full opacity-30 blur-3xl top-10 left-1/3 animate-pulse"></div>
      <div class="absolute w-55 h-55 bg-amber-500 rounded-full opacity-20 blur-3xl bottom-50 right-35 animate-pulse"></div>
      <div class="absolute w-30 h-30 bg-gray-300 rounded-full opacity-20 blur-3xl bottom-50 left-50 animate-pulse"></div>
    </div>
    
    <div class="z-10 max-w-2xl">
      <h1 class="text-5xl md:text-6xl font-extrabold text-white mb-6 tracking-tight leading-tight animate-fade-in">
        Welcome to <span class="text-purple-500 drop-shadow">ITB MShop</span>
      </h1>
      <p class="text-neutral-400 text-lg md:text-xl mb-12 animate-fade-in delay-100">
        Discover the latest trends in tech and fashion — at unbeatable prices.
      </p>

      <!-- Shop Now button (bottom center on mobile) -->
      <div class="fixed bottom-6 inset-x-0 flex justify-around px-4 md:static md:justify-center md:gap-8">
        <router-link
          :to="{ name: 'ListGallery' }"
          class="itbms-shopnow bg-purple-600 hover:bg-purple-700 text-white px-6 py-3 rounded-2xl text-lg font-semibold shadow-xl transition-all duration-200 ease-in-out animate-fade-up"
        >
          Shop Now
        </router-link>

        <router-link
          :to="{ name: 'ListSaleItem' }"
          class="itbms-seller bg-amber-600 hover:bg-amber-700 text-white px-7 py-3 rounded-2xl text-lg font-semibold shadow-xl transition-all duration-200 ease-in-out animate-fade-up delay-150"
        >
          Seller
        </router-link>
      </div>
    </div>

    <section class="z-10 mt-32 max-w-6xl w-full text-left">
      <h2 class="text-3xl md:text-4xl font-bold text-white mb-12 text-center animate-fade-in">Why Shop with ITB MShop?</h2>
      <div class="grid grid-cols-1 md:grid-cols-3 gap-10 animate-fade-up delay-150">
        
        <!-- Feature 1 -->
        <div class="bg-neutral-800 rounded-2xl p-6 shadow-lg hover:shadow-purple-800/30 transition">
          <h3 class="text-xl font-semibold text-white mb-2">Latest Smartphones</h3>
          <p class="text-neutral-400 text-sm">
            From flagship models to budget-friendly phones — explore the newest devices from top brands.
          </p>
        </div>
        
        <!-- Feature 2 -->
        <div class="bg-neutral-800 rounded-2xl p-6 shadow-lg hover:shadow-amber-700/30 transition">
          <h3 class="text-xl font-semibold text-white mb-2">Unbeatable Prices</h3>
          <p class="text-neutral-400 text-sm">
            Get your dream phone at the best deals — no hidden fees, just value.
          </p>
        </div>
        
        <!-- Feature 3 -->
        <div class="bg-neutral-800 rounded-2xl p-6 shadow-lg hover:shadow-gray-700/30 transition">
          <h3 class="text-xl font-semibold text-white mb-2">Fast & Secure Delivery</h3>
          <p class="text-neutral-400 text-sm">
            Receive your phone in days, safely packed and tracked from our store to your doorstep.
          </p>
        </div>

      </div>
    </section>


    <!-- Footer Section -->
    <footer class="mt-32 w-full border-t border-neutral-800 bg-black/90 pt-16 pb-10 text-sm text-neutral-400">
      <div class="max-w-7xl mx-auto px-6 grid grid-cols-1 md:grid-cols-4 gap-10">

        <!-- Branding -->
        <div>
          <h2 class="text-white text-xl font-semibold mb-2">ITB MShop</h2>
          <p class="text-neutral-500 text-sm leading-relaxed">
            Discover premium tech and fashion at unbeatable prices.<br />
            Inspired by innovation. Driven by quality.
          </p>
        </div>

        <!-- Navigation Links -->
        <div>
          <h3 class="text-white font-semibold mb-3">Quick Links</h3>
          <ul class="space-y-2">
            <li><router-link to="/" class="hover:text-white transition">Home</router-link></li>
            <li><router-link to="/about" class="hover:text-white transition">About Us</router-link></li>
            <li><router-link to="/contact" class="hover:text-white transition">Contact</router-link></li>
            <li><router-link to="/privacy" class="hover:text-white transition">Privacy Policy</router-link></li>
          </ul>
        </div>

        <!-- Categories -->
        <div>
          <h3 class="text-white font-semibold mb-3">Categories</h3>
          <ul class="space-y-2">
            <li><router-link to="/tech" class="hover:text-white transition">Tech Gadgets</router-link></li>
            <li><router-link to="/fashion" class="hover:text-white transition">Fashion</router-link></li>
            <li><router-link to="/sale" class="hover:text-white transition">On Sale</router-link></li>
            <li><router-link to="/new" class="hover:text-white transition">New Arrivals</router-link></li>
          </ul>
        </div>

        <!-- Social Media -->
        <div>
          <h3 class="text-white font-semibold mb-4">Follow Us</h3>
          <div class="grid grid-cols-2 gap-4 sm:grid-cols-2 sm:gap-4">
            <!-- Facebook -->
            <a href="https://facebook.com" target="_blank"
              class="flex items-center gap-3 hover:text-blue-500 transition group">
              <div class="bg-neutral-800 p-2 rounded-full group-hover:bg-blue-600 transition">
                <i class="fab fa-facebook-f text-white text-lg"></i>
              </div>
              <span class="text-sm text-neutral-400 group-hover:text-blue-400">Facebook</span>
            </a>
            
            <!-- Twitter -->
            <a href="https://twitter.com" target="_blank"
              class="flex items-center gap-3 hover:text-sky-400 transition group">
              <div class="bg-neutral-800 p-2 rounded-full group-hover:bg-sky-500 transition">
                <i class="fab fa-twitter text-white text-lg"></i>
              </div>
              <span class="text-sm text-neutral-400 group-hover:text-sky-300">Twitter</span>
            </a>

            <!-- Instagram -->
            <a href="https://instagram.com" target="_blank"
              class="flex items-center gap-3 hover:text-pink-400 transition group">
              <div class="bg-neutral-800 p-2 rounded-full group-hover:bg-pink-500 transition">
                <i class="fab fa-instagram text-white text-lg"></i>
              </div>
              <span class="text-sm text-neutral-400 group-hover:text-pink-300">Instagram</span>
            </a>

            <!-- GitHub -->
            <a href="https://github.com" target="_blank"
              class="flex items-center gap-3 hover:text-white transition group">
              <div class="bg-neutral-800 p-2 rounded-full group-hover:bg-white transition">
                <i class="fab fa-github text-white text-lg group-hover:text-black"></i>
              </div>
              <span class="text-sm text-neutral-400 group-hover:text-white">GitHub</span>
            </a>
          </div>
        </div>
      </div>

      <!-- Bottom Footer -->
      <div class="mt-12 border-t border-neutral-800 pt-6 text-center text-xs text-neutral-500">
        <p>&copy; 2025 ITB MShop. All rights reserved.</p>
        <p>Made with by the ITB NW3 Team</p>
      </div>
    </footer>
  </main>
</template>

<style scoped>
@keyframes fade-in {
  from {
    opacity: 0;
    transform: translateY(80px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fade-up {
  0% {
    opacity: 0;
    transform: translateY(50px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-fade-in {
  animation: fade-in 1s ease-out forwards;
}
.animate-fade-up {
  animation: fade-up 0.8s ease-out forwards;
}
.animate-fade-up.delay-150 {
  animation-delay: 0.15s;
}
</style>
