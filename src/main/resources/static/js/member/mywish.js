$(document).ready(function() {
    viewStyle();

    sortSection();

    deleteWishList();

    pagination();
});

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

function sortSection(){
    // Sort functionality
    $('#sortSelect').change(function() {
        const sortValue = $(this).val();
        const $wishlistContent = $('#wishlistContent');
        const $movies = $wishlistContent.children('.movieCard').get();

        $movies.sort(function(a, b) {
            switch(sortValue) {
                case 'latest':
                    return $(b).find('.movieYear').text() - $(a).find('.movieYear').text();
                case 'rating':
                    return $(b).find('.movieRating').text() - $(a).find('.movieRating').text();
                case 'title':
                    return $(a).find('.movieTitle').text().localeCompare($(b).find('.movieTitle').text());
                default:
                    return 0;
            }
        });

        $wishlistContent.empty().append($movies);
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


