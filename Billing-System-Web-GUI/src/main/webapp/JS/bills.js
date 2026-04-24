//==============================================================================
//==================        GLOBAL VARIABLES               ======================
//==============================================================================

let allInvoices = [];
let currentInvoice = null;

// DOM Elements
const monthSelect = document.getElementById('monthSelect');
const searchForm = document.getElementById('searchForm');
const invoicesTableBody = document.getElementById('invoicesTableBody');
const totalInvoicesSpan = document.getElementById('totalInvoices');
const totalAmountSpan = document.getElementById('totalAmount');
const modal = document.getElementById('invoiceModal');
const modalBody = document.getElementById('invoiceModalBody');
const closeModalBtn = document.getElementById('closeModalBtn');
const closeSpan = document.querySelector('.close');

//==============================================================================
//==================        LOAD MONTHS INTO SELECT        ======================
//==============================================================================

function loadMonths() {
    const months = [
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    ];
    
    const currentYear = new Date().getFullYear();
    const currentMonth = new Date().getMonth();
    
    // Clear existing options except "All Months"
    monthSelect.innerHTML = '<option value="">All Months</option>';
    
    months.forEach((month, index) => {
        let option = document.createElement("option");
        option.value = month;
        option.text = `${month} ${currentYear}`;
        if (index === currentMonth) {
            option.selected = true;
        }
        monthSelect.appendChild(option);
    });
}

//==============================================================================
//==================        FORMAT DATE FROM TIMESTAMP     ======================
//==============================================================================

function formatDateFromTimestamp(timestamp) {
    if (!timestamp) return 'N/A';
    try {
        const date = new Date(timestamp);
        return date.toLocaleDateString('en-US', { 
            year: 'numeric', 
            month: 'long', 
            day: 'numeric' 
        });
    } catch (e) {
        return timestamp.toString();
    }
}

function formatDate(dateValue) {
    if (!dateValue) return 'N/A';
    if (typeof dateValue === 'number') {
        return formatDateFromTimestamp(dateValue);
    }
    if (typeof dateValue === 'string') {
        try {
            const date = new Date(dateValue);
            if (!isNaN(date.getTime())) {
                return date.toLocaleDateString('en-US', { 
                    year: 'numeric', 
                    month: 'long', 
                    day: 'numeric' 
                });
            }
        } catch (e) {}
        return dateValue;
    }
    return 'N/A';
}

//==============================================================================
//==================        FORMAT CURRENCY                 ======================
//==============================================================================

function formatCurrency(amount) {
    if (!amount && amount !== 0) return 'EGP 0';
    return new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'EGP',
        minimumFractionDigits: 2
    }).format(amount);
}

//==============================================================================
//==================        ESCAPE HTML                    ======================
//==============================================================================

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

//==============================================================================
//==================        LOAD INVOICES FROM JSON        =====================
//==============================================================================

async function loadInvoices(msisdn = '', month = null) {
    try {
        invoicesTableBody.innerHTML = `
            <tr class="loading-row">
                <td colspan="7">
                    <div class="loading-spinner">
                        <i class="fa-solid fa-spinner fa-spin"></i> Loading invoices...
                    </div>
                </td>
            </tr>
        `;
        
        const timestamp = Date.now();
        const response = await fetch(`/Billing-System-Web-GUI/JSON/Invoices_data.json?t=${timestamp}`);
        
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        
        let invoices = await response.json();

        if (!Array.isArray(invoices)) {
            invoices = [];
        }

        // ================== FILTER LOGIC ==================
        const currentMonth = new Date().toLocaleString('en-US', { month: 'long' });

        if (month === null) {
            // ✅ First load → current month only
            invoices = invoices.filter(inv => inv.invoice_Month === currentMonth);
        } else if (month === '') {
            // ✅ All Months → no filter
        } else {
            // ✅ Specific month selected
            invoices = invoices.filter(inv => inv.invoice_Month === month);
        }

        // ================== FILTER BY MSISDN ==================
        if (msisdn && msisdn.trim() !== '') {
            invoices = invoices.filter(
                inv => inv.msisdn === msisdn || inv.MSISDN === msisdn
            );
        }

        // ================== UPDATE UI ==================
        allInvoices = invoices;
        renderInvoices(allInvoices);
        updateSummary(allInvoices);
        
    } catch (error) {
        console.error('Error loading invoices:', error);
        invoicesTableBody.innerHTML = `
            <tr class="loading-row">
                <td colspan="7">
                    <div class="loading-spinner" style="color: #ef4444;">
                        <i class="fa-solid fa-exclamation-circle"></i> Failed to load invoices: ${error.message}
                    </div>
                </td>
            </tr>
        `;
    }
}

//==============================================================================
//==================        RENDER INVOICES IN TABLE       ======================
//==============================================================================

function renderInvoices(invoices) {
    if (!invoices || invoices.length === 0) {
        invoicesTableBody.innerHTML = `
            <tr class="loading-row">
                <td colspan="7">
                    <div class="loading-spinner">
                        <i class="fa-solid fa-info-circle"></i> No invoices found
                    </div>
                </td>
            </tr>
        `;
        return;
    }
    
    invoicesTableBody.innerHTML = '';
    
    invoices.forEach(invoice => {
        const row = document.createElement('tr');
        
        // Get first letter of subscriber name for avatar
        const firstLetter = invoice.name ? invoice.name.charAt(0).toUpperCase() : '?';
        
        // Format the due date
        const dueDate = invoice.invoice_Due_date ? formatDate(invoice.invoice_Due_date) : 'N/A';
        const billMonth = invoice.invoice_Month || 'N/A';
        
        row.innerHTML = `
            <td>#${invoice.invoice_ID || invoice.id || 'N/A'}</td>
            <td class="user-cell">
                <div class="avatar">${escapeHtml(firstLetter)}</div>
                <span>${escapeHtml(invoice.name || 'N/A')}</span>
            </td>
            <td>${escapeHtml(invoice.msisdn || 'N/A')}</td>
            <td>${escapeHtml(billMonth)}</td>
            <td>${escapeHtml(dueDate)}</td>
            <td class="amount">${formatCurrency(invoice.invoice_Total_Amount || 0)}</td>
            <td class="actions">
                <button class="view-btn" onclick="viewInvoice(${invoice.invoice_ID})">
                    <i class="fa-solid fa-eye"></i>
                </button>
            </td>
        `;
        
        invoicesTableBody.appendChild(row);
    });
}

//==============================================================================
//==================        UPDATE SUMMARY CARDS           ======================
//==============================================================================

function updateSummary(invoices) {
    const totalCount = invoices.length;
    const totalAmount = invoices.reduce((sum, invoice) => sum + (invoice.invoice_Total_Amount || 0), 0);
    
    totalInvoicesSpan.textContent = totalCount;
    totalAmountSpan.textContent = formatCurrency(totalAmount);
}

//==============================================================================
//==================        VIEW INVOICE DETAILS           ======================
//==============================================================================

function viewInvoice(invoiceId) {
    try {
        // Find the invoice from the loaded data
        const invoice = allInvoices.find(inv => inv.invoice_ID === invoiceId);
        
        if (!invoice) {
            console.error('Invoice not found:', invoiceId);
            return;
        }
        
        currentInvoice = invoice;
        displayInvoiceDetails(invoice);
        modal.style.display = 'block';
        
    } catch (error) {
        console.error('Error loading invoice details:', error);
        alert('Failed to load invoice details. Please try again.');
    }
}

//==============================================================================
//==================        DISPLAY INVOICE DETAILS        ======================
//==============================================================================

function displayInvoiceDetails(invoice) {
    const dueDate = invoice.invoice_Due_date ? formatDate(invoice.invoice_Due_date) : 'N/A';
    const billMonth = invoice.invoice_Month || 'N/A';
    
    modalBody.innerHTML = `
        <div class="invoice-details">
            <div class="invoice-header">
                <h3><i class="fa-solid fa-building"></i> Postpaid Billing System</h3>
                <p>Official Invoice</p>
            </div>
            
            <div class="detail-row">
                <span class="detail-label"><i class="fa-solid fa-hashtag"></i> Invoice ID:</span>
                <span class="detail-value">#${invoice.invoice_ID}</span>
            </div>
            
            <div class="detail-row">
                <span class="detail-label"><i class="fa-solid fa-user"></i> Subscriber Name:</span>
                <span class="detail-value">${escapeHtml(invoice.name || 'N/A')}</span>
            </div>
            
            <div class="detail-row">
                <span class="detail-label"><i class="fa-solid fa-phone"></i> MSISDN:</span>
                <span class="detail-value">${escapeHtml(invoice.msisdn || 'N/A')}</span>
            </div>
            
            <div class="detail-row">
                <span class="detail-label"><i class="fa-solid fa-calendar"></i> Bill Month:</span>
                <span class="detail-value">${escapeHtml(billMonth)}</span>
            </div>
            
            <div class="detail-row">
                <span class="detail-label"><i class="fa-solid fa-calendar-check"></i> Due Date:</span>
                <span class="detail-value">${escapeHtml(dueDate)}</span>
            </div>
            
            <div class="detail-row">
                <span class="detail-label"><i class="fa-solid fa-address-card"></i> Address:</span>
                <span class="detail-value">${escapeHtml(invoice.address || 'N/A')}</span>
            </div>
            
            <div class="detail-row">
                <span class="detail-label"><i class="fa-solid fa-layer-group"></i> Plan:</span>
                <span class="detail-value">${escapeHtml(invoice.plan || 'N/A')}</span>
            </div>
            
            <div class="detail-row total-row">
                <span class="detail-label"><i class="fa-solid fa-dollar-sign"></i> Total Amount:</span>
                <span class="detail-value">${formatCurrency(invoice.invoice_Total_Amount || 0)}</span>
            </div>
            
            <div class="detail-row">
                <span class="detail-label"><i class="fa-solid fa-flag-checkered"></i> Status:</span>
                <span class="detail-value">
                    ${invoice.status ? 
                        '<span style="color: #22c55e;"><i class="fa-solid fa-check-circle"></i> Paid</span>' : 
                        '<span style="color: #ef4444;"><i class="fa-solid fa-times-circle"></i> Unpaid</span>'}
                </span>
            </div>
        </div>
    `;
}

//==============================================================================
//==================        PRINT INVOICE                  ======================
//==============================================================================

function printInvoice() {
    const printContent = document.getElementById('invoiceModalBody').innerHTML;
    const printWindow = window.open('', '_blank');
    printWindow.document.write(`
        <!DOCTYPE html>
        <html>
        <head>
            <title>Print Invoice</title>
            <style>
                body {
                    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                    padding: 40px;
                    color: #333;
                    background: white;
                }
                .invoice-details {
                    max-width: 800px;
                    margin: 0 auto;
                }
                .invoice-header {
                    text-align: center;
                    margin-bottom: 30px;
                    padding-bottom: 20px;
                    border-bottom: 2px solid #38bdf8;
                }
                .invoice-header h3 {
                    color: #38bdf8;
                    margin-bottom: 5px;
                }
                .detail-row {
                    display: flex;
                    justify-content: space-between;
                    padding: 10px 0;
                    border-bottom: 1px solid #ddd;
                }
                .detail-label {
                    font-weight: bold;
                    color: #666;
                }
                .detail-value {
                    font-weight: normal;
                }
                .total-row {
                    margin-top: 20px;
                    padding-top: 20px;
                    border-top: 2px solid #22c55e;
                }
                .total-row .detail-label {
                    font-size: 18px;
                    color: #22c55e;
                }
                .total-row .detail-value {
                    font-size: 22px;
                    font-weight: bold;
                    color: #22c55e;
                }
                @media print {
                    body {
                        padding: 20px;
                    }
                }
            </style>
        </head>
        <body>
            ${printContent}
            <script>window.print();<\/script>
        </body>
        </html>
    `);
    printWindow.document.close();
}

//==============================================================================
//==================        CLOSE MODAL                    ======================
//==============================================================================

function closeModal() {
    modal.style.display = 'none';
}

//==============================================================================
//==================        HANDLE FORM SUBMIT             ======================
//==============================================================================

function handleSearchSubmit(event) {
    event.preventDefault();
    const formData = new FormData(searchForm);
    const msisdn = formData.get('msisdn');
    const month = formData.get('month');
    loadInvoices(msisdn, month);
}

//==============================================================================
//==================        INITIALIZE PAGE                ======================
//==============================================================================

document.addEventListener('DOMContentLoaded', () => {
    // Load months into select dropdown
    loadMonths();
    
    // Load invoices from JSON file
    loadInvoices('', null);
    
    // Set up event listeners
    if (searchForm) {
        searchForm.addEventListener('submit', handleSearchSubmit);
    }
    if (closeModalBtn) {
        closeModalBtn.addEventListener('click', closeModal);
    }
    if (closeSpan) {
        closeSpan.addEventListener('click', closeModal);
    }
    
    // Close modal when clicking outside
    window.addEventListener('click', (event) => {
        if (event.target === modal) {
            closeModal();
        }
    });
});

// Make functions available globally
window.viewInvoice = viewInvoice;
window.printInvoice = printInvoice;