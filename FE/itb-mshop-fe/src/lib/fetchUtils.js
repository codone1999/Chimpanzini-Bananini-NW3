async function getItems(url) {
  try {
    const data = await fetch(url)
    const items = await data.json()
    return items
  } catch (error) {
    throw new Error('can not get your items')
  }
}
async function getItemById(url, id) {
  try {
    const data = await fetch(`${url}/${id}`)
    const item = await data.json()
    return item
  } catch (error) {
    if (data.status === 404) return []
    throw new Error('can not get your item')
  }
}

async function deleteItemById(url, id) {
  try {
    const res = await fetch(`${url}/${id}`, {
      method: 'DELETE'
    })
    return res.status
  } catch (error) {
    throw new Error('can not delete your item')
  }
}

async function addItem(url, newItem) {
  try {
    const res = await fetch(url, {
      method: 'POST',
      headers: {
        'content-type': 'application/json'
      },
      body: JSON.stringify({
        ...newItem
      })
    })
    const addedItem = await res.json()
    return addedItem
  } catch (error) {
    throw new Error('can not add your item')
  }
}

async function addItemAndImage(url, newItem, files) {
  try {
    const formData = new FormData()
    
    // Handle each property for @ModelAttribute binding
    Object.keys(newItem).forEach(key => {
      const value = newItem[key]
      if (value !== null && value !== undefined) {
        if (typeof value === 'object' && !Array.isArray(value)) {
          // Handle nested objects using dot notation (e.g., brand.id, brand.name)
          Object.keys(value).forEach(nestedKey => {
            if (value[nestedKey] !== null && value[nestedKey] !== undefined) {
              formData.append(`${key}.${nestedKey}`, value[nestedKey])
            }
          })
        } else if (Array.isArray(value)) {
          // Handle arrays using indexed notation (e.g., tags[0], tags[1])
          value.forEach((item, index) => {
            if (typeof item === 'object') {
              Object.keys(item).forEach(itemKey => {
                if (item[itemKey] !== null && item[itemKey] !== undefined) {
                  formData.append(`${key}[${index}].${itemKey}`, item[itemKey])
                }
              })
            } else {
              formData.append(`${key}[${index}]`, item)
            }
          })
        } else {
          // Handle primitive values (string, number, boolean)
          formData.append(key, value)
        }
      }
    })
    
    // Add files with parameter name 'images' to match your @RequestParam
    if (files && files.length > 0) {
      files.forEach(file => {
        formData.append("images", file)
      })
    }
    
    const res = await fetch(url, {
      method: 'POST',
      body: formData
    })
    
    if (!res.ok) {
      const errorText = await res.text()
      throw new Error(`HTTP error! status: ${res.status}, message: ${errorText}`)
    }
    
    return await res.json()
  } catch (error) {
    console.error('Error adding item:', error)
    throw new Error('Cannot add your item: ' + error.message)
  }
}

async function editItem(url, id, editItem) {
  try {
    const res = await fetch(`${url}/${id}`, {
      method: 'PUT',
      headers: {
        'content-type': 'application/json'
      },
      body: JSON.stringify({
        ...editItem
      })
    })
    const editedItem = await res.json()
    return editedItem
  } catch (error) {
    throw new Error('can not edit your item')
  }
}

async function editItemAndImage(url, id, editItem) {
  try {
    const formData = new FormData()

    // Handle saleItem nested object
    if (editItem.saleItem) {
      Object.keys(editItem.saleItem).forEach(key => {
        const value = editItem.saleItem[key]
        if (value !== null && value !== undefined) {
          if (typeof value === 'object' && !Array.isArray(value)) {
            // Handle nested objects like brand
            Object.keys(value).forEach(nestedKey => {
              if (value[nestedKey] !== null && value[nestedKey] !== undefined) {
                formData.append(`saleItem.${key}.${nestedKey}`, value[nestedKey])
              }
            })
          } else {
            // Handle primitive values
            formData.append(`saleItem.${key}`, value)
          }
        }
      })
    }

    // Handle imagesInfos array
    if (editItem.imagesInfos && Array.isArray(editItem.imagesInfos)) {
      editItem.imagesInfos.forEach((imageInfo, index) => {
        // Handle order
        if (imageInfo.order !== null && imageInfo.order !== undefined) {
          formData.append(`imagesInfos[${index}].order`, imageInfo.order)
        }
        
        // Handle pictureName
        if (imageInfo.pictureName) {
          formData.append(`imagesInfos[${index}].pictureName`, imageInfo.pictureName)
        }
        
        // Handle status
        if (imageInfo.status) {
          formData.append(`imagesInfos[${index}].status`, imageInfo.status)
        }
        
        // Handle pictureFile (MultipartFile)
        if (imageInfo.pictureFile && imageInfo.pictureFile instanceof File) {
          formData.append(`imagesInfos[${index}].pictureFile`, imageInfo.pictureFile, imageInfo.pictureFile.name)
        }
      })
    }

    // Debug: Log what's being sent (optional, remove in production)
    // console.log('FormData entries:')
    // for (let [key, value] of formData.entries()) {
    //   console.log(`${key}:`, value instanceof File ? `File: ${value.name}` : value)
    // }

    const res = await fetch(`${url}/${id}`, {
      method: 'PUT',
      body: formData
    })

    if (!res.ok) {
      const errorText = await res.text()
      throw new Error(`HTTP error! status: ${res.status}, message: ${errorText}`)
    }

    return await res.json()
  } catch (error) {
    console.error('Error editing item:', error)
    throw new Error('Cannot edit your item: ' + error.message)
  }
}

export { getItems, getItemById, deleteItemById, addItem, editItem, addItemAndImage, editItemAndImage }
