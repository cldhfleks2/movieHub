$(document).ready(function() {
    formSubmit();
    formCancelButton();
});

function formSubmit() {
    $('#postEditForm').on('submit', function(e) {
        e.preventDefault();

        const postId = $(".submitButton").data("postid");
        const formData = {
            postType: $('#categorySelect').val(),
            title: $('#postTitle').val(),
            content: $('#postContent').val(),
            postId: postId
        };

        //게시글 수정 요청
        $.ajax({
            url: "/api/post/edit",
            method: "post",
            contentType: 'application/json',
            data: JSON.stringify(formData),
            success: function (response, textStatus, xhr){
                if (xhr.status === 200) {
                    alert("게시글 수정이 완료 되었습니다.")
                    window.location.href = `/postDetail/` + postId; //완료시 수정한 게시글 상세 페이지로
                }
                console.log("/api/post/edit ajax success")
            },
            error: function (xhr){
                if (xhr.status === 403) { // 본인 게시글이 아닐때 보내는 코드
                    alert('본인의 게시글만 수정할 수 있습니다.');
                }
                console.log(xhr.responseText);
                console.log("/api/post/edit ajax failed")
            }
        })
    });
}

function formCancelButton() {
    $('.cancelButton').on('click', function() {
        if (confirm('수정을 취소하시겠습니까? 변경사항이 저장되지 않습니다.')) {
            window.history.back();
        }
    });
}