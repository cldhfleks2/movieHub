$(document).ready(function() {
    loadPostData();
    initFormSubmit();
    initCancelButton();
});

function loadPostData() {
    // URL에서 게시글 ID 추출
    const postId = new URLSearchParams(window.location.search).get('id');

    // TODO: API 호출하여 게시글 데이터 가져오기
    // 예시 데이터
    const postData = {
        postType: 'DISCUSSION',
        title: '기존 제목',
        content: '기존 내용'
    };

    $('#categorySelect').val(postData.postType);
    $('#postTitle').val(postData.title);
    $('#postContent').val(postData.content);
}

function initFormSubmit() {
    $('#postEditForm').on('submit', function(e) {
        e.preventDefault();

        const postId = new URLSearchParams(window.location.search).get('id');
        const formData = {
            postType: $('#categorySelect').val(),
            title: $('#postTitle').val(),
            content: $('#postContent').val(),
        };

        // TODO: API 연동 로직 추가
        console.log('Edit form submitted:', formData);
    });
}

function initCancelButton() {
    $('.cancelButton').on('click', function() {
        if (confirm('수정을 취소하시겠습니까? 변경사항이 저장되지 않습니다.')) {
            window.history.back();
        }
    });
}