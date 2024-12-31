$(document).ready(function() {
    postLike();
    reviewSubmit();
    reviewLike();
    sharePost();
    reviewEdit();
    reviewTextareaAdjust();
    postDelete();
});

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

// 게시글 좋아요 처리
function postLike() {
    $(document).on('click', '.postActions .likeButton', function() {
        const $likeButton = $(this);
        $likeButton.toggleClass('active');

        const currentLikes = parseInt($likeButton.find('span').text());
        const newLikes = $likeButton.hasClass('active') ? currentLikes + 1 : currentLikes - 1;
        $likeButton.find('span').text(newLikes);
    });
}

// 리뷰 등록
function reviewSubmit() {
    $(document).on('click', '.submitReview', function() {
        const $textarea = $('.reviewInputWrapper textarea');
        const reviewText = $textarea.val().trim();

        if (reviewText) {
            $textarea.val('');
            $('.reviewItem').css('animation', 'none');
            setTimeout(() => {
                $('.reviewItem').css('animation', '');
            }, 10);
        }
    });
}

// 리뷰 좋아요 처리
function reviewLike() {
    $(document).on('click', '.reviewActions .likeButton', function(e) {
        const $button = $(e.currentTarget);
        $button.toggleClass('active');

        const $count = $button.find('span');
        const currentLikes = parseInt($count.text());
        $count.text($button.hasClass('active') ? currentLikes + 1 : currentLikes - 1);
    });
}

// 공유 기능
function sharePost() {
    $(document).on('click', '.shareButton', function() {
        const currentUrl = window.location.href;
        navigator.clipboard.writeText(currentUrl).then(() => {
            alert('링크가 복사되었습니다.');
        });
    });
}

// 리뷰 수정 모드 및 수정 처리
function reviewEdit() {
    $(document).on('click', '.reviewActions .editButton', function(e) {
        const $review = $(e.currentTarget).closest('.reviewItem');
        const $reviewText = $review.find('.reviewText');
        const currentText = $reviewText.text();

        const $editArea = $('<div class="reviewEditArea">')
            .append(`<textarea rows="3">${currentText}</textarea>`)
            .append('<div class="editActions"><button class="saveEdit">저장</button><button class="cancelEdit">취소</button></div>');

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

// 리뷰 textarea 자동 높이 조절
function reviewTextareaAdjust() {
    $(document).on('input', '.reviewInputWrapper textarea', function() {
        this.style.height = 'auto';
        this.style.height = this.scrollHeight + 'px';
    });
}
