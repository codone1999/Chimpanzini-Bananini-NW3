

// SaleItems //
function isValidDecimal(value) {
  const parts = String(value).split('.')
  return parts.length === 1 || (parts.length === 2 && parts[1].length <= 2)
}
function validateInputSaleItem(saleItem, field, validationMessages) {
  const value = saleItem[field]
  let message = ''

  switch (field) {
    case 'brandName':
      message = value.trim() !== '' 
        ? '' 
        : 'Brand must be selected.';
      break
    case 'model':
      message = value.trim().length >= 1 && value.trim().length <= 60
        ? ''
        : 'Model must be 1-60 characters long.'; 
      break
    case 'description':
      message = value.trim().length >= 1 && value.trim().length <= 16384
        ? ''
        : 'Description must be 1-16,384 characters long.';
      break
    case 'price':
      message = typeof value === 'number' && value >= 0
        ? ''
        : 'Price must be non-negative integer.';
      break
    case 'ramGb':
      message = value === null || value === '' || (typeof value === 'number' && value > 0)
        ? ''
        : 'RAM size must be positive integer or not specified.';
      break
    case 'screenSizeInch':
      message = value === null || value === '' || (typeof value === 'number' && value > 0 && isValidDecimal(value) )
          ? ''
          : 'Screen size must be positive number with at most 2 decimal points or not specified.';
      break
    case 'storageGb':
      message = value === null || value === '' || (typeof value === 'number' && value > 0)
        ? ''
        : 'Storage size must be positive integer or not specified.';
      break;
    case 'color':
      message = value === null || value.trim() === '' || (value.trim().length >= 1 && value.trim().length <= 40)
          ? ''
          : 'Color must be 1-40 characters long or not specified.';
      break;
    case 'quantity':
      message = typeof value === 'number' && value >= 0
        ? ''
        : 'Quantity must be non-negative integer.';
      break;
  }

  if (message) {
    validationMessages[field] = message
  } else {
    validationMessages[field] = null
  }
}
function isFormSaleItemValid(form){
  return (
    form.brandName.trim() !== '' &&

    form.model.trim().length >= 1 &&
    form.model.trim().length <= 60 &&

    form.description.trim().length >= 1 &&
    form.description.trim().length <= 16384 &&

    typeof form.price === 'number' &&
    form.price >= 0 &&

    (
      form.ramGb === null ||
      form.ramGb === '' ||
      (
        typeof form.ramGb === 'number' && 
        form.ramGb > 0
      )
    ) &&

    (
      form.screenSizeInch === null ||
      form.screenSizeInch === '' ||
      (
        typeof form.screenSizeInch === 'number' &&
        form.screenSizeInch > 0 &&
        isValidDecimal(form.screenSizeInch)
      )
    ) &&

    (
      form.storageGb === null ||
      form.storageGb === '' ||
      (
        typeof form.storageGb === 'number' && 
        form.storageGb > 0
      )
    ) &&

    (
      form.color === null ||
      form.color.trim() === '' ||
      (
        form.color.trim().length >= 1 &&
        form.color.trim().length <= 40
      )
    ) &&

    
    typeof form.quantity === 'number' &&
    form.quantity >= 0
  )
}

// Brands //
function isValidUrl(url) {
  try {
    new URL(url)
    return url.startsWith('http://') || url.startsWith('https://')
  } catch {
    return false
  }
}
function validateInputBrands(brand, field, validationMessages) {
  let value = brand[field]
  let message = ''

  switch (field) {
    case 'name':
      message = value.length >= 1 && value.length <= 30
        ? ''
        : 'Brand name must be 1-30 characters long.'
      break
    case 'websiteUrl':
      message = value === '' || isValidUrl(value)
        ? ''
        : 'Brand URL must be a valid URL or not specified.'
      break
    case 'countryOfOrigin':
      message = value === '' || (value.length >= 1 && value.length <= 80) 
        ? ''
        : 'Brand country of origin must be 1-80 characters long or not specified.'
      break
  }
  
  if (message) {
    validationMessages[field] = message
  } else {
    validationMessages[field] = null
  }
}
function isFormBrandValid(form){
   return (
    (
      form.name.length >= 1 && 
      form.name.length <= 30 
    ) &&

    (
      form.websiteUrl === '' || 
      isValidUrl(form.websiteUrl)
    ) &&

    (
      form.countryOfOrigin === '' || 
      (
        form.countryOfOrigin.length >= 1 && 
        form.countryOfOrigin.length <= 80 
      )
    )
  )
}
export { validateInputSaleItem, validateInputBrands, isFormBrandValid, isFormSaleItemValid }
