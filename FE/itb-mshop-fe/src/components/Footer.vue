<template>
  <!-- Footer Section -->
    <footer class="w-full border-t border-neutral-800 bg-black/90 pt-16 pb-10 text-sm text-neutral-400">
      <div class="max-w-7xl mx-auto px-6 grid grid-cols-1 md:grid-cols-4 gap-10">

        <!-- Branding -->
        <div>
          <router-link to="/">
          <h2 class="text-white text-xl font-semibold mb-2">ITB MShop</h2>
          </router-link>
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
</template>
