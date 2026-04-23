//==============================================================================
//==================        GLOBAL VARIABLES               ======================
//==============================================================================

let plansData = [];
let currentEditId = null;

// DOM Elements
const plansContainer = document.getElementById('plansContainer');
const modal = document.getElementById('planModal');
const modalTitle = document.getElementById('modalTitle');
const planForm = document.getElementById('planForm');
const openAddModalBtn = document.getElementById('openAddModalBtn');
const cancelModalBtn = document.getElementById('cancelModalBtn');
const closeSpan = document.querySelector('.close');

// Form inputs
const planIdInput = document.getElementById('planId');
const planNameInput = document.getElementById('planName');
const planColorInput = document.getElementById('planColor');
const monthlyFeeInput = document.getElementById('monthlyFee');
const minutesInput = document.getElementById('minutes');
const dataInput = document.getElementById('data');
const smsInput = document.getElementById('sms');
const descriptionInput = document.getElementById('description');

// New fields for free allowances
const voiceFreeInput = document.getElementById('voiceFree');
const dataFreeInput = document.getElementById('dataFree');
const smsFreeInput = document.getElementById('smsFree');

// Flag to track if initial load has been done
let isInitialLoadDone = false;

//==============================================================================
//==================        LOAD PLANS FROM JSON           ======================
//==============================================================================

async function loadPlans() {
    if (isInitialLoadDone) {
        console.log('Plans already loaded, skipping...');
        return;
    }
    
    try {
        plansContainer.innerHTML = '<div class="loading">Loading plans...</div>';
        
        const response = await fetch('/Billing-System-Web-GUI/JSON/plans_data.json?t=' + Date.now());
        
        if (!response.ok) {
            throw new Error('Failed to load plans');
        }
        
        plansData = await response.json();
        
        if (!plansData || !Array.isArray(plansData)) {
            plansData = [];
        }
        
        renderPlans();
        isInitialLoadDone = true;
        
    } catch (error) {
        console.error('Error loading plans:', error);
        plansContainer.innerHTML = '<div class="loading" style="color: #ef4444;">Failed to load plans. Please refresh the page.</div>';
    }
}

//==============================================================================
//==================        FORMAT ALLOWANCE DISPLAY       ======================
//==============================================================================

function formatMinutes(allowance, free) {
    if (allowance && free) {
        return `${allowance} min + ${free} Free`;
    } else if (allowance) {
        return `${allowance} min`;
    }
    return 'N/A';
}

function formatData(allowance, free) {
    if (allowance && free) {
        const allowanceGB = (allowance / 1024).toFixed(1);
        const freeGB = (free / 1024).toFixed(1);
        return `${allowanceGB} GB + ${freeGB} Free`;
    } else if (allowance) {
        return `${(allowance / 1024).toFixed(1)} GB`;
    }
    return 'N/A';
}

function formatSMS(allowance, free) {
    if (allowance && free) {
        return `${allowance} SMS + ${free} Free`;
    } else if (allowance) {
        return `${allowance} SMS`;
    }
    return 'N/A';
}

//==============================================================================
//==================        RENDER PLANS CARDS             ======================
//==============================================================================

function renderPlans() {
    if (!plansData || plansData.length === 0) {
        plansContainer.innerHTML = '<div class="loading">No plans available. Click "Add New Plan" to create one.</div>';
        return;
    }
    
    plansContainer.innerHTML = '';
    
    plansData.forEach(plan => {
        const card = document.createElement('div');
        card.className = `plan-card ${plan.isactive === false ? 'inactive' : ''}`;
        card.dataset.id = plan.planid;
        
        const statusText = plan.isactive !== false ? 'Active' : 'Inactive';
        const statusColor = plan.isactive !== false ? '#22c55e' : '#ef4444';
        
        const minutesDisplay = formatMinutes(plan.voice_allowance, plan.voice_free);
        const dataDisplay = formatData(plan.data_allowance, plan.data_free);
        const smsDisplay = formatSMS(plan.sms_allowance, plan.sms_free);
        
        const description = plan.description || '';
        const color = plan.color || '#38bdf8';
        
        card.innerHTML = `
            <div class="plan-header">
                <div class="plan-name" style="color: ${color}">${escapeHtml(plan.planname)}</div>
                <div class="plan-fee">${plan.monthlyfee || 0}<span> EGP/month</span></div>
                <div class="plan-status" style="background: ${statusColor}">${statusText}</div>
            </div>
            <div class="plan-details">
                <div class="detail-item">
                    <i class="fas fa-phone"></i>
                    <span><strong>Minutes:</strong> ${escapeHtml(minutesDisplay)}</span>
                </div>
                <div class="detail-item">
                    <i class="fas fa-wifi"></i>
                    <span><strong>Data:</strong> ${escapeHtml(dataDisplay)}</span>
                </div>
                <div class="detail-item">
                    <i class="fas fa-envelope"></i>
                    <span><strong>SMS:</strong> ${escapeHtml(smsDisplay)}</span>
                </div>
                ${description ? `<div class="plan-description"><i class="fas fa-info-circle"></i> ${escapeHtml(description)}</div>` : ''}
            </div>
            <div class="plan-actions">
                <button class="btn-edit" onclick="editPlan(${plan.planid})">
                    <i class="fas fa-edit"></i> Edit
                </button>
                <button class="btn-toggle ${plan.isactive === false ? 'inactive' : ''}" onclick="togglePlanStatus(${plan.planid})">
                    <i class="fas ${plan.isactive !== false ? 'fa-pause' : 'fa-play'}"></i>
                    ${plan.isactive !== false ? 'Deactivate' : 'Activate'}
                </button>
            </div>
        `;
        
        plansContainer.appendChild(card);
    });
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function showToast(message, type = 'success') {
    const existingToasts = document.querySelectorAll('.toast');
    existingToasts.forEach(toast => toast.remove());
    
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `<i class="fas ${type === 'success' ? 'fa-check-circle' : 'fa-exclamation-circle'}"></i> ${message}`;
    document.body.appendChild(toast);
    
    setTimeout(() => {
        toast.remove();
    }, 3000);
}

//==============================================================================
//==================        CLEAR VALIDATION               ======================
//==============================================================================

function clearValidation() {
    document.querySelectorAll('.error-message').forEach(msg => {
        msg.classList.remove('show');
        msg.textContent = '';
    });
    document.querySelectorAll('.invalid').forEach(el => {
        el.classList.remove('invalid');
    });
}

//==============================================================================
//==================        SHOW FIELD ERROR               ======================
//==============================================================================

function showFieldError(inputElement, errorId, message) {
    if (inputElement) {
        inputElement.classList.add('invalid');
    }
    if (errorId) {
        const errorDiv = document.getElementById(errorId);
        if (errorDiv) {
            errorDiv.textContent = message;
            errorDiv.classList.add('show');
        }
    }
}

//==============================================================================
//==================        VALIDATE ALL FIELDS            ======================
//==============================================================================

function validateForm() {
    clearValidation();
    let isValid = true;
    
    // 1. Plan Name validation (required, min 2 chars)
    const planName = planNameInput.value.trim();
    if (!planName) {
        showFieldError(planNameInput, 'planNameError', 'Plan name is required');
        isValid = false;
    } else if (planName.length < 2) {
        showFieldError(planNameInput, 'planNameError', 'Plan name must be at least 2 characters');
        isValid = false;
    }
    
    // 2. Color validation (required, valid hex)
    const color = planColorInput.value;
    if (!color || !/^#[0-9A-Fa-f]{6}$/.test(color)) {
        showFieldError(planColorInput, 'planColorError', 'Please select a valid color');
        isValid = false;
    }
    
    // 3. Monthly Fee validation (required, > 0)
    const monthlyFee = parseFloat(monthlyFeeInput.value);
    if (isNaN(monthlyFee)) {
        showFieldError(monthlyFeeInput, 'monthlyFeeError', 'Monthly fee is required');
        isValid = false;
    } else if (monthlyFee <= 0) {
        showFieldError(monthlyFeeInput, 'monthlyFeeError', 'Monthly fee must be greater than 0');
        isValid = false;
    }
    
    // 4. Voice Minutes validation (required, >= 0)
    const voiceAllowance = minutesInput.value === '' ? NaN : parseInt(minutesInput.value);
    if (isNaN(voiceAllowance)) {
        showFieldError(minutesInput, 'minutesError', 'Voice minutes are required');
        isValid = false;
    } else if (voiceAllowance < 0) {
        showFieldError(minutesInput, 'minutesError', 'Voice minutes cannot be negative');
        isValid = false;
    }
    
    // 5. Free Minutes validation (required, >= 0)
    const voiceFree = voiceFreeInput.value === '' ? NaN : parseInt(voiceFreeInput.value);
    if (isNaN(voiceFree)) {
        showFieldError(voiceFreeInput, 'voiceFreeError', 'Free minutes are required');
        isValid = false;
    } else if (voiceFree < 0) {
        showFieldError(voiceFreeInput, 'voiceFreeError', 'Free minutes cannot be negative');
        isValid = false;
    }
    
    // 6. Data allowance validation (required, >= 0)
    const dataAllowance = dataInput.value === '' ? NaN : parseInt(dataInput.value);
    if (isNaN(dataAllowance)) {
        showFieldError(dataInput, 'dataError', 'Data allowance is required');
        isValid = false;
    } else if (dataAllowance < 0) {
        showFieldError(dataInput, 'dataError', 'Data allowance cannot be negative');
        isValid = false;
    }
    
    // 7. Free Data validation (required, >= 0)
    const dataFree = dataFreeInput.value === '' ? NaN : parseInt(dataFreeInput.value);
    if (isNaN(dataFree)) {
        showFieldError(dataFreeInput, 'dataFreeError', 'Free data is required');
        isValid = false;
    } else if (dataFree < 0) {
        showFieldError(dataFreeInput, 'dataFreeError', 'Free data cannot be negative');
        isValid = false;
    }
    
    // 8. SMS allowance validation (required, >= 0)
    const smsAllowance = smsInput.value === '' ? NaN : parseInt(smsInput.value);
    if (isNaN(smsAllowance)) {
        showFieldError(smsInput, 'smsError', 'SMS allowance is required');
        isValid = false;
    } else if (smsAllowance < 0) {
        showFieldError(smsInput, 'smsError', 'SMS allowance cannot be negative');
        isValid = false;
    }
    
    // 9. Free SMS validation (required, >= 0)
    const smsFree = smsFreeInput.value === '' ? NaN : parseInt(smsFreeInput.value);
    if (isNaN(smsFree)) {
        showFieldError(smsFreeInput, 'smsFreeError', 'Free SMS is required');
        isValid = false;
    } else if (smsFree < 0) {
        showFieldError(smsFreeInput, 'smsFreeError', 'Free SMS cannot be negative');
        isValid = false;
    }
    
    // 10. Description validation (required, not empty)
    const description = descriptionInput.value.trim();
    if (!description) {
        showFieldError(descriptionInput, 'descriptionError', 'Description is required');
        isValid = false;
    } else if (description.length < 5) {
        showFieldError(descriptionInput, 'descriptionError', 'Description must be at least 5 characters');
        isValid = false;
    }
    
    return isValid;
}

//==============================================================================
//==================        SAVE PLAN (ADD/EDIT)            ======================
//==============================================================================

function savePlan(event) {
    event.preventDefault();
    
    // Run validation
    if (!validateForm()) {
        console.log('❌ Validation failed - servlet will NOT run');
        const firstInvalid = document.querySelector('.invalid');
        if (firstInvalid) {
            firstInvalid.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
        return;
    }
    
    console.log('✅ Validation passed - submitting to servlet');
    
    // Get all validated values
    const planName = planNameInput.value.trim();
    const monthlyFee = parseFloat(monthlyFeeInput.value);
    const voiceAllowance = parseInt(minutesInput.value);
    const dataAllowance = parseInt(dataInput.value);
    const smsAllowance = parseInt(smsInput.value);
    const voiceFree = parseInt(voiceFreeInput.value);
    const dataFree = parseInt(dataFreeInput.value);
    const smsFree = parseInt(smsFreeInput.value);
    const description = descriptionInput.value.trim();
    const color = planColorInput.value;
    
    // Create form to submit to servlet
    const form = document.createElement('form');
    form.method = 'POST';
    form.style.display = 'none';
    
    if (currentEditId) {
        form.action = '/Billing-System-Web-GUI/EditPlanServlet';
    } else {
        form.action = '/Billing-System-Web-GUI/AddPlanServlet';
    }
    
    // Add all parameters with proper names for servlet
    // These names will be used in request.getParameter() in the servlet
    
    // Hidden field for plan ID (only for edit)
    if (currentEditId) {
        const planIdField = document.createElement('input');
        planIdField.type = 'hidden';
        planIdField.name = 'planid';
        planIdField.value = currentEditId;
        form.appendChild(planIdField);
    }
    
    // Plan Name
    const planNameField = document.createElement('input');
    planNameField.type = 'hidden';
    planNameField.name = 'planname';
    planNameField.value = planName;
    form.appendChild(planNameField);
    
    // Monthly Fee
    const monthlyFeeField = document.createElement('input');
    monthlyFeeField.type = 'hidden';
    monthlyFeeField.name = 'monthlyfee';
    monthlyFeeField.value = monthlyFee;
    form.appendChild(monthlyFeeField);
    
    // Description
    const descriptionField = document.createElement('input');
    descriptionField.type = 'hidden';
    descriptionField.name = 'description';
    descriptionField.value = description;
    form.appendChild(descriptionField);
    
    // Status (always active for new/edited plans)
    const statusField = document.createElement('input');
    statusField.type = 'hidden';
    statusField.name = 'isactive';
    statusField.value = 'true';
    form.appendChild(statusField);
    
    // Voice Minutes
    const voiceAllowanceField = document.createElement('input');
    voiceAllowanceField.type = 'hidden';
    voiceAllowanceField.name = 'voice_allowance';
    voiceAllowanceField.value = voiceAllowance;
    form.appendChild(voiceAllowanceField);
    
    // Data Allowance (MB)
    const dataAllowanceField = document.createElement('input');
    dataAllowanceField.type = 'hidden';
    dataAllowanceField.name = 'data_allowance';
    dataAllowanceField.value = dataAllowance;
    form.appendChild(dataAllowanceField);
    
    // SMS Allowance
    const smsAllowanceField = document.createElement('input');
    smsAllowanceField.type = 'hidden';
    smsAllowanceField.name = 'sms_allowance';
    smsAllowanceField.value = smsAllowance;
    form.appendChild(smsAllowanceField);
    
    // Free Voice Minutes
    const voiceFreeField = document.createElement('input');
    voiceFreeField.type = 'hidden';
    voiceFreeField.name = 'voice_free';
    voiceFreeField.value = voiceFree;
    form.appendChild(voiceFreeField);
    
    // Free Data (MB)
    const dataFreeField = document.createElement('input');
    dataFreeField.type = 'hidden';
    dataFreeField.name = 'data_free';
    dataFreeField.value = dataFree;
    form.appendChild(dataFreeField);
    
    // Free SMS
    const smsFreeField = document.createElement('input');
    smsFreeField.type = 'hidden';
    smsFreeField.name = 'sms_free';
    smsFreeField.value = smsFree;
    form.appendChild(smsFreeField);
    
    // Color
    const colorField = document.createElement('input');
    colorField.type = 'hidden';
    colorField.name = 'color';
    colorField.value = color;
    form.appendChild(colorField);
    
    document.body.appendChild(form);
    form.submit();
}

//==============================================================================
//==================        OPEN ADD MODAL                  ======================
//==============================================================================

function openAddModal() {
    currentEditId = null;
    modalTitle.textContent = 'Add New Plan';
    planForm.reset();
    planIdInput.value = '';
    planColorInput.value = '#38bdf8';
    monthlyFeeInput.value = '';
    minutesInput.value = '';
    dataInput.value = '';
    smsInput.value = '';
    voiceFreeInput.value = '';
    dataFreeInput.value = '';
    smsFreeInput.value = '';
    descriptionInput.value = '';
    clearValidation();
    modal.style.display = 'block';
}

//==============================================================================
//==================        EDIT PLAN                       ======================
//==============================================================================

function editPlan(planId) {
    const plan = plansData.find(p => p.planid === planId);
    
    if (!plan) {
        showToast('Plan not found', 'error');
        return;
    }
    
    currentEditId = planId;
    modalTitle.textContent = 'Edit Plan';
    
    planIdInput.value = plan.planid;
    planNameInput.value = plan.planname || '';
    planColorInput.value = plan.color || '#38bdf8';
    monthlyFeeInput.value = plan.monthlyfee || '';
    minutesInput.value = plan.voice_allowance || '';
    dataInput.value = plan.data_allowance || '';
    smsInput.value = plan.sms_allowance || '';
    voiceFreeInput.value = plan.voice_free || '';
    dataFreeInput.value = plan.data_free || '';
    smsFreeInput.value = plan.sms_free || '';
    descriptionInput.value = plan.description || '';
    
    clearValidation();
    modal.style.display = 'block';
}

//==============================================================================
//==================        TOGGLE PLAN STATUS              ======================
//==============================================================================

function togglePlanStatus(planId) {
    const plan = plansData.find(p => p.planid === planId);
    
    if (!plan) {
        showToast('Plan not found', 'error');
        return;
    }
    
    const newStatus = plan.isactive !== false ? false : true;
    
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = '/Billing-System-Web-GUI/UpdatePlanStatusServlet';
    
    const planIdInput = document.createElement('input');
    planIdInput.type = 'hidden';
    planIdInput.name = 'planid';
    planIdInput.value = planId;
    form.appendChild(planIdInput);
    
    const statusInput = document.createElement('input');
    statusInput.type = 'hidden';
    statusInput.name = 'status';
    statusInput.value = newStatus;
    form.appendChild(statusInput);
    
    document.body.appendChild(form);
    form.submit();
}

//==============================================================================
//==================        CLOSE MODAL                     ======================
//==============================================================================

function closeModal() {
    modal.style.display = 'none';
    planForm.reset();
    currentEditId = null;
    clearValidation();
}

//==============================================================================
//==================        CHECK IF PAGE IS RELOADED      ======================
//==============================================================================

function checkPageReload() {
    const urlParams = new URLSearchParams(window.location.search);
    const success = urlParams.get('success');
    const message = urlParams.get('message');
    
    if (success === 'true' && message) {
        showToast(decodeURIComponent(message));
        const newUrl = window.location.pathname;
        window.history.replaceState({}, document.title, newUrl);
    } else if (success === 'false' && message) {
        showToast(decodeURIComponent(message), 'error');
        window.history.replaceState({}, document.title, window.location.pathname);
    }
}

//==============================================================================
//==================        REAL-TIME VALIDATION           ======================
//==============================================================================

function setupRealTimeValidation() {
    const inputs = [
        { element: planNameInput, errorId: 'planNameError', validate: () => planNameInput.value.trim().length >= 2, message: 'Plan name must be at least 2 characters' },
        { element: monthlyFeeInput, errorId: 'monthlyFeeError', validate: () => parseFloat(monthlyFeeInput.value) > 0, message: 'Monthly fee must be greater than 0' },
        { element: minutesInput, errorId: 'minutesError', validate: () => parseInt(minutesInput.value) >= 0, message: 'Voice minutes cannot be negative' },
        { element: voiceFreeInput, errorId: 'voiceFreeError', validate: () => parseInt(voiceFreeInput.value) >= 0, message: 'Free minutes cannot be negative' },
        { element: dataInput, errorId: 'dataError', validate: () => parseInt(dataInput.value) >= 0, message: 'Data allowance cannot be negative' },
        { element: dataFreeInput, errorId: 'dataFreeError', validate: () => parseInt(dataFreeInput.value) >= 0, message: 'Free data cannot be negative' },
        { element: smsInput, errorId: 'smsError', validate: () => parseInt(smsInput.value) >= 0, message: 'SMS allowance cannot be negative' },
        { element: smsFreeInput, errorId: 'smsFreeError', validate: () => parseInt(smsFreeInput.value) >= 0, message: 'Free SMS cannot be negative' },
        { element: descriptionInput, errorId: 'descriptionError', validate: () => descriptionInput.value.trim().length >= 5, message: 'Description must be at least 5 characters' }
    ];
    
    inputs.forEach(input => {
        input.element.addEventListener('input', function() {
            if (input.validate()) {
                this.classList.remove('invalid');
                const errorDiv = document.getElementById(input.errorId);
                if (errorDiv) {
                    errorDiv.classList.remove('show');
                }
            } else if (this.value !== '') {
                this.classList.add('invalid');
                const errorDiv = document.getElementById(input.errorId);
                if (errorDiv) {
                    errorDiv.textContent = input.message;
                    errorDiv.classList.add('show');
                }
            }
        });
        
        input.element.addEventListener('blur', function() {
            if (!input.validate()) {
                this.classList.add('invalid');
                const errorDiv = document.getElementById(input.errorId);
                if (errorDiv) {
                    errorDiv.textContent = input.message;
                    errorDiv.classList.add('show');
                }
            }
        });
    });
    
    // Color validation
    planColorInput.addEventListener('input', function() {
        if (!this.value || !/^#[0-9A-Fa-f]{6}$/.test(this.value)) {
            this.classList.add('invalid');
        } else {
            this.classList.remove('invalid');
        }
    });
}

//==============================================================================
//==================        EVENT LISTENERS                 ======================
//==============================================================================

document.addEventListener('DOMContentLoaded', () => {
    checkPageReload();
    loadPlans();
    setupRealTimeValidation();
    
    openAddModalBtn.addEventListener('click', openAddModal);
    cancelModalBtn.addEventListener('click', closeModal);
    closeSpan.addEventListener('click', closeModal);
    planForm.addEventListener('submit', savePlan);
    
    window.addEventListener('click', (event) => {
        if (event.target === modal) {
            closeModal();
        }
    });
});

window.editPlan = editPlan;
window.togglePlanStatus = togglePlanStatus;