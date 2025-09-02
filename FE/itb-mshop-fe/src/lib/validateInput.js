//lib/validateInput.js

// ===================== Login Form ====================== //
function isValidEmail(email) {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

function validateEmail(email) {
  if (!email || email.trim() === '') {
    return 'Email cannot be empty'
  }
  if (email.length > 50) {
    return 'Email must not exceed 50 characters'
  }
  if (!isValidEmail(email)) {
    return 'Please enter a valid email format'
  }
  return null
}

function validatePassword(password) {
  if (!password || password === '') { // removed trim() to allow whitespace
    return 'Password cannot be empty'
  }
  if (password.length > 14) {
    return 'Password must not exceed 14 characters'
  }
  return null
}

function isLoginFormValid(form) {
  // Check if email is not empty and valid
  const emailValid = form.email && 
                     form.email.trim() !== '' && 
                     form.email.length <= 50 && 
                     isValidEmail(form.email)
  
  // Check if password is not empty and meets length requirements
  const passwordValid = form.password && 
                        form.password !== '' && 
                        form.password.length <= 14
  
  return emailValid && passwordValid
}

// ===================== Register Form ===================== //
function validateNickName(nickName) {
  const trimmed = nickName.trim()
  if (!trimmed) return 'Nickname cannot be empty'
  return null
}

function validateRegistrationEmail(email) {
  const trimmed = email.trim()
  if (!trimmed) return 'Email cannot be empty'
  if (trimmed.length > 50) return 'Email must not exceed 50 characters'
  if (!isValidEmail(email)) return 'Please enter a valid email format'
  return null
}

function validateRegistrationPassword(password) {
  if (!password) return 'Password cannot be empty'
  if (password.length < 8) return 'Password must be at least 8 characters'
  if (password.length > 14) return 'Password must be less than or equal 14 characters'
  const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&]).+$/
  if (!passwordRegex.test(password)) {
    return 'Password must contain uppercase, lowercase, number, and special character (@$!%*?&)'
  }
  return null
}

function validateFullname(fullName) {
  const trimmed = fullName.trim()
  if (!trimmed) return 'Full name cannot be empty'
  if (trimmed.length < 4) return 'Full name must be at least 4 characters'
  if (trimmed.length > 40) return 'Full name must not exceed 40 characters'
  return null
}

function validateMobile(mobile) {
  const trimmed = mobile.trim()
  if (!trimmed) return 'Mobile cannot be empty'
  if (trimmed.length < 10) return 'Mobile must be at least 10 digits'
  if (trimmed.length > 15) return 'Mobile must not exceed 15 digits'
  if (!/^\d+$/.test(trimmed)) return 'Mobile must contain only numbers'
  return null
}

function validateBankAccountNo(bankAccountNo) {
  const trimmed = bankAccountNo.trim()
  if (!trimmed) return 'Bank account number cannot be empty'
  if (trimmed.length < 8) return 'Bank account number must be at least 8 digits'
  if (trimmed.length > 20) return 'Bank account number must not exceed 20 digits'
  if (!/^\d+$/.test(trimmed)) return 'Bank account number must contain only numbers'
  return null
}

function validateBankName(bankName) {
  const trimmed = bankName.trim()
  if (!trimmed) return 'Bank name cannot be empty'
  if (trimmed.length < 2) return 'Bank name must be at least 2 characters'
  if (trimmed.length > 50) return 'Bank name must not exceed 50 characters'
  return null
}

function validateNationalCardNo(nationalCardNo) {
  const trimmed = nationalCardNo.trim()
  if (!trimmed) return 'National card number cannot be empty'
  if (trimmed.length < 10) return 'National card number must be at least 10 digits'
  if (trimmed.length > 20) return 'National card number must not exceed 20 digits'
  return null
}

function isBuyerFormValid(form) {
  return form.nickName.trim() !== '' &&
         form.email.trim() !== '' &&
         form.password !== '' &&
         form.fullName.trim() !== '' &&
         !validateRegistrationPassword(form.password) &&
         !validateFullname(form.fullName) &&
         isValidEmail(form.email)
}

function isSellerFormValid(form) {
  return form.nickName?.trim() !== '' &&
         form.email?.trim() !== '' &&
         form.password !== '' &&
         form.fullName?.trim() !== '' &&
         form.mobile?.trim() !== '' &&
         form.bankAccountNo?.trim() !== '' &&
         form.bankName?.trim() !== '' &&
         form.nationalCardNo?.trim() !== '' &&
         form.nationalCardFront !== null &&
         form.nationalCardBack !== null &&
         !validateRegistrationPassword(form.password) &&
         !validateFullname(form.fullName) &&
         isValidEmail(form.email)
}

// =====================  SaleItems =====================  //
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

// ===================== Brands ===================== //
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

export { 
  // Login validations
  isValidEmail,
  validateEmail, 
  validatePassword,
  isLoginFormValid,
  
  // Registration validations
  validateNickName,
  validateRegistrationEmail,
  validateRegistrationPassword,
  validateFullname,
  validateMobile,
  validateBankAccountNo,
  validateBankName,
  validateNationalCardNo,
  isBuyerFormValid,
  isSellerFormValid,
  
  // SaleItems & Brands
  validateInputSaleItem, 
  validateInputBrands, 
  isFormBrandValid, 
  isFormSaleItemValid 
}