$(document).ready(function() {
    initImageUpload();
    initFormSubmit();
    initCancelButton();
});

function initImageUpload() {
    $(document).on('change', '#imageUpload', function(e) {
        const files = e.target.files;
        handleImagePreview(files);
    });

    $(document).on('click', '.removeImage', function() {
        $(this).closest('.previewImage').remove();
    });
}

function handleImagePreview(files) {
    Array.from(files).forEach(file => {
        if (file.type.startsWith('image/')) {
            const reader = new FileReader();

            reader.onload = function(e) {
                const previewHtml = `
                    <div class="previewImage">
                        <img src="${e.target.result}" alt="Preview">
                        <button type="button" class="removeImage">×</button>
                    </div>
                `;
                $('#imagePreview').append(previewHtml);
            };

            reader.readAsDataURL(file);
        }
    });
}

function initFormSubmit() {
    $('#postCreateForm').on('submit', function(e) {
        e.preventDefault();

        const formData = {
            category: $('#categorySelect').val(),
            title: $('#postTitle').val(),
            content: $('#postContent').val(),
        };

        // TODO: API 연동 로직 추가
        console.log('Form submitted:', formData);
    });
}

function initCancelButton() {
    $('.cancelButton').on('click', function() {
        if (confirm('작성을 취소하시겠습니까? 작성중인 내용은 저장되지 않습니다.')) {
            window.history.back();
        }
    });
}