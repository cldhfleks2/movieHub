$(document).ready(function() {
    categorySection();
    sortSection();
    pagination();
    searchSection();
});

//카테고리 선택기능
function categorySection() {
    // 카테고리 탭 클릭 이벤트 위임
    $(document).on('click', '.categoryTab', function () {
        // 모든 카테고리에서 'active' 클래스 제거
        $('.categoryTab').removeClass('active');

        // 현재 클릭된 탭에 'active' 클래스 추가
        $(this).addClass('active');

        //1페이지로 선택한 카테고리 게시글들을 가져옴
        postListReload();
    });
}

function sortSection(){
    $(document).on('change', '.sortSelect', function () {
        //1페이지로 정렬기준을 적용해서 게시글들을 가져옴
        postListReload();
    });
}

//페이지네이션 : 페이지버튼들 동작
function pagination() {
    $(document).on("click", "#prevPage, #nextPage, .pageNumber", function () {
        const pageIdx = $(this).data("pageidx")
        postListReload(pageIdx)
    })
}

//게시물뷰와 페이지번호뷰를 새로고침하는 코드 : pageIdx와 searchText(자동적용)
function postListReload(pageIdx = 1){
    const category = $(".categoryTab.active").data("category"); //ALL, FREE, NEWS, DISCUSSION
    const sort = $(".sortSelect").val() //latest, popular, review
    let keyword = $(".searchBox input").val();

    $.ajax({
        url: "/community",
        method: "get",
        data: { pageIdx: pageIdx, keyword: keyword, category: category, sort: sort },
        success: function (data){
            var data = $.parseHTML(data);
            var dataHtml = $("<div>").append(data);
            $("#postList").replaceWith(dataHtml.find("#postList"));
            $("#pagination").replaceWith(dataHtml.find("#pagination"));

            console.log("/community pagination ajax success")
        },
        error: function (xhr){
            console.log(xhr.responseText);
            console.log("/community pagination ajax failed")
        }
    });
}

//검색 기능
function searchSection() {
    // 검색창에서 엔터 키 눌렀을 때
    $(".searchBox input").on("keydown", function (e) {
        postListReload();
    });
}





