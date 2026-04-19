// Plan selection handling
document.querySelectorAll('.plan-card').forEach(card => {
    card.addEventListener('click', function() {
        document.querySelectorAll('.plan-card').forEach(c => {
            c.classList.remove('selected');
        });
        this.classList.add('selected');
        const plan = this.getAttribute('data-plan');
        document.getElementById('selectedPlan').value = plan;
        document.getElementById('plan-error').textContent = '';
    });
});

// Validation functions
function validateMSISDN(msisdn) {
    // Must start with 010, 011, 012, or 015 and be exactly 11 digits
    const pattern = /^(010|011|012|015)[0-9]{8}$/;
    return pattern.test(msisdn);
}

function validateName(name) {
    // Must have at least 3 parts (first, middle, last name)
    const parts = name.trim().split(/\s+/);
    return parts.length >= 3 && name.trim().length > 0;
}

function validateInternationalID(id) {
    // Must be at least 8 digits
    const pattern = /^[0-9]{8,}$/;
    return pattern.test(id);
}

function validateAddress(address) {
    // Must contain city and country (e.g., Cairo, Egypt)
    const addressText = address.trim().toLowerCase();
    return addressText.length >= 5 && (addressText.includes(',') || (addressText.includes('egypt') || addressText.includes('cairo')));
}

// Show error for a field
function showError(fieldId, message) {
    const field = document.getElementById(fieldId);
    const errorDiv = document.getElementById(fieldId + '-error');
    if (field) field.classList.add('input-error');
    if (errorDiv) errorDiv.textContent = message;
}

// Clear error for a field
function clearError(fieldId) {
    const field = document.getElementById(fieldId);
    const errorDiv = document.getElementById(fieldId + '-error');
    if (field) field.classList.remove('input-error');
    if (errorDiv) errorDiv.textContent = '';
}

// Real-time validation for MSISDN
document.getElementById('msisdn').addEventListener('input', function(e) {
    let value = this.value.replace(/[^0-9]/g, '');
    if (value.length > 11) value = value.slice(0, 11);
    this.value = value;
    
    if (value.length === 11) {
        if (validateMSISDN(value)) {
            clearError('msisdn');
        } else {
            showError('msisdn', 'MSISDN must start with 010, 011, 012, or 015 and be 11 digits');
        }
    } else if (value.length > 0) {
        showError('msisdn', 'MSISDN must be exactly 11 digits');
    } else {
        clearError('msisdn');
    }
});

// Real-time validation for Name
document.getElementById('name').addEventListener('input', function(e) {
    if (this.value.trim().length > 0) {
        if (validateName(this.value)) {
            clearError('name');
        } else {
            showError('name', 'Please enter full name (First, Middle, Last name)');
        }
    } else {
        clearError('name');
    }
});

// Real-time validation for International ID
document.getElementById('internationalid').addEventListener('input', function(e) {
    let value = this.value.replace(/[^0-9]/g, '');
    this.value = value;
    
    if (value.length > 0) {
        if (validateInternationalID(value)) {
            clearError('internationalid');
        } else {
            showError('internationalid', 'International ID must be at least 8 digits');
        }
    } else {
        clearError('internationalid');
    }
});

// Real-time validation for Address
document.getElementById('address').addEventListener('input', function(e) {
    if (this.value.trim().length > 0) {
        if (validateAddress(this.value)) {
            clearError('address');
        } else {
            showError('address', 'Please enter complete address (e.g., City, Country)');
        }
    } else {
        clearError('address');
    }
});

// Form submission validation
document.getElementById('subscriberForm').addEventListener('submit', function(e) {
    let isValid = true;
    
    // Get values
    const msisdn = document.getElementById('msisdn').value;
    const name = document.getElementById('name').value;
    const internationalId = document.getElementById('internationalid').value;
    const address = document.getElementById('address').value;
    const selectedPlan = document.getElementById('selectedPlan').value;
    
    // Validate MSISDN
    if (!msisdn) {
        showError('msisdn', 'MSISDN is required');
        isValid = false;
    } else if (!validateMSISDN(msisdn)) {
        showError('msisdn', 'MSISDN must start with 010, 011, 012, or 015 and be 11 digits');
        isValid = false;
    } else {
        clearError('msisdn');
    }
    
    // Validate Name
    if (!name) {
        showError('name', 'Full name is required');
        isValid = false;
    } else if (!validateName(name)) {
        showError('name', 'Please enter full name (First, Middle, Last name) - at least 3 parts');
        isValid = false;
    } else {
        clearError('name');
    }
    
    // Validate International ID
    if (!internationalId) {
        showError('internationalid', 'International ID is required');
        isValid = false;
    } else if (!validateInternationalID(internationalId)) {
        showError('internationalid', 'International ID must be at least 8 digits');
        isValid = false;
    } else {
        clearError('internationalid');
    }
    
    // Validate Address
    if (!address) {
        showError('address', 'Address is required');
        isValid = false;
    } else if (!validateAddress(address)) {
        showError('address', 'Please enter complete address (e.g., Cairo, Egypt)');
        isValid = false;
    } else {
        clearError('address');
    }
    
    // Validate Plan
    if (!selectedPlan) {
        showError('plan', 'Please select a subscription plan');
        isValid = false;
    } else {
        clearError('plan');
    }
    
    // If not valid, prevent form submission
    if (!isValid) {
        e.preventDefault();
    }
});

// Reset button handler
document.querySelector('.btn-reset').addEventListener('click', function(e) {
    e.preventDefault();
    document.getElementById('subscriberForm').reset();
    document.querySelectorAll('.plan-card').forEach(c => c.classList.remove('selected'));
    document.getElementById('selectedPlan').value = '';
    document.querySelectorAll('.error-message').forEach(el => el.textContent = '');
    document.querySelectorAll('.input-error').forEach(el => el.classList.remove('input-error'));
});

// Add CSS class for input-error styling
const style = document.createElement('style');
style.textContent = `
    .input-error {
        border-color: #ef4444 !important;
        box-shadow: 0 0 5px rgba(239, 68, 68, 0.3) !important;
    }
`;
document.head.appendChild(style);