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

async function handleDeleteAlerts(showMessage, getMessage, setMessage){
  showMessage.value = true
  getMessage.value = setMessage

  setTimeout(() => {
    showMessage.value = false
    router.go(0)
  }, 3000)
}

export { handleQueryAlerts, handleDeleteAlerts }
