$(document).ready(function() {
    formSubmit();
    formCancelButton();
});

function formSubmit() {
    $('#postEditForm').on('submit', function(e) {
        e.preventDefault();

        const postId = new URLSearchParams(window.location.search).get('id');
        const formData = {
            postType: $('#categorySelect').val(),
            title: $('#postTitle').val(),
            content: $('#postContent').val(),
        };


        //ajax



    });
}

function formCancelButton() {
    $('.cancelButton').on('click', function() {
        if (confirm('수정을 취소하시겠습니까? 변경사항이 저장되지 않습니다.')) {
            window.history.back();
        }
    });
}