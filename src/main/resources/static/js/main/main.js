$(document).ready(function() {
    initializeSliders();
});

function initializeSliders() {
    $('.movieSection').each(function() {
        const $section = $(this);
        const $movieGrid = $section.find('.movieGrid');
        const $cards = $movieGrid.find('.movieCard');

        if ($cards.length === 0) return;

        const $sliderTrack = $('<div>').addClass('slider-track');

        // 복제 요소 추가 (앞과 뒤로 한 세트씩)
        const $firstClone = $cards.clone();
        const $lastClone = $cards.clone();

        $sliderTrack.append($lastClone); // 뒤쪽에 복제본 추가
        $cards.each(function() {
            $sliderTrack.append($(this));
        });
        $sliderTrack.append($firstClone); // 앞쪽에 복제본 추가

        $movieGrid.append($sliderTrack);

        const cardWidth = $cards.first().outerWidth(true);
        const totalWidth = cardWidth * ($cards.length * 3); // 원본 + 복제(앞뒤 한 세트)
        let currentPosition = -($cards.length * cardWidth); // 복제된 첫 번째 카드 위치로 초기화
        let isAnimating = false;
        let isHovered = false;

        const autoSlideSpeed = 0.5;
        const buttonSlideSpeed = 400;

        function autoSlide() {
            if (!isHovered && !isAnimating) {
                currentPosition -= autoSlideSpeed;

                // 경계 처리 (오른쪽 끝 도달 시)
                if (Math.abs(currentPosition) >= totalWidth - $cards.length * cardWidth) {
                    currentPosition = -$cards.length * cardWidth; // 첫 번째 원본으로 이동
                    $sliderTrack.css('transition', 'none'); // 애니메이션 끄기
                    $sliderTrack.css('transform', `translateX(${currentPosition}px)`);
                } else {
                    $sliderTrack.css('transition', `transform 0s`);
                }

                $sliderTrack.css('transform', `translateX(${currentPosition}px)`);
            }
            requestAnimationFrame(autoSlide);
        }

        function moveSlide(direction) {
            if (isAnimating) return;
            isAnimating = true;

            const moveAmount = direction === 'prev' ? -cardWidth * 2 : cardWidth * 2;
            currentPosition += moveAmount;

            $sliderTrack.css({
                'transition': `transform ${buttonSlideSpeed}ms ease`,
                'transform': `translateX(${currentPosition}px)`
            });

            setTimeout(() => {
                // 경계 처리
                if (currentPosition <= -(totalWidth - $cards.length * cardWidth)) {
                    currentPosition = -$cards.length * cardWidth; // 첫 번째 원본으로 이동
                    $sliderTrack.css('transition', 'none');
                    $sliderTrack.css('transform', `translateX(${currentPosition}px)`);
                } else if (currentPosition >= 0) {
                    currentPosition = -(totalWidth - 2 * $cards.length * cardWidth); // 마지막 원본으로 이동
                    $sliderTrack.css('transition', 'none');
                    $sliderTrack.css('transform', `translateX(${currentPosition}px)`);
                }

                isAnimating = false;
            }, buttonSlideSpeed);
        }

        // 버튼 클릭 이벤트
        $section.find('.prev').on('click', function(e) {
            e.preventDefault();
            moveSlide('prev');
        });

        $section.find('.next').on('click', function(e) {
            e.preventDefault();
            moveSlide('next');
        });

        $movieGrid.hover(
            function() { isHovered = true; },
            function() { isHovered = false; }
        );

        // 초기 설정
        $sliderTrack.css({
            'display': 'flex',
            'gap': '2rem',
            'transform': `translateX(${currentPosition}px)`
        });

        requestAnimationFrame(autoSlide);
    });
}
