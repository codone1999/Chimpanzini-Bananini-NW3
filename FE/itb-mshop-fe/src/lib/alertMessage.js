import { useRoute, useRouter } from 'vue-router'

function handleQueryAlerts(messages, showMessage, getMessage) {
  const route = useRoute()
  const router = useRouter()

  for (const key in messages) {
    if (route.query[key] === 'true') {
      showMessage.value = true
      getMessage.value = messages[key]

      setTimeout(() => {
        showMessage.value = false

        const updatedQuery = { ...route.query }
        delete updatedQuery[key]

        router.replace({ name: route.name, query: updatedQuery })
      }, 3000)

      break // stop after the first match
    }
  }
}

async function handleDeleteAlerts(showMessage, getMessage, setMessage, items, url = null){
  showMessage.value = true
  getMessage.value = setMessage

  setTimeout(() => {
    showMessage.value = false
  }, 3000)

  // Fetch the updated list
  try {
    const updatedItems = await getItems(url)
    if (updatedItems && updatedItems !== 404) {
      items.value = updatedItems
    }
  } catch (err) {
    console.error('Failed to fetch:', err)
  }
}

export { handleQueryAlerts, handleDeleteAlerts }
