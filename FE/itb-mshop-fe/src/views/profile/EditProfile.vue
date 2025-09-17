<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// Example loaded profile (replace with API data)
const form = ref({
  nickName: 'JohnDoe',
  email: 'john@example.com',
  password: 'SecretPass123!', // real password won't be shown, masked instead
  fullName: 'John Doe',
  mobile: '0123456789',
  bankAccountNo: '987654321',
  bankName: 'Bank of Vue',
  nationalCardNo: 'AB123456',
  role: 'buyer'
})

const isLoading = ref(false)

// --- Mask helper function ---
function maskNumber(num) {
  if (!num) return ''
  const arr = num.split('')
  return arr
    .map((char, i) => {
      if (i === 1 || i === 2 || i === 3 || i === arr.length - 1) return char
      return 'x'
    })
    .join('')
}

// Submit
async function handleSubmit() {
  isLoading.value = true
  try {
    console.log('Saving profile:', form.value)
    // TODO: send to API
    router.push({ name: 'Profile' })
  } finally {
    isLoading.value = false
  }
}

// Cancel
function handleCancel() {
  router.push({ name: 'Profile' })
}
</script>

<template>
  <div class="max-w-lg mx-auto bg-white shadow-md rounded-lg p-6 my-10">
    <h2 class="text-xl font-bold mb-6 text-center">Edit Profile</h2>

    <form @submit.prevent="handleSubmit" class="space-y-4">
      <!-- Nickname -->
      <div>
        <label class="block text-sm font-medium">Nickname *</label>
        <input v-model="form.nickName" type="text" class="w-full border px-3 py-2 rounded-md" />
      </div>

      <!-- Email (readonly) -->
      <div>
        <label class="block text-sm font-medium">Email *</label>
        <input v-model="form.email" type="email" readonly class="w-full border px-3 py-2 rounded-md bg-gray-100 text-gray-600" />
      </div>

      <!-- Password (masked, readonly) -->
      <div>
        <label class="block text-sm font-medium">Password</label>
        <input type="password" value="xxxxxxxx" readonly class="w-full border px-3 py-2 rounded-md bg-gray-100 text-gray-600" />
      </div>

      <!-- Fullname -->
      <div>
        <label class="block text-sm font-medium">Fullname *</label>
        <input v-model="form.fullName" type="text" class="w-full border px-3 py-2 rounded-md" />
      </div>

      <!-- Seller-only fields -->
      <template v-if="form.role === 'seller'">
        <!-- Mobile (masked, readonly) -->
        <div>
          <label class="block text-sm font-medium">Mobile</label>
          <input :value="maskNumber(form.mobile)" readonly class="w-full border px-3 py-2 rounded-md bg-gray-100 text-gray-600" />
        </div>

        <!-- Bank Account No (masked, readonly) -->
        <div>
          <label class="block text-sm font-medium">Bank Account No</label>
          <input :value="maskNumber(form.bankAccountNo)" readonly class="w-full border px-3 py-2 rounded-md bg-gray-100 text-gray-600" />
        </div>

        <!-- Bank Name (readonly) -->
        <div>
          <label class="block text-sm font-medium">Bank Name</label>
          <input v-model="form.bankName" readonly class="w-full border px-3 py-2 rounded-md bg-gray-100 text-gray-600" />
        </div>

        <!-- National Card No (masked, readonly) -->
        <div>
          <label class="block text-sm font-medium">National Card No</label>
          <input :value="maskNumber(form.nationalCardNo)" readonly class="w-full border px-3 py-2 rounded-md bg-gray-100 text-gray-600" />
        </div>

        <!-- Photos hidden, just message -->
        <div>
          <label class="block text-sm font-medium">National Card Photos</label>
          <p class="text-gray-500 italic">Provided</p>
        </div>
      </template>

      <!-- Buttons -->
      <div class="flex gap-2 pt-4">
        <button type="submit" :disabled="isLoading" class="flex-1 py-2 bg-green-500 text-white rounded-md hover:bg-green-600">
          {{ isLoading ? 'Saving...' : 'Save' }}
        </button>
        <button type="button" @click="handleCancel" class="flex-1 py-2 bg-gray-300 text-gray-700 rounded-md hover:bg-gray-400">
          Cancel
        </button>
      </div>
    </form>
  </div>
</template>
