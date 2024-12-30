$(document).ready(function() {
    // initImageUpload();
    formValidation();
    initCancelButton();
});

// function initImageUpload() {
//     $(document).on('change', '#imageUpload', function(e) {
//         const files = e.target.files;
//         handleImagePreview(files);
//     });
//
//     $(document).on('click', '.removeImage', function() {
//         $(this).closest('.previewImage').remove();
//     });
// }

// function handleImagePreview(files) {
//     Array.from(files).forEach(file => {
//         if (file.type.startsWith('image/')) {
//             const reader = new FileReader();
//
//             reader.onload = function(e) {
//                 const previewHtml = `
//                     <div class="previewImage">
//                         <img src="${e.target.result}" alt="Preview">
//                         <button type="button" class="removeImage">×</button>
//                     </div>
//                 `;
//                 $('#imagePreview').append(previewHtml);
//             };
//
//             reader.readAsDataURL(file);
//         }
//     });
// }


function formValidation() {
    $('#postCreateForm').on('submit', function(e) {
        e.preventDefault();
        const title = $('#postTitle').val().trim();
        const content = $('#postContent').val().trim();
        const category = $('#categorySelect').val();

        if (!category) {
            alert('카테고리를 선택해주세요.');
            return false;
        }

        if (title.length < 2 || title.length > 100) {
            alert('제목은 2자 이상 100자 이하로 입력해주세요.');
            return false;
        }

        if (content.length < 10) {
            alert('내용은 10자 이상 입력해주세요.');
            return false;
        }

        submitForm();
    });
}

function submitForm() {
    const formData = {
        postType: $('#categorySelect').val(),
        title: $('#postTitle').val().trim(),
        content: $('#postContent').val().trim()
    };

    $.ajax({
        url: '/api/post/write',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(formData),
        success: function() {
            alert('게시글이 등록되었습니다.');
            window.location.href = `/community`;
        },
        error: function(xhr) {
            const errorMsg = xhr.responseJSON?.message || '게시글 등록에 실패했습니다.';
            alert(errorMsg);
        }
    });
}

function initCancelButton() {
    $('.cancelButton').on('click', function() {
        if (confirm('작성을 취소하시겠습니까?')) {
            window.history.back();
        }
    });
}





