$(document).ready(function() {
    sliderSection();
});

//슬라이더 동작
function sliderSection() {
    let currentPosition = 0;
    const $movieGrid = $('.movieGrid');
    const $movies = $('.movieCard');
    const totalMovies = $movies.length;
    const visibleMovies = 5;
    const movieWidth = $movies.first().outerWidth(true);
    const maxPosition = Math.max(0, totalMovies - visibleMovies);

    function updateSliderButtons() {
        $('.prevArrow').prop('disabled', currentPosition === 0);
        $('.nextArrow').prop('disabled', currentPosition >= maxPosition);
    }

    function moveSlider(direction) {
        const movement = direction === 'next' ? 1 : -1;
        currentPosition = Math.max(0, Math.min(maxPosition, currentPosition + movement));

        const translateX = -(currentPosition * movieWidth);
        $movieGrid.css('transform', `translateX(${translateX}px)`);

        updateSliderButtons();
    }

    $('.nextArrow').on('click', function() {
        if (currentPosition < maxPosition) {
            moveSlider('next');
        }
    });

    $('.prevArrow').on('click', function() {
        if (currentPosition > 0) {
            moveSlider('prev');
        }
    });

    // 초기 버튼 상태 설정
    updateSliderButtons();

    // 윈도우 리사이즈 시 슬라이더 위치 재조정
    $(window).on('resize', function() {
        currentPosition = 0;
        $movieGrid.css('transform', 'translateX(0)');
        updateSliderButtons();
    });
}









