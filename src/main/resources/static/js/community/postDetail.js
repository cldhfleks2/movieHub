$(document).ready(function() {
    sharePost();
    reviewTextareaAdjust();
    postLike();
    reviewSubmit();
    reviewReply();
    reviewLike();
    reviewEdit();
    postDelete();
});

// 공유 기능
function sharePost() {
    $(document).on('click', '.shareButton', function() {
        const currentUrl = window.location.href;
        navigator.clipboard.writeText(currentUrl).then(() => {
            alert('링크가 복사되었습니다.');
        });
    });
}

// 리뷰 textarea 자동 높이 조절
function reviewTextareaAdjust() {
    $(document).on('input', '.reviewInputWrapper textarea', function() {
        this.style.height = 'auto';
        this.style.height = this.scrollHeight + 'px';
    });
}


//게시글 삭제 요청
function postDelete(){
    $(document).on("click", ".actionButton.deleteButton", function () {
        const postId = $(this).data("postid")

        $.ajax({
            url: "/api/post/delete",
            method: "delete",
            data: {postId: postId},
            success: function (response, textStatus, xhr){
                if (xhr.status === 204) { //정상 삭제일 경우
                    alert("게시글이 삭제 되었습니다.")
                    window.location.href = `/community`; //삭제 완료시 커뮤니티 게시판으로
                }else{
                    alert("알 수 없는 성공")
                }
                console.log("/api/post/delete ajax success")
            },
            error: function (xhr){
                console.log(xhr.responseText);
                console.log("/api/post/delete ajax failed")
            }
        })
    })
}

// TODO 게시글 좋아요 처리
function postLike() {
    $(document).on('click', '.postActions .likeButton', function() {
        const $likeButton = $(this);
        $likeButton.toggleClass('active');

        const currentLikes = parseInt($likeButton.find('span').text());
        const newLikes = $likeButton.hasClass('active') ? currentLikes + 1 : currentLikes - 1;
        $likeButton.find('span').text(newLikes);
    });
}

//리뷰 리스트를 부분 새로고침하는 함수
function reviewListReload(postId){
    $.ajax({
        url: "/api/post/review/list/" + postId,
        method: "get",
        success: function (data){
            var data = $.parseHTML(data);
            var dataHtml = $("<div>").append(data);
            $("#reviewsSection").replaceWith(dataHtml.find("#reviewsSection"));

            console.log("/api/post/review/list/postId ajax success")
        },
        error: function (xhr){
            console.log(xhr.responseText);
            console.log("/api/post/review/list/postId ajax failed")
        }

    })
}

//댓글 작성 : 처음작성할때는 parentId = null전달
function reviewSubmit() {
    $(document).on('click', '.submitReview', function() {
        const $textarea = $('.reviewInputWrapper textarea');
        const content = $textarea.val().trim();
        const postId = $(this).data("postid");

        const postReviewRequestDTO = {
            content: content,
            parentId: null, //답글일경우, 부모 댓글 Id를 지정할거임
            postId: postId
        };

        $.ajax({
            url: "/api/post/review/add",
            method: "post",
            contentType: 'application/json',
            data: JSON.stringify(postReviewRequestDTO),
            success: function (response, textStatus, xhr){
                if (xhr.status === 201) { //정상 성공시 201 created
                    alert("댓글을 작성하였습니다.") //불편하면 지워도 돼
                    $textarea.val(''); //입력 내용 지움
                    reviewListReload(postId)
                }else{
                    alert("알 수 없는 성공")
                }
                console.log("/api/post/review/add ajax success")
            },
            error: function (xhr){
                console.log(xhr.responseText);
                console.log("/api/post/review/add ajax failed")
            }
        })
    });
}

//TODO : 댓글에 답글 기능 : parentId를 전달해야해
function reviewReply() {
    let activeReplyForm = null; // 현재 활성화된 답글 폼을 추적

    // 답글 버튼 클릭 이벤트 위임
    $(document).on('click', '.replyButton', function(e) {
        const parentId = $(this).data('parent-id');
        const reviewItem = $('#reviewItem-' + parentId);

        // 이미 답글 폼이 열려있다면 제거
        if (activeReplyForm) {
            activeReplyForm.remove();
            // 같은 댓글의 답글 폼을 닫는 경우
            if (activeReplyForm.data('parent-id') === parentId) {
                activeReplyForm = null;
                return;
            }
        }

        // 새로운 답글 폼 생성
        const replyForm = createReplyForm(parentId); //답글 작성 폼을 만듬
        const location = reviewItem.children(".reviewActions:first"); // 가장 가까운 reviewActions를 선택
        location.after(replyForm); //reviewActions 다음으로 넣음
        activeReplyForm = replyForm;

        // 텍스트 영역에 포커스
        replyForm.find('textarea').focus();
    });

    // 답글 폼 생성 함수
    function createReplyForm(parentId) {
        const formWrapper = $(`
            <div class="replyForm reviewForm" data-parent-id="${parentId}">
                <div class="reviewInputWrapper">
                    <textarea placeholder="답글을 입력하세요..." rows="3"></textarea>
                    <div class="formActions">
                        <button class="submitReply replySubmit">답글 작성</button>
                        <button class="cancelReply replyCancel">취소</button>
                    </div>
                </div>
            </div>
        `);

        // 취소 버튼 이벤트
        formWrapper.on('click', '.cancelReply', function() {
            formWrapper.remove();
            activeReplyForm = null;
        });

        // 답글 제출 이벤트
        formWrapper.on('click', '.submitReply', function() {
            const content = formWrapper.find('textarea').val().trim();
            const postId = $(".postDetailContainer").data("postid");

            const postReviewRequestDTO = {
                content: content,
                parentId: parentId, //답글이므로 부모 댓글 Id를 지정
                postId: postId
            };

            if (content) {
                // 답글 제출 AJAX 함수
                $.ajax({
                    url: '/api/post/review/add',
                    method: 'POST',
                    contentType: 'application/json',
                    data: JSON.stringify(postReviewRequestDTO),
                    success: function() {
                        // 성공적으로 답글이 저장되면 리뷰 리스트 새로고침
                        reviewListReload(postId)
                    },
                    error: function(xhr, status, error) {
                        console.error('Error:', error);
                        alert('답글 작성에 실패했습니다.');
                    }
                });
            }
        });
        return formWrapper;
    }
}




// TODO 댓글 좋아요 처리
function reviewLike() {
    $(document).on('click', '.reviewActions .likeButton', function(e) {
        const $button = $(e.currentTarget);
        $button.toggleClass('active');

        const $count = $button.find('span');
        const currentLikes = parseInt($count.text());
        $count.text($button.hasClass('active') ? currentLikes + 1 : currentLikes - 1);
    });
}

// TODO 댓글 수정 모드 및 수정 처리 : 싹다 고쳐라.
//reviewId : data("review-id") 가져옴
//reviewId던지고, 수정할 내용만 던지는 Patch요청으로 하자

//이건 form
// content: content,
// parentId: parentId,
// postId: postId
function reviewEdit() {
    $(document).on('click', '.reviewActions .editButton', function(e) {
        const $review = $(e.currentTarget).closest('.reviewItem');
        const $reviewText = $review.find('.reviewText');
        const currentText = $reviewText.text();

        const $editArea = $('<div class="reviewEditArea">')
            .append(`<textarea rows="3">${currentText}</textarea>`)
            .append('<div class="editActions">' +
                '<button class="saveEdit">저장</button>' +
                '<button class="cancelEdit">취소</button>' +
                '</div>');

        $reviewText.hide().after($editArea);
    });

    $(document).on('click', '.cancelEdit', function(e) {
        const $review = $(e.currentTarget).closest('.reviewItem');
        $review.find('.reviewText').show();
        $review.find('.reviewEditArea').remove();
    });

    $(document).on('click', '.saveEdit', function(e) {
        const $review = $(e.currentTarget).closest('.reviewItem');
        const newText = $review.find('.reviewEditArea textarea').val().trim();

        if (newText) {
            $review.find('.reviewText').text(newText).show();
            $review.find('.reviewEditArea').remove();
        }
    });
}
