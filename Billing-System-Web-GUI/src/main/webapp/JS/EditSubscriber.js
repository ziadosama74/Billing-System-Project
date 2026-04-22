//==============================================================================
//==================        GLOBAL VARIABLES               ======================
//==============================================================================

const urlParams = new URLSearchParams(window.location.search);
const subscriberId = urlParams.get('id');

// Inputs
const subscriberIdInput = document.getElementById('subscriberId');
const msisdn = document.getElementById('msisdn');
const name = document.getElementById('name');
const internationalid = document.getElementById('internationalid');
const address = document.getElementById('address');
const selectedPlan = document.getElementById('selectedPlan');

// Status
const statusActive = document.querySelector('#statusActive input[type="radio"]');
const statusInactive = document.querySelector('#statusInactive input[type="radio"]');
const statusActiveWrapper = document.getElementById('statusActive');
const statusInactiveWrapper = document.getElementById('statusInactive');

// Container
const planContainer = document.getElementById("planCards");

// Loading indicators
let loadingRetryCount = 0;
const MAX_RETRIES = 5;
const RETRY_DELAY = 500;


//==============================================================================
//==================        CHECK IF JSON FILES ARE READY   ======================
//==============================================================================

function checkJsonFilesReady(callback, retryCount = 0) {
    const maxFileChecks = 8;
    const checkDelay = 300;
    
    Promise.all([
        fetch('/Billing-System-Web-GUI/JSON/plans_data.json?t=' + Date.now(), { method: 'HEAD' }),
        fetch('/Billing-System-Web-GUI/JSON/subscriber_data.json?t=' + Date.now(), { method: 'HEAD' })
    ]).then(responses => {
        const allOk = responses.every(res => res.ok);
        if (allOk) {
            console.log("Both JSON files are ready");
            callback();
        } else if (retryCount < maxFileChecks) {
            console.log(`Waiting for JSON files... (attempt ${retryCount + 1}/${maxFileChecks})`);
            setTimeout(() => checkJsonFilesReady(callback, retryCount + 1), checkDelay);
        } else {
            console.warn("JSON files not ready after maximum checks, attempting to load anyway");
            callback();
        }
    }).catch(error => {
        console.error("Error checking JSON files:", error);
        if (retryCount < maxFileChecks) {
            setTimeout(() => checkJsonFilesReady(callback, retryCount + 1), checkDelay);
        } else {
            callback();
        }
    });
}

//==============================================================================
//==================        LOAD PLANS FUNCTION (WITH RETRY) ======================
//==============================================================================

function loadPlans(currentPlanId, retryCount = 0) {
    // Show loading state
    if (retryCount === 0) {
        planContainer.innerHTML = '<div style="text-align:center; padding:20px;">Loading plans...</div>';
    } else {
        planContainer.innerHTML = `<div style="text-align:center; padding:20px;">Loading plans... (attempt ${retryCount + 1}/${MAX_RETRIES + 1})</div>`;
    }
    
    const fetchUrl = '/Billing-System-Web-GUI/JSON/plans_data.json?t=' + Date.now() + '_' + Math.random();
    
    fetch(fetchUrl)
        .then(res => {
            if (!res.ok) {
                throw new Error(`HTTP ${res.status}: ${res.statusText}`);
            }
            return res.json();
        })
        .then(plans => {
            // Validate plans data
            if (!plans || !Array.isArray(plans) || plans.length === 0) {
                throw new Error("Invalid or empty plans data");
            }
            
            console.log(`Successfully loaded ${plans.length} plans`);
            renderPlans(plans, currentPlanId);
            loadingRetryCount = 0; // Reset retry counter on success
        })
        .catch(err => {
            console.error(`Error loading plans (attempt ${retryCount + 1}/${MAX_RETRIES + 1}):`, err);
            
            if (retryCount < MAX_RETRIES) {
                // Retry with exponential backoff
                const delay = RETRY_DELAY * Math.pow(1.5, retryCount);
                console.log(`Retrying in ${delay}ms...`);
                setTimeout(() => loadPlans(currentPlanId, retryCount + 1), delay);
            } else {
                planContainer.innerHTML = `
                    <div style="text-align:center; padding:20px; color:#d32f2f;">
                        <strong>Error loading plans</strong><br>
                        Please <a href="javascript:location.reload()">refresh the page</a> or try again later.
                    </div>
                `;
                console.error("Failed to load plans after maximum retries");
            }
        });
}

//==============================================================================
//==================        RENDER PLANS FUNCTION           ======================
//==============================================================================

function renderPlans(plans, currentPlanId) {
    if (!planContainer) {
        console.error("Plan container not found");
        return;
    }
    
    planContainer.innerHTML = "";
    let foundSelected = false;
    
    // Store plans in sessionStorage as backup
    try {
        sessionStorage.setItem("cachedPlans", JSON.stringify(plans));
        sessionStorage.setItem("cachedPlansTimestamp", Date.now().toString());
    } catch (e) {
        console.warn("Could not cache plans in sessionStorage:", e);
    }

    plans.forEach((plan, index) => {
        const card = document.createElement("div");
        card.classList.add("plan-card");
        card.dataset.plan = plan.planid;
        card.dataset.index = index;

        // Check if this plan should be selected
        const isSelected = Number(plan.planid) === Number(currentPlanId);
        if (isSelected) {
            card.classList.add("active");
            selectedPlan.value = plan.planid;
            console.log("ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ = " + selectedPlan.value);
            foundSelected = true;
        }

        // Use default values if properties are missing
        const planName = plan.planname || "Unnamed Plan";
        const planColor = plan.color || "#1976d2";
        const planDescription = plan.description || "No description available";
        const monthlyFee = plan.monthlyfee || 0;

        card.innerHTML = `
            <div class="plan-card-inner">
                <div class="plan-name" style="color:${planColor}; font-weight:bold; margin-bottom:8px;">
                    ${escapeHtml(planName)}
                </div>
                <div style="margin:8px 0; font-size:0.9em;">${escapeHtml(planDescription)}</div>
                <div style="margin-top:8px; font-weight:bold; color:#F2EBEB;">
                    ${monthlyFee} EGP
                </div>
            </div>
        `;

        // Click selection handler
        card.addEventListener("click", (e) => {
            e.stopPropagation();
            
            // Remove active class from all cards
            document.querySelectorAll(".plan-card").forEach(c => {
                c.classList.remove("active");
            });
            
            // Add active class to clicked card
            card.classList.add("active");
            
            // Update hidden input value
            selectedPlan.value = plan.planid;
            
            console.log(`Selected plan: ${planName} (ID: ${plan.planid})`);
        });

        planContainer.appendChild(card);
    });
    
    // If no plan was selected but we have plans, select the first one
    if (!foundSelected && plans.length > 0) {
        const firstCard = planContainer.querySelector(".plan-card");
        if (firstCard) {
            firstCard.classList.add("active");
            selectedPlan.value = plans[0].planid;
            console.log(`No matching plan found. Defaulted to first plan: ${plans[0].planname}`);
        }
    }
    
    console.log(`Rendered ${plans.length} plans, selected plan ID: ${selectedPlan.value || 'none'}`);
}

// Helper function to escape HTML
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

//==============================================================================
//==================     LOAD SUBSCRIBER FUNCTION          ======================
//==============================================================================

function loadSubscriber(retryCount = 0) {
    const maxSubscriberRetries = 3;
    
    fetch('/Billing-System-Web-GUI/JSON/subscriber_data.json?t=' + Date.now() + '_' + Math.random())
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            // Validate subscriber data
            if (!data || typeof data !== 'object') {
                throw new Error("Invalid subscriber data");
            }
            
            console.log("Subscriber data loaded:", data);
            
            // Populate form fields
            subscriberIdInput.value = data.subscriberid || "";
            name.value = data.name || "";
            msisdn.value = data.msisdn || "";
            internationalid.value = data.internationalid || "";
            address.value = data.address || "";

            // Determine the plan ID to use
            let planIdToUse = data.planid || 1;
            
            // Check for recently selected plan from add plan operation
            const recentlySelectedPlan = sessionStorage.getItem("selectedPlanId");
            if (recentlySelectedPlan) {
                planIdToUse = recentlySelectedPlan;
                sessionStorage.removeItem("selectedPlanId");
                console.log(`Using recently selected plan ID: ${planIdToUse}`);
            }
            
            // Also check URL parameter for plan ID
            const urlPlanId = urlParams.get('planId');
            if (urlPlanId && !recentlySelectedPlan) {
                planIdToUse = urlPlanId;
                console.log(`Using plan ID from URL: ${planIdToUse}`);
            }

            // Load plans with the determined plan ID
            loadPlans(planIdToUse);

            // Update status UI
            statusActiveWrapper.classList.remove('selected');
            statusInactiveWrapper.classList.remove('selected');

            if (data.isactive === true || data.isactive === "true") {
                statusActive.checked = true;
                statusActiveWrapper.classList.add('selected');
            } else {
                statusInactive.checked = true;
                statusInactiveWrapper.classList.add('selected');
            }
        })
        .catch(error => {
            console.error(`Error loading subscriber (attempt ${retryCount + 1}/${maxSubscriberRetries + 1}):`, error);
            
            if (retryCount < maxSubscriberRetries) {
                setTimeout(() => loadSubscriber(retryCount + 1), 500);
            } else {
                // Show error in form
                if (subscriberIdInput) {
                    subscriberIdInput.placeholder = "Error loading data";
                }
                console.error("Failed to load subscriber after multiple retries");
            }
        });
}

//==============================================================================
//==================        TRY LOAD FROM CACHE            ======================
//==============================================================================

function tryLoadFromCache() {
    const cachedPlans = sessionStorage.getItem("cachedPlans");
    const cachedTimestamp = sessionStorage.getItem("cachedPlansTimestamp");
    
    if (cachedPlans && cachedTimestamp) {
        const age = Date.now() - parseInt(cachedTimestamp);
        // Use cache if less than 5 minutes old
        if (age < 5 * 60 * 1000) {
            try {
                const plans = JSON.parse(cachedPlans);
                console.log("Loading plans from cache (age: " + Math.round(age / 1000) + "s)");
                
                // Get selected plan from session or use default
                let savedPlanId = sessionStorage.getItem("selectedPlanId");
                if (!savedPlanId) {
                    savedPlanId = urlParams.get('planId') || "1";
                }
                
                renderPlans(plans, savedPlanId);
                return true;
            } catch (e) {
                console.warn("Failed to parse cached plans:", e);
            }
        }
    }
    return false;
}

//==============================================================================
//==================        REFRESH FUNCTIONS              ======================
//==============================================================================

function refreshData() {
    console.log("Refreshing data...");
    
    // Try to show cached plans immediately for better UX
    const hasCache = tryLoadFromCache();
    
    if (hasCache) {
        // Still fetch fresh data in background
        setTimeout(() => {
            console.log("Fetching fresh data in background...");
            checkJsonFilesReady(() => {
                loadSubscriber();
            });
        }, 100);
    } else {
        // No cache, wait for files to be ready
        planContainer.innerHTML = '<div style="text-align:center; padding:20px;">Waiting for data...</div>';
        checkJsonFilesReady(() => {
            loadSubscriber();
        });
    }
}

//==============================================================================
//==================        INITIAL LOAD                  ========================
//==============================================================================

document.addEventListener("DOMContentLoaded", () => {
    console.log("DOM Content Loaded, initializing...");
    refreshData();
});

//==============================================================================
//==================   HANDLE BACK BUTTON                ========================
//==============================================================================

window.addEventListener("pageshow", function(event) {
    if (event.persisted) {
        console.log("Page restored from bfcache, refreshing...");
        setTimeout(() => refreshData(), 100);
    }
});

//==============================================================================
//==================   AFTER SAVE SUBSCRIBER             ========================
//==============================================================================

function onSaveSuccess() {
    console.log("Save successful, refreshing data...");
    // Clear any cached data to force fresh load
    sessionStorage.removeItem("cachedPlans");
    sessionStorage.removeItem("cachedPlansTimestamp");
    refreshData();
}

//==============================================================================
//==================   AFTER ADD PLAN                    ========================
//==============================================================================

function onAddPlanSuccess(newPlanId) {
    console.log(`Add plan success, new plan ID: ${newPlanId}`);
    
    // Save selected plan before reload
    sessionStorage.setItem("selectedPlanId", newPlanId);
    
    // Clear cache to force fresh load
    sessionStorage.removeItem("cachedPlans");
    sessionStorage.removeItem("cachedPlansTimestamp");
    
    // Reload page with cache-busting
    const reloadUrl = window.location.pathname + window.location.search + "&reload=" + Date.now();
    window.location.href = reloadUrl;
}

//==============================================================================
//==================   EXPORT FUNCTIONS FOR GLOBAL USE    ======================
//==============================================================================

// Make functions available globally
window.onSaveSuccess = onSaveSuccess;
window.onAddPlanSuccess = onAddPlanSuccess;
window.refreshData = refreshData;
window.loadPlans = loadPlans;






//==============================================================================
//==================   STATUS SELECTION HANDLER (ADDED)   ======================
//==============================================================================

/**
 * Updates the 'selected' class on the status labels based on which radio is checked.
 */
function updateStatusSelection() {
    const activeRadio = document.querySelector('#statusActive input[type="radio"]');
    const inactiveRadio = document.querySelector('#statusInactive input[type="radio"]');
    const activeLabel = document.getElementById('statusActive');
    const inactiveLabel = document.getElementById('statusInactive');

    if (activeRadio.checked) {
        activeLabel.classList.add('selected');
        inactiveLabel.classList.remove('selected');
    } else if (inactiveRadio.checked) {
        inactiveLabel.classList.add('selected');
        activeLabel.classList.remove('selected');
    } else {
        // If none selected, remove both (optional fallback)
        activeLabel.classList.remove('selected');
        inactiveLabel.classList.remove('selected');
    }
}

/**
 * Attaches event listeners to the status options to handle switching.
 */
function initStatusHandlers() {
    const activeRadio = document.querySelector('#statusActive input[type="radio"]');
    const inactiveRadio = document.querySelector('#statusInactive input[type="radio"]');
    const activeLabel = document.getElementById('statusActive');
    const inactiveLabel = document.getElementById('statusInactive');

    // Listen for changes on the radio buttons themselves
    if (activeRadio) activeRadio.addEventListener('change', updateStatusSelection);
    if (inactiveRadio) inactiveRadio.addEventListener('change', updateStatusSelection);

    // Also allow clicking on the label wrapper to ensure class updates
    // (the radio's change event will fire anyway, but this covers any edge cases)
    if (activeLabel) {
        activeLabel.addEventListener('click', function(e) {
            // If click is directly on the radio, change event already fires
            if (e.target.type !== 'radio') {
                // Small delay to let the radio get checked
                setTimeout(updateStatusSelection, 10);
            }
        });
    }
    if (inactiveLabel) {
        inactiveLabel.addEventListener('click', function(e) {
            if (e.target.type !== 'radio') {
                setTimeout(updateStatusSelection, 10);
            }
        });
    }
}

// Call the initializer after DOM is ready (without removing existing DOMContentLoaded)
// We wrap it to avoid overwriting the existing listener
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initStatusHandlers);
} else {
    initStatusHandlers();
}







//==============================================================================
//==================        FORM VALIDATION (ADDED)        ======================
//==============================================================================

/**
 * Removes 'invalid' class and error message from a field.
 */
function clearInvalid(element) {
    if (element) {
        element.classList.remove('invalid');
        const errorSpan = element.parentElement?.querySelector('.error-message');
        if (errorSpan) errorSpan.remove();
    }
}

/**
 * Adds 'invalid' class and shows an error message for a field.
 */
function markInvalid(element, message) {
    if (element) {
        element.classList.add('invalid');
        
        // Remove existing error for this field
        const existingError = element.parentElement?.querySelector('.error-message');
        if (existingError) existingError.remove();
        
        // Create new error message
        const errorSpan = document.createElement('span');
        errorSpan.className = 'error-message';
        errorSpan.style.color = '#d32f2f';
        errorSpan.style.fontSize = '0.8rem';
        errorSpan.style.marginTop = '4px';
        errorSpan.style.display = 'block';
        errorSpan.textContent = message;
        element.parentElement?.appendChild(errorSpan);
    }
}

/**
 * Validates the entire form.
 * @returns {boolean} true if all fields are valid, false otherwise.
 */
function validateForm() {
    let isValid = true;
    
    // 1. Name: required and at least 3 characters
    const nameValue = name.value.trim();
    if (nameValue === '') {
        markInvalid(name, 'Name is required.');
        isValid = false;
    } else if (nameValue.length < 3) {
        markInvalid(name, 'Name must be at least 3 characters.');
        isValid = false;
    } else {
        clearInvalid(name);
    }
    
    // 2. MSISDN: must start with 010, 011, 012, or 015 and be exactly 11 digits
    const msisdnValue = msisdn.value.trim();
    const msisdnPattern = /^(010|011|012|015)\d{8}$/;
    if (!msisdnValue) {
        markInvalid(msisdn, 'MSISDN is required.');
        isValid = false;
    } else if (!msisdnPattern.test(msisdnValue)) {
        markInvalid(msisdn, 'MSISDN must start with 010, 011, 012 or 015 and be 11 digits long.');
        isValid = false;
    } else {
        clearInvalid(msisdn);
    }
    
    // 3. International ID: exactly 8 digits
    const intIdValue = internationalid.value.trim();
    const intIdPattern = /^\d{8}$/;
    if (!intIdValue) {
        markInvalid(internationalid, 'International ID is required.');
        isValid = false;
    } else if (!intIdPattern.test(intIdValue)) {
        markInvalid(internationalid, 'International ID must be exactly 8 digits.');
        isValid = false;
    } else {
        clearInvalid(internationalid);
    }
    
    // 4. Address: required and at least 5 characters (e.g., "Giza, Egypt")
    const addressValue = address.value.trim();
    if (addressValue === '') {
        markInvalid(address, 'Address is required.');
        isValid = false;
    } else if (addressValue.length < 5) {
        markInvalid(address, 'Please enter a complete address (at least 5 characters).');
        isValid = false;
    } else {
        clearInvalid(address);
    }
    
    // 5. Status: one radio must be selected
    const activeRadio = document.querySelector('#statusActive input[type="radio"]');
    const inactiveRadio = document.querySelector('#statusInactive input[type="radio"]');
    const isStatusSelected = (activeRadio && activeRadio.checked) || (inactiveRadio && inactiveRadio.checked);
    const statusContainer = document.querySelector('.status-toggle');
    if (!isStatusSelected) {
        const existingError = statusContainer?.parentElement?.querySelector('.error-message');
        if (!existingError) {
            const errorSpan = document.createElement('span');
            errorSpan.className = 'error-message';
            errorSpan.style.color = '#d32f2f';
            errorSpan.style.fontSize = '0.8rem';
            errorSpan.style.marginTop = '4px';
            errorSpan.style.display = 'block';
            errorSpan.textContent = 'Please select a status (Active or Inactive).';
            statusContainer?.parentElement?.appendChild(errorSpan);
        }
        isValid = false;
    } else {
        const statusError = statusContainer?.parentElement?.querySelector('.error-message');
        if (statusError) statusError.remove();
    }
    
    // 6. Plan: a plan card must be active and selectedPlan must have a value
    const activePlanCard = document.querySelector('.plan-card.active');
    if (!activePlanCard || !selectedPlan.value) {
        const existingError = planContainer.parentElement?.querySelector('.error-message');
        if (!existingError) {
            const errorSpan = document.createElement('span');
            errorSpan.className = 'error-message';
            errorSpan.style.color = '#d32f2f';
            errorSpan.style.fontSize = '0.8rem';
            errorSpan.style.marginTop = '8px';
            errorSpan.style.display = 'block';
            errorSpan.textContent = 'Please select a plan.';
            planContainer.parentElement?.appendChild(errorSpan);
        }
        isValid = false;
    } else {
        const planError = planContainer.parentElement?.querySelector('.error-message');
        if (planError) planError.remove();
    }
    
    return isValid;
}

/**
 * Handles form submission – validates first, prevents servlet if invalid.
 */
function onFormSubmit(event) {
    // Prevent default form submission
    if (event) event.preventDefault();
    
    // Clear previous validation styles
    document.querySelectorAll('.invalid').forEach(el => el.classList.remove('invalid'));
    document.querySelectorAll('.error-message').forEach(el => el.remove());
    
    if (validateForm()) {
        // Validation passed – submit the form to servlet
        const form = document.getElementById('editForm');
        if (form) {
            console.log('Validation passed, submitting form...');
            form.submit();
        }
    } else {
        console.log('Form validation failed – servlet will NOT run.');
        // Scroll to first invalid field
        const firstInvalid = document.querySelector('.invalid');
        if (firstInvalid) {
            firstInvalid.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
    }
}

// Attach validation to the form's submit event
document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('editForm');
    if (form) {
        form.addEventListener('submit', onFormSubmit);
        console.log('Validation attached to editForm');
    } else {
        console.error('Form with id="editForm" not found');
    }
});