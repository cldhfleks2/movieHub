$(document).ready(function() {
    sharePost();
    reviewTextareaAdjust();
    postLike();
    reviewSubmit();
    reviewReply();
    reviewLike();
    reviewEdit();
    postDelete();
    reviewDelete();
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

//리뷰 리스트를 부분 새로고침하는 함수
function reviewListReload(){
    const postId = $(".postDetailContainer").data("postid");
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

//댓글 작성 : 처음작성할때는 parentId = null 전달
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
                    reviewListReload()
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

//댓글 답글 작성 : parentId를 전달
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
                        reviewListReload()
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

//댓글 수정 : reviewId던지고, 수정할 내용만 보내는 Patch요청으로 하자
function reviewEdit() {
    let activeEditForm = null; // 현재 활성화된 수정 폼을 추적

    // 수정 버튼 클릭 이벤트
    $(document).on('click', '.reviewActions .editButton', function (e) {
        const $button = $(e.currentTarget);
        const $review = $button.closest('.reviewItem'); // 클릭된 댓글 요소만 선택
        const $reviewAction = $button.closest(".reviewActions");
        const $reviewText = $review.children('.reviewText:first'); //가장 가까운 리뷰 내용만 선택(안하면 전체 리뷰 내용이 숨겨짐)
        const currentText = $reviewText.text();
        const reviewId = $button.data('review-id');
        const postId = $(".postDetailContainer").data("postid");

        // 이미 활성화된 수정 폼이 있으면 제거
        if (activeEditForm) {
            activeEditForm.remove();
            activeEditForm = null;
        }

        // 기존 댓글 숨기기
        $(".reviewText").show(); //취소 버튼을 누르고 왔을때를위해 모든 뷰를 살려줌
        $reviewText.hide(); //현재것만 숨김
        // 기존 버튼들 숨기기
        $(".reviewActions").show();
        $reviewAction.hide(); //마찬가지

        // 수정 폼 생성
        const editForm = createEditForm(currentText, reviewId, postId);
        const location = $review.children('.reviewActions:first'); // 가장 가까운 reviewActions를 선택
        location.after(editForm); // reviewActions 바로 뒤에 수정 폼 삽입
        activeEditForm = editForm;

        // 텍스트 영역에 포커스
        editForm.find('textarea').focus();
    });

    // 수정 폼 생성 함수
    function createEditForm(currentText, reviewId, postId) {
        return $(`
            <div class="reviewEditArea" data-review-id="${reviewId}">
                <div class="reviewInputWrapper">
                    <textarea class="editTextarea" rows="3">${currentText}</textarea>
                    <div class="editActions">
                        <button class="saveEdit replySubmit">저장</button>
                        <button class="cancelEdit replyCancel">취소</button>
                    </div>
                </div>
            </div>
        `);
    }

    // 수정 취소 동작
    $(document).on('click', '.cancelEdit', function () {
        const $formWrapper = $(this).closest('.reviewEditArea');
        const reviewId = $formWrapper.data('review-id');
        $formWrapper.remove(); // 수정 폼 제거
        $(`#reviewItem-${reviewId} .reviewText`).show(); // 원본 댓글 다시 표시
        $(`#reviewItem-${reviewId} .reviewActions`).show(); // 기존 버튼들 다시 표시
        activeEditForm = null;
    });

    // 수정 저장 버튼 클릭 이벤트
    $(document).on('click', '.saveEdit', function (e) {
        const $review = $(e.currentTarget).closest('.reviewItem');
        const content = $review.find('.editTextarea').val().trim();
        const reviewId = $review.find('.editButton').data('review-id');

        if (content) {
            // PATCH 요청으로 댓글 수정
            $.ajax({
                url: `/api/post/review/edit/${reviewId}`,
                method: 'PATCH',
                data: {content: content},
                success: function (response, textStatus, xhr) {
                    if (xhr.status === 200) {
                        activeEditForm = null;
                        reviewListReload() // 성공적으로 수정되면 댓글 리스트 새로고침
                    }else{
                        alert("알 수 없는 성공")
                    }
                    console.log("/api/post/review/edit/ ajax success")
                },
                error: function (xhr) {
                    console.log(xhr.responseText);
                    console.log("/api/post/review/edit/ ajax failed")
                }
            });
        } else {
            alert('내용을 입력해주세요.');
        }
    });
}

//댓글 삭제 : 답글이 달린 댓글 삭제시 답글까지 전부 삭제
function reviewDelete(){
    $(document).on('click', '.deleteButton', function () {
        const $formWrapper = $(".reviewEditArea");
        const reviewId = $(this).data('review-id');
        $formWrapper.remove(); // 모든 댓글 수정 폼 제거

        $.ajax({
            url: "/api/post/review/delete/" + reviewId,
            method: "delete",
            success: function (response, textStatus, xhr){
                if (xhr.status === 204) { //정상 삭제일 경우
                    alert("댓글을 삭제 하였습니다.")
                    reviewListReload() // 성공적으로 삭제 되면 댓글 리스트 새로고침
                }else{
                    alert("알 수 없는 성공")
                }
                console.log("/api/post/review/delete/ ajax success")
            },
            error: function (xhr){
                console.log(xhr.responseText);
                console.log("/api/post/review/delete/ ajax failed")
            }
        })

    });
}


//댓글 좋아요
function reviewLike() {
    $(document).on('click', '.reviewActions .likeButton', function(e) {
        const reviewId = $(this).data("review-id");

        $.ajax({
            url: "/api/post/review/like",
            method: "post",
            data: {reviewId: reviewId},
            success: function (response, textStatus, xhr){
                if (xhr.status === 200) {
                    reviewListReload() // 댓글 리스트 새로고침
                }else{
                    alert("알 수 없는 성공")
                }
                console.log("/api/post/review/like ajax success")
            },
            error: function (xhr){
                console.log(xhr.responseText);
                console.log("/api/post/review/like ajax failed")
            }
        })

    });
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

