console.log("Ahly");

function confirmDelete(id, name, msisdn) {
    // Check if Swal is loaded
    if (typeof Swal === 'undefined') {
        console.error('SweetAlert not loaded');
        // Fallback to browser confirm
        if (confirm('Are you sure you want to delete subscriber: ' + name + '?')) {
            var form = document.createElement('form');
            form.method = 'POST';
            form.action = '/Billing-System-Web-GUI/DeleteSubscriber';
            var input = document.createElement('input');
            input.type = 'hidden';
            input.name = 'id';
            input.value = id;
            form.appendChild(input);
            document.body.appendChild(form);
            form.submit();
        }
        return false;
    }
    
    // SweetAlert popup
    Swal.fire({
        title: '<span style="color: #ef4444">⚠️ Delete Subscriber</span>',
        html: `
            <div style="text-align: left; margin-top: 20px;">
                <p style="margin: 10px 0; color: #94a3b8;">
                    <i class="fas fa-user" style="color: #38bdf8; margin-right: 10px;"></i>
                    <strong style="color: white;">Name:</strong> <span style="color: #fbbf24;">${name}</span>
                </p>
                <p style="margin: 10px 0; color: #94a3b8;">
                    <i class="fas fa-phone" style="color: #38bdf8; margin-right: 10px;"></i>
                    <strong style="color: white;">MSISDN:</strong> <span style="color: #fbbf24;">${msisdn}</span>
                </p>
                <hr style="border-color: #334155; margin: 15px 0;">
                <p style="color: #f87171; text-align: center;">
                    <i class="fas fa-exclamation-triangle"></i> This action cannot be undone!
                </p>
            </div>
        `,
        icon: 'warning',
        iconColor: '#ef4444',
        showCancelButton: true,
        confirmButtonColor: '#ef4444',
        cancelButtonColor: '#475569',
        confirmButtonText: '<i class="fas fa-trash"></i> Yes, Delete!',
        cancelButtonText: '<i class="fas fa-times"></i> Cancel',
        background: 'linear-gradient(135deg, #1e293b, #0f172a)',
        backdrop: 'rgba(0,0,0,0.8)',
        allowOutsideClick: false,
        allowEscapeKey: true
    }).then((result) => {
        if (result.isConfirmed) {
            // Show loading state
            Swal.fire({
                title: '<span style="color: #38bdf8">Deleting...</span>',
                html: '<div style="color: #94a3b8;">Please wait while we delete the subscriber</div>',
                icon: 'info',
                showConfirmButton: false,
                allowOutsideClick: false,
                background: 'linear-gradient(135deg, #1e293b, #0f172a)',
                didOpen: () => {
                    Swal.showLoading();
                }
            });
            
            // Submit the form
            var form = document.createElement('form');
            form.method = 'POST';
            form.action = '/Billing-System-Web-GUI/DeleteSubscriberServlet';
            var input = document.createElement('input');
            input.type = 'hidden';
            input.name = 'id';
            input.value = id;
            form.appendChild(input);
            document.body.appendChild(form);
            form.submit();
        }
    });
    return false;
}