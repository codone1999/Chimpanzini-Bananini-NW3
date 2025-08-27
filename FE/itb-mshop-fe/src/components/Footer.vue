<template>
  <!-- Footer Section -->
    <footer class="w-full border-t border-gray-800 bg-gray-950 pt-16 pb-10 text-sm text-gray-400">
      <div class="max-w-7xl mx-auto px-6 grid grid-cols-1 md:grid-cols-4 gap-10">

        <!-- Branding -->
        <div>
          <router-link to="/">
          <h2 class="text-xl font-bold mb-3 bg-gradient-to-r from-purple-400 to-indigo-500 bg-clip-text text-transparent">ITB MShop</h2>
          </router-link>
          <p class="text-gray-500 text-sm leading-relaxed">
            Discover premium tech and fashion at unbeatable prices.<br />
            Inspired by innovation. Driven by quality.
          </p>
        </div>

        <!-- Navigation Links -->
        <div>
          <h3 class="text-white font-semibold mb-3">Quick Links</h3>
          <ul class="space-y-2">
            <li><router-link to="/" class="hover:text-purple-400 transition">Home</router-link></li>
            <li><router-link to="/about" class="hover:text-purple-400 transition">About Us</router-link></li>
            <li><router-link to="/contact" class="hover:text-purple-400 transition">Contact</router-link></li>
            <li><router-link to="/privacy" class="hover:text-purple-400 transition">Privacy Policy</router-link></li>
          </ul>
        </div>

        <!-- Categories -->
        <div>
          <h3 class="text-white font-semibold mb-3">Categories</h3>
          <ul class="space-y-2">
            <li><router-link to="/tech" class="hover:text-purple-400 transition">Tech Gadgets</router-link></li>
            <li><router-link to="/fashion" class="hover:text-purple-400 transition">Fashion</router-link></li>
            <li><router-link to="/sale" class="hover:text-purple-400 transition">On Sale</router-link></li>
            <li><router-link to="/new" class="hover:text-purple-400 transition">New Arrivals</router-link></li>
          </ul>
        </div>

        <!-- Social Media -->
        <div>
          <h3 class="text-white font-semibold mb-4">Follow Us</h3>
          <div class="grid grid-cols-2 gap-4">
            <!-- Facebook -->
            <a href="https://facebook.com" target="_blank"
              class="flex items-center gap-3 hover:text-blue-400 transition group">
              <div class="bg-gray-800 p-3 rounded-full group-hover:bg-blue-600 transition shadow-lg">
                <i class="fab fa-facebook-f text-white text-lg"></i>
              </div>
              <span class="text-sm group-hover:text-blue-400">Facebook</span>
            </a>

            <!-- Twitter -->
            <a href="https://twitter.com" target="_blank"
              class="flex items-center gap-3 hover:text-sky-400 transition group">
              <div class="bg-gray-800 p-3 rounded-full group-hover:bg-sky-500 transition shadow-lg">
                <i class="fab fa-twitter text-white text-lg"></i>
              </div>
              <span class="text-sm group-hover:text-sky-400">Twitter</span>
            </a>

            <!-- Instagram -->
            <a href="https://instagram.com" target="_blank"
              class="flex items-center gap-3 hover:text-pink-400 transition group">
              <div class="bg-gray-800 p-3 rounded-full group-hover:bg-pink-500 transition shadow-lg">
                <i class="fab fa-instagram text-white text-lg"></i>
              </div>
              <span class="text-sm group-hover:text-pink-400">Instagram</span>
            </a>

            <!-- GitHub -->
            <a href="https://github.com" target="_blank"
              class="flex items-center gap-3 hover:text-purple-400 transition group">
              <div class="bg-gray-800 p-3 rounded-full group-hover:bg-purple-600 transition shadow-lg">
                <i class="fab fa-github text-white text-lg group-hover:text-black"></i>
              </div>
              <span class="text-sm group-hover:text-purple-400">GitHub</span>
            </a>
          </div>
        </div>
      </div>

      <!-- Bottom Footer -->
      <div class="mt-12 border-t border-gray-800 pt-6 text-center text-xs text-gray-500">
        <p>&copy; 2025 ITB MShop. All rights reserved.</p>
        <p class="mt-1">Made with by the ITB NW3 Team</p>
      </div>
    </footer>
</template>
