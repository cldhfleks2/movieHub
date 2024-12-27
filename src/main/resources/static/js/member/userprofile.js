$(document).ready(function() {
    initializeTab();
});

//탭 클릭시 목록을 보여줌
function initializeTab(){
    // 탭 전환 기능
    $('.tabButton').on('click', function() {
        // 활성 탭 변경
        $('.tabButton').removeClass('active');
        $(this).addClass('active');

        // 섹션 전환
        const targetTab = $(this).data('tab');
        $('.contentSection').removeClass('active');
        $(`#${targetTab}Section`).addClass('active');
    });
}

// 별점 표시 함수
function displayRating(score) {
    const fullStar = '★';
    const emptyStar = '☆';
    const totalStars = 5;
    const fullStars = Math.round(score);

    return fullStar.repeat(fullStars) + emptyStar.repeat(totalStars - fullStars);
}

// 날짜 포맷 함수
function formatDate(dateString) {
    const date = new Date(dateString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}.${month}.${day}`;
}





