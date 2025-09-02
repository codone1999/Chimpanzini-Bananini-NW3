// composables/useImageUpload.js
import { ref, computed } from 'vue'
import phoneImg from "../../public/phone.png"

export function useImageUpload(isEditMode = false) {
  const files = ref([])
  const images = ref([])
  const showMaxImageWarning = ref(false)
  const oversizedFiles = ref([])
  const selectedImageIndex = ref(0)

  // Computed properties
  const filePreviews = computed(() => {
    if (isEditMode) {
      return images.value.map(image => {
        if (image.src) {
          // Existing image from server
          return image.src
        } else if (image.file) {
          // New uploaded file
          return URL.createObjectURL(image.file)
        }
        return null
      }).filter(Boolean)
    } else {
      return files.value.map(file => URL.createObjectURL(file))
    }
  })

  const mainDisplayImage = computed(() => {
    if (isEditMode) {
      if (images.value.length > 0 && selectedImageIndex.value < images.value.length) {
        const selectedImage = images.value[selectedImageIndex.value]
        if (selectedImage.src) {
          // Existing image from server
          return selectedImage.src
        } else if (selectedImage.file) {
          // New uploaded file
          return URL.createObjectURL(selectedImage.file)
        }
      }
      return phoneImg // Fallback to default phone image
    } else {
      if (files.value.length > 0 && selectedImageIndex.value < files.value.length) {
        return URL.createObjectURL(files.value[selectedImageIndex.value])
      }
      return phoneImg // default phone image
    }
  })

  // Image selection
  function selectImageForDisplay(index) {
    selectedImageIndex.value = index
  }

  // File upload handling
  function handleFileChange(event) {
    if (!event.target.files) return
    
    const newFiles = Array.from(event.target.files)
    const maxFileSize = 2 * 1024 * 1024 // 2MB in bytes
    
    // Separate valid and oversized files
    const validFiles = []
    const currentOversizedFiles = []
    
    newFiles.forEach(file => {
      if (isEditMode) {
        // Check for duplicate files by name and size in edit mode
        const isDuplicate = images.value.some(existingImg => 
          (existingImg.name === file.name || existingImg.fileName === file.name) &&
          existingImg.file?.size === file.size
        )
        
        if (isDuplicate) {
          return
        }
      }

      if (file.size > maxFileSize) {
        currentOversizedFiles.push(file.name)
      } else {
        validFiles.push(file)
      }
    })
    
    // Update oversized files list
    oversizedFiles.value = currentOversizedFiles
    
    if (isEditMode) {
      const previousLength = images.value.length
      const totalImages = images.value.length + validFiles.length
      
      // Check if user is trying to upload more than 4 total
      if (totalImages > 4) {
        showMaxImageWarning.value = true
        const allowedCount = 4 - images.value.length
        if (allowedCount > 0) {
          // Only add the allowed number of files
          const filesToAdd = validFiles.slice(0, allowedCount).map((file, index) => ({
            file: file,
            name: file.name,
            imageViewOrder: images.value.length + index + 1,
            isNewFile: true
          }))
          images.value = [...images.value, ...filesToAdd]
        }
      } else {
        // Add all new files if within limit
        const filesToAdd = validFiles.map((file, index) => ({
          file: file,
          name: file.name,
          imageViewOrder: images.value.length + index + 1,
          isNewFile: true
        }))
        images.value = [...images.value, ...filesToAdd]
        showMaxImageWarning.value = false

        // Auto-select first uploaded image
        if (previousLength === 0 && validFiles.length > 0) {
          selectedImageIndex.value = 0
        }
      }
    } else {
      const totalFiles = files.value.length + validFiles.length
      
      // Check if user is trying to upload more than 4 total (only counting valid files)
      if (totalFiles > 4) {
        showMaxImageWarning.value = true
        const allowedCount = 4 - files.value.length
        if (allowedCount > 0) {
          // Only add the allowed number of valid files
          files.value = [...files.value, ...validFiles.slice(0, allowedCount)]
        }
      } else {
        // Add all valid files if within limit
        files.value = [...files.value, ...validFiles]
        showMaxImageWarning.value = false
      }
    }
    
    // Reset the input value -> same files can be selected again
    event.target.value = ''
  }

  // Remove image
  function removeImage(index) {
    if (isEditMode) {
      images.value.splice(index, 1)
      
      // Adjust selected image index if necessary
      if (index <= selectedImageIndex.value) {
        if (images.value.length === 0) {
          selectedImageIndex.value = 0 // Reset to show default image
        } else if (selectedImageIndex.value >= images.value.length) {
          selectedImageIndex.value = images.value.length - 1
        }
      }
      
      // Hide warning if we're back under the limit
      if (images.value.length <= 4) {
        showMaxImageWarning.value = false
      }
      // Reorder remaining images
      reorderImages()
    } else {
      const removedIndex = index
      files.value.splice(index, 1)

      // Adjust selected image index if necessary
      if (removedIndex <= selectedImageIndex.value) {
        if (files.value.length === 0) {
          selectedImageIndex.value = 0 // Reset to show default image
        } else if (selectedImageIndex.value >= files.value.length) {
          selectedImageIndex.value = files.value.length - 1 // After re-order the selected Image must change
        }
      }

      // Hide warning if we're back under the limit
      if (files.value.length <= 4) {
        showMaxImageWarning.value = false
      }
    }
  }

  // Move image up
  function moveImageUp(index) {
    if (index > 0) {
      if (isEditMode) {
        // Swap the images in the array
        const temp = images.value[index - 1]
        images.value[index - 1] = images.value[index]
        images.value[index] = temp

        // Update selected index if it's affected
        if (selectedImageIndex.value === index) {
          selectedImageIndex.value = index - 1
        } else if (selectedImageIndex.value === index - 1) {
          selectedImageIndex.value = index
        }

        // Update the imageViewOrder for all images after swapping
        reorderImages()
      } else {
        const temp = files.value[index - 1]
        files.value[index - 1] = files.value[index]
        files.value[index] = temp

        // Update selected index if it's affected
        if (selectedImageIndex.value === index) {
          selectedImageIndex.value = index - 1
        } else if (selectedImageIndex.value === index - 1) {
          selectedImageIndex.value = index
        }
      }
    }
  }

  // Move image down
  function moveImageDown(index) {
    const maxLength = isEditMode ? images.value.length : files.value.length
    
    if (index < maxLength - 1) {
      if (isEditMode) {
        // Swap the images in the array
        const temp = images.value[index + 1]
        images.value[index + 1] = images.value[index]
        images.value[index] = temp

        // Update selected index if it's affected
        if (selectedImageIndex.value === index) {
          selectedImageIndex.value = index + 1
        } else if (selectedImageIndex.value === index + 1) {
          selectedImageIndex.value = index
        }

        // Update the imageViewOrder for all images after swapping
        reorderImages()
      } else {
        const temp = files.value[index + 1]
        files.value[index + 1] = files.value[index]
        files.value[index] = temp

        // Update selected index if it's affected
        if (selectedImageIndex.value === index) {
          selectedImageIndex.value = index + 1
        } else if (selectedImageIndex.value === index + 1) {
          selectedImageIndex.value = index
        }
      }
    }
  }

  // Reorder images (only for edit mode)
  function reorderImages() {
    if (isEditMode) {
      images.value.forEach((image, index) => {
        image.imageViewOrder = index + 1
      })
    }
  }

  // Get image name (only for edit mode)
  function getImageName(item) {
    if (!isEditMode) return item.name

    if (typeof item === 'string') {
      // It's a URL - extract filename after the last '/'
      return item.split('/').pop()
    } else if (item.fileName) {
      // It's an existing image object with fileName
      return item.fileName
    } else if (item.name) {
      // It's a new file object with name property
      return item.name
    } else {
      // Fallback
      return 'Unknown'
    }
  }

  // Check and clear oversized files warning
  function checkAndClearOversized() {
    if (oversizedFiles.value.length > 0) {
      setTimeout(() => {
        oversizedFiles.value = []
      }, 2000)
      return true
    }
    return false
  }

  // Load images (only for edit mode)
  async function loadImages(product, baseUrl) {
    if (!isEditMode || !product?.saleItemImages) {
      console.warn('No images found for this product')
      return
    }

    // Sort images by imageViewOrder before loading
    const sortedImages = [...product.saleItemImages]
      .sort((a, b) => (a.imageViewOrder || 0) - (b.imageViewOrder || 0))
    
    // Clear existing images
    images.value = []

    // Load each image with error handling and proper structure
    for (let i = 0; i < sortedImages.length; i++) {
      try {
        const imageData = sortedImages[i]
        if (imageData && imageData.fileName) {
          const imageUrl = `${baseUrl}/picture/${imageData.fileName}`
          
          // Create image object with all necessary properties
          const imageObject = {
            src: imageUrl,
            fileName: imageData.fileName,
            imageViewOrder: i + 1, // Ensure sequential order starting from 1
          }
          
          images.value.push(imageObject)
        }
      } catch (error) {
        console.warn(`Failed to load image ${i}:`, error)
      }
    }

    // Auto-select first image if images were loaded
    if (images.value.length > 0) {
      selectedImageIndex.value = 0
    }
  }

  // Convert URL to file (only for edit mode)
  async function urlToFile(imageName, baseUrl) {
    if (!isEditMode) return null
    
    const response = await fetch(`${baseUrl}/picture/${imageName}`)
    const blob = await response.blob()
    return new File([blob], imageName, { type: blob.type })
  }

  return {
    // Reactive refs
    files,
    images,
    showMaxImageWarning,
    oversizedFiles,
    selectedImageIndex,
    
    // Computed
    filePreviews,
    mainDisplayImage,
    
    // Methods
    selectImageForDisplay,
    handleFileChange,
    removeImage,
    moveImageUp,
    moveImageDown,
    reorderImages,
    getImageName,
    checkAndClearOversized,
    loadImages,
    urlToFile
  }
}
