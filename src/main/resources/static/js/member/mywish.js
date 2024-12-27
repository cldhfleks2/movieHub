$(document).ready(function() {
    initializeSetting();
    viewStyle();
    deleteWishList();
    pagination();
});

function initializeSetting(){
    sortSection(); //페이지 로딩되자마자 한번 정렬
    //정렬기준을 선택하면 정렬하도록 지시
    $('#sortSelect').on('change', function() {
        sortSection();
    });
}

function pagination(){
    $('#prevPage, #nextPage').click(function() {
        const $activeButton = $('.pageNumber.active');
        let $targetButton;

        if ($(this).attr('id') === 'prevPage') {
            $targetButton = $activeButton.prev('.pageNumber');
        } else {
            $targetButton = $activeButton.next('.pageNumber');
        }

        if ($targetButton.length) {
            $targetButton.click();
        }
    });
}

function viewStyle(){
    // View toggle functionality
    $('#gridView').click(function() {
        $(this).addClass('active');
        $('#listView').removeClass('active');
        $('#wishlistContent').removeClass('wishlistList').addClass('wishlistGrid');
    });

    $('#listView').click(function() {
        $(this).addClass('active');
        $('#gridView').removeClass('active');
        $('#wishlistContent').removeClass('wishlistGrid').addClass('wishlistList');
    });
}

function deleteWishList(){
    // 마우스를 올려야 버튼을 보여줌
    $(document).on('mouseenter', '.movieCard', function() {
        $(this).find('.movieOverlay button').show();
    });

    $(document).on('mouseleave', '.movieCard', function() {
        $(this).find('.movieOverlay button').hide();
    });
    
    //좋아요 취소 버튼 : 해당
    $(document).on('click', '.btnRemoveWish', function(e) {
        e.stopPropagation();

        const $buttons = $(this).closest('.movieOverlay').find('button');
        // const isActive = $(this).hasClass('active'); //주석처리함
        $(this).toggleClass('active');
        $buttons.hide();

        // const $overlay = $(this).closest('.movieOverlay'); //주석처리함
        // const message = isActive ? '찜하기를 취소하였습니다.' : '찜하기가 완료되었습니다.'; //주석처리함
        // showNotification($overlay, message); //주석처리함

        const movieCd = $(this).data("moviecd");
        const pageIdx = $(this).data("current-pageidx") + 1; //첫페이지값이 0으로 넘어오기때문임.
        console.log(pageIdx);
        const render = true;

        //서버로 삭제 요청을 보내고 페이지를 다시 가져옴
        $.ajax({
            url: "/api/remove/movielike",
            method: "delete",
            data: {movieCd: movieCd, pageIdx: pageIdx, render: render},
            success: function (data){
                var data = $.parseHTML(data);
                var dataHtml = $("<div>").append(data);
                $("#wishlistContainer").replaceWith(dataHtml.find("#wishlistContainer"));

                console.log("/api/remove/movielike ajax success")
            },
            error: function (xhr){
                console.log(xhr.responseText);
                console.log("/api/remove/movielike ajax failed")
            }
        })


        // setTimeout(() => $buttons.show(), 1300); //주석처리함
    });

    // $(document).on('click', '.btnWatchNow', function(e) {
    //     e.stopPropagation();
    //     const $buttons = $(this).closest('.movieOverlay').find('button');
    //     $buttons.hide();
    //
    //     const $overlay = $(this).closest('.movieOverlay');
    //     const movieNm = $(this).data('movieNm');
    //     const openDt = $(this).data('openDt');
    //
    //     showNotification($overlay, '예매 페이지로 이동합니다.');
    //
    //     setTimeout(() => {
    //         // 예매 페이지로 이동하는 로직
    //         // window.location.href = `/booking/${movieNm}/${openDt}`;
    //         $buttons.show();
    //     }, 1300);
    // });

    

    // // 알림 표시 함수
    // function showNotification($element, message) {
    //     const $notification = $('<div>', {
    //         class: 'notification',
    //         text: message
    //     });
    //
    //     $element.append($notification);
    //
    //     setTimeout(() => $notification.addClass('show'), 100);
    //     setTimeout(() => {
    //         $notification.removeClass('show');
    //         setTimeout(() => $notification.remove(), 300);
    //     }, 1000);
    // }
}

//DOM요소를 정렬하는 함수
function sortMovieCards(movieCards, sortBy) {
    movieCards.sort(function(a, b) {
        // 제목 순
        if (sortBy === "title") {
            let movieNmA = $(a).data('movienm').toLowerCase();
            let movieNmB = $(b).data('movienm').toLowerCase();

            // 알파벳 순으로 정렬
            if (movieNmA < movieNmB) return -1;
            if (movieNmA > movieNmB) return 1;
            return 0;
        }

        // 개봉일순
        else if (sortBy === "latest") {
            let openDtA = $(a).data('opendt');
            let openDtB = $(b).data('opendt');
            return new Date(openDtB) - new Date(openDtA); // 최신 개봉일이 먼저 오도록
        }

        // // 평점순 (rating)
        // else if (sortBy === "rating") {
        //     let ratingA = $(a).data('rating');
        //     let ratingB = $(b).data('rating');
        //
        //     if (ratingA === "not-found" && ratingB !== "not-found") {
        //         return 1; // "not-found"인 영화는 마지막으로 배치
        //     } else if (ratingA !== "not-found" && ratingB === "not-found") {
        //         return -1; // "not-found"인 영화는 마지막으로 배치
        //     } else if (ratingA === "not-found" && ratingB === "not-found") {
        //         return 0; // 둘 다 "not-found"일 경우 순서 유지
        //     } else {
        //         return parseFloat(ratingB) - parseFloat(ratingA); // 평점 내림차순
        //     }
        // }
        return 0; // 기본값: 변경 없이 그대로
    });

    return movieCards;
}

function sortSection(){
    // 영화 카드들 가져오기
    const movieCards = $('.movieCard');
    const sortBy = $("#sortSelect").val();// 정렬 기준 : 'relevance', 'date'

    // 영화 카드 정렬
    const sortedMovies = sortMovieCards(movieCards, sortBy);

    // 정렬된 결과로 업데이트
    $('#wishlistContent').empty(); // 기존 내용 제거
    sortedMovies.each(function() {
        $('#wishlistContent').append(this); // 컨테이너의 자식으로 삽입
    });
}




