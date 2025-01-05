$(document).ready(function() {
    initialize();
    tabSetting()
    searching();

});

function initialize(){
    //가장 처음 영화 신고 내역을 보여줌
    tabContentReload("movie")
}

// 탭 초기화 및 탭 전환 처리
function tabSetting() {
    $(document).on('click', '.tabButton', function() {
        // 모든 탭 버튼에서 active 클래스 제거
        $('.tabButton').removeClass('active');
        // 클릭된 탭 버튼에 active 클래스 추가
        $(this).addClass('active');

        // 클릭된 탭
        const tabType = $(this).data('tab');

        // 모든 탭 컨텐츠 숨기기
        $('.tabContent').removeClass('active');

        // 선택된 탭의 컨텐츠만 보이기
        $(`#${tabType}Content`).addClass('active');
    });
}

//각 탭별로 검색바 동작
function searching(){
    $(document).on("input", ".searchInput", function () {
        // 클릭된 탭
        const tabType = $(this).data('tab');
        tabContentReload(tabType); //검색 결과로 페이지 새로고침
    });
}

//각 탭별로 페이지 새로고침
function tabContentReload(tabType, pageIdx = 1) {
    const searchType = $(`#${tabType}SearchType`).val();
    const keyword = $(`#${tabType}SearchInput`).val();

    $.ajax({
        url: `/api/manager/report/${tabType}`,
        method: 'GET',
        data: { pageIdx: pageIdx, searchType: searchType, keyword: keyword },
        success: function (data){
            var data = $.parseHTML(data);
            var dataHtml = $("<div>").append(data);
            $(`#${tabType}Table`).replaceWith(dataHtml.find(`#${tabType}Table`));
            // v페이지네이션도 새로고침

            console.log("searching ajax success")
        },
        error: function (xhr){
            console.log(xhr.responseText);
            console.log("searching ajax failed")
        }
    });
}