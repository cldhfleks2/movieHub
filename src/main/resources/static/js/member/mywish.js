$(document).ready(function() {
    sortSection()
    viewStyle();
    deleteWishList();
    pagination();
});

//정렬 선택 기능
function sortSection(){
    //정렬기준을 선택하면 정렬하도록 지시
    $('#sortSelect').on('change', function() {
        pageReload()
    });
}

//페이지네이션 뷰
function pagination(){
    $(document).on("click", "#prevPage, #nextPage, .pageNum", function () {
        const pageIdx = $(this).data("pageidx")
        pageReload(pageIdx);
    })
}

//사용 X 목록 정렬뷰
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

//찜한목록 뷰, 페이지네이션 뷰 새로 고침
function pageReload(pageIdx = 1){
    const sort = $("#sortSelect").val();// 정렬 기준 : 'latest', 'title'

    $.ajax({
        url: "/mywish",
        method: "get",
        data: {pageIdx: pageIdx, sort: sort},
        success: function (data){
            var data = $.parseHTML(data);
            var dataHtml = $("<div>").append(data);
            $("#wishlistContent").replaceWith(dataHtml.find("#wishlistContent"));
            $("#wishlistPagination").replaceWith(dataHtml.find("#wishlistPagination"));
            console.log("page reload ajax success")
        },
        error: function (xhr){
            console.log(xhr.responseText);
            console.log("page reload ajax failed")
        }

    })
}

//찜하기 취소 버튼
function deleteWishList(){
    //마우스를 올려야 버튼을 보여줌
    $(document).on('mouseenter', '.movieCard', function() {
        $(this).find('.movieOverlay button').show();
    });
    //마우스 나가면 오버레이 감추기
    $(document).on('mouseleave', '.movieCard', function() {
        $(this).find('.movieOverlay button').hide();
    });
    //찜하기 취소 버튼
    $(document).on('click', '.btnRemoveWish', function(e) {
        e.stopPropagation();

        const $buttons = $(this).closest('.movieOverlay').find('button');
        // const isActive = $(this).hasClass('active'); //주석처리함
        $(this).toggleClass('active');
        $buttons.hide(); //오버레이 감추기

        // const $overlay = $(this).closest('.movieOverlay'); //주석처리함
        // const message = isActive ? '찜하기를 취소하였습니다.' : '찜하기가 완료되었습니다.'; //주석처리함
        // showNotification($overlay, message); //주석처리함
        const movieCd = $(this).data("moviecd");

        //서버로 삭제 요청을 보내고 페이지 새로고침
        $.ajax({
            url: "/api/bookmark/delete",
            method: "delete",
            data: {movieCd: movieCd},
            success: function (response, textStatus, xhr){
                if (xhr.status === 204) {
                    pageReload(); //북마크 삭제에 성공하면 페이지 새로고침
                }else{
                    alert("알 수 없는 성공")
                }
                console.log("delete bookmark ajax success")
            },
            error: function (xhr){
                console.log(xhr.responseText);
                console.log("delete bookmark ajax failed")
            }
        })
        // setTimeout(() => $buttons.show(), 1300); //주석처리함
    });
}



